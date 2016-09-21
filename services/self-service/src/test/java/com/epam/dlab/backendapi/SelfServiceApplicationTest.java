package com.epam.dlab.backendapi;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.epam.dlab.backendapi.resources.LoginResource;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * Created by Alexey_Suprun on 20-Sep-16.
 */
public class SelfServiceApplicationTest {
    private final SelfServiceApplication application = new SelfServiceApplication();
    private final SelfServiceApplicationConfiguration config = new SelfServiceApplicationConfiguration();
    private final Environment environment = Mockito.mock(Environment.class);
    private final LifecycleEnvironment lifecycle = mock(LifecycleEnvironment.class);
    private final HealthCheckRegistry healthCheck = mock(HealthCheckRegistry.class);
    private final JerseyEnvironment jersey = mock(JerseyEnvironment.class);

    @Before
    public void setup() {
        when(environment.lifecycle()).thenReturn(lifecycle);
        when(environment.healthChecks()).thenReturn(healthCheck);
        when(environment.jersey()).thenReturn(jersey);
    }

    @Test
    public void loginResourceRegistered() throws Exception {
        application.run(config, environment);
        verify(jersey).register(any(LoginResource.class));
    }
}
