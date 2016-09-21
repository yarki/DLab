package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.api.LDAPUser;
import com.epam.dlab.backendapi.api.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by Alexey_Suprun on 21-Sep-16.
 */
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    @POST
    public LDAPUser login(User user) {
        LOGGER.info("Try login user {}", user);
        return new LDAPUser("Ivanov", "Ivan", "data science");
    }
}
