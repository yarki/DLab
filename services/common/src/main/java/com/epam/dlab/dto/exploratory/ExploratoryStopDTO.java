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

package com.epam.dlab.dto.exploratory;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExploratoryStopDTO extends ExploratoryActionDTO<ExploratoryStopDTO> {
    @JsonProperty("conf_os_user")
    private String ñonfOsUser;
    @JsonProperty("conf_key_dir")
    private String confKeyDir;

    public String getConfOsUser() {
        return ñonfOsUser;
    }

    public void setConfOsUser(String ñonfOsUser) {
        this.ñonfOsUser = ñonfOsUser;
    }

    public ExploratoryStopDTO withConfOsUser(String ñonfOsUser) {
        setConfOsUser(ñonfOsUser);
        return this;
    }

    public String getConfKeyDir() {
        return confKeyDir;
    }

    public void setConfKeyDir(String confKeyDir) {
        this.confKeyDir = confKeyDir;
    }

    public ExploratoryStopDTO withConfKeyDir(String confKeyDir) {
        setConfKeyDir(confKeyDir);
        return this;
    }
}
