package com.epam.dlab.automation.model;

public class DeployEMRDto {
    
    private String name;
    private String emr_instance_count;
    private String emr_master_instance_type;
    private String emr_slave_instance_type;
    private String emr_version;
    private String notebook_name;
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmr_instance_count() {
        return emr_instance_count;
    }
    
    public void setEmr_instance_count(String emr_instance_count) {
        this.emr_instance_count = emr_instance_count;
    }
    
    public String getEmr_master_instance_type() {
        return emr_master_instance_type;
    }
    
    public void setEmr_master_instance_type(String emr_master_instance_type) {
        this.emr_master_instance_type = emr_master_instance_type;
    }
    
    public String getEmr_slave_instance_type() {
        return emr_slave_instance_type;
    }
    
    public void setEmr_slave_instance_type(String emr_slave_instance_type) {
        this.emr_slave_instance_type = emr_slave_instance_type;
    }
    
    public String getEmr_version() {
        return emr_version;
    }
    
    public void setEmr_version(String emr_version) {
        this.emr_version = emr_version;
    }
    
    public String getNotebook_name() {
        return notebook_name;
    }
    
    public void setNotebook_name(String notebook_name) {
        this.notebook_name = notebook_name;
    }

}
