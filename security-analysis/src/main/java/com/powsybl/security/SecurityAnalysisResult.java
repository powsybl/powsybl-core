/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import com.powsybl.iidm.network.Network;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisResult {

    private final Network network;

    private final LimitViolationsResult preContingencyResult;

    private final List<PostContingencyResult> postContingencyResults;

    public SecurityAnalysisResult(LimitViolationsResult preContingencyResult,
                                  List<PostContingencyResult> postContingencyResults) {
        this.network = null;
        this.preContingencyResult = Objects.requireNonNull(preContingencyResult);
        this.postContingencyResults = Objects.requireNonNull(postContingencyResults);
    }

    public SecurityAnalysisResult(Network network,
                                  LimitViolationsResult preContingencyResult,
                                  List<PostContingencyResult> postContingencyResults) {
        this.network = Objects.requireNonNull(network);
        this.preContingencyResult = Objects.requireNonNull(preContingencyResult);
        this.postContingencyResults = Objects.requireNonNull(postContingencyResults);
    }

    public Network getNetwork() {
        return network;
    }

    public LimitViolationsResult getPreContingencyResult() {
        return preContingencyResult;
    }

    public List<PostContingencyResult> getPostContingencyResults() {
        return postContingencyResults;
    }
}
