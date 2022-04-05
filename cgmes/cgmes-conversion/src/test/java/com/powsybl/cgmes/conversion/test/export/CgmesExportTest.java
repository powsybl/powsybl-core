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
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
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
    public void testFromIidm() throws IOException { // Test from IIDM with configuration that does not exist in CGMES (disconnected node)
        Network network = FictitiousSwitchFactory.create();
        network.getVoltageLevel("C").getNodeBreakerView().newSwitch().setId("TEST_SW")
                .setKind(SwitchKind.DISCONNECTOR)
                .setOpen(true)
                .setNode1(0)
                .setNode2(6)
                .add();
        try (FileSystem fs = Jimfs.newFileSystem(Configuration.unix())) {
            Path tmpDir = Files.createDirectory(fs.getPath("/cgmes"));
            Exporters.export("CGMES", network, null, tmpDir.resolve("tmp"));
            Network n2 = Importers.loadNetwork(new GenericReadOnlyDataSource(tmpDir, "tmp"));
            VoltageLevel c = n2.getVoltageLevel("C");
            assertNull(Networks.getEquivalentTerminal(c, c.getNodeBreakerView().getNode2("TEST_SW")));
        }
    }
}
