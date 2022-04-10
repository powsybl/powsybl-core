/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.powerfactory.model;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DataSchemeTest {

    @Test
    public void test() {
        DataScheme scheme = new DataScheme();
        scheme.addClass(DataClass.init("ElmFoo")
                .addAttribute(new DataAttribute("i", DataAttributeType.INTEGER)));
        scheme.addClass(DataClass.init("ElmBar")
                .addAttribute(new DataAttribute("d", DataAttributeType.DOUBLE)));
        assertTrue(scheme.classExists("ElmFoo"));
        assertFalse(scheme.classExists("ElmBaz"));
        assertEquals("ElmFoo", scheme.getClassByName("ElmFoo").getName());
    }
}
