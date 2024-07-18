/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.powsybl.iidm.serde.IidmSerDeConstants.CURRENT_IIDM_VERSION;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class BinaryImporterTest {

    @Test
    void testMetaInfos() {
        var importer = new BinaryImporter();
        assertEquals("BIIDM", importer.getFormat());
        assertEquals("IIDM binary v " + CURRENT_IIDM_VERSION.toString(".") + " importer", importer.getComment());
        assertEquals(List.of("biidm", "bin", "iidm.bin"), importer.getSupportedExtensions());
        assertEquals(5, importer.getParameters().size());
    }
}
