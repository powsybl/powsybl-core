/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.shortcircuit.json;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.json.LimitViolationDeserializer;
import com.powsybl.security.json.LimitViolationSerializer;
import com.powsybl.shortcircuit.*;
import com.powsybl.shortcircuit.FaultParameters;

/**
 * @author Boubakeur Brahimi
 */
public class ShortCircuitAnalysisJsonModule extends SimpleModule {

    public ShortCircuitAnalysisJsonModule() {
        addSerializer(FortescueValue.class, new FortescueValuesSerializer());
        addDeserializer(FortescueValue.class, new FortescueValuesDeserializer());
        addSerializer(LimitViolation.class, new LimitViolationSerializer());
        addDeserializer(LimitViolation.class, new LimitViolationDeserializer());
        addSerializer(Fault.class, new FaultSerializer());
        addDeserializer(Fault.class, new FaultDeserializer());
        addSerializer(FaultResult.class, new FaultResultSerializer());
        addSerializer(ShortCircuitAnalysisResult.class, new ShortCircuitAnalysisResultSerializer());
        addDeserializer(ShortCircuitAnalysisResult.class, new ShortCircuitAnalysisResultDeserializer());
        addSerializer(ShortCircuitParameters.class, new ShortCircuitParametersSerializer());
        addDeserializer(ShortCircuitParameters.class, new ShortCircuitParametersDeserializer());
        addSerializer(ShortCircuitBusResults.class, new ShortCircuitBusResultsSerializer());
        addSerializer(FeederResult.class, new FeederResultSerializer());
        addDeserializer(FeederResult.class, new FeederResultDeserializer());
        addSerializer(FaultParameters.class, new FaultParametersSerializer());
        addDeserializer(FaultParameters.class, new FaultParametersDeserializer());
        addSerializer(VoltageRange.class, new VoltageRangeSerializer());
        addDeserializer(VoltageRange.class, new VoltageRangeDeserializer());
    }
}
