package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maksym_Pendyshchuk on 10/20/2016.
 */
abstract public class EMRBaseDTO<T extends EMRBaseDTO<?>> extends ResourceBaseDTO<T> {
    @JsonProperty("edge_user_name")
    private String edgeUserName;

    public String getEdgeUserName() {
        return edgeUserName;
    }

    public void setEdgeUserName(String edgeUserName) {
        this.edgeUserName = edgeUserName;
    }

    @SuppressWarnings("unchecked")
    public T withEdgeUserName(String edgeUserName) {
        setEdgeUserName(edgeUserName);
        return (T) this;
    }
}
