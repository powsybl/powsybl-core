/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.dynamic.tools;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.io.FileUtil;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.computation.ComputationManager;
import com.powsybl.contingency.ContingenciesProviders;
import com.powsybl.dynamicsimulation.DynamicModelsSupplier;
import com.powsybl.dynamicsimulation.groovy.DynamicSimulationSupplierFactory;
import com.powsybl.iidm.network.ImportersServiceLoader;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VariantManagerConstants;
import com.powsybl.security.LimitViolationFilter;
import com.powsybl.security.SecurityAnalysisReport;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisInput;
import com.powsybl.security.dynamic.DynamicSecurityAnalysisParameters;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecutionBuilder;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisExecutionInput;
import com.powsybl.security.dynamic.execution.DynamicSecurityAnalysisInputBuildStrategy;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptors;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessorFactory;
import com.powsybl.security.preprocessor.SecurityAnalysisPreprocessors;
import com.powsybl.security.tools.AbstractSecurityAnalysisTool;
import com.powsybl.tools.Command;
import com.powsybl.tools.Tool;
import com.powsybl.tools.ToolOptions;
import com.powsybl.tools.ToolRunningContext;
import org.apache.commons.cli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

import static com.powsybl.security.dynamic.tools.DynamicSecurityAnalysisToolConstants.EVENT_MODELS_FILE_OPTION;
import static com.powsybl.security.tools.SecurityAnalysisToolConstants.DEFAULT_SERVICE_IMPL_NAME_PROPERTY;
import static com.powsybl.security.tools.SecurityAnalysisToolConstants.PARAMETERS_FILE_OPTION;
import static com.powsybl.tools.ToolConstants.TASK;

/**
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
@AutoService(Tool.class)
public class DynamicSecurityAnalysisTool extends AbstractSecurityAnalysisTool<DynamicSecurityAnalysisExecutionInput,
        DynamicSecurityAnalysisExecutionBuilder> implements Tool {

    @Override
    public Command getCommand() {
        return new DynamicSecurityAnalysisCommand();
    }

    private static DynamicSecurityAnalysisInputBuildStrategy configBasedInputBuildStrategy(PlatformConfig config) {
        return preprocessedInputBuildStrategy(() -> LimitViolationFilter.load(config),
            SecurityAnalysisPreprocessors.configuredFactory(config)
                .orElseGet(() -> SecurityAnalysisPreprocessors.wrap(ContingenciesProviders.newDefaultFactory(config))));
    }

    private static DynamicSecurityAnalysisInputBuildStrategy preprocessedInputBuildStrategy(Supplier<LimitViolationFilter> filterInitializer,
                                                                                     SecurityAnalysisPreprocessorFactory preprocessorFactory) {
        return (executionInput, providerName) -> buildPreprocessedInput(executionInput, providerName, filterInitializer, preprocessorFactory);
    }

    static DynamicSecurityAnalysisInput buildPreprocessedInput(DynamicSecurityAnalysisExecutionInput executionInput,
                                                               String providerName,
                                                               Supplier<LimitViolationFilter> filterInitializer,
                                                               SecurityAnalysisPreprocessorFactory preprocessorFactory) {

        DynamicModelsSupplier dynamicModelsSupplier;
        try (InputStream is = executionInput.getDynamicModelsSource().openBufferedStream()) {
            dynamicModelsSupplier = DynamicSimulationSupplierFactory.createDynamicModelsSupplier(is, providerName);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        DynamicSecurityAnalysisInput input = new DynamicSecurityAnalysisInput(executionInput.getNetworkVariant(), dynamicModelsSupplier)
            .setParameters(executionInput.getParameters())
            .setFilter(filterInitializer.get());

        executionInput.getResultExtensions().stream()
            .map(SecurityAnalysisInterceptors::createInterceptor)
            .forEach(input::addInterceptor);

        if (!executionInput.getViolationTypes().isEmpty()) {
            input.getFilter().setViolationTypes(ImmutableSet.copyOf(executionInput.getViolationTypes()));
        }

        executionInput.getEventModelsSource().ifPresent(
            es -> {
                try (InputStream is = es.openBufferedStream()) {
                    input.setEventModelsSupplier(DynamicSimulationSupplierFactory.createEventModelsSupplier(is, providerName));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        );

        executionInput.getContingenciesSource()
            .map(preprocessorFactory::newPreprocessor)
            .ifPresent(p -> p.preprocess(input));

        return input;
    }

    private static DynamicSecurityAnalysisExecutionBuilder createBuilder(PlatformConfig platformConfig) {
        String providerName = platformConfig.getOptionalModuleConfig(DynamicSecurityAnalysisToolConstants.MODULE_CONFIG_NAME_PROPERTY)
                .flatMap(c -> c.getOptionalStringProperty(DEFAULT_SERVICE_IMPL_NAME_PROPERTY))
                .orElse(null);
        return new DynamicSecurityAnalysisExecutionBuilder(() -> ExternalSecurityAnalysisConfig.load(platformConfig),
                providerName, configBasedInputBuildStrategy(platformConfig));
    }

    @Override
    public void run(CommandLine line, ToolRunningContext context) throws Exception {
        run(line, context,
            createBuilder(PlatformConfig.defaultConfig()),
            new ImportersServiceLoader(),
            TableFormatterConfig::load);
    }

    @Override
    protected DynamicSecurityAnalysisExecutionInput getExecutionInput(Network network) {
        return new DynamicSecurityAnalysisExecutionInput()
                .setNetworkVariant(network, VariantManagerConstants.INITIAL_VARIANT_ID)
                .setParameters(DynamicSecurityAnalysisParameters.load());
    }

    @Override
    public void updateInput(ToolOptions options, DynamicSecurityAnalysisExecutionInput inputs) {
        super.updateInput(options, inputs);
        options.getPath(PARAMETERS_FILE_OPTION)
                .ifPresent(f -> inputs.getParameters().update(f));
        options.getPath(EVENT_MODELS_FILE_OPTION)
                .map(FileUtil::asByteSource)
                .ifPresent(inputs::setEventModelsSource);
    }

    @Override
    protected Supplier<SecurityAnalysisReport> getReportSupplier(ToolRunningContext context, ToolOptions options, DynamicSecurityAnalysisExecutionBuilder executionBuilder,
                                                                                    DynamicSecurityAnalysisExecutionInput executionInput) {
        ComputationManager computationManager = options.hasOption(TASK) ? context.getShortTimeExecutionComputationManager() :
                context.getLongTimeExecutionComputationManager();
        setupExecutionBuilder(options, executionBuilder);
        return () -> executionBuilder.build().execute(computationManager, executionInput).join();
    }
}
