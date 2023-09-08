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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
class ReferenceDataProviderTest {

    @Test
    void testReferenceDataProviderWithSourcingActor() {
        String sourcingActorName = "ELIA";
        ReferenceDataProvider referenceDataProvider = new ReferenceDataProvider(sourcingActorName, null, referenceDataSource(), new CgmesImport(), null);
        checkReferenceDataIsLoaded(referenceDataProvider);

        PropertyBag actor = referenceDataProvider.getSourcingActor();
        assertEquals("http://www.elia.be/OperationalPlanning", actor.getLocal("masUri"));
        assertEquals("BE", referenceDataProvider.getSourcingActorRegion().getRight());
    }

    @Test
    void testReferenceDataProviderWithCountry() {
        String countryName = "BE";
        ReferenceDataProvider referenceDataProvider = new ReferenceDataProvider(null, countryName, referenceDataSource(), new CgmesImport(), null);
        checkReferenceDataIsLoaded(referenceDataProvider);

        PropertyBag actor = referenceDataProvider.getSourcingActor();
        assertEquals("ELIA", actor.getLocal("name"));
        assertEquals("http://www.elia.be/OperationalPlanning", actor.getLocal("masUri"));
        assertEquals("BE", referenceDataProvider.getSourcingActorRegion().getRight());
    }

    @Test
    void testReferenceDataProviderNoActorFoundForCountry() {
        String countryName = "XX";
        ReferenceDataProvider referenceDataProvider = new ReferenceDataProvider(null, countryName, referenceDataSource(), new CgmesImport(), null);
        checkReferenceDataIsLoaded(referenceDataProvider);

        PropertyBag actor = referenceDataProvider.getSourcingActor();
        assertEquals(0, actor.size());
    }

    @Test
    void testReferenceDataProviderNoActorFound() {
        String sourcingActorName = "XXXXX";
        ReferenceDataProvider referenceDataProvider = new ReferenceDataProvider(sourcingActorName, null, referenceDataSource(), new CgmesImport(), null);
        checkReferenceDataIsLoaded(referenceDataProvider);

        PropertyBag actor = referenceDataProvider.getSourcingActor();
        assertEquals(0, actor.size());
    }

    private ResourceDataSource referenceDataSource() {
        return new ResourceDataSource("sample", new ResourceSet("/reference-data-provider", "sample_EQBD.xml"));
    }

    private void checkReferenceDataIsLoaded(ReferenceDataProvider referenceDataProvider) {
        assertEquals("urn:uuid:99999999-cfff-4252-a5a8-1784fb5a4514", referenceDataProvider.getEquipmentBoundaryId());
        assertEquals("65dd04e792584b3b912374e35dec032e", referenceDataProvider.getBaseVoltage(400));
    }

}
