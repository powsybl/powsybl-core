/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test.export;

import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.export.ReferenceDataProvider;
import com.powsybl.commons.datasource.ResourceDataSource;
import com.powsybl.commons.datasource.ResourceSet;
import com.powsybl.triplestore.api.PropertyBag;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
class ReferenceDataProviderTest {

    @Test
    void testReferenceDataProvider() {
        String sourcingActorName = "ELIA";
        Properties params = new Properties();
        ResourceDataSource referenceDataSource = new ResourceDataSource("sample", new ResourceSet("/reference-data-provider", "sample_EQBD.xml"));
        ReferenceDataProvider referenceDataProvider = new ReferenceDataProvider(sourcingActorName, referenceDataSource, new CgmesImport(), params);

        assertEquals("urn:uuid:99999999-cfff-4252-a5a8-1784fb5a4514", referenceDataProvider.getEquipmentBoundaryId());
        assertEquals("65dd04e792584b3b912374e35dec032e", referenceDataProvider.getBaseVoltage(400));

        PropertyBag actor = referenceDataProvider.getSourcingActor();
        assertEquals("http://www.elia.be/OperationalPlanning", actor.getLocal("masUri"));
        assertEquals("BE", referenceDataProvider.getSourcingActorRegion().getRight());
    }

}
