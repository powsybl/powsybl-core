/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.security;

import eu.itesla_project.commons.io.ModuleConfig;
import eu.itesla_project.commons.io.PlatformConfig;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LimitViolationFilter {

    private static final Set<LimitViolationType> DEFAULT_VIOLATION_TYPES = null;
    private static final float DEFAULT_MIN_BASE_VOLTAGE = 0f;

    private static Set<LimitViolationType> checkViolationTypes(Set<LimitViolationType> violationTypes) {
        if (violationTypes != null && violationTypes.isEmpty()) {
            throw new IllegalArgumentException("Bad violation types filter");
        }
        return violationTypes;
    }

    private static float checkMinBaseVoltage(float minBaseVoltage) {
        if (Float.isNaN(minBaseVoltage) || minBaseVoltage < 0) {
            throw new IllegalArgumentException("Bad min base voltage filter " + minBaseVoltage);
        }
        return minBaseVoltage;
    }

    public static LimitViolationFilter load() {
        return load(PlatformConfig.defaultConfig());
    }

    static LimitViolationFilter load(PlatformConfig platformConfig) {
        LimitViolationFilter filter = new LimitViolationFilter();
        ModuleConfig moduleConfig = platformConfig.getModuleConfigIfExists("limit-violation-default-filter");
        if (moduleConfig != null) {
            filter.setViolationTypes(moduleConfig.getEnumSetProperty("violationTypes", LimitViolationType.class, null));
            filter.setMinBaseVoltage(moduleConfig.getFloatProperty("minBaseVoltage", DEFAULT_MIN_BASE_VOLTAGE));
        }
        return filter;
    }

    private Set<LimitViolationType> violationTypes;

    private float minBaseVoltage;

    public LimitViolationFilter() {
        this(DEFAULT_VIOLATION_TYPES, DEFAULT_MIN_BASE_VOLTAGE);
    }

    public LimitViolationFilter(Set<LimitViolationType> violationTypes, float minBaseVoltage) {
        this.violationTypes = checkViolationTypes(violationTypes);
        this.minBaseVoltage = checkMinBaseVoltage(minBaseVoltage);
    }

    public Set<LimitViolationType> getViolationTypes() {
        return violationTypes;
    }

    public LimitViolationFilter setViolationTypes(Set<LimitViolationType> violationTypes) {
        this.violationTypes = checkViolationTypes(violationTypes);
        return this;
    }

    public float getMinBaseVoltage() {
        return minBaseVoltage;
    }

    public LimitViolationFilter setMinBaseVoltage(float minBaseVoltage) {
        this.minBaseVoltage = checkMinBaseVoltage(minBaseVoltage);
        return this;
    }

    public List<LimitViolation> apply(List<LimitViolation> violations) {
        return violations.stream()
                .filter(violation -> violationTypes == null || violationTypes.contains(violation.getLimitType()))
                .filter(violation -> Float.isNaN(violation.getBaseVoltage()) || violation.getBaseVoltage() > minBaseVoltage)
                .collect(Collectors.toList());
    }
}
