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

package com.epam.dlab.backendapi.core;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.commands.CommandExecutor;
import com.epam.dlab.backendapi.core.commands.DockerCommands;
import com.epam.dlab.backendapi.core.commands.RunDockerCommand;
import com.epam.dlab.backendapi.core.DockerCommands;
import com.epam.dlab.backendapi.core.ICommandExecutor;
import com.epam.dlab.backendapi.core.docker.command.RunDockerCommand;
import com.epam.dlab.backendapi.core.response.folderlistener.FileHandlerCallback;
import com.epam.dlab.backendapi.core.response.folderlistener.FolderListenerExecutor;
import com.epam.dlab.dto.imagemetadata.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.lifecycle.Managed;
import org.eclipse.jetty.util.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Singleton
public class DockerWarmuper implements Managed, DockerCommands, MetadataHolder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerWarmuper.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private FolderListenerExecutor folderListenerExecutor;
    @Inject
    private ICommandExecutor commandExecutor;
    private Map<String, String> uuids = new ConcurrentHashMap<>();
    private Set<ImageMetadataDTO> metadataDTOs = new ConcurrentHashSet<>();


    @Override
    public void start() throws Exception {
        LOGGER.debug("docker warm up start");
        folderListenerExecutor.start(configuration.getWarmupDirectory(),
                configuration.getWarmupPollTimeout(),
                getFileHandlerCallback());
        /*List<String> images = new ArrayList<>(); // commandExecutor.executeSync(GET_IMAGES);
        images.add("docker.epmc-bdcc.projects.epam.com/dlab-aws-jupyter");
        images.add("docker.epmc-bdcc.projects.epam.com/dlab-aws-rstudio");
        for (String image : images) {
            LOGGER.debug("image: {}", image);
            String uuid = UUID.randomUUID().toString();
            uuids.put(uuid, image);
            String command = new RunDockerCommand()
                    .withVolumeForRootKeys(configuration.getKeyDirectory())
                    .withVolumeForResponse(configuration.getWarmupDirectory())
                    .withRequestId(uuid)
                    .withActionDescribe(image)
                    .toCMD();
            commandExecutor.executeAsync(command);
        }*/

        metadataDTOs.add(prepareJupiterImage());
        metadataDTOs.add(prepareEmrImage());
    }

    private ComputationalMetadataDTO prepareEmrImage() {
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

        ComputationalMetadataDTO imageMetadataDTO = new ComputationalMetadataDTO(
                "test computational image", "template", "description",
                "request_id", ImageType.COMPUTATIONAL.getType(),
                Arrays.asList(templateDTO, templateDTO1));

        List<ComputationalResourceShapeDto> crsList = new ArrayList<>();
        crsList.add(new ComputationalResourceShapeDto(
                "cg1.4xlarge", "22.5 GB", 16));
        crsList.add(new ComputationalResourceShapeDto(
                "t2.medium", "4.0 GB", 2));
        crsList.add(new ComputationalResourceShapeDto(
                "t2.large", "8.0 GB", 2));
        crsList.add(new ComputationalResourceShapeDto(
                "t2.large", "8.0 GB", 2));

        imageMetadataDTO.setComputationResourceShapes(crsList);
        return imageMetadataDTO;
    }

    private ExploratoryMetadataDTO prepareJupiterImage() {
        ExploratoryMetadataDTO imageMetadataDTO = new ExploratoryMetadataDTO();
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
        imageMetadataDTO.setExploratoryEnvironmentShapes(crsList);
        imageMetadataDTO.setExploratoryEnvironmentVersions(eevList);

        return imageMetadataDTO;
    }

    FileHandlerCallback getFileHandlerCallback() {
        return new FileHandlerCallback() {
            @Override
            public boolean checkUUID(String uuid) {
                return uuids.containsKey(uuid);
            }

            @Override
            public boolean handle(String fileName, byte[] content) throws Exception {
                String uuid = DockerCommands.extractUUID(fileName);
                if (uuids.containsKey(uuid)) {
                    LOGGER.debug("handle file {}", fileName);
                    addMetadata(content, uuid);
                    return true;
                }
                return false;
            }

            @Override
            public void handleError() {
                LOGGER.warn("docker warmuper returned no result");
            }
        };
    }

    private void addMetadata(byte[] content, String uuid) throws IOException {
        final JsonNode jsonNode = MAPPER.readTree(content);
        ImageMetadataDTO metadata;
        if (jsonNode.has("exploratory_environment_shapes")) {
            metadata = MAPPER.readValue(content, ExploratoryMetadataDTO.class);
            metadata.setImageType(ImageType.EXPLORATORY);
        } else {
            metadata = MAPPER
                    .readValue(content, ComputationalMetadataDTO.class);
            metadata.setImageType(ImageType.COMPUTATIONAL);
        }
        metadata.setImage(uuids.get(uuid));
        metadataDTOs.add(metadata);
    }

    @Override
    public void stop() throws Exception {
    }

    public Map<String, String> getUuids() {
        return Collections.unmodifiableMap(uuids);
    }

    public Set<ImageMetadataDTO> getMetadatas(ImageType type) {
        return metadataDTOs.stream().filter(m -> m.getImageType().equals(type))
                .collect(Collectors.toSet());
    }
}
