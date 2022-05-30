/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.json;

import com.powsybl.contingency.json.ContingencyJsonModule;
import com.powsybl.security.*;
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

        addDeserializer(SecurityAnalysisResult.class, new SecurityAnalysisResultDeserializer());
        addDeserializer(NetworkMetadata.class, new NetworkMetadataDeserializer());
        addDeserializer(PostContingencyResult.class, new PostContingencyResultDeserializer());
        addDeserializer(LimitViolationsResult.class, new LimitViolationResultDeserializer());
        addDeserializer(LimitViolation.class, new LimitViolationDeserializer());
        addDeserializer(PreContingencyResult.class, new PreContingencyResultDeserializer());
        addDeserializer(BusResult.class, new BusResultDeserializer());
        addDeserializer(BranchResult.class, new BranchResultDeserializer());
        addDeserializer(ThreeWindingsTransformerResult.class, new ThreeWindingsTransformerResultDeserializer());
    }
}
