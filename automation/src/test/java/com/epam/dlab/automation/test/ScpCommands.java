package com.epam.dlab.automation.test;


public class ScpCommands {

//    public static String copyToSSNCommand = "scp -i %s -o 'StrictHostKeyChecking no' %s ubuntu@%s:~/";
    public static String copyToNotebookCommand = "scp -i %s -o 'StrictHostKeyChecking no' ~/%s %s@%s:/tmp/";
    public static String runPythonCommand = "/usr/bin/python %s --region %s --bucket %s --cluster_name %s --os_user %s";
}
