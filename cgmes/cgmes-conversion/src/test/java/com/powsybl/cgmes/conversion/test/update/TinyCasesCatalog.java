/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test.update;

import com.powsybl.cgmes.model.test.TestGridModelResources;
import com.powsybl.commons.datasource.ResourceSet;

public class TinyCasesCatalog {

    public TestGridModelResources tinyTest14() {
        return new TestGridModelResources("case1_EQ", null,
            new ResourceSet("/TinyRdfTest/", "case1_EQ.xml"));
    }

}
