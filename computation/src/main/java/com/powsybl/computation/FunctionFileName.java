/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class FunctionFileName implements FileName {

    private final Function<Integer, String> nameFunc;

    private final Consumer<String> validator;

    public FunctionFileName(Function<Integer, String> nameFunc, Consumer<String> validator) {
        this.nameFunc = Objects.requireNonNull(nameFunc);
        this.validator = validator;
    }

    @Override
    public String getName(int executionNumber) {
        String name = nameFunc.apply(executionNumber);
        if (validator != null) {
            validator.accept(name);
        }
        return name;
    }

    @Override
    public boolean dependsOnExecutionNumber() {
        return true;
    }
}
