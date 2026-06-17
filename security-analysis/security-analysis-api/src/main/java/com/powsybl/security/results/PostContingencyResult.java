/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.results;

import com.powsybl.contingency.Contingency;
import com.powsybl.security.LimitViolationsResult;
import com.powsybl.security.PostContingencyComputationStatus;

import java.util.*;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian@ at rte-france.com>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class PostContingencyResult extends AbstractContingencyResult {

    private final Contingency contingency;

    private final PostContingencyComputationStatus status;

    private final ConnectivityResult connectivityResult;

    public PostContingencyResult(Contingency contingency,
                                 PostContingencyComputationStatus status,
                                 LimitViolationsResult limitViolationsResult,
                                 NetworkResult networkResult,
                                 ConnectivityResult connectivityResult,
                                 double distributedActivePower) {
        super(limitViolationsResult, networkResult, distributedActivePower);
        this.contingency = Objects.requireNonNull(contingency);
        this.status = Objects.requireNonNull(status);
        this.connectivityResult = Objects.requireNonNull(connectivityResult);
    }

    public Contingency getContingency() {
        return contingency;
    }

    public PostContingencyComputationStatus getStatus() {
        return status;
    }

    public ConnectivityResult getConnectivityResult() {
        return connectivityResult;
    }
}
