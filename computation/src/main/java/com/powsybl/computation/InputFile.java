/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.io.File;
import java.util.Objects;
import java.util.function.Function;

/**
 * Command input file.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class InputFile {

    private final FileName name;

    private final FilePreProcessor preProcessor;

    public InputFile(String name) {
        this(name, null);
    }

    public InputFile(String name, FilePreProcessor preProcessor) {
        this.name = new StringFileName(checkName(name, preProcessor));
        this.preProcessor = preProcessor;
    }

    public InputFile(Function<Integer, String> nameFunc, FilePreProcessor preProcessor) {
        this.name = new FunctionFileName(nameFunc, fileName -> checkName(fileName, preProcessor));
        this.preProcessor = preProcessor;
    }

    public static String checkName(String name, FilePreProcessor preProcessor) {
        Objects.requireNonNull(name, "name is null");
        if (name.contains(File.separator)) {
            throw new IllegalArgumentException("input file name must not contain directory path");
        }
        if (preProcessor != null) {
            switch (preProcessor) {
                case FILE_GUNZIP:
                    if (!name.endsWith(".gz")) {
                        throw new IllegalArgumentException(name + " is expected to end with .gz");
                    }
                    break;
                case ARCHIVE_UNZIP:
                    if (!name.endsWith(".zip")) {
                        throw new IllegalArgumentException(name + " is expected to end with .zip");
                    }
                    break;
                default:
                    throw new AssertionError("Unexpected FilePreProcessor value: " + preProcessor);
            }
        }
        return name;
    }

    public String getName(int executionNumber) {
        return name.getName(executionNumber);
    }

    public FilePreProcessor getPreProcessor() {
        return preProcessor;
    }

    public boolean dependsOnExecutionNumber() {
        return name.dependsOnExecutionNumber();
    }
}
