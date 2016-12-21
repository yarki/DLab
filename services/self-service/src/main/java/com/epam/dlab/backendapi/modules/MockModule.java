/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.backendapi.modules;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.epam.dlab.dto.imagemetadata.*;
import com.epam.dlab.mongo.MongoService;
import com.epam.dlab.rest.client.RESTService;
import com.epam.dlab.rest.contracts.DockerAPI;
import com.epam.dlab.rest.contracts.SecurityAPI;
import com.epam.dlab.utils.ResourceUtils;
import com.google.inject.name.Names;
import io.dropwizard.setup.Environment;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.epam.dlab.auth.SecurityRestAuthenticator.SECURITY_SERVICE;
import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;
import static com.epam.dlab.rest.contracts.ExploratoryAPI.EXPLORATORY_CREATE;
import static com.epam.dlab.rest.contracts.KeyLoaderAPI.KEY_LOADER;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockModule extends BaseModule implements SecurityAPI, DockerAPI {
    public MockModule(SelfServiceApplicationConfiguration configuration, Environment environment) {
        super(configuration, environment);
    }

    @Override
    protected void configure() {
        super.configure();
        bind(MongoService.class).toInstance(configuration.getMongoFactory().build(environment));
        bind(RESTService.class).annotatedWith(Names.named(SECURITY_SERVICE))
                .toInstance(createAuthenticationService());
        bind(RESTService.class).annotatedWith(Names.named(PROVISIONING_SERVICE))
                .toInstance(createProvisioningService());
    }

    private RESTService createAuthenticationService() {
        RESTService result = mock(RESTService.class);
        when(result.post(eq(LOGIN), any(), any())).then(invocationOnMock -> Response.ok("token123").build());
        when(result.post(eq(GET_USER_INFO), eq("token123"), eq(UserInfo.class)))
                .thenReturn(new UserInfo("test", "token123"));
        return result;
    }

    private RESTService createProvisioningService() {
        RESTService result = mock(RESTService.class);
        when(result.post(eq(KEY_LOADER), any(), eq(Response.class)))
                .then(invocationOnMock -> Response.accepted().build());
        when(result.get(eq(DOCKER_EXPLORATORY), any()))
                .thenReturn(new ExploratoryMetadataDTO[]{
                        prepareJupiterImage()
                });
        when(result.get(eq(DOCKER_COMPUTATIONAL), any()))
                .thenReturn(new ComputationalMetadataDTO[]{prepareEmrImage()});
        when(result.post(eq(EXPLORATORY_CREATE), any(), eq(String.class))).thenReturn(UUID.randomUUID().toString());
        return result;
    }

    private ComputationalMetadataDTO prepareEmrImage() {
        try {
            return ResourceUtils.readResourceAsClass(getClass(),
                    "/metadata/computational_mock.json",
                    ComputationalMetadataDTO.class);
        }
        catch (Exception e) {
            return null;
        }
    }

    private ExploratoryMetadataDTO prepareJupiterImage() {
        try {
            return ResourceUtils.readResourceAsClass(getClass(),
                    "/metadata/exploratory_mock.json",
                    ExploratoryMetadataDTO.class);
        }
        catch (Exception e) {
            return null;
        }
    }
}
