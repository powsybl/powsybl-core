/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.test;

import java.util.List;

import java.util.stream.Collectors;

import com.powsybl.iidm.network.Substation;
import org.junit.Assert;
import org.junit.Test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.test.FakeCgmesModel;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class FakeSubstationsConversionTest {
    @Test
    public void fakeSubstations() {
        CgmesModel cgmes = new FakeCgmesModel()
                .substations("Sub1", "Sub2", "Sub3");
        Network n = new Conversion(cgmes).convertedNetwork();
        List<String> actuals = n.getSubstationStream()
                .map(Substation::getId)
                .collect(Collectors.toList());
        List<String> expecteds = cgmes.substations().pluckLocals("Substation");
        Assert.assertEquals(expecteds, actuals);
    }
}
