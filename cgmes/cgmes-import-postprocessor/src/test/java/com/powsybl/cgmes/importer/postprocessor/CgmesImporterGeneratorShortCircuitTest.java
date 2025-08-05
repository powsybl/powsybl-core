/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.importer.postprocessor;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class CgmesImporterGeneratorShortCircuitTest {

    @Test
    void testImportCgmesGeneratorShortCircuitData() {
        Properties p = new Properties();
        p.put(CgmesImport.POST_PROCESSORS, List.of("shortcircuit"));
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.miniBusBranch().dataSource(),
                NetworkFactory.findDefault(), p);

        Generator generator = network.getGenerator("392ea173-4f8e-48fa-b2a3-5c3721e93196");
        assertNotNull(generator);

        GeneratorShortCircuit generatorShortCircuit = generator.getExtension(GeneratorShortCircuit.class);
        assertNotNull(generatorShortCircuit);

        double tol = 0.000001;
        assertEquals(0.1, generatorShortCircuit.getDirectSubtransX(), tol);
        assertEquals(1.8, generatorShortCircuit.getDirectTransX(), tol);
        assertTrue(Double.isNaN(generatorShortCircuit.getStepUpTransformerX()));
    }

}
