package com.epam.dlab.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticationConfiguration {
    @JsonProperty
    private String host = "localhost";
    @JsonProperty
    private int port = 0;
    @JsonProperty
    private String type = "http";
    @JsonProperty
    private String username = "";
    @JsonProperty
    private String password = "";
    @JsonProperty
    private String loginFormUrl = "/?";

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (type == null || "".equals(type)) {
            sb.append("//");
        } else {
            sb.append(type).append("://");
        }
        if (username != null && !"".equals(username)) {
            sb.append(username).append(":").append(password).append("@");
        }
        sb.append(host);
        if (port > 0) {
            sb.append(":").append(port);
        }
        return sb.toString();
    }

    public String getAccessTokenUrl() {
        return toString() + "/validate?";
    }

    public String getAuthenticateAndRedirectUrl() {
        return toString() + "/login?";
    }

    public String getUserInfoUrl() {
        return toString() + "/user_info?";
    }

    public String getLogoutUrl() {
        return toString() + "/logout?";
    }

    public String getLoginUrl() {
        if ("/?".equals(loginFormUrl)) {
            return toString() + "/?";
        } else {
            return loginFormUrl;
        }
    }

    public void setLoginUrl(String loginFormUrl) {
        this.loginFormUrl = loginFormUrl;
    }

}
