package com.epam.dlab.backendapi.core.docker.command;

import com.epam.dlab.backendapi.core.docker.command.ImagesDockerCommand;
import com.epam.dlab.backendapi.core.docker.command.UnixCommand;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Vladyslav_Valt on 10/19/2016.
 */
public class ImagesDockerCommandTest {

    String GET_IMAGES = "docker images | awk '{print $1\":\"$2}' | sort | uniq | grep \"dlab\" | grep -v \"none\" | grep -v \"edge\"";

    @Test
    public void testBuildGetImagesCommand(){
        String getImagesCommand = new ImagesDockerCommand()
                .pipe(UnixCommand.awk("{print $1\":\"$2}"))
                .pipe(UnixCommand.sort())
                .pipe(UnixCommand.uniq())
                .pipe(UnixCommand.grep("dlab"))
                .pipe(UnixCommand.grep("none", "-v"))
                .pipe(UnixCommand.grep("edge", "-v"))
                .toCMD();
        assertEquals(GET_IMAGES, getImagesCommand);
    }
}
