/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.shortcircuit;

import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.extensions.AbstractExtendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionConfigLoader;
import com.powsybl.commons.extensions.ExtensionProviders;

import java.util.Objects;
import java.util.function.Supplier;

import static com.powsybl.shortcircuit.ShortCircuitConstants.*;

/**
 * Generic parameters for short circuit-computations.
 * May contain extensions for implementation-specific parameters.
 *
 * @author Boubakeur Brahimi
 */
public class ShortCircuitParameters extends AbstractExtendable<ShortCircuitParameters> {

    public interface ConfigLoader<E extends Extension<ShortCircuitParameters>>
            extends ExtensionConfigLoader<ShortCircuitParameters, E> {
    }

    // VERSION = 1.0
    public static final String VERSION = "1.0";

    private static final Supplier<ExtensionProviders<ConfigLoader>> SUPPLIER = Suppliers
            .memoize(() -> ExtensionProviders.createProvider(ConfigLoader.class, "short-circuit-parameters"));

    private boolean withLimitViolations = DEFAULT_WITH_LIMIT_VIOLATIONS;
    private boolean withVoltageMap = DEFAULT_WITH_VOLTAGE_MAP;
    private boolean withFeederResult = DEFAULT_WITH_FEEDER_RESULT;
    private StudyType studyType = DEFAULT_STUDY_TYPE;
    private double minVoltageDropProportionalThreshold = DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD;
    private boolean useResistances = DEFAULT_USE_RESISTANCES;
    private boolean useLoads = DEFAULT_USE_LOADS;
    private VoltageMapType voltageMapType = DEFAULT_VOLTAGE_MAP_TYPE;
    private NominalVoltageMapType nominalVoltageMapType = DEFAULT_NOMINAL_VOLTAGE_MAP_TYPE;

    /**
     * Load parameters from platform default config.
     */
    public static ShortCircuitParameters load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static ShortCircuitParameters load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        ShortCircuitParameters parameters = new ShortCircuitParameters();

        platformConfig.getOptionalModuleConfig("short-circuit-parameters").ifPresent(config ->
                parameters.setWithLimitViolations(config.getBooleanProperty("with-limit-violations", DEFAULT_WITH_LIMIT_VIOLATIONS))
                        .setWithVoltageMap(config.getBooleanProperty("with-voltage-map", DEFAULT_WITH_VOLTAGE_MAP))
                        .setWithFeederResult(config.getBooleanProperty("with-feeder-result", DEFAULT_WITH_FEEDER_RESULT))
                        .setStudyType(config.getEnumProperty("study-type", StudyType.class, DEFAULT_STUDY_TYPE))
                        .setMinVoltageDropProportionalThreshold(config.getDoubleProperty("min-voltage-drop-proportional-threshold", DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD))
                        .setUseResistances(config.getBooleanProperty("use-resistances", DEFAULT_USE_RESISTANCES))
                        .setVoltageMapType(config.getEnumProperty("voltage-map-type", VoltageMapType.class, DEFAULT_VOLTAGE_MAP_TYPE))
                        .setNominalVoltageMapType(config.getEnumProperty("nominal-voltage-map-type", NominalVoltageMapType.class, DEFAULT_NOMINAL_VOLTAGE_MAP_TYPE)));

        parameters.readExtensions(platformConfig);

        return parameters;
    }

    private void readExtensions(PlatformConfig platformConfig) {
        for (ConfigLoader provider : SUPPLIER.get().getProviders()) {
            addExtension(provider.getExtensionClass(), provider.load(platformConfig));
        }
    }

    public boolean isWithLimitViolations() {
        return withLimitViolations;
    }

    public ShortCircuitParameters setWithLimitViolations(boolean withLimitViolations) {
        this.withLimitViolations = withLimitViolations;
        return this;
    }

    public boolean isWithVoltageMap() {
        return withVoltageMap;
    }

    public ShortCircuitParameters setWithVoltageMap(boolean withVoltageMap) {
        this.withVoltageMap = withVoltageMap;
        return this;
    }

    public boolean isWithFeederResult() {
        return withFeederResult;
    }

    public ShortCircuitParameters setWithFeederResult(boolean withFeederResult) {
        this.withFeederResult = withFeederResult;
        return this;
    }

    public StudyType getStudyType() {
        return studyType;
    }

    public ShortCircuitParameters setStudyType(StudyType studyType) {
        this.studyType = studyType;
        return this;
    }

    /** The maximum voltage drop threshold in %. */
    public double getMinVoltageDropProportionalThreshold() {
        return minVoltageDropProportionalThreshold;
    }

    public ShortCircuitParameters setMinVoltageDropProportionalThreshold(double minVoltageDropProportionalThreshold) {
        this.minVoltageDropProportionalThreshold = minVoltageDropProportionalThreshold;
        return this;
    }

    /** Indicates if the resistances should be taken into account in the calculation of
     * the impedance of the branches, tap changer and transformers.
     */
    public boolean isUseResistances() {
        return useResistances;
    }

    public ShortCircuitParameters setUseResistances(boolean useResistances) {
        this.useResistances = useResistances;
        return this;
    }

    /** Indicates if the loads should be taken into account in the calculation.
     * If set to true, the direct reactance of the transformer connecting the load is used.
     * If set to false, the direct admittance of the loads is equal to zero, as well as the active and reactive power.
     */
    public boolean isUseLoads() {
        return useLoads;
    }

    public void setUseLoads(boolean useLoads) {
        this.useLoads = useLoads;
    }

    public VoltageMapType getVoltageMapType() {
        return voltageMapType;
    }

    public ShortCircuitParameters setVoltageMapType(VoltageMapType voltageMapType) {
        this.voltageMapType = voltageMapType;
        return this;
    }

    public NominalVoltageMapType getNominalVoltageMapType() {
        return nominalVoltageMapType;
    }

    public ShortCircuitParameters setNominalVoltageMapType(NominalVoltageMapType nominalVoltageMapType) {
        this.nominalVoltageMapType = nominalVoltageMapType;
        return this;
    }
}
