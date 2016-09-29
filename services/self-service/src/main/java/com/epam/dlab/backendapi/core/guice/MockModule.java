package com.epam.dlab.backendapi.core.guice;

import com.epam.dlab.backendapi.api.ImageMetadata;
import com.epam.dlab.backendapi.api.User;
import com.epam.dlab.backendapi.client.mongo.MongoService;
import com.epam.dlab.backendapi.client.rest.ProvisioningAPI;
import com.epam.dlab.backendapi.client.rest.RESTService;
import com.epam.dlab.backendapi.client.rest.SecurityAPI;
import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import com.mongodb.client.MongoCollection;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;
import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.SECURITY_SERVICE;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Alexey Suprun
 */
public class MockModule extends AbstractModule implements SecurityAPI, ProvisioningAPI {
    @Override
    protected void configure() {
        bind(MongoService.class).toInstance(createMongoService());
        bind(RESTService.class).annotatedWith(Names.named(SECURITY_SERVICE))
                .toInstance(createSecurityService());
        bind(RESTService.class).annotatedWith(Names.named(PROVISIONING_SERVICE))
                .toInstance(createProvisioningService());
    }

    private MongoService createMongoService() {
        MongoService result = mock(MongoService.class);
        when(result.getCollection(anyString())).thenReturn(mock(MongoCollection.class));
        return result;

    }

    private RESTService createSecurityService() {
        RESTService result = mock(RESTService.class);
        when(result.post(eq(LOGIN), any(), any())).thenReturn(new User("Test", "Testov", Collections.singletonList("test")));
        return result;
    }

    private RESTService createProvisioningService() {
        RESTService result = mock(RESTService.class);
        when(result.get(eq(DOCKER), any()))
                .thenReturn(new HashSet<>(Arrays.asList(new ImageMetadata("test image", "template", "decription", "request_id"))));
        return result;
    }
}
