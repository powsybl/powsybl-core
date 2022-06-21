/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.conformity.CgmesConformity1Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.iidm.import_.Importers;
import com.powsybl.iidm.network.Network;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class SourceForIidmIdentifiersTest {

    @Test
    public void microGridMasterResourceIdsExplicit() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.SOURCE_FOR_IIDM_ID, CgmesImport.SOURCE_FOR_IIDM_ID_MRID);
        Network network = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), importParams);
        network.getIdentifiables().forEach(idf -> assertFalse(idf.getId().startsWith("_")));
    }

    @Test
    public void microGridMasterResourceIdsDefault() {
        Network network = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), null);
        network.getIdentifiables().forEach(idf -> assertFalse(idf.getId().startsWith("_")));
    }

    @Test
    public void microGridRDFIds() {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.SOURCE_FOR_IIDM_ID, CgmesImport.SOURCE_FOR_IIDM_ID_RDFID);
        Network network = Importers.importData("CGMES", CgmesConformity1Catalog.microGridBaseCaseBE().dataSource(), importParams);
        network.getIdentifiables().forEach(idf -> assertTrue(idf.getId().startsWith("_") || idf.getId().startsWith("urn:uuid:")));
    }

}
