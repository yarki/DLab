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

import com.epam.dlab.UserInstanceStatus;
import com.epam.dlab.dto.status.EnvResource;
import com.epam.dlab.dto.status.EnvResourceList;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.utils.ServiceUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public class CommandExecutorMock implements ICommandExecutor {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutorMock.class);

    private ObjectMapper MAPPER = new ObjectMapper()
    		.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
    		.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    
    private Map<String, String> variables = new HashMap<String, String>();
    private String responseFileName;
    private String jsonContent;
    
    /** Return variables for substitution into Json response file. */
    public Map<String, String> getVariables() {
    	return variables;
    }
    
    /** Response file name. */
    public String getResponseFileName() {
    	return responseFileName;
    }

    /** Json content for docker. */
    public String getJsonContent() {
    	return jsonContent;
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
    	
    	String resourceType = getResourceType(parser.getEnvironment());
    	String imageType = getImageType(parser.getOther());
    	String requestId = parser.getEnvironment().get("request_id");
    	String responsePath = parser.getVariable().get("/response");
    	jsonContent = parser.getJson();
    	LOGGER.debug("resourceType is {}, imageType is {}, requestId is {}, response path is {}", resourceType, imageType, requestId, responsePath);

    	variables.putAll(parser.getEnvironment());
    	variables.putAll(getJsonVariables(parser.getJson()));
    	variables.put("instance_id", "i-" + requestId.replace("-", "").substring(0, 17));
    	variables.put("cluster_id", "j-" + requestId.replace("-", "").substring(0, 13).toUpperCase());
    	variables.put("notebook_id", requestId.replace("-", "").substring(17, 22));
    	LOGGER.debug("jsonVars is {}", variables);
    	
    	switch (action) {
		case DESCRIBE:
			describe(imageType, requestId, responsePath);
			break;
		case CREATE:
		case START:
		case STOP:
		case TERMINATE:
		case CONFIGURE:
			action(user, resourceType, action, requestId, responsePath);
			break;
		case STATUS:
			variables.put("list_resources", getResponseStatus());
			action(user, resourceType, action, requestId, responsePath);
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
     * @exception if resource type name not found.
     */
    public static String getResourceType(Map<String, String> environment) throws DlabException {
		return environment.get("conf_resource");
    }
    
    /** Return name of image type: edge, jupyter, zeppelin, ...
     * @param args list of arguments.
     * @exception if image name not found.
     */
    public static String getImageType(List<String> args) throws DlabException {
		String imageName = getImageName(args);
		return imageName.replace("docker.dlab-", "").replace(":latest", "");
    }
    
    /** Describe action.
     * @param uuid UUID for request.
     * @param imageType name of docker image.
     * @param responsePath path for response file.
     */
    public void describe(String imageType, String uuid, String responsePath) {
    	String templateFileName = getAbsolutePath(ServiceUtils.getUserDir(), "../../infrastructure-provisioning/src", imageType, "description.json");
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
    
    /** Perform docker action.
     * @param user the name of user.
     * @param resourceType the name of resource type: edge, emr, zeppelin, etc.
     * @param action docker action.
     * @param uuid UUID for response.
     * @param responsePath the path to store response file.
     */
    public void action(String user, String resourceType, DockerAction action, String uuid, String responsePath) {
    	String prefixFileName = (resourceType.equals("edge") || resourceType.equals("emr") ?
    			resourceType : "notebook") + "_";
    	String templateFileName = "mock_response/" + prefixFileName + action.toString() + ".json";
    	responseFileName = getAbsolutePath(responsePath, prefixFileName + user + "_" + uuid + ".json");
    	setResponse(templateFileName, responseFileName);
    }
    
    /** Return the section of resource statuses for docker action status.
     */
    private String getResponseStatus() {
    	EnvResourceList resourceList;
    	try {
        	JsonNode json = MAPPER.readTree(getJsonContent());
			json = json.get("edge_list_resources");
			resourceList = MAPPER.readValue(json.toString(), EnvResourceList.class);
		} catch (IOException e) {
			throw new DlabException("Can't parse json content: " + e.getLocalizedMessage(), e);
		}
    	
    	if (resourceList.getHostList() !=  null) {
    		for (EnvResource host : resourceList.getHostList()) {
    			host.setStatus(UserInstanceStatus.RUNNING.toString());
    		}
    	}
    	if (resourceList.getClusterList() != null) {
    		for (EnvResource host : resourceList.getClusterList()) {
    			host.setStatus(UserInstanceStatus.RUNNING.toString());
    		}
    	}
    	
    	try {
			return MAPPER.writeValueAsString(resourceList);
		} catch (JsonProcessingException e) {
			throw new DlabException("Can't generate json content: " + e.getLocalizedMessage(), e);
		}
    }

    /** Return the value of json property or <b>null</b>.
     * @param jsonNode - Json node.
     */
    private String getTextValue(JsonNode jsonNode) {
        return jsonNode != null ? jsonNode.textValue() : null;
    }
    
    /** Extract Json properties from json content.
     * @param jsonContent Json content.
     * @return
     */
    private Map<String, String> getJsonVariables(String jsonContent) {
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
    
    /** Write response file.
     * @param sourceFileName template file name.
     * @param targetFileName response file name.
     * @throws DlabException if can't read template or write response files.
     */
    private void setResponse(String sourceFileName, String targetFileName) throws DlabException {
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
    			"\"conf_service_base_name\":\"usein1122v4\",\"conf_os_user\":\"ubuntu\",\"exploratory_name\":\"useinj1\"," +
    			"\"application\":\"jupyter\",\"computational_name\":\"useine2\",\"emr_version\":\"emr-5.2.0\"," +
    			"\"notebook_instance_name\":\"usein1122v4-usein_faradzhev-nb-useinj1-b0a2e\"}' | " +
    			"docker run -i --name usein_faradzhev_configure_computational_useine2_1487676513703 " +
    			"-v /home/ubuntu/keys:/root/keys " +
    			"-v /opt/dlab/tmp/result:/response " +
    			"-v /var/opt/dlab/log/emr:/logs/emr " +
    			"-e \"conf_resource=emr\" " +
    			"-e \"request_id=dc3c1002-c07d-442b-99f9-18085aeb2881\" " +
    			"-e \"conf_key_name=BDCC-DSS-POC\" " +
    			"docker.dlab-jupyter --action configure";
    	commandExecutor.executeAsync("user", "uuid", cmd);
    }
}
