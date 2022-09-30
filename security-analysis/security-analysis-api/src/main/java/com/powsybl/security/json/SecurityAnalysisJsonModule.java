/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.powsybl.contingency.json.ContingencyJsonModule;
import com.powsybl.security.*;
import com.powsybl.security.action.Action;
import com.powsybl.security.action.ActionList;
import com.powsybl.security.condition.Condition;
import com.powsybl.security.strategy.OperatorStrategy;
import com.powsybl.security.strategy.OperatorStrategyList;
import com.powsybl.security.results.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisJsonModule extends ContingencyJsonModule {

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
        addSerializer(Action.class, new ActionSerializer());
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
        addDeserializer(Action.class, new ActionDeserializer());
        addDeserializer(ActionList.class, new ActionListDeserializer());
        addDeserializer(Condition.class, new ConditionDeserializer());
        addDeserializer(NetworkResult.class, new NetworkResultDeserializer());
    }
}
