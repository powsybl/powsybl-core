/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.export;

import com.google.common.collect.Sets;
import com.powsybl.iidm.IidmImportExportMode;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.*;


/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExportOptionsTest {

    @Test
    public void importOptionsTest() throws IOException {
        ExportOptions options = new ExportOptions();
        options.setMode(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE);
        assertEquals(Boolean.TRUE, options.isOneFilePerExtensionType());
        assertEquals(Boolean.FALSE, options.isSeparateBaseAndExtensions());

        Set<String> extensionsList = Sets.newHashSet("loadFoo", "loadBar");
        options.setExtensions(extensionsList);
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertTrue(options.withExtension("loadFoo"));

        assertFalse(options.withAllExtensions());
        assertEquals(Boolean.FALSE, options.isAnonymized());
        assertEquals(Boolean.TRUE, options.isIndent());
        assertEquals(Boolean.TRUE, options.isWithBranchSV());

    }

}
