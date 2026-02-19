/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.cgmes.conversion.test;

import java.util.List;
import java.util.stream.Collectors;

import com.powsybl.iidm.network.Substation;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.InMemoryCgmesModel;
import com.powsybl.iidm.network.Network;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class FakeSubstationsConversionTest {
    @Test
    void fakeSubstations() {
        CgmesModel cgmes = new InMemoryCgmesModel()
                .substations("Sub1", "Sub2", "Sub3");
        Network n = new Conversion(cgmes).convert();
        List<String> actuals = n.getSubstationStream()
                .map(Substation::getId)
                .collect(Collectors.toList());
        List<String> expecteds = cgmes.substations().pluckLocals("Substation");
        assertEquals(expecteds, actuals);
    }
}
