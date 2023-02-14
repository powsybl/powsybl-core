/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import com.google.common.collect.Sets;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ImportOptionsTest {

    @Test
    void importOptionsTest() {
        ImportOptions options = new ImportOptions();
        Set<String> extensionsList = Sets.newHashSet("loadFoo", "loadBar");
        options.setExtensions(extensionsList);
        assertEquals(Boolean.FALSE, options.withNoExtension());

        options.addExtension("loadBar");
        assertEquals(2, (int) options.getExtensions().map(Set::size).orElse(-1));
    }

    @Test
    void importOptionsTest2() {
        Set<String> extensionsList = Sets.newHashSet("loadFoo", "loadBar");
        ImportOptions options = new ImportOptions(Boolean.FALSE);
        options.setExtensions(extensionsList);

        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertEquals(2, (int) options.getExtensions().map(Set::size).orElse(-1));
    }

    @Test
    void importOptionsDefaultValues() {
        ImportOptions options = new ImportOptions(Boolean.FALSE);

        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertEquals(-1, (int) options.getExtensions().map(Set::size).orElse(-1));
        assertEquals(Boolean.TRUE, options.withAllExtensions());
    }
}
