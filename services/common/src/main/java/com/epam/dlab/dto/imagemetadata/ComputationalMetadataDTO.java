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

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ComputationalMetadataDTO extends ImageMetadataDTO{
    @JsonProperty("template_name")
    private String templateName;
    @JsonProperty
    private String description;
    @JsonProperty("environment_type")
    private String type;
    @JsonProperty
    private List<TemplateDTO> templates;
    @JsonProperty("request_id")
    private String requestId;
    @JsonProperty(value = "computation_resources_shapes")
    private HashMap<String, List<ComputationalResourceShapeDto>> computationResourceShapes;
    @JsonProperty
    protected String image;

    public ComputationalMetadataDTO(String imageName) {
        this.image = imageName;
        setImageType(ImageType.COMPUTATIONAL);
    }

    public ComputationalMetadataDTO() {
        this("");
    }


    public ComputationalMetadataDTO(String image, String templateName, String description, String requestId,
                                    String type,
                                    List<TemplateDTO> templates) {
        this.image = image;
        this.templateName = templateName;
        this.description = description;
        this.requestId = requestId;
        this.type = type;
        this.templates = templates;
        setImageType(ImageType.COMPUTATIONAL);
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

    public HashMap<String, List<ComputationalResourceShapeDto>> getComputationResourceShapes() {
        return computationResourceShapes;
    }

    public void setComputationResourceShapes(HashMap<String, List<ComputationalResourceShapeDto>> computationResourceShapes) {
        this.computationResourceShapes = computationResourceShapes;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComputationalMetadataDTO that = (ComputationalMetadataDTO) o;

        return Objects.equals(templateName, that.templateName) &&
                Objects.equals(description, that.description) &&
                Objects.equals(type, that.type) &&
                Objects.equals(templates, that.templates) &&
                Objects.equals(requestId, that.requestId) &&
                Objects.equals(computationResourceShapes, that.computationResourceShapes) &&
                Objects.equals(image, that.image);
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateName,
                description,
                type,
                templates,
                requestId,
                computationResourceShapes,
                image);
    }
}
