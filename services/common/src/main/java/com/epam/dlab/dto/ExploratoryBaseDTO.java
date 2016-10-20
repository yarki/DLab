package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maksym_Pendyshchuk on 10/20/2016.
 */
public class ExploratoryBaseDTO<T extends ExploratoryBaseDTO<?>> extends ResourceBaseDTO<T> {
    @JsonProperty("notebook_user_name")
    private String notebookUserName;

    public String getNotebookUserName() {
        return notebookUserName;
    }

    public void setNotebookUserName(String notebookUserName) {
        this.notebookUserName = notebookUserName;
    }

    @SuppressWarnings("unchecked")
    public T withNotebookUserName(String notebookUserName) {
        setNotebookUserName(notebookUserName);
        return (T) this;
    }
}
