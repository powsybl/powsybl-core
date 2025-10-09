/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.iidm.network.Importers;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStore;
import com.powsybl.triplestore.api.TripleStoreFactory;
import com.powsybl.triplestore.api.TripleStoreOptions;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class SourceForIidmIdentifiersTest {

    @Test
    void microGridMasterResourceIdsExplicit() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.SOURCE_FOR_IIDM_ID, CgmesImport.SOURCE_FOR_IIDM_ID_MRID);
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), importParams);
        network.getIdentifiables().forEach(idf -> assertFalse(idf.getId().startsWith("_")));
    }

    @Test
    void microGridMasterResourceIdsDefault() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), importParams);
        network.getIdentifiables().forEach(idf -> assertFalse(idf.getId().startsWith("_")));
    }

    @Test
    void microGridRDFIds() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.SOURCE_FOR_IIDM_ID, CgmesImport.SOURCE_FOR_IIDM_ID_RDFID);
        importParams.put(CgmesImport.IMPORT_CGM_WITH_SUBNETWORKS, "false");
        Network network = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), importParams);
        network.getIdentifiables().forEach(idf -> assertTrue(idf.getId().startsWith("_") || idf.getId().startsWith("urn:uuid:")));
    }

    @Test
    void tipleStoreOptions() {
        TripleStoreOptions options0 = new TripleStoreOptions();
        assertTrue(options0.isRemoveInitialUnderscoreForIdentifiers());
        assertTrue(options0.unescapeIdentifiers());
        TripleStoreOptions options1 = new TripleStoreOptions(true, true);
        assertTrue(options1.isRemoveInitialUnderscoreForIdentifiers());
        assertTrue(options1.unescapeIdentifiers());
        TripleStoreOptions options2 = new TripleStoreOptions(false, false);
        assertFalse(options2.isRemoveInitialUnderscoreForIdentifiers());
        assertFalse(options2.unescapeIdentifiers());

        TripleStore ts = TripleStoreFactory.create(options2);
        assertFalse(ts.getOptions().isRemoveInitialUnderscoreForIdentifiers());
        assertFalse(ts.getOptions().unescapeIdentifiers());
    }

}
