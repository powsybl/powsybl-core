/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.model.InMemoryCgmesModel;
import com.powsybl.triplestore.api.PropertyBags;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Luma Zamarreño {@literal <zamarrenolm at aia.es>}
 */
class FakeTapChangerConversionTest {
    @Test
    void fakeTapChangersEmpty() {
        CgmesModel cgmes = new InMemoryCgmesModel();
        assertEquals(new PropertyBags(), cgmes.ratioTapChangers("Fake transformer Id"));
        assertEquals(new PropertyBags(), cgmes.phaseTapChangers("Fake transformer Id"));
    }
}
