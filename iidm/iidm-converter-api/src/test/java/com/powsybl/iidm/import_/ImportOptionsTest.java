/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.collect.Sets;
import com.powsybl.iidm.IidmImportExportMode;
import com.powsybl.iidm.IidmImportExportType;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;


/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportOptionsTest {

    @Test
    public void importOptionsTest() {
        ImportOptions options = new ImportOptions();
        options.setMode(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE);
        assertEquals(IidmImportExportMode.ONE_SEPARATED_FILE_PER_EXTENSION_TYPE, options.getMode());

        options.setImportExportType(IidmImportExportType.FULL_IIDM);
        assertEquals(IidmImportExportType.FULL_IIDM, options.getImportExportType());

        Set<String> extensionsList = Sets.newHashSet("loadFoo", "loadBar");
        options.setExtensions(extensionsList);
        assertEquals(Boolean.FALSE, options.withNoExtension());

        options.addExtension("loadBar");
        assertEquals(2, (int) options.getExtensions().map(Set::size).orElse(-1));
    }

    @Test
    public void importOptionsTest2() {
        Set<String> extensionsList = Sets.newHashSet("loadFoo", "loadBar");
        ImportOptions options = new ImportOptions(Boolean.FALSE);
        options.setExtensions(extensionsList);
        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(IidmImportExportMode.UNIQUE_FILE, options.getMode());
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertEquals(2, (int) options.getExtensions().map(Set::size).orElse(-1));
        assertEquals(IidmImportExportType.FULL_IIDM, options.getImportExportType());

        options.setImportExportType(IidmImportExportType.INCREMENTAL_IIDM);
        assertEquals(IidmImportExportType.INCREMENTAL_IIDM, options.getImportExportType());
        options.setTopo(false);
        options.setControl(true);
        options.setState(false);
        assertEquals(Boolean.TRUE, options.isControl());
        assertEquals(Boolean.FALSE, options.isState());
        assertEquals(Boolean.FALSE, options.isTopo());
    }

    @Test
    public void importOptionsDefaultValues() {
        ImportOptions options = new ImportOptions(Boolean.FALSE);

        assertEquals(Boolean.FALSE, options.isThrowExceptionIfExtensionNotFound());
        assertEquals(IidmImportExportMode.UNIQUE_FILE, options.getMode());
        assertEquals(Boolean.FALSE, options.withNoExtension());
        assertEquals(-1, (int) options.getExtensions().map(Set::size).orElse(-1));
        assertEquals(Boolean.TRUE, options.withAllExtensions());
    }
}
