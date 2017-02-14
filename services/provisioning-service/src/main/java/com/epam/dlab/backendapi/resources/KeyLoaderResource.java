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

package com.epam.dlab.backendapi.resources;

import static com.epam.dlab.rest.contracts.ApiCallbacks.KEY_LOADER;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.backendapi.ProvisioningServiceApplication;
import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.Directories;
import com.epam.dlab.backendapi.core.commands.DockerCommands;
import com.epam.dlab.dto.keyload.UploadFileDTO;
import com.google.inject.Inject;

import io.dropwizard.auth.Auth;

@Path("/keyloader")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class KeyLoaderResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyLoaderResource.class);

    private static final String KEY_EXTENTION = ".pub";

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;

    @POST
    public String loadKey(@Auth UserInfo ui, UploadFileDTO dto) throws IOException, InterruptedException {
        LOGGER.debug("Load key for user {}", dto.getEdge().getAwsIamUser());
        saveKeyToFile(dto);
        EdgeResource edgeApi = new EdgeResource();
        ProvisioningServiceApplication.getInjector().injectMembers(edgeApi);
        return edgeApi.create(ui.getName(), KEY_LOADER, dto.getEdge());
    }

    private void saveKeyToFile(UploadFileDTO dto) throws IOException {
    	java.nio.file.Path keyFilePath = Paths.get(configuration.getKeyDirectory(), dto.getEdge().getEdgeUserName() + KEY_EXTENTION);
        LOGGER.debug("Saving key to {}", keyFilePath.toString());
        Files.write(keyFilePath, dto.getContent().getBytes());
    }

    @Override
    public String getResourceType() {
        return Directories.EDGE_LOG_DIRECTORY;
    }
}
