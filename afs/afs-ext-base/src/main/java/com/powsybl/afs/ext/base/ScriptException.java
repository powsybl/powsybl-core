/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.ProjectFile;
import com.powsybl.commons.PowsyblException;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ScriptException extends PowsyblException {

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("lang/ScriptException");

    private final String projectFilePath;

    private final ScriptError error;

    public ScriptException(ProjectFile projectFile, ScriptError error) {
        super("");
        this.projectFilePath = Objects.requireNonNull(projectFile).getPath().toString();
        this.error = Objects.requireNonNull(error);
    }

    @Override
    public String getMessage() {
        List<String> lines = new ArrayList<>(4);
        lines.add("\t" + MessageFormat.format(RESOURCE_BUNDLE.getString("ScriptError"), error.getMessage()));
        lines.add("\t" + MessageFormat.format(RESOURCE_BUNDLE.getString("File"), projectFilePath));
        lines.add("\t" + MessageFormat.format(RESOURCE_BUNDLE.getString("Line"), error.getStartLine()));
        if (error.getStartColumn() != -1) {
            lines.add("\t" + MessageFormat.format(RESOURCE_BUNDLE.getString("Column"), error.getStartColumn()));
        }
        return String.join(System.lineSeparator(), lines);
    }

    public String getProjectFilePath() {
        return projectFilePath;
    }

    public ScriptError getError() {
        return error;
    }
}
