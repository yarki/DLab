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

import static com.epam.dlab.backendapi.core.health.HealthChecks.MONGO_HEALTH_CHECKER;
import static com.epam.dlab.backendapi.core.health.HealthChecks.PROVISIONING_HEALTH_CHECKER;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.dao.ComputationalDAO;
import com.epam.dlab.backendapi.dao.KeyDAO;
import com.epam.dlab.backendapi.dao.SettingsDAO;
import com.epam.dlab.backendapi.resources.dto.HealthStatusDTO;
import com.epam.dlab.constants.ServiceConsts;
import com.epam.dlab.contracts.HealthChecker;
import com.epam.dlab.dto.status.EnvResource;
import com.epam.dlab.dto.status.EnvResourceDTO;
import com.epam.dlab.dto.status.EnvResourceList;
import com.epam.dlab.dto.status.EnvStatusDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.ApiCallbacks;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.dropwizard.auth.Auth;

/** Provides the REST API for the basic information about infrastructure.
 */
@Path(ApiCallbacks.INFRASTRUCTURE)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class InfrasctructureResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(InfrasctructureResource.class);

    @Inject
    private SettingsDAO settingsDAO;
    @Inject
    private KeyDAO keyDAO;
    @Inject
    private ComputationalDAO ifnCompDao;
    
    @Inject
    @Named(ServiceConsts.PROVISIONING_SERVICE_NAME)
    private RESTService provisioningService;

    @Inject
    @Named(MONGO_HEALTH_CHECKER)
    private HealthChecker mongoHealthChecker;
    @Inject
    @Named(PROVISIONING_HEALTH_CHECKER)
    private HealthChecker provisioningHealthChecker;

    /** Returns the status of infrastructure: database and provisioning service.
     * @param userInfo user info.
     */
    @GET
    @Path(ApiCallbacks.STATUS_URI)
    public HealthStatusDTO status(@Auth UserInfo userInfo) {
        return new HealthStatusDTO()
                .withMongoAlive(mongoHealthChecker.isAlive())
                .withProvisioningAlive(provisioningHealthChecker.isAlive());
    }
    
    
    @POST
    @Path(ApiCallbacks.STATUS_URI + "_test")
    public Response statusTest(@Auth UserInfo userInfo) {
    	List<EnvResource> hostList = new ArrayList<EnvResource>();
    	String edgePrivateIp = keyDAO
    			.getUserAWSCredential(userInfo.getName())
    			.getPrivateIp();
    	hostList.add(new EnvResource().withId(edgePrivateIp));
    	
    	//Iterable<Document> documents = ifnCompDao.find(userInfo.getName());
    	
		EnvResourceList resourceList = new EnvResourceList()
    			.withHostList(hostList);
		EnvResourceDTO dto = new EnvResourceDTO()
    			.withAwsRegion(settingsDAO.getAwsRegion())
    			.withIamUserName(userInfo.getName())
    			.withResourceList(resourceList);
    	
		return Response.ok(
    			provisioningService.post(ApiCallbacks.INFRASTRUCTURE + ApiCallbacks.STATUS_URI, userInfo.getAccessToken(),
    					dto, EnvResourceDTO.class)
    			).build();
    }
    
    /** Updates the status of the resources for user.
     * @param dto DTO info about the statuses of resources.
     * @return Always return code 200 (OK).
     */
    @POST
    @Path(ApiCallbacks.STATUS_URI)
    public Response status(@Auth UserInfo userInfo, EnvStatusDTO dto) {
        LOGGER.debug("Updating status for resources for user {}: {}", dto.getUser(), dto);
        try {
        	//infrastructureProvisionDAO.updateComputationalFields(dto);
        } catch (DlabException e) {
        	LOGGER.error("Could not update status for resources for user {}: {}", dto.getUser(), e.getLocalizedMessage(), e);
        }
        return Response.ok().build();
    }

}
