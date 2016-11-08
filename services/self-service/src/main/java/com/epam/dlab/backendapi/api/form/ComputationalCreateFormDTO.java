/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.api.form;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

public class ComputationalCreateFormDTO {
    @NotBlank
    @JsonProperty
    private String name;

    @NotBlank
    @JsonProperty("emr_instance_count")
    private String instanceCount;

    @NotBlank
    @JsonProperty("emr_master_instance_type")
    private String masterInstanceType;

    @NotBlank
    @JsonProperty("emr_slave_instance_type")
    private String slaveInstanceType;

    @NotBlank
    @JsonProperty("emr_version")
    private String version;

    @NotBlank
    @JsonProperty("notebook_name")
    private String notebookName;

    public String getName() {
        return name;
    }

    public String getInstanceCount() {
        return instanceCount;
    }

    public String getMasterInstanceType() {
        return masterInstanceType;
    }

    public String getSlaveInstanceType() {
        return slaveInstanceType;
    }

    public String getVersion() {
        return version;
    }

    public String getNotebookName() {
        return notebookName;
    }
}
