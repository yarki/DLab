package com.epam.dlab.dto.imagemetadata;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Viktor Chukhra <Viktor_Chukhra@epam.com>
 */
public class ExploratoryMetadataDTO extends ImageMetadataDTO {
    @JsonProperty(value = "exploratory_environment_versions")
    private List<ExploratoryEnvironmentVersion> exploratoryEnvironmentVersions;
    @JsonProperty(value = "exploratory_environment_shapes")
    private List<ComputationalResourceShapeDto> exploratoryEnvironmentShapes;
    @JsonProperty
    protected String image;

    public ExploratoryMetadataDTO(String imageName) {
        this.image = imageName;
        setImageType(ImageType.EXPLORATORY);
    }

    public ExploratoryMetadataDTO() {
        this("");
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExploratoryMetadataDTO that = (ExploratoryMetadataDTO) o;

        if (exploratoryEnvironmentVersions != null
                ? !exploratoryEnvironmentVersions
                .equals(that.exploratoryEnvironmentVersions)
                : that.exploratoryEnvironmentVersions != null) {
            return false;
        }
        if (exploratoryEnvironmentShapes != null ? !exploratoryEnvironmentShapes
                .equals(that.exploratoryEnvironmentShapes)
                : that.exploratoryEnvironmentShapes != null) {
            return false;
        }
        return image != null ? image.equals(that.image) : that.image == null;
    }

    @Override
    public int hashCode() {
        int result = exploratoryEnvironmentVersions != null
                ? exploratoryEnvironmentVersions.hashCode() : 0;
        result = 31 * result + (exploratoryEnvironmentShapes != null
                ? exploratoryEnvironmentShapes.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        return result;
    }
}
