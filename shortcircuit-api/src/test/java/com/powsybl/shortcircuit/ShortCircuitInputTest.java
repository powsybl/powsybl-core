/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.shortcircuit.json.JsonShortCircuitInput;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ShortCircuitInputTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        ShortCircuitInput input = new ShortCircuitInput();
        List<Fault> faults = new ArrayList<>();
        faults.add(new BranchFault("id", 1.0, 2.0, Fault.ConnectionType.PARALLEL, Fault.FaultType.SINGLE_PHASE, true, true, 3.0));
        faults.add(new BusFault("id", 1.1, 2.2, Fault.ConnectionType.SERIES, Fault.FaultType.TWO_PHASE, true, true));
        input.setFaults(faults);
        roundTripTest(input, JsonShortCircuitInput::write, JsonShortCircuitInput::read,
                "/ShortCircuitInput.json");
    }

    @Test
    public void readError() {
        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unexpected");
        JsonShortCircuitInput.read(getClass().getResourceAsStream("/ShortCircuitInputInvalid.json"));
    }
}
