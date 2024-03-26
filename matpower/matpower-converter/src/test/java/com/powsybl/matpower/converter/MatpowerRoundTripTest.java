/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.converter;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.FileDataSource;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class MatpowerRoundTripTest {

    private FileSystem fileSystem;

    private Path dir;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        dir = fileSystem.getPath("/work");
    }

    @AfterEach
    void tearDown() throws IOException {
        fileSystem.close();
    }

    private static double calculateRatio(Network network, String id) {
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id);
        double ratio = twt.getRatedU2() / twt.getRatedU1();
        if (twt.getRatioTapChanger() != null) {
            ratio *= twt.getRatioTapChanger().getCurrentStep().getRho();
        }
        if (twt.getPhaseTapChanger() != null) {
            ratio *= twt.getPhaseTapChanger().getCurrentStep().getRho();
        }
        return ratio;
    }

    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        Properties parameters = new Properties();
        parameters.setProperty("matpower.import.ignore-base-voltage", "false");
        new MatpowerExporter().export(network, parameters, new FileDataSource(dir, "test"));
        Network network2 = new MatpowerImporter().importData(new FileDataSource(dir, "test"), NetworkFactory.findDefault(), parameters);
        assertEquals(calculateRatio(network, "NGEN_NHV1"), calculateRatio(network2, "TWT-1-2"), 1e-16);
        assertEquals(calculateRatio(network, "NHV2_NLOAD"), calculateRatio(network2, "TWT-3-4"), 1e-16);
    }
}
