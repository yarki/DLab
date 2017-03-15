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
import java.util.Map;
import java.util.function.Supplier;

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

public class CommandExecutorMockAsync implements Supplier<Boolean> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommandExecutorMockAsync.class);

    private ObjectMapper MAPPER = new ObjectMapper()
    		.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true)
    		.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    
    private String user;
    private String uuid;
    private String command;
    
    private CommandParserMock parser = new CommandParserMock();
    private String responseFileName;
    
    public CommandExecutorMockAsync(String user, String uuid, String command) {
    	this.user = user;
    	this.uuid = uuid;
    	this.command = command;
	}
    
	@Override
	public Boolean get() {
		run();
		return true;
	}

	
	/** Return parser of command line. */
    public CommandParserMock getParser() {
    	return parser;
    }
    
	/** Return variables for substitution into Json response file. */
    public Map<String, String> getVariables() {
    	return parser.getVariables();
    }
    
    /** Response file name. */
    public String getResponseFileName() {
    	return responseFileName;
    }

    public void run() {
    	LOGGER.debug("Run OS command for user {} with UUID {}: {}", user, uuid, command);

        responseFileName = null;
    	parser = new CommandParserMock(command, uuid);
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
			action(user, action);
			break;
		case CONFIGURE:
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) { }
			action(user, action);
			break;
		case STATUS:
			parser.getVariables().put("list_resources", getResponseStatus(true));
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
    private static String getAbsolutePath(String first, String ... more) {
    	return Paths.get(first, more).toAbsolutePath().toString();
    }

    
    /** Describe action.
     */
    private void describe() {
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
     * @param action docker action.
     */
    private void action(String user, DockerAction action) {
    	String resourceType = parser.getResourceType();
		String prefixFileName = (resourceType.equals("edge") || resourceType.equals("emr") ?
    			resourceType : "notebook") + "_";
    	String templateFileName = "mock_response/" + prefixFileName + action.toString() + ".json";
    	responseFileName = getAbsolutePath(parser.getResponsePath(), prefixFileName + user + "_" + parser.getRequestId() + ".json");
    	setResponse(templateFileName, responseFileName);
    }
    
    /** Return the section of resource statuses for docker action status.
     */
    private String getResponseStatus(boolean noUpdate) {
    	if (noUpdate) {
    		return "{}";
    	}
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
    	
    	File fileResponse = new File(responseFileName);
    	try (BufferedWriter out = new BufferedWriter(new FileWriter(fileResponse))) {
        	Files.createParentDirs(fileResponse);
    	    out.write(content);
    	} catch (IOException e) {
			throw new DlabException("Can't write response file " + targetFileName + ": " + e.getLocalizedMessage(), e);
    	}
    	LOGGER.debug("Create response file from {} to {}", sourceFileName, targetFileName);
    }
}
