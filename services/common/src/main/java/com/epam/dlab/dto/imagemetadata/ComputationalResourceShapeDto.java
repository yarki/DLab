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

import java.util.Objects;

public class ComputationalResourceShapeDto {
    @JsonProperty(value = "Type")
    private String type;
    @JsonProperty(value = "Size")
    private String size;
    @JsonProperty(value = "Ram")
    private String ram;
    @JsonProperty(value = "Cpu")
    private int cpu;

    public ComputationalResourceShapeDto(){
    }

    public ComputationalResourceShapeDto(String type, String size, String ram, int cpu) {
        this.type = type;
        this.size = size;
        this.ram = ram;
        this.cpu = cpu;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSize() { return size; }

    public void setSize(String size) { this.size = size; }

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

        return Objects.equals(type, that.type) &&
                Objects.equals(size, that.size) &&
                Objects.equals(ram, that.ram) &&
                Objects.equals(cpu, that.cpu);
    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (ram != null ? ram.hashCode() : 0);
        result = 31 * result + cpu;
        return result;
    }
}
