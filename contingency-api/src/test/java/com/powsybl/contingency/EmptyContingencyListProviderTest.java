/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class EmptyContingencyListProviderTest {

    @Test
    public void test() {
        ContingenciesProviderFactory factory = new EmptyContingencyListProviderFactory();
        ContingenciesProvider provider = factory.create();

        Assert.assertTrue(provider instanceof EmptyContingencyListProvider);
        Assert.assertEquals(0, provider.getContingencies(null).size());
    }
}
