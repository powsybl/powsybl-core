/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisJsonModule extends ContingencyJsonModule {

    private final List<SecurityAnalysisJsonPlugin> plugins;

    public SecurityAnalysisJsonModule() {
        this(getServices());
    }

    private static List<SecurityAnalysisJsonPlugin> getServices() {
        return new ServiceLoaderCache<>(SecurityAnalysisJsonPlugin.class).getServices();
    }

    /**
     * Deserializer for actions will be chosen based on the "type" property.
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
    interface ActionMixIn {
    }

    public SecurityAnalysisJsonModule(Collection<SecurityAnalysisJsonPlugin> plugins) {
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
        addSerializer(SecurityAnalysisParameters.class, new SecurityAnalysisParametersSerializer());
        addSerializer(OperatorStrategyResult.class, new OperatorStrategyResultSerializer());
        addSerializer(OperatorStrategy.class, new OperatorStrategySerializer());
        addSerializer(OperatorStrategyList.class, new OperatorStrategyListSerializer());
        addSerializer(ActionList.class, new ActionListSerializer());
        addSerializer(Condition.class, new ConditionSerializer());

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
        addDeserializer(ActionList.class, new ActionListDeserializer());
        addDeserializer(Condition.class, new ConditionDeserializer());
        addDeserializer(NetworkResult.class, new NetworkResultDeserializer());

        configureActionsSerialization();
    }

    private void configureActionsSerialization() {
        setMixInAnnotation(Action.class, ActionMixIn.class);
        registerActionType(SwitchAction.class, SwitchAction.NAME,
                new SwitchActionSerializer(), new SwitchActionDeserializer());
        registerActionType(LineConnectionAction.class, LineConnectionAction.NAME,
                new LineConnectionActionSerializer(), new LineConnectionActionDeserializer());
        registerActionType(MultipleActionsAction.class, MultipleActionsAction.NAME,
                new MultipleActionsActionSerializer(), new MultipleActionsActionDeserializer());
        registerActionType(PhaseTapChangerTapPositionAction.class, PhaseTapChangerTapPositionAction.NAME,
                new PhaseTapChangerTapPositionActionSerializer(), new PhaseTapChangerTapPositionActionDeserializer());
        registerActionType(GeneratorAction.class, GeneratorAction.NAME,
                new GeneratorActionSerializer(), new GeneratorActionDeserializer());
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
