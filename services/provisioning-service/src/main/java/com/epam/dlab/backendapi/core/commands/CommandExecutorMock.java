/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.backendapi.core.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.utils.ServiceUtils;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class CommandExecutorMock implements ICommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutorMock.class);

    private ObjectMapper MAPPER = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
    
    private Map<String, String> variables = new HashMap<String, String>();
    
    private String responseFileName;
    
    public Map<String, String> getVariables() {
    	return variables;
    }
    
    public String getResponseFileName() {
    	return responseFileName;
    }

    @Override
    public List<String> executeSync(String user, String uuid, String command) throws IOException, InterruptedException {
        LOGGER.debug("Command execution Sync: {}", command);
        if (command.startsWith("docker images |")) {
        	return Arrays.asList(
        			"docker.dlab-emr:latest",
            		"docker.dlab-jupyter:latest",
            		"docker.dlab-rstudio:latest",
            		"docker.dlab-tensor:latest",
            		"image: docker.dlab-zeppelin:latest");
        }
        return new ArrayList<String>();
    }

    @Override
    public void executeAsync(String user, String uuid, String command) {
        LOGGER.debug("Command execution Async: {}", command);

        variables.clear();
        responseFileName = null;
        
    	CommandParserMock parser = new CommandParserMock(command);
    	LOGGER.debug("Args is {}", parser);
    	DockerAction action = DockerAction.of(parser.getAction());
    	LOGGER.debug("Action is {}", action);
    	
    	if (action == null) {
    		throw new DlabException("Docker action not defined");
    	}
    	
    	String resourceType = getResourceType(parser.getOther());
    	String requestId = parser.getEnvironment().get("request_id");
    	String responsePath = parser.getVariable().get("/response");
    	LOGGER.debug("resourse type is {}, requestId is {}, pesponse path is {}", resourceType, requestId, responsePath);

    	variables.putAll(parser.getEnvironment());
    	variables.putAll(getJsonVariables(parser.getJson()));
    	variables.put("instance_id", "i-" + requestId.replace("-", "").substring(0, 17));
    	variables.put("notebook_id", requestId.replace("-", "").substring(17, 22));
    	LOGGER.debug("jsonVars is {}", variables);
    	
    	switch (action) {
		case DESCRIBE:
			describe(resourceType, requestId, responsePath);
			break;
		case CREATE:
		case START:
		case STOP:
		case TERMINATE:
			action(user, resourceType, action, requestId, responsePath);
			break;
		case CONFIGURE:
			break;
		case STATUS:
			break;
		default:
			break;
		}
    }
    
    /** Return absolute path to the file or folder.
     * @param first part of path.
     * @param more next path components.
     */
    public static String getAbsolutePath(String first, String ... more) {
    	return Paths.get(first, more).toAbsolutePath().toString();
    }

    /** Return name of docker image.
     * @param args list of arguments.
     * @exception if image name not found.
     */
    public static String getImageName(List<String> args) throws DlabException {
		for (String s : args) {
			if (s.startsWith("docker.dlab-")) {
				return s;
			}
		}
		throw new DlabException("Name of docker image not found");
    }
    
    /** Return name of resource: edge, emr, ...
     * @param args list of arguments.
     * @exception if image name not found.
     */
    public static String getResourceType(List<String> args) throws DlabException {
		String imageName = getImageName(args);
		return imageName.replace("docker.dlab-", "").replace(":latest", "");
    }
    
    /** Describe action.
     * @param uuid UUID for request.
     * @param resourceType name of docker image.
     * @param responsePath path for response file.
     */
    public void describe(String resourceType, String uuid, String responsePath) {
    	String templateFileName = getAbsolutePath(ServiceUtils.getUserDir(), "../../infrastructure-provisioning/src", resourceType, "description.json");
    	responseFileName = getAbsolutePath(responsePath, uuid + ".json");

    	LOGGER.debug("Create response file from {} to {}", templateFileName, responseFileName);
    	File fileResponse = new File(responseFileName);
		try {
			Files.createParentDirs(fileResponse);
			Files.copy(new File(templateFileName), fileResponse);
		} catch (IOException e) {
			throw new DlabException("Can't create response file " + responseFileName + ": " + e.getLocalizedMessage(), e);
		}
    }
    
    public void action(String user, String resourceType, DockerAction action, String uuid, String responsePath) {
    	String prefixFileName = (resourceType.equals("edge") ? resourceType : "notebook") + "_";
    	String templateFileName = prefixFileName + action.toString() + ".json";
    	responseFileName = getAbsolutePath(responsePath, prefixFileName + user + "_" + uuid + ".json");
    	setResponse(templateFileName, responseFileName);
    }
    
    protected String getTextValue(JsonNode jsonNode) {
        return jsonNode != null ? jsonNode.textValue() : null;
    }
    
    public Map<String, String> getJsonVariables(String jsonContent) {
    	Map<String, String> vars = new HashMap<String, String>();
    	if (jsonContent == null) {
    		return vars;
    	}
    	
    	JsonNode json;
    	try {
			json = MAPPER.readTree(jsonContent);
		} catch (IOException e) {
			throw new DlabException("Can't parse json content: " + e.getLocalizedMessage(), e);
		}
    	
    	Iterator<String> keys = json.fieldNames();
    	while (keys.hasNext()) {
    		String key = keys.next();
    		String value = getTextValue(json.get(key));
    		if (value != null) {
    			vars.put(key, value);
    		}
    	}
    	return vars;
    }
    
    public void setResponse(String sourceFileName, String targetFileName) throws DlabException {
    	String content;
    	URL url = Resources.getResource(sourceFileName);
    	try {
    		content = Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			throw new DlabException("Can't read resource " + sourceFileName + ": " + e.getLocalizedMessage(), e);
		}
    	
    	for (String key : variables.keySet()) {
    		String value = variables.get(key);
    		content = content.replace("${" + key.toUpperCase() + "}", value);
    	}
    	
    	try (BufferedWriter out = new BufferedWriter(new FileWriter(targetFileName))) {
    	    out.write(content);  
    	} catch (IOException e) {
			throw new DlabException("Can't write response file " + targetFileName + ": " + e.getLocalizedMessage(), e);
    	}
    }
    
    public static void main(String [] args) {
    	ICommandExecutor commandExecutor = new CommandExecutorMock();
    	String cmd;

    	cmd = "echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"usein_faradzhev@epam.com\",\"edge_user_name\":\"usein_faradzhev\"," +
    			"\"edge_list_resources\":{\"host\":[{\"id\":\"i-05c1a0d0ad030cdc5\"}]}}' | " +
    			"docker run -i --name usein_faradzhev_status_resources_1487607145484 " +
    			"-v /home/ubuntu/keys:/root/keys " +
    			"-v /opt/dlab/tmp/result:/response " +
    			"-v /var/opt/dlab/log/edge:/logs/edge " +
    			"-e \"conf_resource=status\" " +
    			"-e \"request_id=0fb82e16-deb2-4b18-9ab3-f9f1c12d9e62\" " +
    			"-e \"conf_key_name=BDCC-DSS-POC\" " +
    			"docker.dlab-edge --action status";
    	commandExecutor.executeAsync("user", "uuid", cmd);
    }
}
