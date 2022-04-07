/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.test.export;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.iidm.export.Exporters;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import com.powsybl.iidm.network.util.Networks;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertNull;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class CgmesExportTest {

    @Test
    public void testFromIidm() throws IOException {
        // Test from IIDM with configuration that does not exist in CGMES (disconnected node on switch and HVDC line)

        Network network = FictitiousSwitchFactory.create();
        VoltageLevel vl = network.getVoltageLevel("C");

        // Add disconnected node on switch (side 2)
        vl.getNodeBreakerView().newSwitch().setId("TEST_SW")
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(true)
                .setNode1(0)
                .setNode2(6)
                .add();

        // Add disconnected node on DC converter station (side 2)
        vl.newVscConverterStation()
                .setId("C1")
                .setNode(5)
                .setLossFactor(1.1f)
                .setVoltageSetpoint(405.0)
                .setVoltageRegulatorOn(true)
                .add();
        vl.getNodeBreakerView().newInternalConnection().setNode1(0).setNode2(5).add();
        vl.newVscConverterStation()
                .setId("C2")
                .setNode(6)
                .setLossFactor(1.1f)
                .setReactivePowerSetpoint(123)
                .setVoltageRegulatorOn(false)
                .add();
        network.newHvdcLine()
                .setId("hvdc_line")
                .setR(5.0)
                .setConvertersMode(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER)
                .setNominalV(440.0)
                .setMaxP(50.0)
                .setActivePowerSetpoint(20.0)
                .setConverterStationId1("C1")
                .setConverterStationId2("C2")
                .add();

        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/cgmes"));
            Exporters.export("CGMES", network, null, tmpDir.resolve("tmp"));
            Network n2 = Importers.loadNetwork(new GenericReadOnlyDataSource(tmpDir, "tmp"));
            VoltageLevel c = n2.getVoltageLevel("_C");
            assertNull(Networks.getEquivalentTerminal(c, c.getNodeBreakerView().getNode2("_TEST_SW")));
            assertNull(n2.getVscConverterStation("_C2").getTerminal().getBusView().getBus());
        }
    }
}
