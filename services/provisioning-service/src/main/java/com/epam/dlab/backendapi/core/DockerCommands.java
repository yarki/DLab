package com.epam.dlab.backendapi.core;

/**
 * Created by Alexey Suprun
 */
public interface DockerCommands {
    String GET_IMAGES = "docker images | awk '{print $1\":\"$2}' | sort | uniq | grep \"dlab\" | grep -v \"none\"";
    String GET_IMAGE_METADATA = "docker run -v %s:/root/keys -v %s:/response -e \"request_id=%s\" -t %s --action describe";
    String RUN_IMAGE = "docker run -v %s:/root/keys -v %s:/response -e \"dry_run=true\" -e \"request_id=%s\" -t %s --action run";
}
