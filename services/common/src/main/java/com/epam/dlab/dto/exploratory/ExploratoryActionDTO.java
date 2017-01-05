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

public class ExploratoryActionDTO<T extends ExploratoryActionDTO<?>> extends ExploratoryBaseDTO<T> {
    @JsonProperty("notebook_instance_name")
    private String notebookInstanceName;

    public String getNotebookInstanceName() {
        return notebookInstanceName;
    }

    public void setNotebookInstanceName(String notebookInstanceName) {
        this.notebookInstanceName = notebookInstanceName;
    }

    @SuppressWarnings("unchecked")
    public T withNotebookInstanceName(String notebookInstanceName) {
        setNotebookInstanceName(notebookInstanceName);
        return (T) this;
    }
}
