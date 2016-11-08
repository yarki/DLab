package com.epam.dlab.dto.imagemetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Viktor Chukhra <Viktor_Chukhra@epam.com>
 */
public class ApplicationDto {
    @JsonProperty(value = "Version")
    private String version;
    @JsonProperty(value = "Name")
    private String name;

    public ApplicationDto() {
    }


    public ApplicationDto(String version, String name) {
        this.version = version;
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ApplicationDto that = (ApplicationDto) o;

        if (version != null ? !version.equals(that.version) : that.version != null) {
            return false;
        }
        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = version != null ? version.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
