/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.iidm.import_.Importers;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EurostagExportCommand implements Command {

    static final EurostagExportCommand INSTANCE = new EurostagExportCommand();

    @Override
    public String getName() {
        return "export-eurostag";
    }

    @Override
    public String getTheme() {
        return "Eurostag";
    }

    @Override
    public String getDescription() {
        return "Eurostag data export";
    }

    @Override
    @SuppressWarnings("static-access")
    public Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("case-format")
                                .desc("the case format")
                                .hasArg()
                                .argName("FORMAT")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("case-dir")
                                .desc("the directory where the case is")
                                .hasArg()
                                .argName("DIR")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("case-basename")
                                .desc("the case base name")
                                .hasArg()
                                .argName("NAME")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("output-dir")
                                .desc("output directory path")
                                .hasArg()
                                .argName("DIR")
                                .required()
                                .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return "Where FORMAT is one of " + Importers.getFormats();
    }

}
