/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class FilenamesTest {

    @Test
    public void testBasename() {
        assertEquals("test.xiidm", Filenames.getBasename("test.xiidm.gz"));
        assertEquals("", Filenames.getBasename(".test"));
    }

    @Test
    public void testExtension() {
        assertEquals("xiidm", Filenames.getExtension("test.xiidm"));
        assertEquals("xiidm", Filenames.getExtension("test.one.xiidm"));
        assertEquals("xiidm", Filenames.getExtension(".test.one.xiidm"));
        assertEquals("", Filenames.getExtension("test."));
    }
}
