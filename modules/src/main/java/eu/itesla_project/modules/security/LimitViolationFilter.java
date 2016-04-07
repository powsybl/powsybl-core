/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.security;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LimitViolationFilter {

    private final Set<LimitViolationType> violationTypes;

    private final float minBaseVoltage;

    public LimitViolationFilter() {
        this(null, 0);
    }

    public LimitViolationFilter(Set<LimitViolationType> violationTypes, float minBaseVoltage) {
        if (violationTypes != null && violationTypes.isEmpty()) {
            throw new IllegalArgumentException("Bad violation types filter");
        }
        if (Float.isNaN(minBaseVoltage) || minBaseVoltage < 0) {
            throw new RuntimeException("Bad min base voltage filter " + minBaseVoltage);
        }
        this.violationTypes = violationTypes;
        this.minBaseVoltage = minBaseVoltage;
    }

    public Set<LimitViolationType> getViolationTypes() {
        return violationTypes;
    }

    public float getMinBaseVoltage() {
        return minBaseVoltage;
    }

    public List<LimitViolation> apply(List<LimitViolation> violations) {
        return violations.stream()
                .filter(violation -> violationTypes == null || violationTypes.contains(violation.getLimitType()))
                .filter(violation -> Float.isNaN(violation.getBaseVoltage()) || violation.getBaseVoltage() > minBaseVoltage)
                .collect(Collectors.toList());
    }
}
