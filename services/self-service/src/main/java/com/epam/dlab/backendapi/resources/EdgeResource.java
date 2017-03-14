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

import static com.epam.dlab.UserInstanceStatus.FAILED;
import static com.epam.dlab.UserInstanceStatus.RUNNING;
import static com.epam.dlab.UserInstanceStatus.STARTING;
import static com.epam.dlab.UserInstanceStatus.STOPPED;
import static com.epam.dlab.UserInstanceStatus.STOPPING;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.UserInstanceStatus;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.dao.KeyDAO;
import com.epam.dlab.backendapi.dao.SettingsDAO;
import com.epam.dlab.backendapi.domain.RequestId;
import com.epam.dlab.constants.ServiceConsts;
import com.epam.dlab.dto.ResourceSysBaseDTO;
import com.epam.dlab.dto.keyload.UploadFileResultDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.ApiCallbacks;
import com.epam.dlab.rest.contracts.EdgeAPI;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.dropwizard.auth.Auth;

/** Provides the REST API for the exploratory.
 */
@Path("/infrastructure/edge")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EdgeResource implements EdgeAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(EdgeResource.class);

    @Inject
    private SettingsDAO settingsDAO;
    @Inject
    private KeyDAO keyDAO;
    @Inject
    @Named(ServiceConsts.PROVISIONING_SERVICE_NAME)
    private RESTService provisioningService;

    /** Starts EDGE node for user.
     * @param userInfo user info.
     * @return Request Id.
     * @throws DlabException
     */
    @POST
    @Path("/start")
    public String start(@Auth UserInfo userInfo) throws DlabException {
        LOGGER.debug("Starting EDGE node for user {}", userInfo.getName());
        UserInstanceStatus status = UserInstanceStatus.of(keyDAO.getEdgeStatus(userInfo.getName()));
    	if (status == null || !status.in(STOPPED)) {
        	LOGGER.error("Could not start EDGE node for user {} because the status of instance is {}", userInfo.getName(), status);
            throw new DlabException("Could not start EDGE node because the status of instance is " + status);
        }

    	try {
        	return action(userInfo, EDGE_START, STARTING);
        } catch (DlabException e) {
        	LOGGER.error("Could not start EDGE node for user {}", userInfo.getName(), e);
        	throw new DlabException("Could not start EDGE node: " + e.getLocalizedMessage(), e);
        }
    }

    /** Stop EDGE node for user.
     * @param userInfo user info.
     * @return Request Id.
     * @throws DlabException
     */
    @POST
    @Path("/stop")
    public String stop(@Auth UserInfo userInfo) throws DlabException {
        LOGGER.debug("Stopping EDGE node for user {}", userInfo.getName());
        UserInstanceStatus status = UserInstanceStatus.of(keyDAO.getEdgeStatus(userInfo.getName()));
    	if (status == null || !status.in(RUNNING)) {
        	LOGGER.error("Could not stop EDGE node for user {} because the status of instance is {}", userInfo.getName(), status);
            throw new DlabException("Could not stop EDGE node because the status of instance is " + status);
        }

    	try {
        	return action(userInfo, EDGE_STOP, STOPPING);
        } catch (DlabException e) {
        	LOGGER.error("Could not stop EDGE node for user {}", userInfo.getName(), e);
        	throw new DlabException("Could not stop EDGE node: " + e.getLocalizedMessage(), e);
        }
    }

    
    /** Stores the result of the upload the user key.
     * @param dto result of the upload the user key.
     * @return 200 OK
     */
    @POST
    @Path(ApiCallbacks.STATUS_URI)
    public Response status(UploadFileResultDTO dto) throws DlabException {
    	RequestId.checkAndRemove(dto.getRequestId());
    	try {
    		LOGGER.debug("Updating the status of EDGE node for user {} to {}", dto.getUser(), dto.getStatus());
	        keyDAO.updateEdgeStatus(dto.getUser(), dto.getStatus());
    	} catch (DlabException e) {
        	LOGGER.error("Could not update status of EDGE node for user {} to {}", dto.getUser(), dto.getStatus(), e);
        	throw new DlabException("Could not update status of EDGE node to " + dto.getStatus() + ": " + e.getLocalizedMessage(), e);
    	}
        return Response.ok().build();
    }


    /** Sends the post request to the provisioning service and update the status of EDGE node.
     * @param userInfo user info.
     * @param action action for EDGE node.
     * @param status status of EDGE node.
     * @return Request Id.
     * @throws DlabException
     */
    private String action(UserInfo userInfo, String action, UserInstanceStatus status) throws DlabException {
        try {
        	keyDAO.updateEdgeStatus(userInfo.getName(), status.toString());
        	ResourceSysBaseDTO<?> dto = new ResourceSysBaseDTO<>()
                	.withAwsIamUser(userInfo.getName())
                	.withAwsRegion(settingsDAO.getAwsRegion())
                	.withConfOsFamily(settingsDAO.getConfOsFamily())
                	.withConfOsUser(settingsDAO.getConfOsUser())
                	.withEdgeUserName(userInfo.getSimpleName())
                	.withServiceBaseName(settingsDAO.getServiceBaseName());

            String uuid = provisioningService.post(action, userInfo.getAccessToken(), dto, String.class);
            RequestId.put(userInfo.getName(), uuid);
            return uuid;
        } catch (Throwable t) {
        	keyDAO.updateEdgeStatus(userInfo.getName(), FAILED.toString());
            throw new DlabException("Could not " + action + " EDGE node " + ": " + t.getLocalizedMessage(), t);
        }
    }

}