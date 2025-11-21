/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.GridModelReference;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static com.powsybl.cgmes.conversion.test.ConversionUtil.readCgmesResources;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class RegulatingTerminalConversionTest {

    @Test
    void microGridBaseCaseRegulatingTerminalsDefinedOnSwitches() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(Cgmes3ModifiedCatalog.microGridBaseCaseRegulatingTerminalsDefinedOnSwitches(), config);

        // Flow control in transformer
        TwoWindingsTransformer tw2t = n.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        assertNotNull(tw2t);
        PhaseTapChanger ptc = tw2t.getPhaseTapChanger();
        assertNotNull(ptc);

        Terminal regulatingTerminal = ptc.getRegulationTerminal();
        Terminal terminal = tw2t.getTerminal2();
        assertEquals(terminal, regulatingTerminal);

        // Opposite sign
        double regulationValue = ptc.getRegulationValue();
        assertEquals(65.0, regulationValue, 0.0);

        // Voltage control
        Generator gen = n.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa");
        assertNotNull(gen);

        regulatingTerminal = gen.getRegulatingTerminal();
        terminal = gen.getTerminal();
        assertEquals(terminal, regulatingTerminal);

        regulationValue = gen.getTargetV();
        assertEquals(21.987, regulationValue, 0.0);
    }

    @Test
    void microGridBaseBECaseRegulatingTerminalsDefinedOnSwitches() {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERegulatingTerminalsDefinedOnSwitches(), config);

        // Flow control in transformer
        TwoWindingsTransformer tw2t = n.getTwoWindingsTransformer("a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
        assertNotNull(tw2t);
        PhaseTapChanger ptc = tw2t.getPhaseTapChanger();
        assertNotNull(ptc);

        Terminal regulatingTerminal = ptc.getRegulationTerminal();
        Terminal terminal = tw2t.getTerminal2();
        assertEquals(terminal, regulatingTerminal);

        // same sign
        double regulationValue = ptc.getRegulationValue();
        assertEquals(-65.0, regulationValue, 0.0);

        // Voltage control
        Generator gen = n.getGenerator("550ebe0d-f2b2-48c1-991f-cebea43a21aa");
        assertNotNull(gen);

        regulatingTerminal = gen.getRegulatingTerminal();
        terminal = gen.getTerminal();
        assertEquals(terminal, regulatingTerminal);

        regulationValue = gen.getTargetV();
        assertEquals(21.987, regulationValue, 0.0);
    }

    @Test
    void voltageRegulatingTerminalAtBusbarSectionNodeBreakerTest() {
        Network network = readCgmesResources("/issues/voltage-regulating-terminal-at-busbar-section",
                "issue_3560_CGMES_EQ.xml",
                "issue_3560_CGMES_SSH.xml",
                "issue_3560_CGMES_SV.xml",
                "issue_3560_CGMES_TP.xml");

        Generator generator = network.getGenerator("GEN");
        assertNotNull(generator);
        assertEquals("VL2_400_BBS1", generator.getRegulatingTerminal().getConnectable().getId());
        System.err.printf("Type %s %n", generator.getRegulatingTerminal().getConnectable().getType());
        assertSame(IdentifiableType.BUSBAR_SECTION, generator.getRegulatingTerminal().getConnectable().getType());
    }

    @Test
    void voltageRegulatingTerminalAtBusbarSectionBusBreakerTest() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_NODE_BREAKER_AS_BUS_BREAKER, "true");

        Network network = readCgmesResources(importParams,
                "/issues/voltage-regulating-terminal-at-busbar-section",
                "issue_3560_CGMES_EQ.xml",
                "issue_3560_CGMES_SSH.xml",
                "issue_3560_CGMES_SV.xml",
                "issue_3560_CGMES_TP.xml");

        Generator generator = network.getGenerator("GEN");
        assertNotNull(generator);
        assertEquals("LOAD", generator.getRegulatingTerminal().getConnectable().getId());
        assertSame(IdentifiableType.LOAD, generator.getRegulatingTerminal().getConnectable().getType());
    }

    private Network networkModel(GridModelReference testGridModel, Conversion.Config config) {
        config.setConvertSvInjections(true);
        return ConversionUtil.networkModel(testGridModel, config);
    }
}
