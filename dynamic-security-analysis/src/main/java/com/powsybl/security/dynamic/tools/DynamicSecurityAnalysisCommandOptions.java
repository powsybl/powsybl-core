/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.tools;

import com.powsybl.computation.SimpleCommand;
import com.powsybl.computation.SimpleCommandBuilder;
import com.powsybl.security.distributed.AbstractSecurityAnalysisCommandOptions;

import java.nio.file.Path;

import static com.powsybl.security.dynamic.tools.DynamicSecurityAnalysisToolConstants.DYNAMIC_MODELS_FILE_OPTION;
import static com.powsybl.security.dynamic.tools.DynamicSecurityAnalysisToolConstants.EVENT_MODELS_FILE_OPTION;
import static java.util.Objects.requireNonNull;

/**
 * Utility class to programmatically generate an {@literal itools dynamic-security-analysis} command with its various options.
 *
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisCommandOptions extends AbstractSecurityAnalysisCommandOptions<DynamicSecurityAnalysisCommandOptions> {

    private static final String ITOOLS_COMMAND_NAME = "dynamic-security-analysis";
    private Path dynamicModelsFile;
    private Path eventModelsFile;

    public DynamicSecurityAnalysisCommandOptions() {
        super(ITOOLS_COMMAND_NAME);
    }

    public DynamicSecurityAnalysisCommandOptions dynamicModelsFile(Path dynamicModelsFile) {
        this.dynamicModelsFile = requireNonNull(dynamicModelsFile);
        return this;
    }

    public DynamicSecurityAnalysisCommandOptions eventModelsFile(Path eventModelsFile) {
        this.eventModelsFile = requireNonNull(eventModelsFile);
        return this;
    }

    @Override
    public SimpleCommand toCommand() {
        SimpleCommandBuilder commandBuilder = toCommandBuilder()
                .option(DYNAMIC_MODELS_FILE_OPTION, pathToString(dynamicModelsFile));
        setOptionIfPresent(commandBuilder, EVENT_MODELS_FILE_OPTION, eventModelsFile, this::pathToString);
        return commandBuilder.build();
    }

    @Override
    protected String getCommandName() {
        return ITOOLS_COMMAND_NAME;
    }

    @Override
    protected DynamicSecurityAnalysisCommandOptions self() {
        return this;
    }
}
