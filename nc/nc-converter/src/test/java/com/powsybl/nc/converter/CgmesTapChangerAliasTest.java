/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.nc.converter;

import com.powsybl.cgmes.conversion.Conversion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tap-position conversion resolves tap changers through the IIDM aliases written by the CGMES importer.
 * The converter cannot depend on cgmes-conversion, so this test guards the duplicated alias prefixes.
 *
 * @author Roman Vykuka {@literal <vykuka at gmail.com>}
 */
class CgmesTapChangerAliasTest {

    @Test
    void converterAliasPrefixesMatchCgmesAliases() {
        assertTrue(Conversion.ALIAS_RATIO_TAP_CHANGER1.startsWith(ActionConverter.RATIO_TAP_CHANGER_ALIAS_PREFIX),
            Conversion.ALIAS_RATIO_TAP_CHANGER1);
        assertTrue(Conversion.ALIAS_PHASE_TAP_CHANGER1.startsWith(ActionConverter.PHASE_TAP_CHANGER_ALIAS_PREFIX),
            Conversion.ALIAS_PHASE_TAP_CHANGER1);
    }

    @Test
    void aliasSuffixIdentifiesTheTransformerLeg() {
        // The converter reads the leg from the last character of the alias type.
        assertEquals('1', lastChar(Conversion.ALIAS_RATIO_TAP_CHANGER1));
        assertEquals('2', lastChar(Conversion.ALIAS_RATIO_TAP_CHANGER2));
        assertEquals('3', lastChar(Conversion.ALIAS_RATIO_TAP_CHANGER3));
        assertEquals('1', lastChar(Conversion.ALIAS_PHASE_TAP_CHANGER1));
        assertEquals('2', lastChar(Conversion.ALIAS_PHASE_TAP_CHANGER2));
        assertEquals('3', lastChar(Conversion.ALIAS_PHASE_TAP_CHANGER3));
    }

    private static char lastChar(String alias) {
        return alias.charAt(alias.length() - 1);
    }
}
