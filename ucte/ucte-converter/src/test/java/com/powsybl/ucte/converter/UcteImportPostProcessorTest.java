/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Antoine Limouzin {@literal <antoine.limouzin at rte.com>}
 */
class UcteImportPostProcessorTest {
    private final static Properties PARAMETERS = new Properties();

    static {
        PARAMETERS.put(UcteImporter.POST_PROCESSORS, "test-ita-bz");
    }


    private Network getNetwork(final String fileName) {
        final ReadOnlyDataSource dataSource = new ResourceDataSource(fileName, new ResourceSet("/importPostProcessors/", fileName + ".uct"));
        return new UcteImporter().importData(dataSource, NetworkFactory.findDefault(), PARAMETERS);
    }

    @Test
    void shouldHandleCorrectlyFormattedComment() {
        final Network network = getNetwork("ok");
        Assertions.assertEquals("FICTIVE_A", network.getBusBreakerView().getBus("IA____12").getProperty("italianBiddingZone"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ko_count", "ko_wrong_first", "ko_wrong_last"})
    void shouldFailIfWrongData(final String fileName) {
        assertThrows(UcteException.class, () -> getNetwork(fileName));
    }

}