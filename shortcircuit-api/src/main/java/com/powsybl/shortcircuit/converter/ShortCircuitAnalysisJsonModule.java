/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit.converter;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.powsybl.security.LimitViolation;
import com.powsybl.security.NetworkMetadata;
import com.powsybl.security.json.LimitViolationDeserializer;
import com.powsybl.security.json.LimitViolationSerializer;
import com.powsybl.security.json.NetworkMetadataDeserializer;
import com.powsybl.security.json.NetworkMetadataSerializer;
import com.powsybl.shortcircuit.Fault;
import com.powsybl.shortcircuit.FaultResult;
import com.powsybl.shortcircuit.ShortCircuitAnalysisResult;
import com.powsybl.shortcircuit.ShortCircuitParameters;
import com.powsybl.shortcircuit.json.ShortCircuitParametersDeserializer;
import com.powsybl.shortcircuit.json.ShortCircuitParametersSerializer;

/**
 * @author Boubakeur Brahimi
 */
public class ShortCircuitAnalysisJsonModule extends SimpleModule {

    public ShortCircuitAnalysisJsonModule() {
        addSerializer(LimitViolation.class, new LimitViolationSerializer());
        addDeserializer(LimitViolation.class, new LimitViolationDeserializer());
        addSerializer(Fault.class, new FaultSerializer());
        addDeserializer(Fault.class, new FaultDeserializer());
        addSerializer(FaultResult.class, new FaultResultSerializer());
        addDeserializer(FaultResult.class, new FaultResultDeserializer());
        addSerializer(ShortCircuitAnalysisResult.class, new ShortCircuitAnalysisResultSerializer());
        addDeserializer(ShortCircuitAnalysisResult.class, new ShortCircuitAnalysisResultDeserializer());
        addSerializer(NetworkMetadata.class, new NetworkMetadataSerializer());
        addDeserializer(NetworkMetadata.class, new NetworkMetadataDeserializer());
        addSerializer(ShortCircuitParameters.class, new ShortCircuitParametersSerializer());
        addDeserializer(ShortCircuitParameters.class, new ShortCircuitParametersDeserializer());
    }

}
