package com.epam.dlab.backendapi.core;

/**
 * Created by Alexey Suprun
 */
public interface DockerCommands {
    String GET_IMAGES = "docker images | awk '{print $1\":\"$2}' | sort | uniq | grep \"dlab\" | grep -v \"none\"";
    String GET_IMAGE_METADATA = "docker run -v %s:/response -e \"request_id=%s\" %s --action describe";
}
