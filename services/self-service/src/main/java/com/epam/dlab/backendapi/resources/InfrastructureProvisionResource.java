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

package com.epam.dlab.backendapi.resources;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.dao.InfrastructureProvisionDAO;
import com.epam.dlab.backendapi.dao.KeyDAO;
import com.epam.dlab.constants.ServiceConsts;
import com.epam.dlab.dto.imagemetadata.ComputationalMetadataDTO;
import com.epam.dlab.dto.imagemetadata.ExploratoryMetadataDTO;
import com.epam.dlab.dto.keyload.UserAWSCredentialDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.DockerAPI;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.dropwizard.auth.Auth;

/** Provides the REST API for the information about provisioning infrastructure.
 */
@Path("/infrastructure_provision")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InfrastructureProvisionResource implements DockerAPI {
    public static final String EDGE_IP = "edge_node_ip";
    private static final Logger LOGGER = LoggerFactory.getLogger(InfrastructureProvisionResource.class);

    @Inject
    private InfrastructureProvisionDAO dao;
    @Inject
    private KeyDAO keyDAO;
    @Inject
    @Named(ServiceConsts.PROVISIONING_SERVICE_NAME)
    private RESTService provisioningService;

    /** Returns the list of the provisioned user resources.
     * @param userInfo user info.
     */
    @GET
    @Path("/provisioned_user_resources")
    public Iterable<Document> getList(@Auth UserInfo userInfo) throws DlabException {
        LOGGER.debug("Loading list of provisioned resources for user {}", userInfo.getName());
        try {
        	Iterable<Document> documents = appendEdgeInfo(dao.find(userInfo.getName()), userInfo.getName());
        	documents.forEach(d -> {
        		int i = 0;
        		LOGGER.debug("Notebook[{}]: {}", ++i, d);
        		});
    		return documents;
        } catch (Throwable t) {
        	LOGGER.error("Could not load list of provisioned resources for user: {}", userInfo.getName(), t);
            throw new DlabException("Could not load list of provisioned resources for user " + userInfo.getName() + ": " + t.getLocalizedMessage(), t);
        }
    }

    private List<Document> appendEdgeInfo(Iterable<Document> documents, String username) {
        UserAWSCredentialDTO cred = keyDAO.getUserAWSCredential(username);
        return StreamSupport.stream(documents.spliterator(), false)
                .map(document -> document
                		.append(EDGE_IP, cred.getPublicIp())
                		.append(UserAWSCredentialDTO.USER_OWN_BUCKET_NAME, cred.getUserOwnBucketName()))
                .collect(Collectors.toList());
    }

    /** Returns the list of the shapes for user.
     * @param userInfo user info.
     */
    @GET
    @Path("/computational_resources_shapes")
    public Iterable<Document> getShapes(@Auth UserInfo userInfo) {
        LOGGER.debug("Loading list of shapes for user {}", userInfo.getName());
        try {
        	return dao.findShapes();
        } catch (Throwable t) {
        	LOGGER.error("Could not load list of shapes for user: {}", userInfo.getName(), t);
            throw new DlabException("Could not load list of shapes for user " + userInfo.getName() + ": " + t.getLocalizedMessage(), t);
        }
    }

    /** Returns the list of the computational resources templates for user.
     * @param userInfo user info.
     */
    @GET
    @Path("/computational_resources_templates")
    public Iterable<ComputationalMetadataDTO> getComputationalTemplates(@Auth UserInfo userInfo) {
        LOGGER.debug("Loading list of computational templates for user {}", userInfo.getName());
        try {
        	return Stream.of(provisioningService.get(DOCKER_COMPUTATIONAL, userInfo.getAccessToken(), ComputationalMetadataDTO[].class))
                .collect(Collectors.toSet());
        } catch (Throwable t) {
        	LOGGER.error("Could not load list of computational templates for user: {}", userInfo.getName(), t);
            throw new DlabException("Could not load list of computational templates for user " + userInfo.getName() + ": " + t.getLocalizedMessage(), t);
        }
    }

    /** Returns the list of the exploratory environment templates for user.
     * @param userInfo user info.
     */
    @GET
    @Path("/exploratory_environment_templates")
    public Iterable<ExploratoryMetadataDTO> getExploratoryTemplates(@Auth UserInfo userInfo) {
        LOGGER.debug("Loading list of exploratory templates for user {}", userInfo.getName());
        try {
	        List<ExploratoryMetadataDTO> list = Stream.of(provisioningService.get(DOCKER_EXPLORATORY, userInfo.getAccessToken(), ExploratoryMetadataDTO[].class))
	        		.collect(Collectors.toList());
	        list.forEach(m -> {
	            int separatorIndex = m.getImage().indexOf(":");
	            if(separatorIndex > 0) {
	                m.setImage(m.getImage().substring(0, separatorIndex));
	            }
	        });
	        return list;
        } catch (Throwable t) {
        	LOGGER.error("Could not load list of exploratory templates for user: {}", userInfo.getName(), t);
            throw new DlabException("Could not load list of exploratory templates for user " + userInfo.getName() + ": " + t.getLocalizedMessage(), t);
        }
    }
}

