/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
