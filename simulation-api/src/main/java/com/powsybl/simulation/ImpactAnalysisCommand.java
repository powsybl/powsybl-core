/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.simulation;

import com.powsybl.tools.Command;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImpactAnalysisCommand implements Command {

    static final ImpactAnalysisCommand INSTANCE = new ImpactAnalysisCommand();

    @Override
    public String getName() {
        return "run-impact-analysis";
    }

    @Override
    public String getTheme() {
        return "Computation";
    }

    @Override
    public String getDescription() {
        return "run an impact analysis";
    }

    @Override
    public Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("case-file")
                .desc("the case path")
                .hasArg()
                .argName("FILE")
                .required()
                .build());
        options.addOption(Option.builder().longOpt("contingencies")
                .desc("contingencies to test separated by , (all the db in not set)")
                .hasArg()
                .argName("LIST")
                .build());
        options.addOption(Option.builder().longOpt("output-csv-file")
                .desc("output CSV file path (pretty print on standard output if not specified)")
                .hasArg()
                .argName("FILE")
                .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return null;
    }

}
