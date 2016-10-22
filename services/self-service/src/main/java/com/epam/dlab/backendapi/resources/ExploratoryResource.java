/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.api.form.ExploratoryCreateFormDTO;
import com.epam.dlab.backendapi.api.form.ExploratoryTerminateFormDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceStatus;
import com.epam.dlab.backendapi.client.rest.ExploratoryAPI;
import com.epam.dlab.backendapi.dao.KeyDAO;
import com.epam.dlab.backendapi.dao.SettingsDAO;
import com.epam.dlab.backendapi.dao.UserListDAO;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.exploratory.ExploratoryCreateDTO;
import com.epam.dlab.dto.exploratory.ExploratoryTerminateDTO;
import com.epam.dlab.dto.exploratory.ExploratoryCallbackDTO;
import com.epam.dlab.dto.keyload.UserAWSCredentialDTO;
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
import java.io.IOException;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

@Path("/exploratory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExploratoryResource implements ExploratoryAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExploratoryResource.class);
    private static final String DEFAULT_SECURITY_GROUP = "sg-e338c89a";

    @Inject
    private SettingsDAO dao;
    @Inject
    private KeyDAO keyDao;
    @Inject
    private UserListDAO userListDAO;
    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @POST
    @Path("/create")
    public Response create(@Auth UserInfo userInfo, ExploratoryCreateFormDTO formDTO) throws IOException {
        LOGGER.debug("creating exploratory environment {} for user {}", formDTO.getName(), userInfo.getName());
        boolean isAdded = userListDAO.insertExploratory(new UserInstanceDTO()
                .withUser(userInfo.getName())
                .withEnvironmentName(formDTO.getName())
                .withStatus(UserInstanceStatus.CREATING.getStatus())
                .withShape(formDTO.getShape()));
        if (isAdded) {
            UserAWSCredentialDTO credentialDTO = keyDao.findCredential(userInfo.getName());
            ExploratoryCreateDTO dto = new ExploratoryCreateDTO()
                    .withServiceBaseName(dao.getServiceBaseName())
                    .withNotebookUserName(userInfo.getName())
                    .withNotebookSubnet(credentialDTO.getNotebookSubnet())
                    .withRegion(dao.getAwsRegion())
                    .withSecurityGroupIds(DEFAULT_SECURITY_GROUP)
                    .withImage(formDTO.getImage());
            return Response
                    .ok(provisioningService.post(EXPLORATORY_CREATE, dto, String.class))
                    .build();
        } else {
            return Response.status(Response.Status.FOUND).build();
        }
    }

    @POST
    @Path("/create/status")
    public Response create(ExploratoryCallbackDTO dto) throws IOException {
        LOGGER.debug("callback for creating exploratory environment {} for user {}", dto.getName(), dto.getUser());
        userListDAO.updateExploratoryStatus(dto);
        return Response.ok().build();
    }

    @POST
    @Path("/terminate")
    public String terminate(@Auth UserInfo userInfo, ExploratoryTerminateFormDTO formDTO) {
        LOGGER.debug("terminating exploratory environment {} for user {}", formDTO.getNotebookInstanceName(), userInfo.getName());
        ExploratoryTerminateDTO dto = new ExploratoryTerminateDTO()
                .withServiceBaseName(userInfo.getName())
                .withNotebookUserName(userInfo.getName())
                .withNotebookInstanceName(formDTO.getNotebookInstanceName())
                .withRegion(dao.getAwsRegion());
        return provisioningService.post(EXPLORATORY_TERMINATE, dto, String.class);
    }

    @POST
    @Path("/stop")
    public String stop(@Auth UserInfo userInfo, ExploratoryTerminateFormDTO formDTO) {
        LOGGER.debug("stopping exploratory environment {} for user {}", formDTO.getNotebookInstanceName(), userInfo.getName());
        ExploratoryTerminateDTO dto = new ExploratoryTerminateDTO()
                .withServiceBaseName(userInfo.getName())
                .withNotebookUserName(userInfo.getName())
                .withNotebookInstanceName(formDTO.getNotebookInstanceName())
                .withRegion(dao.getAwsRegion());
        return provisioningService.post(EXPLORATORY_STOP, dto, String.class);
    }
}
