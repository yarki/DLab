package Repository;

import AutomationTest.Dlab.HelperMethods;
import DataModel.CreateNotebookDto;
import DataModel.DeployEMRDto;

public class Entities {
    
    public static DeployEMRDto prepareDeployEMREntity(String serviceBaseName) {
        
        DeployEMRDto deployEMR = new DeployEMRDto();
        deployEMR.setEmr_instance_count("1");
        deployEMR.setEmr_master_instance_type("m4.large");
        deployEMR.setEmr_slave_instance_type("m4.large");
        deployEMR.setEmr_version("emr-4.3.0");
        deployEMR.setName(HelperMethods.generateRandomValue());
        deployEMR.setNotebook_name(serviceBaseName);
        return deployEMR;
    }
    
    public static CreateNotebookDto prepareCreateNotebook() {
        
        CreateNotebookDto createNoteBookRequest = new CreateNotebookDto();
        createNoteBookRequest.setName(HelperMethods.generateRandomValue());
        createNoteBookRequest.setShape("t2.medium");
        createNoteBookRequest.setVersion("jupyter-1.6");
        return createNoteBookRequest;
    }

}
