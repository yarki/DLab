package com.epam.dlab.backendapi.core.guice;

import com.epam.dlab.auth.core.AuthenticationServiceConfig;
import com.epam.dlab.auth.core.UserInfo;
import com.epam.dlab.backendapi.api.ImageMetadata;
import com.epam.dlab.backendapi.client.mongo.MongoService;
import com.epam.dlab.backendapi.client.rest.DockerAPI;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.epam.dlab.backendapi.client.rest.SecurityAPI;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mongodb.client.MongoCollection;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.HashSet;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Alexey Suprun
 */
public class MockModule extends AbstractModule implements SecurityAPI, DockerAPI {
    @Override
    protected void configure() {
        bind(MongoService.class).toInstance(createMongoService());
        bind(RESTService.class).annotatedWith(Names.named(PROVISIONING_SERVICE))
                .toInstance(createProvisioningService());
        bind(AuthenticationServiceConfig.class).toInstance(createAuthenticationServiceConfig());
        bind(Client.class).toInstance(createClient());
    }

    private AuthenticationServiceConfig createAuthenticationServiceConfig() {
        AuthenticationServiceConfig c = new AuthenticationServiceConfig() {
            @Override
            public String getLogoutUrl() {
                return "http://localhost:8080/logout?";
            }
        };
        c.setHost("localhost");
        c.setPort(8080);
        return c;
    }

    private Client createClient() {
        Client c = mock(Client.class);
        final WebTarget wt = mock(WebTarget.class);
        when(c.target(anyString())).thenReturn(wt);
        when(wt.queryParam(anyString(), anyString())).thenReturn(wt);
        Invocation.Builder b = mock(Invocation.Builder.class);
        when(wt.request(MediaType.TEXT_PLAIN)).thenReturn(b);
        when(wt.request(MediaType.APPLICATION_JSON_TYPE)).thenReturn(b);
        Invocation in = mock(Invocation.class);
        when(wt.request()).thenReturn(b);
        when(b.get(UserInfo.class)).thenReturn(new UserInfo("test", "test"));
        when(b.get(String.class)).thenReturn("token123");
        when(b.buildGet()).thenReturn(in);
        return c;
    }

    private MongoService createMongoService() {
        MongoService result = mock(MongoService.class);
        when(result.getCollection(anyString())).thenReturn(mock(MongoCollection.class));
        return result;

    }

    private RESTService createProvisioningService() {
        RESTService result = mock(RESTService.class);
        when(result.get(eq(DOCKER), any()))
                .thenReturn(new HashSet<>(Arrays.asList(new ImageMetadata("test image", "template", "decription", "request_id"))));
        return result;
    }
}
