/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.collect.Sets;
import com.powsybl.iidm.ImportExportTypes;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertEquals;


/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportOptionsTest {

    @Test
    public void importOptionsTest() throws IOException {
        ImportOptions options = new ImportOptions();
        options.setMode(ImportExportTypes.BASE_AND_ONE_FILE_PER_EXTENSION_TYPE);
        assertEquals(Boolean.TRUE, options.isImportFromBaseAndMultipleExtensionFiles());
        assertEquals(Boolean.FALSE, options.isImportFromBaseAndExtensionsFiles());


        Set<String> extensionsList = Sets.newHashSet("loadFoo", "loadBar");
        options.setExtensions(extensionsList);
        assertEquals(Boolean.FALSE, options.withNoExtension());
    }

}
