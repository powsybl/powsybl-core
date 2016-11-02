/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.pclfsim;

import eu.itesla_project.commons.config.ModuleConfig;
import eu.itesla_project.commons.config.PlatformConfig;
import eu.itesla_project.loadflow.api.LoadFlowFactory;
import eu.itesla_project.security.Security;

import java.util.Objects;

/**
 *
 * @author Quinary <itesla@quinary.com>
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PostContLoadFlowSimConfig {

    private static final float DEFAULT_MIN_BASE_VOLTAGE_FILTER = 0f;

    private static final Security.CurrentLimitType DEFAULT_CURRENT_LIMITE_TYPE = Security.CurrentLimitType.PATL;

    private static final float DEFAULT_LIMIT_REDUCTION = 1f;

    private Class<? extends LoadFlowFactory> loadFlowFactoryClass;

    private boolean baseCaseConstraintsFiltered;

    private boolean warnStartActivated;

    private float minBaseVoltageFilter;

    private Security.CurrentLimitType currentLimitType;

    private float limitReduction;

    public synchronized static PostContLoadFlowSimConfig load() {
        ModuleConfig config = PlatformConfig.defaultConfig().getModuleConfig("postcont-loadflow-sim");
        Class<? extends LoadFlowFactory> loadFlowFactoryClass = config.getClassProperty("loadFlowFactoryClass", LoadFlowFactory.class);
        boolean baseCaseConstraintsFiltered = config.getBooleanProperty("baseCaseConstraintsFiltered", false);
        boolean warnStartActivated = config.getBooleanProperty("warnStartActivated", false);
        float baseVoltageFilter = config.getFloatProperty("minBaseVoltageFilter", DEFAULT_MIN_BASE_VOLTAGE_FILTER);
        Security.CurrentLimitType currentLimitType = config.getEnumProperty("currentLimitType", Security.CurrentLimitType.class, DEFAULT_CURRENT_LIMITE_TYPE);
        float limitReduction = config.getFloatProperty("limitReduction", DEFAULT_LIMIT_REDUCTION);
        return new PostContLoadFlowSimConfig(loadFlowFactoryClass, baseCaseConstraintsFiltered, warnStartActivated,
                baseVoltageFilter, currentLimitType, limitReduction);
    }

    private static float checkBaseVoltageFilter(float baseVoltageFilter) {
        if (baseVoltageFilter < 0) {
            throw new IllegalArgumentException("Invalid base voltage filter " + baseVoltageFilter);
        }
        return baseVoltageFilter;
    }

    public PostContLoadFlowSimConfig(Class<? extends LoadFlowFactory> loadFlowFactoryClass, boolean baseCaseConstraintsFiltered,
                                     boolean warnStartActivated, float minBaseVoltageFilter, Security.CurrentLimitType currentLimitType,
                                     float limitReduction) {
        if (limitReduction <= 0 || limitReduction > 1) {
            throw new IllegalArgumentException("Bad limit reduction " + limitReduction);
        }
        this.loadFlowFactoryClass = Objects.requireNonNull(loadFlowFactoryClass);
        this.baseCaseConstraintsFiltered = baseCaseConstraintsFiltered;
        this.warnStartActivated = warnStartActivated;
        this.minBaseVoltageFilter = checkBaseVoltageFilter(minBaseVoltageFilter);
        this.currentLimitType = Objects.requireNonNull(currentLimitType);
        this.limitReduction = limitReduction;
    }

    public Class<? extends LoadFlowFactory> getLoadFlowFactoryClass() {
        return loadFlowFactoryClass;
    }

    public void setLoadFlowFactoryClass(Class<? extends LoadFlowFactory> loadFlowFactoryClass) {
        this.loadFlowFactoryClass = loadFlowFactoryClass;
    }

    public boolean isBaseCaseConstraintsFiltered() {
        return baseCaseConstraintsFiltered;
    }

    public void setBaseCaseConstraintsFiltered(boolean baseCaseConstraintsFiltered) {
        this.baseCaseConstraintsFiltered = baseCaseConstraintsFiltered;
    }

    public boolean isWarnStartActivated() {
        return warnStartActivated;
    }

    public void setWarnStartActivated(boolean warnStartActivated) {
        this.warnStartActivated = warnStartActivated;
    }

    public float getMinBaseVoltageFilter() {
        return minBaseVoltageFilter;
    }

    public void setMinBaseVoltageFilter(float minBaseVoltageFilter) {
        this.minBaseVoltageFilter = checkBaseVoltageFilter(minBaseVoltageFilter);
    }

    public Security.CurrentLimitType getCurrentLimitType() {
        return currentLimitType;
    }

    public void setCurrentLimitType(Security.CurrentLimitType currentLimitType) {
        this.currentLimitType = Objects.requireNonNull(currentLimitType);
    }

    public float getLimitReduction() {
        return limitReduction;
    }

    public void setLimitReduction(float limitReduction) {
        this.limitReduction = limitReduction;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() +
                " [loadFlowFactoryClass=" + loadFlowFactoryClass +
                " , baseCaseConstraintsFiltered=" + baseCaseConstraintsFiltered +
                " , warnStartActivated=" + warnStartActivated +
                " , minBaseVoltageFilter=" + minBaseVoltageFilter +
                " , currentLimitType=" + currentLimitType +
                " , limitReduction=" + limitReduction +
                "]";
    }
}
