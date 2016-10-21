package com.epam.dlab.backendapi.core;

import com.epam.dlab.backendapi.core.docker.command.RunDockerCommand;
import com.epam.dlab.dto.EMRCreateDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Vladyslav_Valt on 10/20/2016.
 */
public class CommandBuilderTest {

    String rootKeysVolume = "rkv";
    String responseVolume = "rv";
    String requestID = "rID";
    String toDescribe = "ubuntu";

    @Test
    public void testBuildCommand() throws JsonProcessingException {
        RunDockerCommand dockerBaseCommand = new RunDockerCommand()
                .withInteractive()
                .withAtach("STDIN")
                .withVolumeForRootKeys(rootKeysVolume)
                .withVolumeForResponse(responseVolume)
                .withRequestId(requestID)
                .withActionDescribe(toDescribe);


        EMRCreateDTO emrCreateDTO = new EMRCreateDTO().withServiceBaseName("someName");

     /*   CreateEMRClusterParameters createEMRClusterParameters = CreateEMRClusterParameters.newCreateEMRClusterParameters()
                .confServiceBaseName("someName")
                .emrTimeout("10")
                .build();*/

        CommandBuilder commandBuilder = new CommandBuilder();
        String command = commandBuilder.buildCommand(dockerBaseCommand, emrCreateDTO);
        System.out.println(command);

        assertEquals("echo -e '{\"conf_service_base_name\":\"someName\"}' | docker run -i -a STDIN -v rkv:/root/keys -v rv:/response -e \"request_id=rID\" ubuntu --action describe",
                command);
    }
}
