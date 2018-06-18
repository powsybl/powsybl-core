/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.powsybl.commons.PowsyblException;

import java.util.Objects;
import java.util.regex.Pattern;

public class Partition {

    private static final Pattern PATTERN = Pattern.compile("\\d+/\\d+");

    private final int indexOfPartition;
    private final int ntasks;

    public Partition(String partition) {
        Objects.requireNonNull(partition);

        boolean valid = PATTERN.matcher(partition).find();
        if (!valid) {
            throw new PowsyblException(partition + " is not valid");
        }

        String[] split = partition.split("/");
        indexOfPartition = Integer.valueOf(split[0]);
        ntasks = Integer.valueOf(split[1]);

        if (indexOfPartition > ntasks || indexOfPartition < 1 || ntasks < 1) {
            throw new PowsyblException(partition + " is not valid");
        }
    }

    public int startIndex(int size) {
        checkSize(size);
        return (indexOfPartition - 1) * size / ntasks;
    }

    public int endIndex(int size) {
        checkSize(size);
        return indexOfPartition * size / ntasks;
    }

    private void checkSize(int size) {
        if (size < ntasks) {
            throw new PowsyblException("size is smaller than ntasks");
        }
    }
}
