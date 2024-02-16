/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.contingency.json.ContingencyJsonModule;
import com.powsybl.security.*;
import com.powsybl.security.action.*;
import com.powsybl.security.condition.Condition;
import com.powsybl.security.json.action.*;
import com.powsybl.security.results.*;
import com.powsybl.security.strategy.ConditionalActions;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 * @author Laurent Issertial {@literal <laurent.issertial at rte-france.com>}
 */
public abstract class AbstractSecurityAnalysisJsonModule extends ContingencyJsonModule {

    private final List<SecurityAnalysisJsonPlugin> plugins;

    protected static List<SecurityAnalysisJsonPlugin> getServices() {
        return new ServiceLoaderCache<>(SecurityAnalysisJsonPlugin.class).getServices();
    }

    /**
     * Deserializer for actions will be chosen based on the "type" property.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
    interface ActionMixIn {
    }

    public AbstractSecurityAnalysisJsonModule(Collection<SecurityAnalysisJsonPlugin> plugins) {
        Objects.requireNonNull(plugins);
        this.plugins = List.copyOf(plugins);
        addSerializer(SecurityAnalysisResult.class, new SecurityAnalysisResultSerializer());
        addSerializer(NetworkMetadata.class, new NetworkMetadataSerializer());
        addSerializer(PostContingencyResult.class, new PostContingencyResultSerializer());
        addSerializer(LimitViolationsResult.class, new LimitViolationsResultSerializer());
        addSerializer(LimitViolation.class, new LimitViolationSerializer());
        addSerializer(PreContingencyResult.class, new PreContingencyResultSerializer());
        addSerializer(BusResult.class, new BusResultSerializer());
        addSerializer(BranchResult.class, new BranchResultSerializer());
        addSerializer(ThreeWindingsTransformerResult.class, new ThreeWindingsTransformerResultSerializer());
        addSerializer(OperatorStrategyResult.class, new OperatorStrategyResultSerializer());
        addSerializer(OperatorStrategy.class, new OperatorStrategySerializer());
        addSerializer(OperatorStrategyList.class, new OperatorStrategyListSerializer());
        addSerializer(ConditionalActions.class, new ConditionalActionsSerializer());
        addSerializer(ActionList.class, new ActionListSerializer());
        addSerializer(Condition.class, new ConditionSerializer());
        addSerializer(ConnectivityResult.class, new ConnectivityResultSerializer());

        addDeserializer(SecurityAnalysisResult.class, new SecurityAnalysisResultDeserializer());
        addDeserializer(NetworkMetadata.class, new NetworkMetadataDeserializer());
        addDeserializer(PostContingencyResult.class, new PostContingencyResultDeserializer());
        addDeserializer(LimitViolationsResult.class, new LimitViolationResultDeserializer());
        addDeserializer(LimitViolation.class, new LimitViolationDeserializer());
        addDeserializer(PreContingencyResult.class, new PreContingencyResultDeserializer());
        addDeserializer(BusResult.class, new BusResultDeserializer());
        addDeserializer(BranchResult.class, new BranchResultDeserializer());
        addDeserializer(ThreeWindingsTransformerResult.class, new ThreeWindingsTransformerResultDeserializer());
        addDeserializer(SecurityAnalysisParameters.class, new SecurityAnalysisParametersDeserializer());
        addDeserializer(OperatorStrategyResult.class, new OperatorStrategyResultDeserializer());
        addDeserializer(OperatorStrategy.class, new OperatorStrategyDeserializer());
        addDeserializer(OperatorStrategyList.class, new OperatorStrategyListDeserializer());
        addDeserializer(ConditionalActions.class, new ConditionalActionsDeserializer());
        addDeserializer(ActionList.class, new ActionListDeserializer());
        addDeserializer(Condition.class, new ConditionDeserializer());
        addDeserializer(NetworkResult.class, new NetworkResultDeserializer());
        addDeserializer(ConnectivityResult.class, new ConnectivityResultDeserializer());

        configureActionsSerialization();
    }

    private void configureActionsSerialization() {
        setMixInAnnotation(Action.class, ActionMixIn.class);
        registerActionType(SwitchAction.class, SwitchAction.NAME,
                new SwitchActionSerializer(), new SwitchActionDeserializer());
        registerActionType(TerminalsConnectionAction.class, TerminalsConnectionAction.NAME,
                new TerminalsConnectionActionSerializer(), new TerminalsConnectionActionDeserializer());
        registerActionType(MultipleActionsAction.class, MultipleActionsAction.NAME,
                new MultipleActionsActionSerializer(), new MultipleActionsActionDeserializer());
        registerActionType(PhaseTapChangerTapPositionAction.class, PhaseTapChangerTapPositionAction.NAME,
                new PhaseTapChangerTapPositionActionSerializer(), new PhaseTapChangerTapPositionActionDeserializer());
        registerActionType(RatioTapChangerTapPositionAction.class, RatioTapChangerTapPositionAction.NAME,
                new RatioTapChangerTapPositionActionSerializer(), new RatioTapChangerTapPositionActionDeserializer());
        registerActionType(PhaseTapChangerRegulationAction.class, PhaseTapChangerRegulationAction.NAME,
                new PhaseTapChangerRegulationActionSerializer(), new PhaseTapChangerRegulationActionDeserializer());
        registerActionType(RatioTapChangerRegulationAction.class, RatioTapChangerRegulationAction.NAME,
                new RatioTapChangerRegulationActionSerializer(), new RatioTapChangerRegulationActionDeserializer());
        registerActionType(LoadAction.class, LoadAction.NAME, new LoadActionSerializer(), new LoadActionDeserializer());
        registerActionType(HvdcAction.class, HvdcAction.NAME, new HvdcActionSerializer(), new HvdcActionDeserializer());
        registerActionType(GeneratorAction.class, GeneratorAction.NAME,
                new GeneratorActionSerializer(), new GeneratorActionDeserializer());
        registerActionType(ShuntCompensatorPositionAction.class, ShuntCompensatorPositionAction.NAME,
                new ShuntCompensatorPositionActionSerializer(), new ShuntCompensatorPositionActionDeserializer());
        registerActionType(StaticVarCompensatorAction.class, StaticVarCompensatorAction.NAME,
                new StaticVarCompensatorActionSerializer(), new StaticVarCompensatorActionDeserializer());
    }

    private <T> void registerActionType(Class<T> actionClass, String typeName, JsonSerializer<T> serializer, JsonDeserializer<T> deserializer) {
        registerSubtypes(new NamedType(actionClass, typeName));
        addDeserializer(actionClass, deserializer);
        addSerializer(actionClass, serializer);
    }

    @Override
    public Iterable<? extends Module> getDependencies() {
        return () -> plugins.stream()
                .flatMap(plugin -> plugin.getJsonModules().stream())
                .iterator();
    }
}
