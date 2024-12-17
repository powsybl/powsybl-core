/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.matpower.model;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.eu>}
 */
public final class MatpowerModelFactory {

    private MatpowerModelFactory() {
    }

    private static MatpowerModel readModelJsonFromResources(ObjectMapper mapper, String fileName) {
        MatpowerModel model;
        try (InputStream iStream = MatpowerModelFactory.class.getResourceAsStream("/" + fileName)) {
            model = mapper.readValue(iStream, MatpowerModel.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return model;
    }

    public static MatpowerModel readModelJsonFromResources(String fileName) {
        return readModelJsonFromResources(new ObjectMapper(), fileName);
    }

    public static MatpowerModel create9() {
        return readModelJsonFromResources("ieee9.json");
    }

    public static MatpowerModel create9limits() {
        return readModelJsonFromResources("ieee9_limits.json");
    }

    public static MatpowerModel create14() {
        return readModelJsonFromResources("ieee14.json");
    }

    public static MatpowerModel create14WithPhaseShifter() {
        return readModelJsonFromResources("ieee14-phase-shifter.json");
    }

    public static MatpowerModel create30() {
        return readModelJsonFromResources("ieee30.json");
    }

    public static MatpowerModel create57() {
        return readModelJsonFromResources("ieee57.json");
    }

    public static MatpowerModel create118() {
        return readModelJsonFromResources("ieee118.json");
    }

    public static MatpowerModel create300() {
        return readModelJsonFromResources("ieee300.json");
    }

    public static MatpowerModel create9zeroimpedance() {
        return readModelJsonFromResources("ieee9zeroimpedance.json");
    }

    public static MatpowerModel create9Dcline() {
        return readModelJsonFromResources("t_case9_dcline.json");
    }
}
