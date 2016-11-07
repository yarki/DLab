package com.epam.dlab.backendapi.core.docker.command;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import com.epam.dlab.dto.imagemetadata.ComputationalMetadataDTO;
import com.epam.dlab.dto.imagemetadata.ExploratoryMetadataDTO;
import com.epam.dlab.dto.imagemetadata.ImageMetadataDTO;
import static com.epam.dlab.backendapi.core.DockerCommands.MAPPER;

/**
 * @author Viktor Chukhra <Viktor_Chukhra@epam.com>
 */
public class ReadMetadataTest {
    private static final String EMR_METADATA_DESCRIPTION_JSON = "/metadata/description.json";
    private static final String JUPITER_METADATA_DESCRIPTION_JSON = "/metadata/description_1.json";


    @Test
    public void readEmrMetadataTest() throws IOException {
        ImageMetadataDTO imageMetadataDTO = MAPPER.readValue(
                readTestResource(EMR_METADATA_DESCRIPTION_JSON),
                ComputationalMetadataDTO.class);

        Assert.assertNotNull(imageMetadataDTO);
    }

    @Test
    public void readJupiterMetadataTest() throws IOException {
        ImageMetadataDTO imageMetadataDTO = MAPPER.readValue(
                readTestResource(JUPITER_METADATA_DESCRIPTION_JSON),
                ExploratoryMetadataDTO.class);
        Assert.assertNotNull(imageMetadataDTO);
    }

    private String readTestResource(String testResourceName) throws IOException {
        String file = getClass().getResource(testResourceName).getFile();
        return new String(Files.readAllBytes(Paths.get(file)));
    }
}
