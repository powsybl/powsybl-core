/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.test;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.GenericReadOnlyDataSource;
import com.powsybl.iidm.network.Network;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class ImportNetworkFromZipTest {

    @Test
    @Disabled
    void testZip() throws IOException {
        // Fails because a ReadOnlyMemDataSource on the zip is created
        try (InputStream is = getClass().getResourceAsStream("/RealGrid-Merged_v3.0.zip")) {
            assertThrows(PowsyblException.class, () -> Network.read("RealGrid-Merged_v3.0.zip", is));
        }

        // Archive with files having the same basename "RealGrid-Merged_v3.0"
        File file = new File(Objects.requireNonNull(getClass().getResource("/RealGrid-Merged_v3.0.zip")).getFile());
        Path path = file.toPath();
        Network network = Network.read(new GenericReadOnlyDataSource(path));
        assertNotNull(network);

        // Archive with files having the same basename "RealGrid-Merged_v3.0"
        File file2 = new File(Objects.requireNonNull(getClass().getResource("/RealGrid-Merged_v3.1.zip")).getFile());
        Path path2 = file2.toPath();
        Network network2 = Network.read(new GenericReadOnlyDataSource(path2));
        assertNotNull(network2);
    }
}
