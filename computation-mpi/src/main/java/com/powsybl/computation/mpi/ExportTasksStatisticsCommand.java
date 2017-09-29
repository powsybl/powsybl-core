/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.mpi;

import com.powsybl.tools.Command;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ExportTasksStatisticsCommand implements Command {

    public static final ExportTasksStatisticsCommand INSTANCE = new ExportTasksStatisticsCommand();

    @Override
    public String getName() {
        return "export-tasks-statistics";
    }

    @Override
    public String getTheme() {
        return "MPI statistics";
    }

    @Override
    public String getDescription() {
        return "export tasks statistics to CSV";
    }

    @Override
    @SuppressWarnings("static-access")
    public Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("statistics-db-dir")
                                .desc("statistics db directory")
                                .hasArg()
                                .argName("DIR")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("statistics-db-name")
                                .desc("statistics db name")
                                .hasArg()
                                .argName("NAME")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("output-file")
                                .desc("CSV output file")
                                .hasArg()
                                .argName("FILE")
                                .required()
                                .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return null;
    }

}
