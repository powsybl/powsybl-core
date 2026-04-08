/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.ActivePowerControlAdder;
import com.powsybl.iidm.network.extensions.ConnectablePosition;
import com.powsybl.iidm.network.extensions.ConnectablePositionAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class EurostagJsonTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        ExportOptions exportOptions = new ExportOptions().setFormat(TreeDataFormat.JSON);
        ImportOptions importOptions = new ImportOptions().setFormat(TreeDataFormat.JSON);
        roundTripTest(EurostagTutorialExample1Factory.createWithLFResults(),
                (n, jsonFile) -> NetworkSerDe.write(n, exportOptions, jsonFile),
                jsonFile -> NetworkSerDe.read(jsonFile, importOptions),
                getVersionedNetworkPath("eurostag-tutorial1-lf.json", CURRENT_IIDM_VERSION));

        //backward compatibility
        roundTripVersionedJsonFromMinToCurrentVersionTest("eurostag-tutorial1-lf.json", IidmVersion.V_1_11);
    }

    @Test
    void roundTripTestWithExtension() throws IOException {
        ExportOptions exportOptions = new ExportOptions().setFormat(TreeDataFormat.JSON);
        ImportOptions importOptions = new ImportOptions().setFormat(TreeDataFormat.JSON);
        Network network = EurostagTutorialExample1Factory.createWithLFResults();
        network.getGeneratorStream().findFirst().ifPresent(g -> g.newExtension(ActivePowerControlAdder.class).withDroop(2).withParticipate(true).add());
        network.getLoadStream().forEach(l -> l.newExtension(ConnectablePositionAdder.class).newFeeder().withDirection(ConnectablePosition.Direction.BOTTOM).add().add());
        roundTripTest(network,
                (n, jsonFile) -> NetworkSerDe.write(n, exportOptions, jsonFile),
                jsonFile -> NetworkSerDe.read(jsonFile, importOptions),
                getVersionedNetworkPath("eurostag-tutorial1-lf-extensions.json", CURRENT_IIDM_VERSION));

        //backward compatibility
        roundTripVersionedJsonFromMinToCurrentVersionTest("eurostag-tutorial1-lf-extensions.json", IidmVersion.V_1_11);
    }
}
