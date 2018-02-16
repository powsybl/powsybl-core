/**
 * Copyright (c) 2016-2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security;

import java.util.List;

/**
 * @deprecated Use LimitViolationsResult instead.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
@Deprecated
public class PreContingencyResult extends LimitViolationsResult {

    public PreContingencyResult(boolean computationOk, List<LimitViolation> limitViolations) {
        super(computationOk, limitViolations);
    }

    public PreContingencyResult(boolean computationOk, List<LimitViolation> limitViolations, List<String> actionsTaken) {
        super(computationOk, limitViolations, actionsTaken);
    }
}
