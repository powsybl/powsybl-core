/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.shortcircuit.option.FaultOptions;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class JsonFaultOptionsTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        List<FaultOptions> options = new ArrayList<>();
        options.add(new FaultOptions("f00", false, false));
        options.add(new FaultOptions("f01", false, true));
        options.add(new FaultOptions("f10", true, false));
        options.add(new FaultOptions("f11", true, true));
        roundTripTest(options, FaultOptions::write, FaultOptions::read, "/FaultOptionsFile.json");
    }

    @Test
    public void readUnexpectedField() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultOptionsFileInvalid.json"), fileSystem.getPath("/FaultOptionsFileInvalid.json"));

        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unexpected");
        FaultOptions.read(fileSystem.getPath("/FaultOptionsFileInvalid.json"));
    }
}
