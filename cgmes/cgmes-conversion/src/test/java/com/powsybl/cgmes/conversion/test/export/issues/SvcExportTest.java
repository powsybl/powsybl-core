/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export.issues;

import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.test.ConversionUtil;
import com.powsybl.cgmes.model.CgmesNamespace;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.StaticVarCompensator;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.Properties;

import static com.powsybl.iidm.network.StaticVarCompensator.RegulationMode.OFF;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class SvcExportTest extends AbstractSerDeTest {

    @Test
    void microT4SvcRCDisabled() {
        String svcId = "3c69652c-ff14-4550-9a87-b6fdaccbb5f4";
        String rcId = "caf65447-3cfb-48d7-aaaa-cd9af3d34261";

        // Import a test case with SVC RC disabled and check we have kept the RC id ant its target value
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Network.read(CgmesConformity1ModifiedCatalog.microT4BeBbOffSvcControlV().dataSource(), importParams);
        StaticVarCompensator svc = network.getStaticVarCompensator(svcId);
        assertNotNull(svc);
        assertEquals(OFF, svc.getRegulationMode());
        assertEquals(rcId, svc.getProperty(Conversion.CGMES_PREFIX_ALIAS_PROPERTIES + "RegulatingControl"));
        assertEquals(231.123, svc.getVoltageSetpoint(), 0.0);

        // Do a full export and check that output files contain the reference to the RC
        String basename = "micro-t4-svc-rc-off-v";
        network.write("CGMES", null, tmpDir.resolve(basename));
        Path eq = tmpDir.resolve(basename + "_EQ.xml");
        Path ssh = tmpDir.resolve(basename + "_SSH.xml");

        assertTrue(ConversionUtil.xmlContains(eq, "RegulatingControl", CgmesNamespace.RDF_NAMESPACE, "ID", "_" + rcId));
        assertTrue(ConversionUtil.xmlContains(ssh, "RegulatingControl", CgmesNamespace.RDF_NAMESPACE, "about", "#_" + rcId));
    }

}
