/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class StringFileName implements FileName {

    private final String name;

    public StringFileName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    private static String executionNumberToString(int executionNumber) {
        return executionNumber == -1 ? "*" : Integer.toString(executionNumber);
    }

    @Override
    public String getName(int executionNumber) {
        return name.replace(CommandConstants.EXECUTION_NUMBER_PATTERN, executionNumberToString(executionNumber));
    }

    @Override
    public boolean dependsOnExecutionNumber() {
        return name.contains(CommandConstants.EXECUTION_NUMBER_PATTERN);
    }
}
