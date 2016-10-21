package com.epam.dlab.backendapi.core.docker.command;

import java.util.LinkedList;
import java.util.List;

public class RunDockerCommand implements DockerCommand {
    private String command = "docker run";
    private List<String> options = new LinkedList<>();

    private static final String ROOT_KEYS_PATH = "/root/keys";
    private static final String RESPONSE_PATH = "/response";


    public RunDockerCommand withVolumeForRootKeys(String hostSrcPath) {
        return withVolume(hostSrcPath, ROOT_KEYS_PATH);
    }

    public RunDockerCommand withVolumeForResponse(String hostSrcPath) {
        return withVolume(hostSrcPath, RESPONSE_PATH);
    }

    public RunDockerCommand withVolume(String hostSrcPath, String bindPath) {
        options.add(String.format("-v %s:%s", hostSrcPath, bindPath));
        return this;
    }

    public RunDockerCommand withRequestId(String requestId) {
        options.add(String.format("-e \"request_id=%s\"", requestId));
        return this;
    }

    public RunDockerCommand withAtach(String value) {
        options.add(String.format("-a %s", value));
        return this;
    }

    public RunDockerCommand withInteractive(){
        options.add("-i");
        return this;
    }

    public RunDockerCommand withPseudoTTY() {
        options.add("-t");
        return this;
    }

    public RunDockerCommand withActionDescribe(String toDescribe) {
        options.add(String.format("%s --action describe", toDescribe));
        return this;
    }

    public RunDockerCommand withActionCreate(String toCreate) {
        options.add(String.format("%s --action create", toCreate));
        return this;
    }

    public RunDockerCommand withActionRun(String toRun) {
        options.add(String.format("%s --action run", toRun));
        return this;
    }

    public RunDockerCommand withActionTerminate(String toTerminate) {
        options.add(String.format("%s --action terminate", toTerminate));
        return this;
    }

    public RunDockerCommand withActionStop(String toTerminate) {
        options.add(String.format("%s --action stop", toTerminate));
        return this;
    }

    public RunDockerCommand withCredsKeyName(String keyName) {
        options.add(String.format("-e \"creds_key_name=%s\"", keyName));
        return this;
    }

    public RunDockerCommand withConfServiceBaseName(String confServiceBaseName) {
        options.add(String.format("-e \"conf_service_base_name=%s\"", confServiceBaseName));
        return this;
    }

    public RunDockerCommand withEmrInstanceCount(String emrInstanceCount) {
        options.add(String.format("-e \"emr_instance_count=%s\"", emrInstanceCount));
        return this;
    }

    public RunDockerCommand withEmrInstanceType(String emrInstanceType) {
        options.add(String.format("-e \"emr_instance_type=%s\"", emrInstanceType));
        return this;
    }

    public RunDockerCommand withEmrVersion(String emrVersion) {
        options.add(String.format("-e \"emr_version=%s\"", emrVersion));
        return this;
    }

    public RunDockerCommand withEc2Role(String ec2Role) {
        options.add(String.format("-e \"ec2_role=%s\"", ec2Role));
        return this;
    }

    public RunDockerCommand withServiceRole(String serviceRole) {
        options.add(String.format("-e \"service_role=%s\"", serviceRole));
        return this;
    }

    public RunDockerCommand withNotebookName(String notebookName) {
        options.add(String.format("-e \"notebook_name=%s\"", notebookName));
        return this;
    }

    public RunDockerCommand withEdgeSubnetCidr(String edgeSubnetCidr) {
        options.add(String.format("-e \"edge_subnet_cidr=%s\"", edgeSubnetCidr));
        return this;
    }

    public RunDockerCommand withCredsRegion(String credsRegion) {
        options.add(String.format("-e \"creds_region=%s\"", credsRegion));
        return this;
    }

    public RunDockerCommand withEdgeUserName(String edgeUserName) {
        options.add(String.format("-e \"edge_user_name=%s\"", edgeUserName));
        return this;
    }

    public RunDockerCommand withEmrClusterName(String emrClusterName) {
        options.add(String.format("-e \"emr_cluster_name=%s\"", emrClusterName));
        return this;
    }

    public RunDockerCommand withNotebookUserName(String notebookUserName) {
        options.add(String.format("-e \"notebook_user_name=%s\"", notebookUserName));
        return this;
    }

    public RunDockerCommand withNotebookSubnetCidr(String notebookSubnetCidr) {
        options.add(String.format("-e \"notebook_subnet_cidr=%s\"", notebookSubnetCidr));
        return this;
    }

    public RunDockerCommand withCredsSecurityGroupsIds(String credsSecurityGroupsIds) {
        options.add(String.format("-e \"creds_security_groups_ids=%s\"", credsSecurityGroupsIds));
        return this;
    }

    public RunDockerCommand withNotebookInstanceName(String notebookInstanceName) {
        options.add(String.format("-e \"notebook_instance_name=%s\"", notebookInstanceName));
        return this;
    }

    public RunDockerCommand withUserKeyName(String userKeyName) {
        options.add(String.format("-e \"user_keyname=%s\"", userKeyName));
        return this;
    }

    public RunDockerCommand withDryRun() {
        options.add("-e \"dry_run=true\"");
        return this;
    }

    @Override
    public String toCMD() {
        StringBuilder sb = new StringBuilder(command);
        for (String option : options) {
            sb.append(" ").append(option);
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toCMD();
    }


}