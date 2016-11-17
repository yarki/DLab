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

import com.epam.dlab.backendapi.core.docker.command.RunDockerCommand;
import com.epam.dlab.dto.computational.ComputationalCreateDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

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


        ComputationalCreateDTO computationalCreateDTO = new ComputationalCreateDTO().withServiceBaseName("someName");

     /*   CreateEMRClusterParameters createEMRClusterParameters = CreateEMRClusterParameters.newCreateEMRClusterParameters()
                .confServiceBaseName("someName")
                .emrTimeout("10")
                .build();*/

        CommandBuilder commandBuilder = new CommandBuilder();
        String command = commandBuilder.buildCommand(dockerBaseCommand, computationalCreateDTO);
        System.out.println(command);

        assertEquals("echo -e '{\"conf_service_base_name\":\"someName\"}' | docker run -i -a STDIN -v rkv:/root/keys -v rv:/response -e \"request_id=rID\" ubuntu --action describe",
                command);
    }
}
