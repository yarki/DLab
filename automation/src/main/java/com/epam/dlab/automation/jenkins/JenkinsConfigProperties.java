package com.epam.dlab.automation.jenkins;


public class JenkinsConfigProperties {

    static final long JENKINS_REQUEST_TIMEOUT = 5000;

    static String AUTHORIZATION = "Authorization";
    static String AUTHORIZATION_KEY ="Basic %s";//the replacement is decoded to base64 user:password


    static String SUCCESS_STATUS = "true";
    static String JENKINS_JOB_NAME_SEARCH = "/";

    //TODO: introduce configurable value for cloud provider
    static String JENKINS_JOB_START_BODY = "\"name=Access_Key_ID&value=%s" +
            "&name=Secret_Access_Key&value=%s" +
            "&name=Infrastructure_Tag&value=%s" +
            "name=OS_user&value=%s&name=Cloud_provider&value=aws&name=OS_family&value=%s&name=Action&value=create" +
            "&json=%7B%22parameter" +
            "%22%3A+%5B%7B%22name%22%3A+%22Access_Key_ID%22%2C+%22value%22%3A+%22%s" +
            "%22%7D%2C+%7B%22name%22%3A+%22Secret_Access_Key%22%2C+%22value%22%3A+%22%s" +
            "%22%7D%2C+%7B%22name%22%3A+%22Infrastructure_Tag%22%2C+%22value%22%3A+%22%s" +
            "%22%7D%2C+%7B%22name%22%3A+%22OS_user%22%2C+%22value%22%3A+%22%s" +
            "%22%7D%2C+%7B%22name%22%3A+%22Cloud_provider%22%2C+%22value%22%3A+%22aws" +
            "%22%7D%2C+%7B%22name%22%3A+%22OS_family%22%2C+%22value%22%3A+%22%s" +
            "%22%7D%2C+%7B%22name%22%3A+%22Action%22%2C+%22value%22%3A+%22create" +
            "%22%7D%5D%7D&Submit=Build";
}
