/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.shortcircuit.BranchFault;
import com.powsybl.shortcircuit.BusFault;
import com.powsybl.shortcircuit.Fault;
import org.junit.Test;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class JsonShortCircuitInputTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        List<Fault> faults = new ArrayList<>();
        faults.add(new BranchFault("id", 1.0, 2.0, Fault.ConnectionType.PARALLEL, Fault.FaultType.SINGLE_PHASE, 3.0));
        faults.add(new BusFault("id", 1.1, 2.2, Fault.ConnectionType.SERIES, Fault.FaultType.THREE_PHASE));
        roundTripTest(faults, Fault::write, Fault::read,
                "/FaultsFile.json");
    }

    @Test
    public void readUnexpectedField() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultsFileInvalid.json"), fileSystem.getPath("/FaultsFileInvalid.json"));

        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unexpected");
        Fault.read(fileSystem.getPath("/FaultsFileInvalid.json"));
    }

    @Test
    public void readNoType() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultsFileNoType.json"), fileSystem.getPath("/FaultsFileNoType.json"));

        expected.expect(AssertionError.class);
        expected.expectMessage("Required type field is missing");
        Fault.read(fileSystem.getPath("/FaultsFileNoType.json"));
    }
}
