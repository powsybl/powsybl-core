/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.modules.histo.HistoDbAttr;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.HistoDbClient;
import eu.itesla_project.modules.histo.HistoDbNetworkAttributeId;
import eu.itesla_project.modules.offline.OfflineConfig;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class HistoDbListAttributesTool implements Tool {

    @Override
    public Command getCommand() {
        return new Command() {
            @Override
            public String getName() {
                return "histodb-list-attributes";
            }

            @Override
            public String getTheme() {
                return "Histo DB";
            }

            @Override
            public String getDescription() {
                return "list Histo DB attributes";
            }

            @Override
            @SuppressWarnings("static-access")
            public Options getOptions() {
                Options options = new Options();
                options.addOption(Option.builder().longOpt("attribute-types")
                        .desc("attribute type list separated by a coma")
                        .hasArg()
                        .argName("ATTRIBUTE_TYPE1,ATTRIBUTE_TYPE2,...")
                        .build());
                return options;
            }

            @Override
            public String getUsageFooter() {
                return "Where ATTRIBUTE_TYPE is one of " + Arrays.toString(HistoDbAttr.values());
            }
        };
    }

    @Override
    public void run(CommandLine line) throws Exception {
        OfflineConfig config = OfflineConfig.load();
        Set<HistoDbAttr> types = null;
        if (line.hasOption("attribute-types")) {
            types = EnumSet.noneOf(HistoDbAttr.class);
            for (String str : line.getOptionValue("attribute-types").split(",")) {
                types.add(HistoDbAttr.valueOf(str));
            }
        }
        try (HistoDbClient histoDbClient = config.getHistoDbClientFactoryClass().newInstance().create()) {
            for (HistoDbAttributeId attributeId : histoDbClient.listAttributes()) {
                if (types == null
                        || (attributeId instanceof HistoDbNetworkAttributeId
                        && types.contains(((HistoDbNetworkAttributeId) attributeId).getAttributeType()))) {
                    System.out.println(attributeId);
                }
            }
        }
    }

}
