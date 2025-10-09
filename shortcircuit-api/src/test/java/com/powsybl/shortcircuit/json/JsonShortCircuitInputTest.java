/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.shortcircuit.BranchFault;
import com.powsybl.shortcircuit.BusFault;
import com.powsybl.shortcircuit.Fault;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Thomas Adam {@literal <tadam at silicom.fr>}
 */
class JsonShortCircuitInputTest extends AbstractSerDeTest {

    @Test
    void roundTrip() throws IOException {
        List<Fault> faults = new ArrayList<>();
        faults.add(new BranchFault("F1", "branchId", 1.0, 2.0, Fault.ConnectionType.PARALLEL, Fault.FaultType.SINGLE_PHASE, 3.0));
        faults.add(new BusFault("F2", "busId", 1.1, 2.2, Fault.ConnectionType.SERIES, Fault.FaultType.THREE_PHASE));
        roundTripTest(faults, Fault::write, Fault::read,
                "/FaultsFile.json");
    }

    @Test
    void readUnexpectedField() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultsFileInvalid.json"), fileSystem.getPath("/FaultsFileInvalid.json"));

        Path path = fileSystem.getPath("/FaultsFileInvalid.json");
        UncheckedIOException e = assertThrows(UncheckedIOException.class, () -> Fault.read(path));
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: Unexpected field: unexpected (through reference chain: java.util.ArrayList[0])", e.getMessage());
    }

    @Test
    void readNoType() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultsFileNoType.json"), fileSystem.getPath("/FaultsFileNoType.json"));

        Path path = fileSystem.getPath("/FaultsFileNoType.json");
        UncheckedIOException e = assertThrows(UncheckedIOException.class, () -> Fault.read(path));
        assertEquals("com.fasterxml.jackson.databind.JsonMappingException: Required type field is missing (through reference chain: java.util.ArrayList[0])", e.getMessage());
    }
}
