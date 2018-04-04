/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.ProjectFile;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ScriptResult<T> {

    private final T value;

    private final String output;

    private final ScriptError error;

    public ScriptResult(T value) {
        this(value, "", null);
    }

    public ScriptResult(T value, String output, ScriptError error) {
        this.value = value;
        this.output = Objects.requireNonNull(output);
        this.error = error;
    }

    public static <T> ScriptResult<T> of(T value) {
        return new ScriptResult<>(value);
    }

    public T getValue() {
        return value;
    }

    public T getValueOrThrowIfError(ProjectFile projectFile) {
        if (error == null) {
            return value;
        }
        throw new ScriptException(projectFile, error);
    }

    public String getOutput() {
        return output;
    }

    public ScriptError getError() {
        return error;
    }

    @Override
    public String toString() {
        return "ScriptResult(output=" + output + ", error=" + error + ", value=" + value + ")";
    }
}
