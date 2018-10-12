package com.powsybl.sensitivity;

import org.junit.Test;

import java.net.URISyntaxException;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class JsonSensitivityFactorsProviderFactoryTest {

    @Test
    public void createFromStream() {
        SensitivityFactorsProviderFactory factory = new JsonSensitivityFactorsProviderFactory();
        SensitivityFactorsProvider provider = factory.create(JsonSensitivityFactorsProviderFactoryTest.class.getResourceAsStream("/sensitivityFactorsExample.json"));

        assertTrue(provider instanceof JsonSensitivityFactorsProvider);
    }

    @Test
    public void createFromPath() throws URISyntaxException {
        SensitivityFactorsProviderFactory factory = new JsonSensitivityFactorsProviderFactory();
        SensitivityFactorsProvider provider = factory.create(Paths.get(JsonSensitivityFactorsProviderFactoryTest.class.getResource("/sensitivityFactorsExample.json").toURI()));

        assertTrue(provider instanceof JsonSensitivityFactorsProvider);
    }
}
