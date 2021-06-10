/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.conformity;

import com.powsybl.cgmes.conformity.test.Cgmes3Catalog;
import com.powsybl.cgmes.conversion.CgmesImport;
import com.powsybl.cgmes.conversion.test.ConversionTester;
import com.powsybl.cgmes.conversion.test.network.compare.ComparisonConfig;
import com.powsybl.iidm.network.Network;
import com.powsybl.triplestore.api.TripleStoreFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class Cgmes3ConversionTest {
    //@Test
    public void microGrid() throws IOException {
        Properties importParams = new Properties();
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.microGrid());
    }

    //@Test
    public void microGridConvertBoundary() throws IOException {
        Properties importParams = new Properties();
        importParams.put(CgmesImport.CONVERT_BOUNDARY, "true");
        ConversionTester t = new ConversionTester(
            importParams,
            TripleStoreFactory.onlyDefaultImplementation(),
            new ComparisonConfig());
        Network expected = null;
        t.testConversion(expected, Cgmes3Catalog.microGrid());
    }
}
