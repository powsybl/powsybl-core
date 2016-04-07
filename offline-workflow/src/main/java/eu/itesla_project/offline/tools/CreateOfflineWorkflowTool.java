/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline.tools;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.tools.Command;
import eu.itesla_project.commons.tools.Tool;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.modules.offline.OfflineWorkflowCreationParameters;
import eu.itesla_project.offline.OfflineApplication;
import eu.itesla_project.offline.RemoteOfflineApplicationImpl;
import org.apache.commons.cli.CommandLine;
import org.joda.time.DateTime;
import org.joda.time.Interval;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(Tool.class)
public class CreateOfflineWorkflowTool implements Tool {

    private OfflineWorkflowCreationParameters defaultParameters;
    
    @Override
    public Command getCommand() {
        return CreateOfflineWorkflowCommand.INSTANCE;
    }

    private OfflineWorkflowCreationParameters getDefaultParameters() {
        if (defaultParameters == null) {
            defaultParameters = OfflineWorkflowCreationParameters.load();
        }
        return defaultParameters;
    }
    
    @Override
    public void run(CommandLine line) throws Exception {
        String workflowId = line.getOptionValue("workflow");
        Set<Country> countries = line.hasOption("base-case-countries")
                ? Arrays.stream(line.getOptionValue("base-case-countries").split(",")).map(Country::valueOf).collect(Collectors.toSet())
                : getDefaultParameters().getCountries();
        DateTime baseCaseDate = line.hasOption("base-case-date")
                ? DateTime.parse(line.getOptionValue("base-case-date"))
                : getDefaultParameters().getBaseCaseDate();
        Interval histoInterval = line.hasOption("history-interval")
                ? Interval.parse(line.getOptionValue("history-interval"))
                : getDefaultParameters().getHistoInterval();
        boolean generationSampled = line.hasOption("generation-sampled") || getDefaultParameters().isGenerationSampled();
        boolean boundariesSampled = line.hasOption("boundaries-sampled") || getDefaultParameters().isBoundariesSampled();
        boolean initTopo = line.hasOption("topo-init") || getDefaultParameters().isInitTopo();
        double correlationThreshold = line.hasOption("correlation-threshold") ? Double.parseDouble(line.getOptionValue("correlation-threshold"))
                                                                              : getDefaultParameters().getCorrelationThreshold();
        double probabilityThreshold = line.hasOption("probability-threshold") ? Double.parseDouble(line.getOptionValue("probability-threshold"))
                                                                              : getDefaultParameters().getProbabilityThreshold();
        boolean loadFlowTransformerVoltageControlOn = line.hasOption("loadflow-transformer-voltage-control-on") || getDefaultParameters().isLoadFlowTransformerVoltageControlOn();
        boolean simplifiedWorkflow = line.hasOption("simplified-workflow") || getDefaultParameters().isSimplifiedWorkflow();
        boolean mergeOptimized = line.hasOption("merge-optimized") || getDefaultParameters().isMergeOptimized();
        Set<Country> attributesCountryFilter = line.hasOption("attributes-country-filter")
                ? Arrays.stream(line.getOptionValue("attributes-country-filter").split(",")).map(Country::valueOf).collect(Collectors.toSet())
                : getDefaultParameters().getAttributesCountryFilter();
        int attributesMinBaseVoltageFilter = line.hasOption("attributes-min-base-voltage-filter")
                ? Integer.parseInt(line.getOptionValue("attributes-min-base-voltage-filter"))
                : getDefaultParameters().getAttributesMinBaseVoltageFilter();

        OfflineWorkflowCreationParameters parameters = new OfflineWorkflowCreationParameters(countries,
                                                                                             baseCaseDate,
                                                                                             histoInterval,
                                                                                             generationSampled,
                                                                                             boundariesSampled,
                                                                                             initTopo,
                                                                                             correlationThreshold,
                                                                                             probabilityThreshold,
                                                                                             loadFlowTransformerVoltageControlOn,
                                                                                             simplifiedWorkflow,
                                                                                             mergeOptimized,
                                                                                             attributesCountryFilter,
                                                                                             attributesMinBaseVoltageFilter);
        parameters.print(System.out);
        try (OfflineApplication app = new RemoteOfflineApplicationImpl()) {
            String workflowId2 = app.createWorkflow(workflowId, parameters);
            System.out.println("offline workflow '" + workflowId2 + "' created");
        }
    }

}
