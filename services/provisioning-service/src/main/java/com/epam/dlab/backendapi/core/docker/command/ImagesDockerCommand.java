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

package com.epam.dlab.backendapi.core.docker.command;

import java.util.LinkedList;
import java.util.List;

public class ImagesDockerCommand implements DockerCommand {
    private String command = "docker images";

    private List<UnixCommand> unixCommands;

    public ImagesDockerCommand pipe(UnixCommand unixCommand) {
        if (unixCommands == null) {
            unixCommands = new LinkedList<>();
        }
        unixCommands.add(unixCommand);
        return this;
    }

    @Override
    public String toCMD() {
        StringBuilder sb = new StringBuilder(command);
        if (unixCommands != null) {
            for (UnixCommand unixCommand : unixCommands) {
                sb.append(" | ").append(unixCommand.getCommand());
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toCMD();
    }
}
