package com.epam.dlab.backendapi.core.docker.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import com.epam.dlab.dto.imagemetadata.ImageMetadataDTO;
import static com.epam.dlab.backendapi.core.DockerCommands.MAPPER;

/**
 * @author Viktor Chukhra <Viktor_Chukhra@epam.com>
 */
public class ReadMetadataTest {
    private static final String METADATA_DESCRIPTION_JSON = "/metadata/description.json";
    private static final String METADATA_DESCRIPTION_JSON_1 = "/metadata/description_1.json";


    @Test
    public void readMetadataTest() throws IOException {
        ImageMetadataDTO imageMetadataDTO = MAPPER.readValue(
                readTestResource(METADATA_DESCRIPTION_JSON),
                ImageMetadataDTO.class);

        Assert.assertNotNull(imageMetadataDTO);
    }

    @Test
    public void readMetadataTest_1() throws IOException {
        ImageMetadataDTO imageMetadataDTO = MAPPER.readValue(
                readTestResource(METADATA_DESCRIPTION_JSON_1),
                ImageMetadataDTO.class);
        Assert.assertNotNull(imageMetadataDTO);
    }

    private String readTestResource(String testResourceName) throws IOException {
        String file = getClass().getResource(testResourceName).getFile();
        return new String(Files.readAllBytes(Paths.get(file)));
    }
}
