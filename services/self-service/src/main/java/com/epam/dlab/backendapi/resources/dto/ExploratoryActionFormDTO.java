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

package com.epam.dlab.backendapi.resources.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

/** Stores info about the action of exploratory notebook. 
 * */
public class ExploratoryActionFormDTO {
    @NotBlank
    @JsonProperty("notebook_instance_name")
    private String notebookInstanceName;

    /** Returns the name of notebook instance. */
    public String getNotebookInstanceName() {
        return notebookInstanceName;
    }
}
