package com.epam.dlab.backendapi.resources;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.SELF_SERVICE;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.dao.UserSettingsDAO;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.SelfServiceAPI;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.dropwizard.auth.Auth;


@Path("/user/settings")
@Produces(MediaType.APPLICATION_JSON)
public class UserSettingsResource implements SelfServiceAPI {
	private static final Logger LOGGER = LoggerFactory.getLogger(KeyUploaderResource.class);

    @Inject
    private UserSettingsDAO userSettingsDAO;
    
    @Inject
    @Named(SELF_SERVICE)
    private RESTService selfService;
    
    @GET
    public Response getSettings(@Auth UserInfo userInfo) {
        return Response.ok(
        		selfService.post(USER_SETTINGS, userSettingsDAO.getUISettings(userInfo), String.class)).build();
    }
    
    @POST
    @Path("/save")
    public Response saveSettings(@Auth UserInfo userInfo, @NotBlank String settings) {
        LOGGER.debug("Saves settings for user {}, content is {}", userInfo.getName(), settings);
        userSettingsDAO.setUISettings(userInfo, settings);
        return Response.ok().build();
    }
}
