/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ValidationLevel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */

class NetworkUpdateTest extends AbstractSerDeTest {

    void print(Network network, String message) {
        double totalLoad = network
                .getLoadStream()
                .mapToDouble(Load::getP0)
                .sum();
        double totalGeneration = network
                .getGeneratorStream()
                .mapToDouble(Generator::getTargetP)
                .sum();
        System.out.println(message);
        System.out.printf("total load          = %10.2f%n", totalLoad);
        System.out.printf("total generation    = %10.2f%n", totalGeneration);
        System.out.printf("network valid level = %s%n", network.getValidationLevel());
        System.out.println();
    }

    @Test
    void testReadSSH() {
        Network network0 = Network.read(CgmesConformity1Catalog.microGridBaseCaseBE().dataSource());
        print(network0, "reading everything at once (EQ+SSH)");

        Network network = Network.read(CgmesConformity1Catalog.microGridBaseCaseBEonlyEQ().dataSource());
        print(network, "after read");

        // reset default values and ensure we do not have ssh validation level, but equipment, the lowest
        network.setMinimumAcceptableValidationLevel(ValidationLevel.EQUIPMENT);
        network.getLoadStream().forEach(l -> {
            l.setP0(Double.NaN);
            l.setQ0(Double.NaN);
        });
        network.getGeneratorStream().forEach(g -> g.setTargetP(Double.NaN));
        network.runValidationChecks(false);
        print(network, "after reset default values");
        assertEquals(ValidationLevel.EQUIPMENT, network.getValidationLevel());

        // Now import only SSH data over the current network
        CgmesImport importer = new CgmesImport();
        importer.update(network, CgmesConformity1Catalog.microGridBaseCaseBEonlySSH().dataSource(), null, ReportNode.NO_OP);
        print(network, "after update with SSH");

        network.runValidationChecks(false);
        print(network, "after checks for SSH");
        assertEquals(ValidationLevel.STEADY_STATE_HYPOTHESIS, network.getValidationLevel());
    }
}
