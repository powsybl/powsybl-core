/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.powsybl.contingency.json.ContingencyJsonModule;
import com.powsybl.security.*;
import com.powsybl.security.action.*;
import com.powsybl.security.condition.Condition;
import com.powsybl.security.json.action.*;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;
import com.powsybl.security.results.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisJsonModule extends ContingencyJsonModule {

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", include = JsonTypeInfo.As.EXISTING_PROPERTY, visible = true)
    interface ActionMixIn {
    }

    public SecurityAnalysisJsonModule() {
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

        setMixInAnnotation(Action.class, ActionMixIn.class);
        registerSubtypes(new NamedType(SwitchAction.class, SwitchAction.NAME));
        registerSubtypes(new NamedType(LineConnectionAction.class, LineConnectionAction.NAME));
        registerSubtypes(new NamedType(MultipleActionsAction.class, MultipleActionsAction.NAME));
        registerSubtypes(new NamedType(PhaseTapChangerTapPositionAction.class, PhaseTapChangerTapPositionAction.NAME));
        addDeserializer(SwitchAction.class, new SwitchActionDeserializer());
        addDeserializer(LineConnectionAction.class, new LineConnectionActionDeserializer());
        addDeserializer(MultipleActionsAction.class, new MultipleActionsActionDeserializer());
        addDeserializer(PhaseTapChangerTapPositionAction.class, new PhaseTapChangerTapPositionActionDeserializer());
        addSerializer(SwitchAction.class, new SwitchActionSerializer());
        addSerializer(LineConnectionAction.class, new LineConnectionActionSerializer());
        addSerializer(MultipleActionsAction.class, new MultipleActionsActionSerializer());
        addSerializer(PhaseTapChangerTapPositionAction.class, new PhaseTapChangerTapPositionActionSerializer());
    }
}
