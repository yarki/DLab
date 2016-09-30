package com.epam.dlab.backendapi.core.response.warmup;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.api.ImageMetadata;
import com.epam.dlab.backendapi.core.CommandExecuter;
import com.epam.dlab.backendapi.core.response.FolderListener;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import io.dropwizard.util.Duration;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static junit.framework.TestCase.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Alexey Suprun
 */
public class DockerWarmuperTest {
    @Inject
    private DockerWarmuper warmuper;
    private ImageMetadata metadata = new ImageMetadata("executeResult");

    @Before
    public void setup() {
        createInjector().injectMembers(this);
    }

    @Test
    public void warmupSuccess() throws Exception {
        warmuper.start();
        warmuper.getMetadataHandler().handle(getFileName(), "{}".getBytes());
        assertEquals(metadata, warmuper.getMetadatas().toArray(new ImageMetadata[1])[0]);
    }

    private String getFileName() {
        return warmuper.getUuids().keySet().toArray(new String[1])[0] + ".json";
    }

    private Injector createInjector() {
        return Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(FolderListener.class).toInstance(mock(FolderListener.class));
                bind(ProvisioningServiceApplicationConfiguration.class).toInstance(createConfiguration());
                bind(CommandExecuter.class).toInstance(createCommandExecuter());
            }
        });
    }

    private ProvisioningServiceApplicationConfiguration createConfiguration() {
        ProvisioningServiceApplicationConfiguration result = mock(ProvisioningServiceApplicationConfiguration.class);
        when(result.getWarmupDirectory()).thenReturn("/tmp");
        when(result.getWarmupPollTimeout()).thenReturn(Duration.seconds(3));
        return result;
    }

    private CommandExecuter createCommandExecuter() {
        CommandExecuter result = mock(CommandExecuter.class);
        try {
            when(result.executeSync(anyString())).thenReturn(Collections.singletonList("executeResult"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
