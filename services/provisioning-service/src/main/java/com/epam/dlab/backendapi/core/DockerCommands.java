package com.epam.dlab.backendapi.core;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

/**
 * Created by Alexey Suprun
 */
public interface DockerCommands {
    String JSON_EXTENTION = ".json";
    String GET_IMAGES = "docker images | awk '{print $1\":\"$2}' | sort | uniq | grep \"dlab\" | grep -v \"none\" | grep -v \"edge\"";
    String DOCKER_BASE = "docker run -v %s:/root/keys -v %s:/response -e \"request_id=%s\" -t ";
    String GET_IMAGE_METADATA = DOCKER_BASE + "%s --action describe";
    String CREATE_EDGE_METADATA = DOCKER_BASE + "-e \"creds_key_name=%s\" " +
            "-e \"conf_service_base_name=%s\" " +
            "-e \"edge_user_name=%s\" " +
            "%s --action create";
    String RUN_IMAGE = DOCKER_BASE + "-e \"dry_run=true\" %s --action run";
    ObjectMapper MAPPER = new ObjectMapper();

    static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    static String extractUUID(String fileName) {
        return fileName.replace(JSON_EXTENTION, "");
    }
}
