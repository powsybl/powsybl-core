/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.security;

import eu.itesla_project.contingency.Contingency;

import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian@ at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public class PostContingencyResult extends LimitViolationsResult {

    private final Contingency contingency;

    public PostContingencyResult(Contingency contingency, boolean computationOk, List<LimitViolation> limitViolations) {
        super(computationOk, limitViolations);
        this.contingency = Objects.requireNonNull(contingency);
    }

    public Contingency getContingency() {
        return contingency;
    }
}
