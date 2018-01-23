/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ScriptResult {

    private final String output;

    private final ScriptError error;

    private final Object value;

    public ScriptResult(String output, ScriptError error, Object value) {
        this.output = Objects.requireNonNull(output);
        this.error = error;
        this.value = value;
    }

    public String getOutput() {
        return output;
    }

    public ScriptError getError() {
        return error;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ScriptResult(output=" + output + ", error=" + error + ", value=" + value + ")";
    }
}
