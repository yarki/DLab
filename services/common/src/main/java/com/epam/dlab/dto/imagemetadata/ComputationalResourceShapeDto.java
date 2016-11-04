package com.epam.dlab.dto.imagemetadata;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Viktor Chukhra <Viktor_Chukhra@epam.com>
 */
public class ComputationalResourceShapeDto {
    @JsonProperty(value = "Type")
    private String type;
    @JsonProperty(value = "Ram")
    private String ram;
    @JsonProperty(value = "Cpu")
    private int cpu;

    public ComputationalResourceShapeDto(){
    }

    public ComputationalResourceShapeDto(String type, String ram, int cpu) {
        this.type = type;
        this.ram = ram;
        this.cpu = cpu;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRam() {
        return ram;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public int getCpu() {
        return cpu;
    }

    public void setCpu(int cpu) {
        this.cpu = cpu;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComputationalResourceShapeDto that = (ComputationalResourceShapeDto) o;

        if (cpu != that.cpu) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        return ram != null ? ram.equals(that.ram) : that.ram == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (ram != null ? ram.hashCode() : 0);
        result = 31 * result + cpu;
        return result;
    }
}
