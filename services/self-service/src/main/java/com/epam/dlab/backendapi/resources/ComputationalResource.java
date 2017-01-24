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

import static com.epam.dlab.UserInstanceStatus.CREATING;
import static com.epam.dlab.UserInstanceStatus.FAILED;
import static com.epam.dlab.UserInstanceStatus.TERMINATING;

import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.UserInstanceStatus;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.core.UserInstanceDTO;
import com.epam.dlab.backendapi.dao.InfrastructureProvisionDAO;
import com.epam.dlab.backendapi.dao.SettingsDAO;
import com.epam.dlab.backendapi.resources.dto.ComputationalCreateFormDTO;
import com.epam.dlab.backendapi.resources.dto.ComputationalLimitsDTO;
import com.epam.dlab.constants.ServiceConsts;
import com.epam.dlab.dto.computational.ComputationalCreateDTO;
import com.epam.dlab.dto.computational.ComputationalStatusDTO;
import com.epam.dlab.dto.computational.ComputationalTerminateDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.ApiCallbacks;
import com.epam.dlab.rest.contracts.ComputationalAPI;
import com.epam.dlab.utils.UsernameUtils;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.dropwizard.auth.Auth;

/** Provides the REST API for the computational resource.
 */
@Path("/infrastructure_provision/computational_resources")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ComputationalResource implements ComputationalAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputationalResource.class);

    @Inject
    private SettingsDAO settingsDAO;
    @Inject
    private InfrastructureProvisionDAO infrastructureProvisionDAO;
    @Inject
    @Named(ServiceConsts.PROVISIONING_SERVICE_NAME)
    private RESTService provisioningService;
    @Inject
    private SelfServiceApplicationConfiguration configuration;


    /** Returns the limits for creation the computational resources.
     * @param userInfo user info.
     */
    @GET
    @Path("/limits")
    public ComputationalLimitsDTO getLimits(@Auth UserInfo userInfo) {
    	ComputationalLimitsDTO limits = new ComputationalLimitsDTO()
    			.withMinEmrInstanceCount(configuration.getMinEmrInstanceCount())
    			.withMaxEmrInstanceCount(configuration.getMaxEmrInstanceCount());
    	LOGGER.debug("Returns limits for user {}: {}", userInfo.getName(), limits.toString());
        return limits;
    }
    
    /** Sends request to provisioning service for creation the computational resource for user.
     * @param userInfo user info.
     * @param formDTO DTO info about creation of the computational resource.
     * @return 200 OK - if request success, 302 Found - for duplicates, otherwise throws exception.
     * @exception DlabException
     */
    @PUT
    public Response create(@Auth UserInfo userInfo, @Valid @NotNull ComputationalCreateFormDTO formDTO) throws DlabException {
    	LOGGER.debug("Send request for creation the computational resource {} for user {}", formDTO.getName(), userInfo.getName());

        int slaveInstanceCount = Integer.parseInt(formDTO.getInstanceCount());
        if (slaveInstanceCount < configuration.getMinEmrInstanceCount() || slaveInstanceCount > configuration.getMaxEmrInstanceCount()) {
            LOGGER.warn("Creating computational resource {} for user {} fail: Limit exceeded to creation slave instances. Minimum is {}, maximum is {}",
            		formDTO.getName(), userInfo.getName(), configuration.getMinEmrInstanceCount(), configuration.getMaxEmrInstanceCount());
            throw new DlabException("Limit exceeded to creation slave instances. Minimum is " + configuration.getMinEmrInstanceCount() +
            		", maximum is " + configuration.getMaxEmrInstanceCount() + ".");
        }

        boolean isAdded = infrastructureProvisionDAO.addComputational(userInfo.getName(), formDTO.getNotebookName(),
                new UserComputationalResourceDTO()
                        .withComputationalName(formDTO.getName())
                        .withStatus(CREATING.toString())
                        .withMasterShape(formDTO.getMasterInstanceType())
                        .withSlaveShape(formDTO.getSlaveInstanceType())
                        .withSlaveNumber(formDTO.getInstanceCount()));
        if (isAdded) {
            try {
            	UserInstanceDTO instance = getExploratoryInstance(userInfo.getName(), formDTO.getNotebookName());
                ComputationalCreateDTO dto = new ComputationalCreateDTO()
                        .withServiceBaseName(settingsDAO.getServiceBaseName())
                        .withExploratoryName(formDTO.getNotebookName())
                        .withNotebookTemplateName(instance.getTemplateName())
                        .withApplicationName(getApplicationName(instance.getImageName()))
                        .withComputationalName(formDTO.getName())
                        .withNotebookName(instance.getExploratoryId())
                        .withInstanceCount(formDTO.getInstanceCount())
                        .withMasterInstanceType(formDTO.getMasterInstanceType())
                        .withSlaveInstanceType(formDTO.getSlaveInstanceType())
                        .withVersion(formDTO.getVersion())
                        .withEdgeUserName(UsernameUtils.removeDomain(userInfo.getName()))
                        .withIamUserName(userInfo.getName())
                        .withAwsRegion(settingsDAO.getAwsRegion())
                        .withConfOsUser(settingsDAO.getConfOsUser())
                        .withConfOsFamily(settingsDAO.getConfOsFamily());
                return Response
                        .ok(provisioningService.post(EMR_CREATE, userInfo.getAccessToken(), dto, String.class))
                        .build();
            } catch (Throwable t) {
            	try {
            		updateComputationalStatus(userInfo.getName(), formDTO.getNotebookName(), formDTO.getName(), FAILED);
            	} catch (DlabException e) {
            		LOGGER.warn("Could not update the status of computational resource {} for user {}", formDTO.getName(), userInfo.getName());
            	}
                throw new DlabException("Could not send request for creation the computational resource " + formDTO.getName(), t);
            }
        } else {
            LOGGER.warn("Used existing computational resource {} for user {}", formDTO.getName(), userInfo.getName());
            return Response.status(Response.Status.FOUND).build();
        }
    }

    /** Updates the status of the computational resource for user.
     * @param dto DTO info about the status of the computational resource.
     * @return 200 OK - if request success otherwise throws exception.
     * @exception DlabException if update the status fails.
     */
    @POST
    @Path(ApiCallbacks.STATUS_URI)
    public Response status(ComputationalStatusDTO dto) throws DlabException {
        LOGGER.debug("Updating status for computational resource {} for user {}: {}", dto.getComputationalName(), dto.getUser(), dto.getStatus());
        infrastructureProvisionDAO.updateComputationalFields(dto);
        return Response.ok().build();
    }

    /** Sends request to provisioning service for termination the computational resource for user.
     * @param userInfo user info.
     * @param exploratoryName name of exploratory.
     * @param computationalName name of computational resource.
     * @return JSON formatted string representation of {@link ComputationalTerminateDTO}, otherwise fails.
     * @exception DlabException
     */
    @DELETE
    @Path("/{exploratoryName}/{computationalName}/terminate")
    public String terminate(@Auth UserInfo userInfo,
    		@PathParam("exploratoryName") String exploratoryName,
    		@PathParam("computationalName") String computationalName) throws DlabException {
        LOGGER.debug("Terminating computational resource {} for user {}", computationalName, userInfo.getName());
        try {
    		updateComputationalStatus(userInfo.getName(), exploratoryName, computationalName, TERMINATING);
    	} catch (DlabException e) {
    		LOGGER.warn("Could not update the status of computational resource {} for user {}", computationalName, userInfo.getName());
    		throw new DlabException("Could not terminate computational resource " + computationalName, e);
    	}
        
        try {
            String exploratoryId = infrastructureProvisionDAO.fetchExploratoryId(userInfo.getName(), exploratoryName);
            String computationalId = infrastructureProvisionDAO.fetchComputationalId(userInfo.getName(), exploratoryName, computationalName);
            ComputationalTerminateDTO dto = new ComputationalTerminateDTO()
                    .withServiceBaseName(settingsDAO.getServiceBaseName())
                    .withExploratoryName(exploratoryName)
                    .withComputationalName(computationalName)
                    .withNotebookInstanceName(exploratoryId)
                    .withClusterName(computationalId)
                    .withConfKeyDir(settingsDAO.getConfKeyDir())
                    .withConfOsUser(settingsDAO.getConfOsUser())
                    .withConfOsFamily(settingsDAO.getConfOsFamily())
                    .withEdgeUserName(UsernameUtils.removeDomain(userInfo.getName()))
                    .withIamUserName(userInfo.getName())
                    .withAwsRegion(settingsDAO.getAwsRegion());
            return provisioningService.post(EMR_TERMINATE, userInfo.getAccessToken(), dto, String.class);
        } catch (Throwable t) {
        	try {
        		updateComputationalStatus(userInfo.getName(), exploratoryName, computationalName, FAILED);
        	} catch (DlabException e) {
        		LOGGER.warn("Could not update the status of computational resource {} for user {}", computationalName, userInfo.getName());
        	}
            throw new DlabException("Could not terminate computational resource " + computationalName, t);
        }
    }

    /** Updates the status of computational resource in database.
     * @param user user name.
     * @param exploratoryName name of exploratory.
     * @param computationalName name of computational resource.
     * @param status status
     * @throws DlabException if the update fails.
     */
    private void updateComputationalStatus(String user, String exploratoryName, String computationalName, UserInstanceStatus status) throws DlabException {
        ComputationalStatusDTO computationalStatus = new ComputationalStatusDTO()
                .withUser(user)
                .withExploratoryName(exploratoryName)
                .withComputationalName(computationalName)
                .withStatus(status);
        infrastructureProvisionDAO.updateComputationalStatus(computationalStatus);
    }
    
    /** Finds and returns the instance of exploratory.
     * @param username name of user.
     * @param exploratoryName name of exploratory.
     * @throws DlabException
     */
    private UserInstanceDTO getExploratoryInstance(String username, String exploratoryName) throws DlabException {
    	Optional<UserInstanceDTO> opt = infrastructureProvisionDAO.fetchExploratoryFields(username, exploratoryName);
        if( opt.isPresent() ) {
            return opt
            		.get();
        }
        throw new DlabException(String.format("Exploratory instance for user {} with name {} not found.", username, exploratoryName));
    }

    /** Returns the name of application for notebook: jupiter, rstudio, etc. */
    private String getApplicationName(String imageName) {
    	if (imageName != null) {
    		int pos = imageName.lastIndexOf('-');
    		if (pos > 0) {
    			return imageName.substring(pos + 1);
    		}
    	}
    	return "";
    }

}
