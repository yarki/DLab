package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maksym_Pendyshchuk on 10/20/2016.
 */
public class BaseDTO<T extends BaseDTO<?>> {
    @JsonProperty("conf_service_base_name ")
    private String serviceBaseName;
    @JsonProperty("creds_region")
    private String region;

    public String getServiceBaseName() {
        return serviceBaseName;
    }

    public void setServiceBaseName(String serviceBaseName) {
        this.serviceBaseName = serviceBaseName;
    }

    @SuppressWarnings("unchecked")
    public T withServiceBaseName(String serviceBaseName) {
        setServiceBaseName(serviceBaseName);
        return (T) this;
    }



    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    @SuppressWarnings("unchecked")
    public T withRegion(String region) {
        setRegion(region);
        return (T) this;
    }
}
