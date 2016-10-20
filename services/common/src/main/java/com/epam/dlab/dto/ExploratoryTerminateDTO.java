package com.epam.dlab.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Maksym_Pendyshchuk on 10/20/2016.
 */
public class ExploratoryTerminateDTO extends BaseDTO<ExploratoryTerminateDTO> {

    @JsonProperty("notebook_user_name")
    private String notebookUserName;
    @JsonProperty("notebook_instance_name")
    private String notebookInstanceName;

    public String getNotebookUserName() {
        return notebookUserName;
    }

    public void setNotebookUserName(String notebookUserName) {
        this.notebookUserName = notebookUserName;
    }

    public ExploratoryTerminateDTO withNotebookUserName(String notebookUserName) {
        setNotebookUserName(notebookUserName);
        return this;
    }

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
