/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.commons.extensions.AbstractExtendable;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisResult extends AbstractExtendable<SecurityAnalysisResult> {

    private NetworkMetadata networkMetadata;

    private final LimitViolationsResult preContingencyResult;

    private final List<PostContingencyResult> postContingencyResults;

    public SecurityAnalysisResult(LimitViolationsResult preContingencyResult,
                                  List<PostContingencyResult> postContingencyResults) {
        this.preContingencyResult = Objects.requireNonNull(preContingencyResult);
        this.postContingencyResults = Objects.requireNonNull(postContingencyResults);
    }

    public NetworkMetadata getNetworkMetadata() {
        return networkMetadata;
    }

    public SecurityAnalysisResult setNetworkMetadata(NetworkMetadata networkMetadata) {
        this.networkMetadata = networkMetadata;
        return this;
    }

    public LimitViolationsResult getPreContingencyResult() {
        return preContingencyResult;
    }

    public List<PostContingencyResult> getPostContingencyResults() {
        return postContingencyResults;
    }
}
