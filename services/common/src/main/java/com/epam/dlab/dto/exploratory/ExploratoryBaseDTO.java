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

import com.epam.dlab.dto.ResourceBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects.ToStringHelper;

public class ExploratoryBaseDTO<T extends ExploratoryBaseDTO<?>> extends ResourceBaseDTO<T> {
    @JsonProperty("notebook_image")
    private String notebookImage;

    @JsonProperty("edge_user_name")
    private String notebookUserName;

    public String getNotebookImage() { return notebookImage; }

    public void setNotebookImage(String notebookImage) { this.notebookImage = notebookImage; }

    @SuppressWarnings("unchecked")
    public T withNotebookImage(String notebookImage) {
        setNotebookImage(notebookImage);
        return (T) this;
    }

    public String getNotebookUserName() {
        return notebookUserName;
    }

    public void setNotebookUserName(String notebookUserName) {
        this.notebookUserName = notebookUserName;
    }

    @SuppressWarnings("unchecked")
    public T withNotebookUserName(String notebookUserName) {
        setNotebookUserName(notebookUserName);
        return (T) this;
    }

    @Override
    public ToStringHelper toStringHelper(Object self) {
    	return super.toStringHelper(self)
    	        .add("notebookImage", notebookImage)
    	        .add("notebookUserName", notebookUserName);
    }
    
    @Override
    public String toString() {
    	return toStringHelper(this).toString();
    }
}
