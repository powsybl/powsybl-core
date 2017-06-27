/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.tools;

import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.iidm.export.Exporters;
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
        options.addOption(Option.builder().longOpt("input-file")
                                .desc("the input file")
                                .hasArg()
                                .argName("INPUT_FILE")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("output-format")
                                .desc("the output file format")
                                .hasArg()
                                .argName("OUTPUT_FORMAT")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("output-file")
                                .desc("the output file")
                                .hasArg()
                                .argName("OUTPUT_FILE")
                                .required()
                                .build());
        options.addOption(Option.builder().longOpt("import-parameters")
                                .desc("the importer configuation file")
                                .hasArg()
                                .argName("IMPORT_PARAMETERS")
                                .build());
        options.addOption(Option.builder("I")
                                .desc("use value for given importer parameter")
                                .argName("property=value")
                                .numberOfArgs(2)
                                .valueSeparator('=')
                                .build());
        options.addOption(Option.builder().longOpt("export-parameters")
                                .desc("the exporter configuration file")
                                .hasArg()
                                .argName("EXPORT_PARAMETERS")
                                .build());
        options.addOption(Option.builder("E")
                                .desc("use value for given exporter parameter")
                                .argName("property=value")
                                .numberOfArgs(2)
                                .valueSeparator('=')
                                .build());

        return options;
    }

    @Override
    public String getUsageFooter() {
        return "Where OUTPUT_FORMAT is one of " + Exporters.getFormats();
    }

}
