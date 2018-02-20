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
    private String namePattern;

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
    public String getName() {
        if (namePattern == null) {
            rebuildPattern();
        }
        return namePattern;
    }

    private void rebuildPattern() {
        String str1 = nameFunc.apply(1);
        String str2 = nameFunc.apply(2);
        int idx = -1;
        for (int j = 0; j < str1.length(); j++) {
            if (str1.charAt(j) == '1' && str2.charAt(j) == '2') {
                idx = j;
                String before = str1.substring(0, idx);
                String after = str1.substring(idx + 1, str1.length());
                String pattern = before + Command.EXECUTION_NUMBER_PATTERN + after;
                namePattern = pattern;
                return;
            }
        }
        namePattern = str1;
    }

    @Override
    public boolean dependsOnExecutionNumber() {
        return true;
    }
}
