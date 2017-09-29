/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Command output file.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class OutputFile {

    private final FileName name;

    private final FilePostProcessor postProcessor;

    public static List<OutputFile> of(String... fileNames) {
        List<OutputFile> outputFiles = new ArrayList<>(fileNames.length);
        for (String fileName : fileNames) {
            outputFiles.add(new OutputFile(fileName));
        }
        return outputFiles;
    }

    public OutputFile(String name) {
        this(name, null);
    }

    public OutputFile(String name, FilePostProcessor postProcessor) {
        this.name = new StringFileName(name);
        this.postProcessor = postProcessor;
    }

    public OutputFile(Function<Integer, String> nameFunc, FilePostProcessor postProcessor) {
        this.name = new FunctionFileName(nameFunc, null);
        this.postProcessor = postProcessor;
    }

    public String getName(int executionNumber) {
        return name.getName(executionNumber);
    }

    public FilePostProcessor getPostProcessor() {
        return postProcessor;
    }

    public boolean dependsOnExecutionNumber() {
        return name.dependsOnExecutionNumber();
    }
}
