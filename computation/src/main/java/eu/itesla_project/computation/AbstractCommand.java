/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.computation;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
abstract class AbstractCommand implements Command {

    protected final String id;

    protected final List<InputFile> inputFiles;

    protected final List<OutputFile> outputFiles;

    protected AbstractCommand(String id, List<InputFile> inputFiles, List<OutputFile> outputFiles) {
        this.id = id;
        this.inputFiles = inputFiles;
        this.outputFiles = outputFiles;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public List<InputFile> getInputFiles() {
        return inputFiles;
    }

    @Override
    public List<InputFile> getInputFiles(final String executionNumber) {
        return Lists.transform(inputFiles, new Function<InputFile, InputFile>() {
            @Override
            public InputFile apply(InputFile file) {
                return file.instanciate(executionNumber);
            }
        });
    }

    @Override
    public List<OutputFile> getOutputFiles() {
        return outputFiles;
    }

    @Override
    public List<OutputFile> getOutputFiles(final String executionNumber) {
        return Lists.transform(outputFiles, new Function<OutputFile, OutputFile>() {
            @Override
            public OutputFile apply(OutputFile file) {
                return file.instanciate(executionNumber);
            }
        });
    }

}
