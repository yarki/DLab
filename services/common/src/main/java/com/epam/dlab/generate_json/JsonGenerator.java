package com.epam.dlab.generate_json;

import com.epam.dlab.dto.ResourceBaseDTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by Vladyslav_Valt on 10/20/2016.
 */
public class JsonGenerator {


    public String generateJson(ResourceBaseDTO resourceBaseDTO) throws JsonProcessingException {
        return generateJson(resourceBaseDTO, false);
    }

    public String generateJson(ResourceBaseDTO resourceBaseDTO, boolean pretty) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        if(pretty) {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resourceBaseDTO);
        }
        else {
            return objectMapper.writeValueAsString(resourceBaseDTO);
        }
    }

}
