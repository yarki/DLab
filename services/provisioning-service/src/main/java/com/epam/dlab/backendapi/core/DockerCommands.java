package com.epam.dlab.backendapi.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Created by Alexey Suprun
 */
public interface DockerCommands {
    String JSON_EXTENTION = ".json";
    String GET_IMAGES = "docker images | awk '{print $1\":\"$2}' | sort | uniq | grep \"dlab\" | grep -v \"none\" | grep -v \"edge\"";
    String DOCKER_BASE = "docker run -v %s:/root/keys -v %s:/response -e \"request_id=%s\" ";
    String GET_IMAGE_METADATA = DOCKER_BASE + "%s --action describe";
    String RUN_IMAGE = DOCKER_BASE + "-e \"dry_run=true\" %s --action run";

    String CREATE_EDGE_METADATA = DOCKER_BASE +
            "-e \"conf_service_base_name=%s\" " +
            "-e \"creds_key_name=%s\" " +
            "-e \"edge_user_name=%s\" " +
            "%s --action create";

    String CREATE_EMR_CLUSTER = DOCKER_BASE +
            "-e \"conf_service_base_name=%s\" " +
            "-e \"emr_instance_count=%s\" " +
            "-e \"emr_instance_type=%s\" " +
            "-e \"emr_version=%s\" " +
            "-e \"ec2_role=%s\" " +
            "-e \"service_role=%s\" " +
            "-e \"notebook_name=%s\" " +
            "-e \"edge_user_name=%s\" " +
            "-e \"edge_subnet_cidr=%s\" " +
            "-e \"creds_region=%s\" " +
            "-e \"creds_key_name=%s\" " +
            "%s --action create";
    String TERMINATE_EMR_CLUSTER = DOCKER_BASE +
            "-e \"conf_service_base_name=%s\" " +
            "-e \"edge_user_name=%s\" " +
            "-e \"emr_cluster_name=%s\" " +
            "-e \"creds_region=%s\" " +
            "-e \"creds_key_name=%s\" " +
            "%s --action terminate";

    String CREATE_EXPLORATORY_ENVIRONMENT = DOCKER_BASE +
            "-e \"conf_service_base_name=%s\" " +
            "-e \"notebook_user_name=%s\" " +
            "-e \"notebook_subnet_cidr=%s\" " +
            "-e \"creds_region=%s\" " +
            "-e \"creds_security_groups_ids=%s\" " +
            "-e \"creds_key_name=%s\" " +
            "%s --action create";

    String TERMINATE_EXPLORATORY_ENVIRONMENT = DOCKER_BASE +
            "-e \"conf_service_base_name=%s\" " +
            "-e \"notebook_user_name=%s\" " +
            "-e \"creds_region=%s\" " +
            "-e \"notebook_instance_name=%s\" " +
            "-e \"creds_key_name=%s\" " +
            "%s --action terminate";

    ObjectMapper MAPPER = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

    static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    static String extractUUID(String fileName) {
        return fileName.replace(JSON_EXTENTION, "");
    }
}
