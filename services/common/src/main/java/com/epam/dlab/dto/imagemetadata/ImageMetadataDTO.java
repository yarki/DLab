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

package com.epam.dlab.dto.imagemetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ImageMetadataDTO {
    @JsonProperty
    private String image;
    @JsonProperty(value = "template_name")
    private String templateName;
    @JsonProperty
    private String description;
    @JsonProperty("environment_type")
    private String type;
    @JsonProperty
    private List<TemplateDTO> templates;
    @JsonProperty(value = "request_id")
    private String requestId;

    public ImageMetadataDTO() {
    }

    public ImageMetadataDTO(String image, String templateName, String description, String requestId, String type, List<TemplateDTO> templates) {
        this.image = image;
        this.templateName = templateName;
        this.description = description;
        this.requestId = requestId;
        this.type = type;
        this.templates = templates;
    }

    public ImageMetadataDTO(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<TemplateDTO> getTemplates() {
        return templates;
    }

    public void setTemplates(List<TemplateDTO> templates) {
        this.templates = templates;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageMetadataDTO metadata = (ImageMetadataDTO) o;

        return image != null ? image.equals(metadata.image) : metadata.image == null;

    }

    @Override
    public int hashCode() {
        return image != null ? image.hashCode() : 0;
    }
}
