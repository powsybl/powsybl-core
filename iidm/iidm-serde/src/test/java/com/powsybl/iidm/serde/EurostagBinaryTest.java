/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.test.ComparisonUtils;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
class EurostagBinaryTest extends AbstractIidmSerDeTest {

    @Disabled("This test needs to be modified to make it easier to update IIDM version")
    @Test
    void roundTripTest() throws IOException {
        String fileName = "eurostag-tutorial1-lf.bin";
        roundTripTest(EurostagTutorialExample1Factory.createWithLFResults(),
                (n, jsonFile) -> NetworkSerDe.write(n, new ExportOptions().setFormat(TreeDataFormat.BIN), jsonFile),
                n -> {
                    try (InputStream is = Files.newInputStream(n)) {
                        return Network.read(fileName, is);
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                },
                ComparisonUtils::assertBytesEquals,
                getVersionedNetworkPath(fileName, CURRENT_IIDM_VERSION));

        //backward compatibility
        roundTripVersionedJsonFromMinToCurrentVersionTest(fileName, IidmVersion.V_1_12);
    }

    @Disabled("This test needs to be modified to make it easier to update IIDM version")
    @Test
    void roundTripTestWithExtension() throws IOException {
        ExportOptions exportOptions = new ExportOptions().setFormat(TreeDataFormat.BIN);
        ImportOptions importOptions = new ImportOptions().setFormat(TreeDataFormat.BIN);
        Network network = EurostagTutorialExample1Factory.createWithLFResults();
        network.getGeneratorStream().findFirst().ifPresent(g -> g.newExtension(ActivePowerControlAdder.class).withDroop(2).withParticipate(true).add());
        network.getLoadStream().forEach(l -> l.newExtension(ConnectablePositionAdder.class).newFeeder().withDirection(ConnectablePosition.Direction.BOTTOM).add().add());
        roundTripTest(network,
                (n, binFile) -> NetworkSerDe.write(n, exportOptions, binFile),
                binFile -> NetworkSerDe.read(binFile, importOptions),
                ComparisonUtils::assertBytesEquals,
                getVersionedNetworkPath("eurostag-tutorial1-lf-extensions.bin", CURRENT_IIDM_VERSION));

        //backward compatibility
        roundTripVersionedJsonFromMinToCurrentVersionTest("eurostag-tutorial1-lf-extensions.bin", IidmVersion.V_1_12);
    }
}
