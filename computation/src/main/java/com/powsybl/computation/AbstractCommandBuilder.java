/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractCommandBuilder<T extends AbstractCommandBuilder<T>> {

    protected String id;

    protected List<InputFile> inputFiles = Collections.emptyList();

    protected List<OutputFile> outputFiles = Collections.emptyList();

    protected AbstractCommandBuilder() {
    }

    public T id(String id) {
        this.id = id;
        return (T) this;
    }

    public T inputFiles(List<InputFile> inputFiles) {
        this.inputFiles = inputFiles;
        return (T) this;
    }

    public T inputFiles(InputFile... inputFiles) {
        return inputFiles(Arrays.asList(inputFiles));
    }

    public T outputFiles(List<OutputFile> outputFiles) {
        this.outputFiles = outputFiles;
        return (T) this;
    }

    public T outputFiles(OutputFile... outputFiles) {
        return outputFiles(Arrays.asList(outputFiles));
    }

}
