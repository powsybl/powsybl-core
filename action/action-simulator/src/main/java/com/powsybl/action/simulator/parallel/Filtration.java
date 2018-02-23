/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.simulator.parallel;

import com.powsybl.commons.PowsyblException;

import java.util.Objects;
import java.util.regex.Pattern;

public class Filtration {

    private static final Pattern PATTERN = Pattern.compile("\\d+/\\d+");

    private final Integer x;
    private final Integer y;

    public Filtration(String filtration) {
        Objects.requireNonNull(filtration);

        boolean valid = PATTERN.matcher(filtration).find();
        if (!valid) {
            throw new PowsyblException(filtration + " is not valid");
        }

        String[] split = filtration.split("/");
        x = Integer.valueOf(split[0]);
        y = Integer.valueOf(split[1]);

        if (x > y || x < 1 || y < 1) {
            throw new PowsyblException(filtration + " is not valid");
        }
    }

    int from(int size) {
        checkSize(size);
        return (x - 1) * size / y;
    }

    int to(int size) {
        checkSize(size);
        return x * size / y;
    }

    private void checkSize(int size) {
        if (size < y) {
            throw new PowsyblException("size is smaller than y");
        }
    }
}
