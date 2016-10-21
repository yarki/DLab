/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

package com.epam.dlab.backendapi.core;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.UUID;

public interface DockerCommands {
    String JSON_EXTENTION = ".json";
    String GET_IMAGES = "docker images | awk '{print $1\":\"$2}' | sort | uniq | grep \"dlab\" | grep -v \"none\" | grep -v \"edge\"";
    String DOCKER_BASE = "docker run -v %s:/root/keys -v %s:/response -e \"request_id=%s\" -t ";
    String GET_IMAGE_METADATA = DOCKER_BASE + "%s --action describe";
    String CREATE_EDGE_METADATA = DOCKER_BASE + "-e \"creds_key_name=%s\" " +
            "-e \"conf_service_base_name=%s\" " +
            "-e \"edge_user_name=%s\" " +
            "-e \"user_keyname=%s\" " +
            "%s --action create";
    String RUN_IMAGE = DOCKER_BASE + "-e \"dry_run=true\" %s --action run";
    ObjectMapper MAPPER = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);

    static String generateUUID() {
        return UUID.randomUUID().toString();
    }

    static String extractUUID(String fileName) {
        return fileName.replace(JSON_EXTENTION, "");
    }
}
