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

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class Partition {

    private static final Pattern PATTERN = Pattern.compile("\\d+/\\d+");

    private final int taskIndex;

    private final int taskCount;

    public static Partition parse(String partition) {
        Objects.requireNonNull(partition);

        boolean valid = PATTERN.matcher(partition).find();
        if (!valid) {
            throw new PowsyblException(partition + " is not valid");
        }

        String[] split = partition.split("/");
        int taskIndex = Integer.parseInt(split[0]);
        int taskCount = Integer.parseInt(split[1]);

        return new Partition(taskIndex, taskCount);
    }

    public Partition(int taskIndex, int taskCount) {
        if (taskIndex > taskCount || taskIndex < 1) {
            throw new PowsyblException(toString(taskIndex, taskCount) + " is not valid");
        }

        this.taskIndex = taskIndex;
        this.taskCount = taskCount;
    }

    public int startIndex(int size) {
        checkSize(size);
        return (taskIndex - 1) * size / taskCount;
    }

    public int endIndex(int size) {
        checkSize(size);
        return taskIndex * size / taskCount;
    }

    private void checkSize(int size) {
        if (size < taskCount) {
            throw new PowsyblException("Data size must be greater than task count");
        }
    }

    @Override
    public String toString() {
        return toString(taskIndex, taskCount);
    }

    private static String toString(int taskIndex, int taskCount) {
        return Integer.toString(taskIndex) + "/" + taskCount;
    }
}
