/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.dto.exploratory;

import com.epam.dlab.dto.StatusBaseDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotBlank;

import javax.annotation.Nullable;

public class ExploratoryStatusDTO extends StatusBaseDTO<ExploratoryStatusDTO> {
    @NotBlank
    @JsonProperty("exploratory_id")
    private String exploratoryId;

    @Nullable
    @JsonProperty("exploratory_url")
    private String exploratoryUrl;

    public String getExploratoryId() {
        return exploratoryId;
    }

    public void setExploratoryId(String exploratoryId) {
        this.exploratoryId = exploratoryId;
    }

    public ExploratoryStatusDTO withExploratoryId(String exploratoryId) {
        setExploratoryId(exploratoryId);
        return this;
    }

    public String getExploratoryUrl() {
        return exploratoryUrl;
    }

    public void setExploratoryUrl(String exploratoryUrl) {
        this.exploratoryUrl = exploratoryUrl;
    }

    public ExploratoryStatusDTO withExploratoryUrl(String exploratoryUrl) {
        setExploratoryUrl(exploratoryUrl);
        return this;
    }
}
