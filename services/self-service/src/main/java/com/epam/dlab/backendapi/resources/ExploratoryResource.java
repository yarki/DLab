package com.epam.dlab.backendapi.resources;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.client.rest.ExploratoryAPI;
import com.epam.dlab.client.restclient.RESTService;
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
    @Named(PROVISIONING_SERVICE)
    private RESTService provisioningService;

    @POST
    @Path("/terminate")
    public String terminate(@Auth UserInfo userInfo, String notebook) {
        LOGGER.debug("terminating exploratory {}", userInfo.getName());
        return provisioningService.post(EXPLORATORY_TERMINATE, userInfo.getName(), notebook, String.class);
    }

    @POST
    @Path("/stop")
    public String stop(@Auth UserInfo userInfo, String notebook) {
        LOGGER.debug("stop exploratory {}", userInfo.getName());
        return provisioningService.post(EXPLORATORY_STOP, userInfo.getName(), notebook, String.class);
    }
}
