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

    	Map<String, String> jsonVars = new HashMap<String, String>(parser.getEnvironment());
    	jsonVars.putAll(getJsonVariables(parser.getJson()));
    	jsonVars.put("instance_id", "i-" + requestId.replace("-", "").substring(0, 17));
    	jsonVars.put("notebook_id", requestId.replace("-", "").substring(17, 22));
    	LOGGER.debug("jsonVars is {}", jsonVars);
    	
    	switch (action) {
		case DESCRIBE:
			describe(resourceType, requestId, responsePath);
			break;
		case CREATE:
		case START:
		case STOP:
		case TERMINATE:
			action(user, resourceType, action, requestId, responsePath, jsonVars);
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
    	String responseFileName = getAbsolutePath(responsePath, uuid + ".json");

    	LOGGER.debug("Create response file from {} to {}", templateFileName, responseFileName);
    	File fileResponse = new File(responseFileName);
		try {
			Files.createParentDirs(fileResponse);
			Files.copy(new File(templateFileName), fileResponse);
		} catch (IOException e) {
			throw new DlabException("Can't create response file " + responseFileName + ": " + e.getLocalizedMessage(), e);
		}
    }
    
    public void action(String user, String resourceType, DockerAction action, String uuid, String responsePath, Map<String, String> jsonVars) {
    	String prefixFileName = (action == DockerAction.CREATE ? resourceType : "notebook") + "_";
    	String templateFileName = prefixFileName + action.toString() + ".json";
    	String responseFileName = getAbsolutePath(responsePath, prefixFileName + user + "_" + uuid + ".json");
    	setResponse(templateFileName, responseFileName, jsonVars);
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
    
    public void setResponse(String sourceFileName, String targetFileName, Map<String, String> variables) throws DlabException {
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
    	cmd = "docker run " +
    			"-v /home/ubuntu/keys:/root/keys " +
    			"-v /opt/dlab/tmp/result:/response " +
    			"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    			"-e \"conf_resource=notebook\" " +
    			"-e \"request_id=28ba67a4-b2ee-4753-a406-892977089ad9\" " +
    			"docker.dlab-zeppelin:latest --action describe";
    	commandExecutor.executeAsync("user", "uuid", cmd);
    	
    	cmd = "echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"usein_faradzhev@epam.com\",\"edge_user_name\":\"usein_faradzhev\"," +
    			"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\",\"conf_os_family\":\"debian\"," +
    			"\"aws_vpc_id\":\"vpc-83c469e4\",\"aws_subnet_id\":\"subnet-22db937a\",\"aws_security_groups_ids\":\"sg-4d42dc35\"}' | " +
    			"docker run -i --name usein_faradzhev_create_exploratory_edge_1487309918496 " +
    			"-v /home/ubuntu/keys:/root/keys " +
    			"-v /opt/dlab/tmp/result:/response " +
    			"-v /var/opt/dlab/log/edge:/logs/edge " +
    			"-e \"conf_resource=edge\" " +
    			"-e \"request_id=b8267ae6-07b0-44ef-a489-7714b20cf0a4\" " +
    			"-e \"conf_key_name=BDCC-DSS-POC\" " +
    			"docker.dlab-edge --action create";
    	commandExecutor.executeAsync("user", "uuid", cmd);

    	cmd = "echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"usein_faradzhev@epam.com\",\"edge_user_name\":\"usein_faradzhev\"," +
    			"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\",\"conf_os_family\":\"debian\"," +
    			"\"exploratory_name\":\"useinxz1\",\"application\":\"zeppelin\",\"notebook_image\":\"docker.dlab-zeppelin\"," +
    			"\"aws_notebook_instance_type\":\"t2.medium\",\"aws_security_groups_ids\":\"sg-4d42dc35\"}' | " +
    			"docker run -i --name usein_faradzhev_create_exploratory_useinxz1_1487312574572 " +
    			"-v /home/ubuntu/keys:/root/keys " +
    			"-v /opt/dlab/tmp/result:/response " +
    			"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    			"-e \"conf_resource=notebook\" " +
    			"-e \"request_id=f720f30b-5949-4919-a50b-ce7af58d6fe9\" " +
    			"-e \"conf_key_name=BDCC-DSS-POC\" " +
    			"docker.dlab-zeppelin --action create";
    	commandExecutor.executeAsync("user", "uuid", cmd);

    	cmd = "echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"usein_faradzhev@epam.com\",\"edge_user_name\":\"usein_faradzhev\"," +
    			"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\"," +
    			"\"exploratory_name\":\"useinxz1\",\"notebook_image\":\"docker.dlab-zeppelin\"," +
    			"\"notebook_instance_name\":\"usein1120v13-usein_faradzhev-nb-useinxz1-78af3\",\"conf_key_dir\":\"/root/keys\"}' | " +
    			"docker run -i --name usein_faradzhev_stop_exploratory_useinxz1_1487315364165 " +
    			"-v /home/ubuntu/keys:/root/keys " +
    			"-v /opt/dlab/tmp/result:/response " +
    			"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    			"-e \"conf_resource=notebook\" " +
    			"-e \"request_id=33998e05-7781-432e-b748-bf3f0e7f9342\" " +
    			"-e \"conf_key_name=BDCC-DSS-POC\" " +
    			"docker.dlab-zeppelin --action stop";
    	commandExecutor.executeAsync("user", "uuid", cmd);
    	
    	cmd = "echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"usein_faradzhev@epam.com\",\"edge_user_name\":\"usein_faradzhev\"," +
    			"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\",\"conf_os_family\":\"debian\"," +
    			"\"exploratory_name\":\"useinxz1\",\"notebook_image\":\"docker.dlab-zeppelin\"," +
    			"\"notebook_instance_name\":\"usein1120v13-usein_faradzhev-nb-useinxz1-78af3\"}' | " +
    			"docker run -i --name usein_faradzhev_start_exploratory_useinxz1_1487316756857 " +
    			"-v /home/ubuntu/keys:/root/keys " +
    			"-v /opt/dlab/tmp/result:/response " +
    			"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    			"-e \"conf_resource=notebook\" " +
    			"-e \"request_id=d50b9d20-1b1a-415f-8e47-ed0aca029e73\" " +
    			"-e \"conf_key_name=BDCC-DSS-POC\" " +
    			"docker.dlab-zeppelin --action start";
    	commandExecutor.executeAsync("user", "uuid", cmd);
    	
    	cmd = "echo -e '{\"aws_region\":\"us-west-2\",\"aws_iam_user\":\"usein_faradzhev@epam.com\",\"edge_user_name\":\"usein_faradzhev\"," +
    			"\"conf_service_base_name\":\"usein1120v13\",\"conf_os_user\":\"ubuntu\",\"conf_os_family\":\"debian\"," +
    			"\"exploratory_name\":\"useinxz1\",\"notebook_image\":\"docker.dlab-zeppelin\"," +
    			"\"notebook_instance_name\":\"usein1120v13-usein_faradzhev-nb-useinxz1-78af3\"}' | " +
    			"docker run -i --name usein_faradzhev_terminate_exploratory_useinxz1_1487318040180 " +
    			"-v /home/ubuntu/keys:/root/keys " +
    			"-v /opt/dlab/tmp/result:/response " +
    			"-v /var/opt/dlab/log/notebook:/logs/notebook " +
    			"-e \"conf_resource=notebook\" " +
    			"-e \"request_id=de217441-9757-4c4e-b020-548f66b58e00\" " +
    			"-e \"conf_key_name=BDCC-DSS-POC\" " +
    			"docker.dlab-zeppelin --action terminate";
    	commandExecutor.executeAsync("user", "uuid", cmd);
/*


 */
    }
}
