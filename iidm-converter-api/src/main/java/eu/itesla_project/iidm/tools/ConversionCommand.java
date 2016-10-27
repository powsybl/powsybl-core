/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.tools;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.iidm.export.Exporters;
import eu.itesla_project.iidm.import_.Importers;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ConversionCommand implements Command {

    public static final ConversionCommand INSTANCE = new ConversionCommand();

    @Override
    public String getName() {
        return "convert-network";
    }

    @Override
    public String getTheme() {
        return "Data conversion";
    }

    @Override
    public String getDescription() {
        return "convert a network from one format to another";
    }

    @Override
    @SuppressWarnings("static-access")
    public Options getOptions() {
        Options options = new Options();
        options.addOption(Option.builder().longOpt("source")
                                .desc("the source format")
                                .hasArg()
                                .argName("SOURCE_FORMAT")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("target")
                                .desc("the target format")
                                .hasArg()
                                .argName("TARGET_FORMAT")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("input-dir")
                                .desc("the input directory for input files")
                                .hasArg()
                                .argName("DIR")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("input-basename")
                                .desc("the base name for input files")
                                .hasArg()
                                .argName("NAME")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("output-dir")
                                .desc("the output directory for generated files")
                                .hasArg()
                                .argName("DIR")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("output-basename")
                                .desc("the base name for generated files")
                                .hasArg()
                                .argName("NAME")
                                .required()
                                .build());
        return options;
    }

    @Override
    public String getUsageFooter() {
        return "Where SOURCE_FORMAT is one of " + Importers.getFormats()
                + " and TARGET_FORMAT is one of " + Exporters.getFormats();
    }

}
