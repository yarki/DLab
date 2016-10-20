package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maksym_Pendyshchuk on 10/20/2016.
 */
public class ExploratoryTerminateDTO extends ExploratoryBaseDTO<ExploratoryTerminateDTO> {
    @JsonProperty("notebook_instance_name")
    private String notebookInstanceName;

    public String getNotebookInstanceName() {
        return notebookInstanceName;
    }

    public void setNotebookInstanceName(String notebookInstanceName) {
        this.notebookInstanceName = notebookInstanceName;
    }

    public ExploratoryTerminateDTO withNotebookInstanceName(String notebookInstanceName) {
        setNotebookInstanceName(notebookInstanceName);
        return this;
    }
}
