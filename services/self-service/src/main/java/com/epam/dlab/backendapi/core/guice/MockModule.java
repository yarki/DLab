/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.core.guice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.core.Response;
import com.epam.dlab.auth.SecurityAPI;
import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.SelfServiceApplicationConfiguration;
import com.epam.dlab.backendapi.client.rest.DockerAPI;
import com.epam.dlab.client.mongo.MongoService;
import com.epam.dlab.client.restclient.RESTService;
import com.epam.dlab.dto.imagemetadata.ApplicationDto;
import com.epam.dlab.dto.imagemetadata.ComputationalResourceShapeDto;
import com.epam.dlab.dto.imagemetadata.ExploratoryEnvironmentVersion;
import com.epam.dlab.dto.imagemetadata.ImageMetadataDTO;
import com.epam.dlab.dto.imagemetadata.ImageType;
import com.epam.dlab.dto.imagemetadata.TemplateDTO;
import com.google.inject.name.Names;
import static com.epam.dlab.auth.SecurityRestAuthenticator.SECURITY_SERVICE;
import static com.epam.dlab.backendapi.SelfServiceApplicationConfiguration.PROVISIONING_SERVICE;
import static com.epam.dlab.backendapi.client.rest.ExploratoryAPI.EXPLORATORY_CREATE;
import static com.epam.dlab.backendapi.client.rest.KeyLoaderAPI.KEY_LOADER;
import io.dropwizard.setup.Environment;
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
        when(result.get(eq(DOCKER), any()))
                .thenReturn(new ImageMetadataDTO[]{
                        prepareEmrImage(),
                        prepareJupiterImage()
                });
        when(result.post(eq(EXPLORATORY_CREATE), any(), eq(String.class))).thenReturn(UUID.randomUUID().toString());
        return result;
    }

    private ImageMetadataDTO prepareEmrImage() {
        TemplateDTO templateDTO = new TemplateDTO("emr-6.3.0");
        ArrayList<ApplicationDto> applicationDtos = new ArrayList<>();
        applicationDtos.add(new ApplicationDto("2.7.1", "Hadoop"));
        applicationDtos.add(new ApplicationDto("1.6.0", "Spark"));
        templateDTO.setApplications(applicationDtos);

        TemplateDTO templateDTO1 = new TemplateDTO("emr-5.0.3");
        applicationDtos = new ArrayList<>();
        applicationDtos.add(new ApplicationDto("2.7.3", "Hadoop"));
        applicationDtos.add(new ApplicationDto("2.0.1", "Spark"));
        applicationDtos.add(new ApplicationDto("2.1.0", "Hive"));
        templateDTO1.setApplications(applicationDtos);

        ImageMetadataDTO imageMetadataDTO = new ImageMetadataDTO("test computational image", "template", "description",
                                                                 "request_id", ImageType.COMPUTATIONAL.getType(),
                                                                 Arrays.asList(templateDTO,
                                                                               templateDTO1));
        return imageMetadataDTO;
    }

    private ImageMetadataDTO prepareJupiterImage() {
        ImageMetadataDTO imageMetadataDTO = new ImageMetadataDTO();

//        new ImageMetadataDTO("test exploratory image", "template", "decription", "request_id",
//                             ImageType.EXPLORATORY.getType(),
//                             Arrays.asList(new TemplateDTO("jupyter-2"), new TemplateDTO("jupyter-3")

        List<ComputationalResourceShapeDto> crsList = new ArrayList<>();
        crsList.add(new ComputationalResourceShapeDto(
                "cg1.4xlarge", "22.5 GB", 16));
        crsList.add(new ComputationalResourceShapeDto(
                "t2.medium", "4.0 GB", 2));
        crsList.add(new ComputationalResourceShapeDto(
                "t2.large", "8.0 GB", 2));
        crsList.add(new ComputationalResourceShapeDto(
                "t2.large", "8.0 GB", 2));

        List<ExploratoryEnvironmentVersion> eevList = new ArrayList<>();
        eevList.add(new ExploratoryEnvironmentVersion("Jupyter 1.5", "Base image with jupyter node creation routines",
                                                      "type", "jupyter-1.6", "AWS"));
        imageMetadataDTO.setType(ImageType.EXPLORATORY.getType());
        imageMetadataDTO.setExploratoryEnvironmentShapes(crsList);
        imageMetadataDTO.setExploratoryEnvironmentVersions(eevList);


        return imageMetadataDTO;
    }
}
