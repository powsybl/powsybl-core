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
    private double subTransStudyReactanceCoefficient = DEFAULT_SUB_TRANSIENT_STUDY_REACTANCE_COEFFICIENT; //in % of the transient reactance
    private double minVoltageDropProportionalThreshold = DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD;
    private VoltageMapType voltageMapType = VoltageMapType.NORMALIZED;
    private boolean useResistances = DEFAULT_USE_RESISTANCES;
    private boolean useLoads = DEFAULT_USE_LOADS;
    private boolean useCapacities = DEFAULT_USE_CAPACITIES;
    private boolean useShunts = DEFAULT_USE_SHUNTS;
    private boolean useTapChangers = DEFAULT_USE_TAP_CHANGERS;
    private boolean useMutuals = DEFAULT_USE_MUTUALS;
    private boolean modelVSC = DEFAULT_MODEL_VSC;
    private StartedGroups startedGroupsInsideZone = StartedGroups.ALL;
    private double startedGroupsInsideZoneThreshold = DEFAULT_STARTED_GROUP_THRESHOLD;
    private StartedGroups startedGroupsOutOfZone = StartedGroups.STARTED;
    private double startedGroupsOutOfZoneThreshold = DEFAULT_STARTED_GROUP_THRESHOLD;

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
                        .setSubTransStudyReactanceCoefficient(config.getDoubleProperty("sub-transient-study-reactance-coefficient", DEFAULT_SUB_TRANSIENT_STUDY_REACTANCE_COEFFICIENT))
                        .setMinVoltageDropProportionalThreshold(config.getDoubleProperty("min-voltage-drop-proportional-threshold", DEFAULT_MIN_VOLTAGE_DROP_PROPORTIONAL_THRESHOLD))
                        .setVoltageMapType(config.getEnumProperty("voltage-map-type", VoltageMapType.class, VoltageMapType.NORMALIZED))
                        .setUseResistances(config.getBooleanProperty("use-resistances", DEFAULT_USE_RESISTANCES))
                        .setUseCapacities(config.getBooleanProperty("use-capacities", DEFAULT_USE_CAPACITIES))
                        .setUseLoads(config.getBooleanProperty("use-loads", DEFAULT_USE_LOADS))
                        .setUseShunts(config.getBooleanProperty("use-shunts", DEFAULT_USE_SHUNTS))
                        .setUseTapChangers(config.getBooleanProperty("use-tap-changers", DEFAULT_USE_TAP_CHANGERS))
                        .setUseMutuals(config.getBooleanProperty("use-mutuals", DEFAULT_USE_MUTUALS))
                        .setModelVSC(config.getBooleanProperty("model-vsc", DEFAULT_MODEL_VSC))
                        .setStartedGroupsInsideZone(config.getEnumProperty("started-groups-inside-zone", StartedGroups.class, StartedGroups.ALL))
                        .setStartedGroupsInsideZoneThreshold(config.getDoubleProperty("started-groups-inside-zone-threshold", DEFAULT_STARTED_GROUP_THRESHOLD))
                        .setStartedGroupsOutOfZone(config.getEnumProperty("started-groups-out-of-zone", StartedGroups.class, StartedGroups.STARTED))
                        .setStartedGroupsOutOfZoneThreshold(config.getDoubleProperty("started-groups-out-of-zone-threshold", DEFAULT_STARTED_GROUP_THRESHOLD)));

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

    /** In case of a sub-transient study, the coefficient to apply to the transient reactance to get the subtransient one. */
    public double getSubTransStudyReactanceCoefficient() {
        return subTransStudyReactanceCoefficient;
    }

    public ShortCircuitParameters setSubTransStudyReactanceCoefficient(double subTransStudyReactanceCoefficient) {
        this.subTransStudyReactanceCoefficient = subTransStudyReactanceCoefficient;
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

    public VoltageMapType getVoltageMapType() {
        return voltageMapType;
    }

    public ShortCircuitParameters setVoltageMapType(VoltageMapType voltageMapType) {
        this.voltageMapType = voltageMapType;
        return this;
    }

    public boolean isUseResistances() {
        return useResistances;
    }

    public ShortCircuitParameters setUseResistances(boolean useResistances) {
        this.useResistances = useResistances;
        return this;
    }

    public boolean isUseLoads() {
        return useLoads;
    }

    public ShortCircuitParameters setUseLoads(boolean useLoads) {
        this.useLoads = useLoads;
        return this;
    }

    public boolean isUseCapacities() {
        return useCapacities;
    }

    public ShortCircuitParameters setUseCapacities(boolean useCapacities) {
        this.useCapacities = useCapacities;
        return this;
    }

    public boolean isUseShunts() {
        return useShunts;
    }

    public ShortCircuitParameters setUseShunts(boolean useShunts) {
        this.useShunts = useShunts;
        return this;
    }

    public boolean isUseTapChangers() {
        return useTapChangers;
    }

    public ShortCircuitParameters setUseTapChangers(boolean useTapChangers) {
        this.useTapChangers = useTapChangers;
        return this;
    }

    public boolean isUseMutuals() {
        return useMutuals;
    }

    public ShortCircuitParameters setUseMutuals(boolean useMutuals) {
        this.useMutuals = useMutuals;
        return this;
    }

    public boolean isModelVSC() {
        return modelVSC;
    }

    public ShortCircuitParameters setModelVSC(boolean modelVSC) {
        this.modelVSC = modelVSC;
        return this;
    }

    public StartedGroups getStartedGroupsInsideZone() {
        return startedGroupsInsideZone;
    }

    public ShortCircuitParameters setStartedGroupsInsideZone(StartedGroups startedGroupsInsideZone) {
        this.startedGroupsInsideZone = startedGroupsInsideZone;
        return this;
    }

    public double getStartedGroupsInsideZoneThreshold() {
        return startedGroupsInsideZoneThreshold;
    }

    public ShortCircuitParameters setStartedGroupsInsideZoneThreshold(double startedGroupsInsideZoneThreshold) {
        this.startedGroupsInsideZoneThreshold = startedGroupsInsideZoneThreshold;
        return this;
    }

    public StartedGroups getStartedGroupsOutOfZone() {
        return startedGroupsOutOfZone;
    }

    public ShortCircuitParameters setStartedGroupsOutOfZone(StartedGroups startedGroupsOutOfZone) {
        this.startedGroupsOutOfZone = startedGroupsOutOfZone;
        return this;
    }

    public double getStartedGroupsOutOfZoneThreshold() {
        return startedGroupsOutOfZoneThreshold;
    }

    public ShortCircuitParameters setStartedGroupsOutOfZoneThreshold(double startedGroupsOutOfZoneThreshold) {
        this.startedGroupsOutOfZoneThreshold = startedGroupsOutOfZoneThreshold;
        return this;
    }
}
