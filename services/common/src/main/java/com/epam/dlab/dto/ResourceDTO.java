package com.epam.dlab.dto;

/**
 * Created by Maksym_Pendyshchuk on 10/18/2016.
 */
public class ResourceDTO {

    private String name;
    private String user;
    private String region;

    public ResourceDTO() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceDTO withName(String name) {
        this.setName(name);
        return this;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public ResourceDTO withUser(String user) {
        this.setUser(user);
        return this;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public ResourceDTO withRegion(String region) {
        this.setRegion(region);
        return this;
    }
}
