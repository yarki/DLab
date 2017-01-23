package com.epam.dlab.backendapi.core.response.handlers;

import static com.epam.dlab.backendapi.core.commands.DockerAction.CONFIGURE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.Directories;
import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.ICommandExecutor;
import com.epam.dlab.backendapi.core.commands.CommandBuilder;
import com.epam.dlab.backendapi.core.commands.DockerAction;
import com.epam.dlab.backendapi.core.commands.DockerCommands;
import com.epam.dlab.backendapi.core.commands.RunDockerCommand;
import com.epam.dlab.backendapi.core.response.folderlistener.FolderListenerExecutor;
import com.epam.dlab.dto.computational.ComputationalBaseDTO;
import com.epam.dlab.dto.computational.ComputationalCreateDTO;
import com.epam.dlab.exceptions.DlabException;
import com.epam.dlab.rest.client.RESTService;
import com.google.inject.Inject;

public class ComputationalConfigure  implements DockerCommands {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComputationalConfigure.class);

    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;
    @Inject
    private FolderListenerExecutor folderListenerExecutor;
    @Inject
    private ICommandExecutor commandExecuter;
    @Inject
    private CommandBuilder commandBuilder;
    @Inject
    private RESTService selfService;

    public void run(String username, String accessToken, ComputationalCreateDTO dto) throws DlabException {
        LOGGER.debug("Configure computational resources cluster");
// ???
        String uuid = DockerCommands.generateUUID();
        folderListenerExecutor.start(configuration.getImagesDirectory(),
                configuration.getResourceStatusPollTimeout(),
                getFileHandlerCallback(CONFIGURE, uuid, dto, accessToken));
        try {
            //long timeout = configuration.getResourceStatusPollTimeout().toSeconds();
            commandExecuter.executeAsync(
            		username,
                    uuid,
                    commandBuilder.buildCommand(
                            new RunDockerCommand()
                                    .withInteractive()
                                    //.withName(nameContainer(dto.getEdgeUserName(), TERMINATE, dto.getComputationalName()))
                                    .withVolumeForRootKeys(configuration.getKeyDirectory())
                                    .withVolumeForResponse(configuration.getImagesDirectory())
                                    .withVolumeForLog(configuration.getDockerLogDirectory(), getResourceType())
                                    //.withResource(getResourceType())
                                    .withRequestId(uuid)
                                    //.withEmrTimeout(Long.toString(timeout))
                                    .withCredsKeyName(configuration.getAdminKey())
                                    .withActionConfigure(configuration.getEmrImage()),
                            dto
                    )
            );
        } catch (Throwable t) {
            throw new DlabException("Could not configure computational resource cluster", t);
        }
    }

    private FileHandlerCallback getFileHandlerCallback(DockerAction action, String originalUuid, ComputationalBaseDTO<?> dto, String accessToken) {
        return new ComputationalCallbackHandler(selfService, action, originalUuid, dto, accessToken);
    }

    @Override
    public String getResourceType() {
        return Directories.EMR_LOG_DIRECTORY;
    }
}
