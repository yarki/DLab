/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.resources;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.response.warmup.MetadataHolder;
import com.epam.dlab.dto.ImageMetadataDTO;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Set;

@Path("/docker")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DockerResource implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerResource.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private MetadataHolder metadataHolder;
    @Inject
    private CommandExecuter commandExecuter;

    @GET
    public Set<ImageMetadataDTO> getDockerImages() throws IOException, InterruptedException {
        LOGGER.debug("docker statuses asked");
        return metadataHolder.getMetadatas();
    }

    @Path("/run")
    @POST
    public String run(String image) throws IOException, InterruptedException {
        LOGGER.debug("run docker image {}", image);
        String uuid = DockerCommands.generateUUID();
        commandExecuter.executeAsync(String.format(RUN_IMAGE, configuration.getKeyDirectory(), configuration.getImagesDirectory(), uuid, image));
        return uuid;
    }
}
