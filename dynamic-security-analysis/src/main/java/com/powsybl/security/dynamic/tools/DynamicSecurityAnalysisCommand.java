/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.tools;

import com.powsybl.security.tools.SecurityAnalysisCommand;
import com.powsybl.tools.Command;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import static com.powsybl.security.dynamic.tools.DynamicSecurityAnalysisToolConstants.DYNAMIC_MODELS_FILE_OPTION;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public class DynamicSecurityAnalysisCommand extends SecurityAnalysisCommand implements Command {

    @Override
    public String getName() {
        return "dynamic-security-analysis";
    }

    @Override
    public String getDescription() {
        return "Run dynamic security analysis";
    }

    @Override
    public Options getOptions() {

        Options options = super.getOptions();
        options.addOption(Option.builder().longOpt(DYNAMIC_MODELS_FILE_OPTION)
                .desc("dynamic models description as a Groovy file: defines the dynamic models to be associated to chosen equipments of the network")
                .hasArg()
                .argName("FILE")
                .required()
                .build());
        return options;
    }
}
