package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.api.ExploratoryCreateFormDTO;
import com.epam.dlab.backendapi.client.rest.ExploratoryAPI;
import com.epam.dlab.backendapi.dao.KeyDAO;
import com.epam.dlab.backendapi.dao.SettingsDAO;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.ExploratoryCreateDTO;
import com.epam.dlab.dto.ResourceDTO;
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

import java.io.IOException;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;

/**
 * Created by Maksym_Pendyshchuk on 10/17/2016.
 */
@Path("/exploratory")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExploratoryResource implements ExploratoryAPI {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExploratoryResource.class);

    @Inject
    private SettingsDAO dao;
    @Inject
    private KeyDAO keyDao;
    @Inject
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @POST
    @Path("/create")
    public String terminate(@Auth UserInfo userInfo, ExploratoryCreateFormDTO formDTO) throws IOException {
        LOGGER.debug("creating exploratory environment {}", userInfo.getName());
        UserAWSCredentialDTO credentialDTO = keyDao.findCredential(userInfo.getName());
        ExploratoryCreateDTO dto = new ExploratoryCreateDTO();
        dto.setServiceBaseName(dao.getServiceBaseName());
        dto.setNotebookUserName(credentialDTO.getUserOwnBicketName());
        dto.setNotebookSubnet(credentialDTO.getNotebookSubnet());
        dto.setRegion(dao.getAwsRegion());
        dto.setSecurityGroupIds("");
        dto.setImage(formDTO.getImage());
        return provisioningService.post(EXPLORATORY_CREATE, dto, String.class);
    }

    @POST
    @Path("/terminate")
    public String terminate(@Auth UserInfo userInfo, String notebook) {
        LOGGER.debug("terminating exploratory environment {}", userInfo.getName());
        ResourceDTO exploratoryEnv = new ResourceDTO()
                .withName(notebook)
                .withUser(userInfo.getName())
                .withRegion(dao.getAwsRegion());
        return provisioningService.post(EXPLORATORY_TERMINATE, exploratoryEnv, String.class);
    }

    @POST
    @Path("/stop")
    public String stop(@Auth UserInfo userInfo, String notebook) {
        LOGGER.debug("stopping exploratory environment {}", userInfo.getName());
        ResourceDTO exploratoryEnv = new ResourceDTO()
                .withName(notebook)
                .withUser(userInfo.getName())
                .withRegion(dao.getAwsRegion());
        return provisioningService.post(EXPLORATORY_STOP, exploratoryEnv, String.class);
    }
}
