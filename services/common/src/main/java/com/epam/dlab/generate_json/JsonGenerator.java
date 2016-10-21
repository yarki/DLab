package com.epam.dlab.generate_json;

//import com.epam.dlab.generate_json.docker.run.parameters.CreateEMRClusterParameters;
//import com.epam.dlab.generate_json.docker.run.parameters.DockerRunParameters;
import com.epam.dlab.dto.EMRBaseDTO;
import com.epam.dlab.dto.EMRCreateDTO;
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

    /*public static void main(String[] args) throws JsonProcessingException {
        System.out.println(
                new JsonGenerator().generateJson(
                        new EMRCreateDTO().withInstanceCount("2")
                        ,
                        true
                )
        );
    }*/

    /*public String generateJson(DockerRunParameters parameters) throws JsonProcessingException {
        return generateJson(parameters, false);
    }

    public String generateJson(DockerRunParameters parameters, boolean pretty) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
        if(pretty) {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(parameters);
        }
        else {
            return objectMapper.writeValueAsString(parameters);
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        System.out.println(
                new JsonGenerator().generateJson(
                        CreateEMRClusterParameters.newCreateEMRClusterParameters()
                        .confServiceBaseName("someName")
                        .emrTimeout("10")
                        .build(),
                        true
                )
        );
    }*/
}
