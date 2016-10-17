/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisResult {

    private final PreContingencyResult preContingencyResult;

    private final List<PostContingencyResult> postContingencyResults;

    public SecurityAnalysisResult(PreContingencyResult preContingencyResult, List<PostContingencyResult> postContingencyResults) {
        this.preContingencyResult = Objects.requireNonNull(preContingencyResult);
        this.postContingencyResults = Objects.requireNonNull(postContingencyResults);
    }

    public PreContingencyResult getPreContingencyResult() {
        return preContingencyResult;
    }

    public List<PostContingencyResult> getPostContingencyResults() {
        return postContingencyResults;
    }
}
