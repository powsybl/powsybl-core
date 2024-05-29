/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.tools;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.iidm.network.ImportersServiceLoader;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.SecurityAnalysisInput;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.execution.SecurityAnalysisExecutionBuilder;
import com.powsybl.security.execution.SecurityAnalysisExecutionInput;
import com.powsybl.security.execution.SecurityAnalysisInputBuildStrategy;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessorFactory;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessors;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;

import java.util.function.Supplier;

import static com.powsybl.security.tools.SecurityAnalysisToolConstants.DEFAULT_SERVICE_IMPL_NAME_PROPERTY;
import static com.powsybl.security.tools.SecurityAnalysisToolConstants.MODULE_CONFIG_NAME_PROPERTY;
import static com.powsybl.tools.ToolConstants.TASK;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Teofil Calin BANC {@literal <teofil-calin.banc at rte-france.com>}
 */
@AutoService(Tool.class)
public class SecurityAnalysisTool extends AbstractSecurityAnalysisTool<SecurityAnalysisExecutionInput, SecurityAnalysisParameters,
        SecurityAnalysisExecutionBuilder> implements Tool {

    @Override
    public Command getCommand() {
        return new SecurityAnalysisCommand();
    }

    private static SecurityAnalysisInputBuildStrategy configBasedInputBuildStrategy(PlatformConfig config) {
        return preprocessedInputBuildStrategy(() -> LimitViolationFilter.load(config),
            SecurityAnalysisPreprocessors.configuredFactory(config)
                .orElseGet(() -> SecurityAnalysisPreprocessors.wrap(ContingenciesProviders.newDefaultFactory(config))));
    }

    private static SecurityAnalysisInputBuildStrategy preprocessedInputBuildStrategy(Supplier<LimitViolationFilter> filterInitializer,
                                                                                     SecurityAnalysisPreprocessorFactory preprocessorFactory) {
        return executionInput -> buildPreprocessedInput(executionInput, filterInitializer, preprocessorFactory);
    }

    static SecurityAnalysisInput buildPreprocessedInput(SecurityAnalysisExecutionInput executionInput,
                                                        Supplier<LimitViolationFilter> filterInitializer,
                                                        SecurityAnalysisPreprocessorFactory preprocessorFactory) {

        SecurityAnalysisInput input = new SecurityAnalysisInput(executionInput.getNetworkVariant())
            .setParameters(executionInput.getParameters())
            .setFilter(filterInitializer.get());

        executionInput.getResultExtensions().stream()
            .map(SecurityAnalysisInterceptors::createInterceptor)
            .forEach(input::addInterceptor);

        if (!executionInput.getViolationTypes().isEmpty()) {
            input.getFilter().setViolationTypes(ImmutableSet.copyOf(executionInput.getViolationTypes()));
        }

        executionInput.getContingenciesSource()
            .map(preprocessorFactory::newPreprocessor)
            .ifPresent(p -> p.preprocess(input));

        return input;
    }

    private static SecurityAnalysisExecutionBuilder createBuilder(PlatformConfig platformConfig) {
        String providerName = platformConfig.getOptionalModuleConfig(MODULE_CONFIG_NAME_PROPERTY)
                .flatMap(c -> c.getOptionalStringProperty(DEFAULT_SERVICE_IMPL_NAME_PROPERTY))
                .orElse(null);
        return new SecurityAnalysisExecutionBuilder(() -> ExternalSecurityAnalysisConfig.load(platformConfig),
                providerName, configBasedInputBuildStrategy(platformConfig));
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        run(line, context,
            createBuilder(PlatformConfig.defaultConfig()),
            SecurityAnalysisParameters::load,
            new ImportersServiceLoader(),
            TableFormatterConfig::load);
    }

    @Override
    protected SecurityAnalysisExecutionInput getExecutionInput(Network network, Supplier<SecurityAnalysisParameters> parametersLoader) {
        return new SecurityAnalysisExecutionInput()
                .setNetworkVariant(network, VariantManagerConstants.INITIAL_VARIANT_ID)
                .setParameters(parametersLoader.get());
    }

    @Override
    protected Supplier<SecurityAnalysisReport> getReportSupplier(ToolRunningContext context, ToolOptions options, SecurityAnalysisExecutionBuilder executionBuilder,
                                                                 SecurityAnalysisExecutionInput executionInput) {
        ComputationManager computationManager = options.hasOption(TASK) ? context.getShortTimeExecutionComputationManager() :
                context.getLongTimeExecutionComputationManager();
        setupExecutionBuilder(options, executionBuilder);
        return () -> executionBuilder.build().execute(computationManager, executionInput).join();
    }
}
