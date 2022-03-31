/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;

import com.powsybl.cgmes.conformity.test.Cgmes3ModifiedCatalog;
import com.powsybl.cgmes.conformity.test.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.CgmesModelFactory;
import com.powsybl.cgmes.model.test.TestGridModel;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.PhaseTapChanger;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.triplestore.api.TripleStoreFactory;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class RegulatingTerminalConversionTest {

    @Test
    public void microGridBaseCaseRegulatingTerminalsDefinedOnSwitches() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(Cgmes3ModifiedCatalog.microGridBaseCaseRegulatingTerminalsDefinedOnSwitches(), config);

        // Flow control in transformer
        TwoWindingsTransformer tw2t = n.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
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
        Generator gen = n.getGenerator("_550ebe0d-f2b2-48c1-991f-cebea43a21aa");
        assertNotNull(gen);

        regulatingTerminal = gen.getRegulatingTerminal();
        terminal = gen.getTerminal();
        assertEquals(terminal, regulatingTerminal);

        regulationValue = gen.getTargetV();
        assertEquals(21.987, regulationValue, 0.0);
    }

    @Test
    public void microGridBaseBECaseRegulatingTerminalsDefinedOnSwitches() throws IOException {
        Conversion.Config config = new Conversion.Config();
        Network n = networkModel(CgmesConformity1ModifiedCatalog.microGridBaseCaseBERegulatingTerminalsDefinedOnSwitches(), config);

        // Flow control in transformer
        TwoWindingsTransformer tw2t = n.getTwoWindingsTransformer("_a708c3bc-465d-4fe7-b6ef-6fa6408a62b0");
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
        Generator gen = n.getGenerator("_550ebe0d-f2b2-48c1-991f-cebea43a21aa");
        assertNotNull(gen);

        regulatingTerminal = gen.getRegulatingTerminal();
        terminal = gen.getTerminal();
        assertEquals(terminal, regulatingTerminal);

        regulationValue = gen.getTargetV();
        assertEquals(21.987, regulationValue, 0.0);
    }

    private Network networkModel(TestGridModel testGridModel, Conversion.Config config) throws IOException {

        ReadOnlyDataSource ds = testGridModel.dataSource();
        String impl = TripleStoreFactory.defaultImplementation();

        CgmesModel cgmes = CgmesModelFactory.create(ds, impl);

        config.setConvertSvInjections(true);
        Conversion c = new Conversion(cgmes, config);
        Network n = c.convert();

        return n;
    }
}
