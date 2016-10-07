package com.epam.dlab.dto.keyload;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Alexey Suprun
 */
public class UploadFileResultDTO {
    @JsonProperty
    private String user;
    @JsonProperty
    private boolean success;
    @JsonProperty
    private UserAWSCredentialDTO credential;

    public UploadFileResultDTO() {
    }

    public UploadFileResultDTO(String user) {
        this.user = user;
    }

    public String getUser() {
        return user;
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
