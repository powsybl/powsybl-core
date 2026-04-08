/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.model;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class FullModelTest {

    @Test
    void test() throws IOException {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("/fullModel_SV.xml"))) {
            FullModel fullModel = FullModel.parse(reader);
            assertEquals("FullModel(id='urn:uuid:9e46ebef-179e-433a-b423-7ec9ce62cc65', scenarioTime=2020-07-02T00:30Z, created=2020-06-30T15:36Z, description='null', version=1, profiles=[http://entsoe.eu/CIM/StateVariables/4/1], dependentOn=[urn:uuid:223128a4-6c0b-4da2-9715-b5b54eb02cef, urn:uuid:7e5b1fd4-cbaa-4364-b72a-ea57f269137e, urn:uuid:d8074bbe-d1ad-4e1e-8a2e-bad45abaf021], supersedes=[], modelingAuthoritySet='http://www.rte-france.com/OperationalPlanning')", fullModel.toString());
            assertEquals("urn:uuid:9e46ebef-179e-433a-b423-7ec9ce62cc65", fullModel.getId());
            assertEquals(ZonedDateTime.parse("2020-07-02T00:30Z"), fullModel.getScenarioTime());
            assertEquals(ZonedDateTime.parse("2020-06-30T15:36Z"), fullModel.getCreated());
            assertFalse(fullModel.getDescription().isPresent());
            assertEquals(1, fullModel.getVersion());
            assertEquals(Collections.singletonList("http://entsoe.eu/CIM/StateVariables/4/1"), fullModel.getProfiles());
            assertEquals(Arrays.asList("urn:uuid:223128a4-6c0b-4da2-9715-b5b54eb02cef", "urn:uuid:7e5b1fd4-cbaa-4364-b72a-ea57f269137e", "urn:uuid:d8074bbe-d1ad-4e1e-8a2e-bad45abaf021"), fullModel.getDependentOn());
            assertTrue(fullModel.getSupersedes().isEmpty());
            assertEquals("http://www.rte-france.com/OperationalPlanning", fullModel.getModelingAuthoritySet());
        }
    }
}
