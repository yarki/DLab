package com.epam.dlab.backendapi.core.guice;

import com.epam.dlab.auth.SecurityAPI;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.epam.dlab.backendapi.client.rest.DockerAPI;
import com.epam.dlab.client.mongo.MongoService;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.imagemetadata.ImageMetadataDTO;
import com.epam.dlab.dto.imagemetadata.ImageType;
import com.epam.dlab.dto.imagemetadata.TemplateDTO;
import com.google.inject.name.Names;
import io.dropwizard.setup.Environment;

import java.util.Arrays;
import java.util.HashSet;

import static com.epam.dlab.auth.SecurityRestAuthenticator.SECURITY_SERVICE;
import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Alexey Suprun
 */
public class MockModule extends BaseModule implements SecurityAPI, DockerAPI {
    public MockModule(SelfServiceApplicationConfiguration configuration, Environment environment) {
        super(configuration, environment);
    }

    @Override
    protected void configure() {
        bind(MongoService.class).toInstance(configuration.getMongoFactory().build(environment));
        bind(RESTService.class).annotatedWith(Names.named(SECURITY_SERVICE))
                .toInstance(createAuthenticationService());
        bind(RESTService.class).annotatedWith(Names.named(PROVISIONING_SERVICE))
                .toInstance(createProvisioningService());
    }

    private RESTService createAuthenticationService() {
        RESTService result = mock(RESTService.class);
        when(result.post(eq(LOGIN), any(), any())).thenReturn("token123");
        when(result.post(eq(GET_USER_INFO), eq("token123"), eq(UserInfo.class))).thenReturn(new UserInfo("test", "token123"));
        return result;
    }

    private RESTService createProvisioningService() {
        RESTService result = mock(RESTService.class);
        when(result.get(eq(DOCKER), any()))
                .thenReturn(new ImageMetadataDTO[] {
                        new ImageMetadataDTO("test computational image", "template", "decription", "request_id", ImageType.COMPUTATIONAL.getType(),
                                Arrays.asList(new TemplateDTO("emr-6.3.0"), new TemplateDTO("emr-6.8.0"))),
                        new ImageMetadataDTO("test exploratory image", "template", "decription", "request_id", ImageType.EXPLORATORY.getType(),
                                Arrays.asList(new TemplateDTO("jupyter-2"), new TemplateDTO("jupyter-3")))
                });
        return result;
    }
}
