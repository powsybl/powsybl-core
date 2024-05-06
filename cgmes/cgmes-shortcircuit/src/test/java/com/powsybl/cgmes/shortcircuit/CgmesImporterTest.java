/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.shortcircuit;

import com.powsybl.cgmes.conformity.Cgmes3Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conformity.CgmesConformity1ModifiedCatalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.CgmesModelExtension;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.shorcircuit.CgmesShortCircuitImporter;
import com.powsybl.cgmes.shorcircuit.CgmesShortCircuitModel;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.extensions.GeneratorShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Properties;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 * @author José Antonio Marqués {@literal <marquesja at aia.es>}
 */
class CgmesImporterTest {

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

    @Test
    void testImportCgmes3GeneratorShortCircuitData() {
        Network network = new CgmesImport().importData(Cgmes3Catalog.miniGrid().dataSource(),
                NetworkFactory.findDefault(), new Properties());

        CgmesModelExtension cgmesModelExtension = network.getExtension(CgmesModelExtension.class);
        assertNotNull(cgmesModelExtension);
        CgmesModel cgmesModel = cgmesModelExtension.getCgmesModel();
        assertNotNull(cgmesModel);

        CgmesShortCircuitModel cgmesScModel = new CgmesShortCircuitModel(cgmesModel.tripleStore());
        new CgmesShortCircuitImporter(cgmesScModel, network).importShortcircuitData();

        Generator generator = network.getGenerator("ca67be42-750e-4ebf-bfaa-24d446e59a22");
        assertNotNull(generator);

        GeneratorShortCircuit generatorShortCircuit = generator.getExtension(GeneratorShortCircuit.class);
        assertNotNull(generatorShortCircuit);

        double tol = 0.000001;
        assertEquals(0.6174, generatorShortCircuit.getDirectSubtransX(), tol);
        assertEquals(7.938, generatorShortCircuit.getDirectTransX(), tol);
        assertTrue(Double.isNaN(generatorShortCircuit.getStepUpTransformerX()));
    }

    @Test
    void testImportCgmesBranchModelBusbarSectionShortCircuitData() {
        Network network = new CgmesImport().importData(CgmesConformity1ModifiedCatalog.smallGridBusBranchWithBusbarSectionsAndIpMax().dataSource(),
                NetworkFactory.findDefault(), new Properties());

        CgmesModelExtension cgmesModelExtension = network.getExtension(CgmesModelExtension.class);
        assertNotNull(cgmesModelExtension);
        CgmesModel cgmesModel = cgmesModelExtension.getCgmesModel();
        assertNotNull(cgmesModel);

        CgmesShortCircuitModel cgmesScModel = new CgmesShortCircuitModel(cgmesModel.tripleStore());
        new CgmesShortCircuitImporter(cgmesScModel, network).importShortcircuitData();

        Bus bus = network.getBusBreakerView().getBus("0472a783-c766-11e1-8775-005056c00008");
        assertNotNull(bus);

        IdentifiableShortCircuit busShortCircuit = bus.getExtension(IdentifiableShortCircuit.class);
        assertNotNull(busShortCircuit);

        assertTrue(Double.isNaN(busShortCircuit.getIpMin()));
        assertEquals(1000.0, busShortCircuit.getIpMax(), 0.000001);
    }

    @Test
    void testImportCgmesBusbarSectionShortCircuitData() {
        Network network = new CgmesImport().importData(CgmesConformity1Catalog.miniNodeBreaker().dataSource(),
                NetworkFactory.findDefault(), new Properties());

        CgmesModelExtension cgmesModelExtension = network.getExtension(CgmesModelExtension.class);
        assertNotNull(cgmesModelExtension);
        CgmesModel cgmesModel = cgmesModelExtension.getCgmesModel();
        assertNotNull(cgmesModel);

        CgmesShortCircuitModel cgmesScModel = new CgmesShortCircuitModel(cgmesModel.tripleStore());
        new CgmesShortCircuitImporter(cgmesScModel, network).importShortcircuitData();

        BusbarSection busbarSection = network.getBusbarSection("d9f23c01-d924-4040-ab48-d5c36ccdf1a3");
        assertNotNull(busbarSection);

        IdentifiableShortCircuit busbarSectionShortCircuit = busbarSection.getExtension(IdentifiableShortCircuit.class);
        assertNull(busbarSectionShortCircuit);
    }

    @Test
    void testImportCgmes3BusbarSectionShortCircuitData() {
        Properties importParams = new Properties();
        // Avoid importing assembled micro grid as subnetworks, to have access to whole CGMES model
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = new CgmesImport().importData(Cgmes3Catalog.microGrid().dataSource(),
                NetworkFactory.findDefault(), importParams);

        CgmesModelExtension cgmesModelExtension = network.getExtension(CgmesModelExtension.class);
        assertNotNull(cgmesModelExtension);
        CgmesModel cgmesModel = cgmesModelExtension.getCgmesModel();
        assertNotNull(cgmesModel);

        CgmesShortCircuitModel cgmesScModel = new CgmesShortCircuitModel(cgmesModel.tripleStore());
        new CgmesShortCircuitImporter(cgmesScModel, network).importShortcircuitData();

        BusbarSection busbarSection = network.getBusbarSection("364c9ca2-0d1d-4363-8f46-e586f8f66a8c");
        assertNotNull(busbarSection);

        IdentifiableShortCircuit busbarSectionShortCircuit = busbarSection.getExtension(IdentifiableShortCircuit.class);
        assertNotNull(busbarSectionShortCircuit);

        assertTrue(Double.isNaN(busbarSectionShortCircuit.getIpMin()));
        assertEquals(5000.0, busbarSectionShortCircuit.getIpMax(), 0.000001);
    }
}
