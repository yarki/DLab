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

package com.epam.dlab.dto.keyload;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UploadFileResultDTO {
	@JsonProperty("request_id")
    private String requestId;
    @JsonProperty
    private String user;
    @JsonProperty
    private boolean success;
    @JsonProperty
    private UserAWSCredentialDTO credential;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public UploadFileResultDTO withRequestId(String requestId) {
        setRequestId(requestId);
        return this;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public UploadFileResultDTO withUser(String user) {
        setUser(user);
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public UserAWSCredentialDTO getCredential() {
        return credential;
    }

    public void setSuccessAndCredential(UserAWSCredentialDTO credential) {
        this.success = true;
        this.credential = credential;
    }
}
