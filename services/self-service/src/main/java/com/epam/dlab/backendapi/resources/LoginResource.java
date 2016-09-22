package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.api.LDAPUser;
import com.epam.dlab.backendapi.api.User;
import com.epam.dlab.backendapi.core.RESTService;
import com.epam.dlab.backendapi.dao.MongoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;


/**
 * Copyright 2016 EPAM Systems, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@Path("/login")
@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
@Produces(MediaType.APPLICATION_JSON)
public class LoginResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginResource.class);

    private MongoService mongoService;
    private RESTService securityService;

    public LoginResource(MongoService mongoService, RESTService securityService) {
        this.mongoService = mongoService;
        this.securityService = securityService;
    }

    @POST
    public LDAPUser login(@FormParam("login") String login, @FormParam("password") String password) {
        LOGGER.info("Try login user = {}", login);
        User user = new User(login, password);
        mongoService.getCollection("loginTrys").insertOne(user.getDocument());
        return securityService.post("login", user, LDAPUser.class);
    }
}
