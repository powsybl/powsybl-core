/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.json;

import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.shortcircuit.FaultParameters;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class JsonFaultParametersTest extends AbstractConverterTest {

    @Test
    public void roundTrip() throws IOException {
        List<FaultParameters> parameters = new ArrayList<>();
        parameters.add(new FaultParameters("f00", false, false));
        parameters.add(new FaultParameters("f01", false, true));
        parameters.add(new FaultParameters("f10", true, false));
        parameters.add(new FaultParameters("f11", true, true));
        roundTripTest(parameters, FaultParameters::write, FaultParameters::read, "/FaultParametersFile.json");
    }

    @Test
    public void readUnexpectedField() throws IOException {
        Files.copy(getClass().getResourceAsStream("/FaultParametersFileInvalid.json"), fileSystem.getPath("/FaultParametersFileInvalid.json"));

        expected.expect(AssertionError.class);
        expected.expectMessage("Unexpected field: unexpected");
        FaultParameters.read(fileSystem.getPath("/FaultParametersFileInvalid.json"));
    }
}
