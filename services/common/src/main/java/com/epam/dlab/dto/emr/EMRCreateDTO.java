/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.dto.emr;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EMRCreateDTO extends EMRBaseDTO<EMRCreateDTO> {
    @JsonProperty("emr_instance_count")
    private String instanceCount;
    @JsonProperty("emr_instance_type")
    private String instanceType;
    @JsonProperty("emr_version")
    private String version;
    @JsonProperty("notebook_name")
    private String notebookName;
    @JsonProperty("edge_subnet_cidr")
    private String edgeSubnet;

    public String getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(String instanceCount) {
        this.instanceCount = instanceCount;
    }

    public EMRCreateDTO withInstanceCount(String instanceCount) {
        setInstanceCount(instanceCount);
        return this;
    }

    public String getInstanceType() {
        return instanceType;
    }

    public void setInstanceType(String instanceType) {
        this.instanceType = instanceType;
    }

    public EMRCreateDTO withInstanceType(String instanceType) {
        setInstanceType(instanceType);
        return this;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public EMRCreateDTO withVersion(String version) {
        setVersion(version);
        return this;
    }

    public String getNotebookName() {
        return notebookName;
    }

    public void setNotebookName(String notebookName) {
        this.notebookName = notebookName;
    }

    public EMRCreateDTO withNotebookName(String notebookName) {
        setNotebookName(notebookName);
        return this;
    }

    public String getEdgeSubnet() {
        return edgeSubnet;
    }

    public void setEdgeSubnet(String edgeSubnet) {
        this.edgeSubnet = edgeSubnet;
    }

    public EMRCreateDTO withEdgeSubnet(String edgeSubnet) {
        setEdgeSubnet(edgeSubnet);
        return this;
    }

}
