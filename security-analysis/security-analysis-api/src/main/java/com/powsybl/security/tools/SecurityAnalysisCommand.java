/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.tools;

import com.powsybl.security.LimitViolationType;
import com.powsybl.security.converter.SecurityAnalysisResultExporters;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import com.powsybl.tools.Command;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Arrays;

import static com.powsybl.iidm.network.tools.ConversionToolUtils.createImportParameterOption;
import static com.powsybl.iidm.network.tools.ConversionToolUtils.createImportParametersFileOption;
import static com.powsybl.security.tools.SecurityAnalysisToolConstants.*;
import static com.powsybl.security.tools.SecurityAnalysisToolConstants.MONITORING_FILE;
import static com.powsybl.tools.ToolConstants.TASK;
import static com.powsybl.tools.ToolConstants.TASK_COUNT;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class SecurityAnalysisCommand implements Command {

    @Override
    public String getName() {
        return "security-analysis";
    }

    @Override
    public String getTheme() {
        return "Computation";
    }

    @Override
    public String getDescription() {
        return "Run security analysis";
    }

    @Override
    public Options getOptions() {

        Options options = new Options();
        options.addOption(Option.builder().longOpt(CASE_FILE_OPTION)
                .desc("the case path")
                .hasArg()
                .argName("FILE")
                .required()
                .build());
        options.addOption(Option.builder().longOpt(PARAMETERS_FILE_OPTION)
                .desc("loadflow parameters as JSON file")
                .hasArg()
                .argName("FILE")
                .build());
        options.addOption(Option.builder().longOpt(LIMIT_TYPES_OPTION)
                .desc("limit type filter (all if not set)")
                .hasArg()
                .argName("LIMIT-TYPES")
                .build());
        options.addOption(Option.builder().longOpt(OUTPUT_FILE_OPTION)
                .desc("the output path")
                .hasArg()
                .argName("FILE")
                .build());
        options.addOption(Option.builder().longOpt(OUTPUT_FORMAT_OPTION)
                .desc("the output format " + SecurityAnalysisResultExporters.getFormats())
                .hasArg()
                .argName("FORMAT")
                .build());
        options.addOption(Option.builder().longOpt(CONTINGENCIES_FILE_OPTION)
                .desc("the contingencies path")
                .hasArg()
                .argName("FILE")
                .build());
        options.addOption(Option.builder().longOpt(WITH_EXTENSIONS_OPTION)
                .desc("the extension list to enable")
                .hasArg()
                .argName("EXTENSIONS")
                .build());
        options.addOption(Option.builder().longOpt(TASK_COUNT)
                .desc("number of tasks used for parallelization")
                .hasArg()
                .argName("NTASKS")
                .build());
        options.addOption(Option.builder().longOpt(TASK)
                .desc("task identifier (task-index/task-count)")
                .hasArg()
                .argName("TASKID")
                .build());
        options.addOption(Option.builder().longOpt(EXTERNAL)
                .desc("external execution")
                .build());
        options.addOption(createImportParametersFileOption());
        options.addOption(createImportParameterOption());
        options.addOption(Option.builder().longOpt(OUTPUT_LOG_OPTION)
                .desc("log output path (.zip)")
                .hasArg()
                .argName("FILE")
                .build());
        options.addOption(Option.builder().longOpt(MONITORING_FILE)
                .desc("monitoring file (.json) to get network's infos after computation")
                .hasArg()
                .argName("FILE")
                .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return String.join(System.lineSeparator(),
                "Allowed LIMIT-TYPES values are " + Arrays.toString(LimitViolationType.values()),
                "Allowed EXTENSIONS values are " + SecurityAnalysisInterceptors.getExtensionNames()
        );
    }
}
