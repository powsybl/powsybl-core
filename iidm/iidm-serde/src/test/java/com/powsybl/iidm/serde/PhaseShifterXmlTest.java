/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.test.PhaseShifterTestCaseFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class PhaseShifterXmlTest extends AbstractIidmSerDeTest {
    @Test
    void roundTripTest() throws IOException {
        // backward and current compatibility
        allFormatsRoundTripFromVersionedXmlTest("phaseShifterRoundTripRef.xml", IidmVersion.values());

        allFormatsRoundTripTest(PhaseShifterTestCaseFactory.createWithTargetDeadband(), "phaseShifterRoundTripRef.xml", CURRENT_IIDM_VERSION);
    }

    @Test
    void importAndExportPhaseTapChangerWithFixedTapRegulationModeTest() {
        // for IIDM version < 1.14 a phase tap changer can have a regulation mode set to FIXED_TAP and should still be imported as CURRENT_LIMITER with regulating=false
        Network network = NetworkSerDe.read(getVersionedNetworkAsStream("phaseShifterFixedTapRegulationModeRef.xml", IidmVersion.V_1_13));
        checkPhaseTapChangerRegulation(network);

        // Export network and read it again
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.setExtensions(Collections.emptySet());
        Path exportedPath = tmpDir.resolve("exported.xml");
        NetworkSerDe.write(network, exportOptions, exportedPath);

        Network network2 = NetworkSerDe.read(exportedPath);
        checkPhaseTapChangerRegulation(network2);
    }

    private void checkPhaseTapChangerRegulation(Network network) {
        Assertions.assertEquals(PhaseTapChanger.RegulationMode.CURRENT_LIMITER, network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().getRegulationMode());
        Assertions.assertFalse(network.getTwoWindingsTransformer("PS1").getPhaseTapChanger().isRegulating());
    }
}
