/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.GridModelReferenceResources;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.networkModel;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class DateParsingTest {

    @Test
    void test() {
        Conversion.Config config = new Conversion.Config();
        GridModelReferenceResources gridModel = new GridModelReferenceResources(
                "dateParsing", null, new ResourceSet("/", "dateParsing.xml"));
        Network n = networkModel(gridModel, config);

        ZonedDateTime caseDateExpected = ZonedDateTime.of(2030, 1, 2, 9, 0, 0, 987654321, ZoneOffset.UTC);
        ZonedDateTime createdDateExpected = ZonedDateTime.of(2021, 5, 18, 7, 43, 27, 0, ZoneOffset.of("+01"));
        long forecastMinutes = Duration.between(createdDateExpected, caseDateExpected).toMinutes();

        assertEquals(caseDateExpected, n.getCaseDate());
        assertEquals(forecastMinutes, n.getForecastDistance());
    }
}
