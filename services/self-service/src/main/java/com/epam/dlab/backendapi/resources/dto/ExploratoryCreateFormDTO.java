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
import com.google.common.base.MoreObjects;

import org.hibernate.validator.constraints.NotBlank;

/** Stores info about new exploratory.
 */
public class ExploratoryCreateFormDTO {
    @NotBlank
    @JsonProperty
    private String image;

    @NotBlank
    @JsonProperty("template_name")
    private String templateName;

    @NotBlank
    @JsonProperty
    private String name;

    @NotBlank
    @JsonProperty
    private String shape;

    @NotBlank
    @JsonProperty
    private String version;

    /** Returns the image name of notebook. */
    public String getImage() {
    	return image;
    }

    /** Sets the image name of notebook. */
    public void setImage(String image) {
    	this.image = image;
    }

    /** Returns name of template. */
    public String getTemplateName() {
    	return templateName;
    }

    /** Sets name of template. */
    public void setTemplateName(String templateName) {
    	this.templateName = templateName;
    }

    /** Returns name of notebook. */
    public String getName() {
        return name;
    }

    /** Sets name of notebook. */
    public void setName(String name) {
        this.name = name;
    }

    /** Returns name of shape. */
    public String getShape() {
        return shape;
    }

    /** Sets name of shape. */
    public void setShape(String shape) {
        this.shape = shape;
    }

    /** Returns version. */
    public String getVersion() {
        return version;
    }

    /** Sets version. */
    public void setVersion(String version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
    	return MoreObjects.toStringHelper(this)
    			.add("name", name)
    			.add("templateName", templateName)
    			.add("shape", shape)
    	        .add("version", version)
    			.add("image", image)
    			.toString();
    }
}
