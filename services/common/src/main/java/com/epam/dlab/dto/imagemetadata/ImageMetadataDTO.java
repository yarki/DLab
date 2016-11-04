/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.dto.imagemetadata;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    @JsonProperty(value = "computation_resources_shapes")
    private List<ComputationalResourceShapeDto> computationResourceShapes;
    @JsonProperty(value = "exploratory_environment_versions")
    private List<ExploratoryEnvironmentVersion> exploratoryEnvironmentVersions;
    @JsonProperty(value = "exploratory_environment_shapes")
    private List<ComputationalResourceShapeDto> exploratoryEnvironmentShapes;

    public ImageMetadataDTO() {
    }

    public ImageMetadataDTO(String image) {
        this.image = image;
    }

    public ImageMetadataDTO(String image, String templateName, String description, String requestId, String type,
                            List<TemplateDTO> templates) {
        this.image = image;
        this.templateName = templateName;
        this.description = description;
        this.requestId = requestId;
        this.type = type;
        this.templates = templates;
    }


    public List<ExploratoryEnvironmentVersion> getExploratoryEnvironmentVersions() {
        return exploratoryEnvironmentVersions;
    }

    public void setExploratoryEnvironmentVersions(
            List<ExploratoryEnvironmentVersion> exploratoryEnvironmentVersions) {
        this.exploratoryEnvironmentVersions = exploratoryEnvironmentVersions;
    }

    public List<ComputationalResourceShapeDto> getExploratoryEnvironmentShapes() {
        return exploratoryEnvironmentShapes;
    }

    public void setExploratoryEnvironmentShapes(
            List<ComputationalResourceShapeDto> exploratoryEnvironmentShapes) {
        this.exploratoryEnvironmentShapes = exploratoryEnvironmentShapes;
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

    public List<ComputationalResourceShapeDto> getComputationResourceShapes() {
        return computationResourceShapes;
    }

    public void setComputationResourceShapes(List<ComputationalResourceShapeDto> computationResourceShapes) {
        this.computationResourceShapes = computationResourceShapes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageMetadataDTO that = (ImageMetadataDTO) o;

        if (image != null ? !image.equals(that.image) : that.image != null) {
            return false;
        }
        if (templateName != null ? !templateName.equals(that.templateName) : that.templateName != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (templates != null ? !templates.equals(that.templates) : that.templates != null) {
            return false;
        }
        if (requestId != null ? !requestId.equals(that.requestId) : that.requestId != null) {
            return false;
        }
        return computationResourceShapes != null ? computationResourceShapes.equals(that.computationResourceShapes)
                : that.computationResourceShapes == null;

    }

    @Override
    public int hashCode() {
        int result = image != null ? image.hashCode() : 0;
        result = 31 * result + (templateName != null ? templateName.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (templates != null ? templates.hashCode() : 0);
        result = 31 * result + (requestId != null ? requestId.hashCode() : 0);
        result = 31 * result + (computationResourceShapes != null ? computationResourceShapes.hashCode() : 0);
        return result;
    }
}
