package com.epam.dlab.automation.repository;

import com.epam.dlab.automation.helper.TestNamingHelper;
import com.epam.dlab.automation.model.CreateNotebookDto;
import com.epam.dlab.automation.model.DeployEMRDto;

// TODO Unused code?
public class Entities {
    
    public static DeployEMRDto prepareDeployEMREntity(String serviceBaseName) {
        
        DeployEMRDto deployEMR = new DeployEMRDto();
        deployEMR.setEmr_instance_count("2");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version("emr-5.2.0");
        deployEMR.setName(TestNamingHelper.generateRandomValue());
        deployEMR.setNotebook_name(serviceBaseName);
        deployEMR.setTemplate_name("DLab AWS EMR");
        deployEMR.setImage("docker.dlab-emr");
        return deployEMR;
    }
    
    public static CreateNotebookDto prepareCreateNotebook() {
        
        CreateNotebookDto createNoteBookRequest = new CreateNotebookDto();
        createNoteBookRequest.setImage("docker.dlab-jupyter");
        createNoteBookRequest.setTemplateName("Jupyter notebook 5.0.0");
        createNoteBookRequest.setName(TestNamingHelper.generateRandomValue());
        createNoteBookRequest.setShape("t2.medium");
        createNoteBookRequest.setVersion("jupyter_notebook-5.0.0");
        return createNoteBookRequest;
    }

}
