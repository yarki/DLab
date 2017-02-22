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
    
    private CommandParserMock parser = new CommandParserMock();
    private String responseFileName;
    
    /** Return variables for substitution into Json response file. */
    public Map<String, String> getVariables() {
    	return parser.getVariables();
    }
    
    /** Response file name. */
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

        responseFileName = null;
        
    	parser = new CommandParserMock(command);
    	LOGGER.debug("Parser is {}", parser);
    	DockerAction action = DockerAction.of(parser.getAction());
    	LOGGER.debug("Action is {}", action);
    	
    	if (action == null) {
    		throw new DlabException("Docker action not defined");
    	}

    	switch (action) {
		case DESCRIBE:
			describe();
			break;
		case CREATE:
		case START:
		case STOP:
		case TERMINATE:
		case CONFIGURE:
			action(user, action);
			break;
		case STATUS:
			parser.getVariables().put("list_resources", getResponseStatus());
			action(user, action);
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

    
    /** Describe action.
     * @param uuid UUID for request.
     * @param imageType name of docker image.
     * @param responsePath path for response file.
     */
    public void describe() {
    	String templateFileName = getAbsolutePath(
    			ServiceUtils.getUserDir(),
    			"../../infrastructure-provisioning/src",
    			parser.getImageType(),
    			"description.json");
    	responseFileName = getAbsolutePath(parser.getResponsePath(), parser.getRequestId() + ".json");

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
    public void action(String user, DockerAction action) {
    	String resourceType = parser.getResourceType();
		String prefixFileName = (resourceType.equals("edge") || resourceType.equals("emr") ?
    			resourceType : "notebook") + "_";
    	String templateFileName = "mock_response/" + prefixFileName + action.toString() + ".json";
    	responseFileName = getAbsolutePath(parser.getResponsePath(), prefixFileName + user + "_" + parser.getRequestId() + ".json");
    	setResponse(templateFileName, responseFileName);
    }
    
    /** Return the section of resource statuses for docker action status.
     */
    private String getResponseStatus() {
    	EnvResourceList resourceList;
    	try {
        	JsonNode json = MAPPER.readTree(parser.getJson());
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
    	
    	for (String key : parser.getVariables().keySet()) {
    		String value = parser.getVariables().get(key);
    		content = content.replace("${" + key.toUpperCase() + "}", value);
    	}
    	
    	try (BufferedWriter out = new BufferedWriter(new FileWriter(targetFileName))) {
    	    out.write(content);  
    	} catch (IOException e) {
			throw new DlabException("Can't write response file " + targetFileName + ": " + e.getLocalizedMessage(), e);
    	}
    }
    
}
