/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.api.form.EMRCreateFormDTO;
import com.epam.dlab.backendapi.api.form.EMRTerminateFormDTO;
import com.epam.dlab.backendapi.api.instance.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceStatus;
import com.epam.dlab.backendapi.client.rest.EmrAPI;
import com.epam.dlab.backendapi.dao.SettingsDAO;
import com.epam.dlab.backendapi.dao.UserListDAO;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.emr.EMRCreateDTO;
import com.epam.dlab.dto.emr.EMRStatusDTO;
import com.epam.dlab.dto.emr.EMRTerminateDTO;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.dropwizard.auth.Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

@Path("/emr")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class EmrResource implements EmrAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmrResource.class);

    @Inject
    private SettingsDAO settingsDAO;
    @Inject
    private UserListDAO userListDAO;
    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @POST
    @Path("/create")
    public Response create(@Auth UserInfo userInfo, EMRCreateFormDTO formDTO) {
        LOGGER.debug("creating emr {} for user {}", formDTO.getName(), userInfo.getName());
        boolean isAdded = userListDAO.addComputational(userInfo.getName(), formDTO.getNotebookName(),
                new UserComputationalResourceDTO()
                        .withResourceName(formDTO.getName())
                        .withStatus(UserInstanceStatus.CREATING.getStatus())
                        .withMasterShape(formDTO.getMasterInstanceType())
                        .withSlaveShape(formDTO.getSlaveInstanceType())
                        .withSlaveNumber(formDTO.getInstanceCount()));
        if (isAdded) {
            EMRCreateDTO dto = new EMRCreateDTO()
                    .withServiceBaseName(settingsDAO.getServiceBaseName())
                    .withInstanceCount(formDTO.getInstanceCount())
                    .withMasterInstanceType(formDTO.getMasterInstanceType())
                    .withSlaveInstanceType(formDTO.getSlaveInstanceType())
                    .withVersion(formDTO.getVersion())
                    .withNotebookName(formDTO.getNotebookName())
                    .withEdgeUserName(userInfo.getName())
                    .withRegion(settingsDAO.getAwsRegion());
            LOGGER.debug("created emr {} for user {}", formDTO.getName(), userInfo.getName());
            return Response
                    .ok(provisioningService.post(EMR_CREATE, dto, String.class))
                    .build();
        } else {
            LOGGER.debug("used existing emr {} for user {}", formDTO.getName(), userInfo.getName());
            return Response.status(Response.Status.FOUND).build();
        }
    }

    @POST
    @Path("/status")
    public Response create(EMRStatusDTO dto) {
        LOGGER.debug("update status for emr {} for user {}", dto.getResourceName(), dto.getUser());
        userListDAO.updateComputationalStatus(dto);
        return Response.ok().build();
    }

    @POST
    @Path("/terminate")
    public String terminate(@Auth UserInfo userInfo, EMRTerminateFormDTO formDTO) {
        LOGGER.debug("terminating emr {} for user {}", formDTO.getClusterName(), userInfo.getName());
        userListDAO.updateComputationalStatus(new EMRStatusDTO()
                .withUser(userInfo.getName())
                .withName(formDTO.getNotebookName())
                .withResourceName(formDTO.getClusterName())
                .withStatus(UserInstanceStatus.TERMINATING.getStatus()));
        EMRTerminateDTO dto = new EMRTerminateDTO()
                .withServiceBaseName(settingsDAO.getServiceBaseName())
                .withEdgeUserName(userInfo.getName())
                .withClusterName(formDTO.getClusterName())
                .withRegion(settingsDAO.getAwsRegion());
        return provisioningService.post(EMR_TERMINATE, dto, String.class);
    }

}
