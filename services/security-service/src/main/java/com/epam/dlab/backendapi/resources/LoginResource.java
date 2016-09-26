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
import java.util.*;

/**
 * Created by Alexey Suprun
 */
@Path("/login")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);
    private static final Map<String, LDAPUser> USERS = new HashMap<>();

    static {
        USERS.put(getKey("ivanov", "123"), new LDAPUser("Ivanov", "Ivan", Collections.singletonList("data science")));
        USERS.put(getKey("petrov", "123"), new LDAPUser("Petrov", "Petrv", Collections.singletonList("big data engineer")));
        USERS.put(getKey("sidorov", "123"), new LDAPUser("Sidorov", "Sidr", Arrays.asList("data science", "big data engineer")));
    }

    @POST
    public Optional<LDAPUser> login(User user) {
        LOGGER.info("Try login user {}", user);
        return Optional.ofNullable(USERS.get(getKey(user.getLogin(), user.getPassword())));
    }

    private static String getKey(String login, String password) {
        return login + "#" + password;
    }
}
