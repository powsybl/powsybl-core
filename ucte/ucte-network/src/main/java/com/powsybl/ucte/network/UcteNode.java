/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UcteNode implements UcteRecord {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteNode.class);

    private static final float DEFAULT_POWER_LIMIT = 9999;

    private UcteNodeCode code;
    private String geographicalName;
    private UcteNodeStatus status;
    private UcteNodeTypeCode typeCode;
    private float voltageReference;
    private float activeLoad;
    private float reactiveLoad;
    private float activePowerGeneration;
    private float reactivePowerGeneration;
    private float minimumPermissibleActivePowerGeneration;
    private float maximumPermissibleActivePowerGeneration;
    private float minimumPermissibleReactivePowerGeneration;
    private float maximumPermissibleReactivePowerGeneration;
    private float staticOfPrimaryControl;
    private float nominalPowerPrimaryControl;
    private float threePhaseShortCircuitPower;
    private float xrRatio;
    private UctePowerPlantType powerPlantType;

    public UcteNode(UcteNodeCode code, String geographicalName, UcteNodeStatus status, UcteNodeTypeCode typeCode,
                    float voltageReference, float activeLoad, float reactiveLoad, float activePowerGeneration,
                    float reactivePowerGeneration, float minimumPermissibleActivePowerGeneration,
                    float maximumPermissibleActivePowerGeneration, float minimumPermissibleReactivePowerGeneration,
                    float maximumPermissibleReactivePowerGeneration, float staticOfPrimaryControl,
                    float nominalPowerPrimaryControl, float threePhaseShortCircuitPower, float xrRatio,
                    UctePowerPlantType powerPlantType) {
        this.code = Objects.requireNonNull(code);
        this.geographicalName = geographicalName;
        this.status = Objects.requireNonNull(status);
        this.typeCode = Objects.requireNonNull(typeCode);
        this.voltageReference = voltageReference;
        this.activeLoad = activeLoad;
        this.reactiveLoad = reactiveLoad;
        this.activePowerGeneration = activePowerGeneration;
        this.reactivePowerGeneration = reactivePowerGeneration;
        this.minimumPermissibleActivePowerGeneration = minimumPermissibleActivePowerGeneration;
        this.maximumPermissibleActivePowerGeneration = maximumPermissibleActivePowerGeneration;
        this.minimumPermissibleReactivePowerGeneration = minimumPermissibleReactivePowerGeneration;
        this.maximumPermissibleReactivePowerGeneration = maximumPermissibleReactivePowerGeneration;
        this.staticOfPrimaryControl = staticOfPrimaryControl;
        this.nominalPowerPrimaryControl = nominalPowerPrimaryControl;
        this.threePhaseShortCircuitPower = threePhaseShortCircuitPower;
        this.xrRatio = xrRatio;
        this.powerPlantType = powerPlantType;
    }

    /**
     * Gets node code.
     * @return node code
     */
    public UcteNodeCode getCode() {
        return code;
    }

    /**
     * Sets node code.
     * @param code node code
     */
    public void setCode(UcteNodeCode code) {
        this.code = Objects.requireNonNull(code);
    }

    /**
     * Gets node geographical name.
     * @return node geographical name
     */
    public String getGeographicalName() {
        return geographicalName;
    }

    /**
     * Sets node geographical name.
     * @param geographicalName node geographical name
     */
    public void setGeographicalName(String geographicalName) {
        this.geographicalName = geographicalName;
    }

    /**
     * Gets node status.
     * @return node status
     */
    public UcteNodeStatus getStatus() {
        return status;
    }

    /**
     * Sets node status.
     * @param status node status
     */
    public void setStatus(UcteNodeStatus status) {
        this.status = Objects.requireNonNull(status);
    }

    /**
     * Gets node type code.
     * @return node type code
     */
    public UcteNodeTypeCode getTypeCode() {
        return typeCode;
    }

    /**
     * Sets node type code.
     * @param typeCode node type code
     */
    public void setTypeCode(UcteNodeTypeCode typeCode) {
        this.typeCode = Objects.requireNonNull(typeCode);
    }

    /**
     * Gets voltage reference value (0 not allowed) in kV.
     * @return voltage reference value
     */
    public float getVoltageReference() {
        return voltageReference;
    }

    /**
     * Sets voltage reference value (0 not allowed) in kV.
     * @param voltageReference voltage reference value
     */
    public void setVoltageReference(float voltageReference) {
        this.voltageReference = voltageReference;
    }

    /**
     * Gets active load (MW).
     * @return active load (MW)
     */
    public float getActiveLoad() {
        return activeLoad;
    }

    /**
     * Gets active load (MW).
     * @param activeLoad active load (MW)
     */
    public void setActiveLoad(float activeLoad) {
        this.activeLoad = activeLoad;
    }

    /**
     * Gets reactive load (MVar).
     * @return reactive load (MVar)
     */
    public float getReactiveLoad() {
        return reactiveLoad;
    }

    /**
     * Sets reactive load (MVar).
     * @param reactiveLoad reactive load (MVar)
     */
    public void setReactiveLoad(float reactiveLoad) {
        this.reactiveLoad = reactiveLoad;
    }

    /**
     * Gets active power generation (MW).
     * @return active power generation (MW)
     */
    public float getActivePowerGeneration() {
        return activePowerGeneration;
    }

    /**
     * Sets active power generation (MW).
     * @param activePowerGeneration active power generation (MW)
     */
    public void setActivePowerGeneration(float activePowerGeneration) {
        this.activePowerGeneration = activePowerGeneration;
    }

    /**
     * Gets reactive power generation (MVar).
     * @return reactive power generation (MVar)
     */
    public float getReactivePowerGeneration() {
        return reactivePowerGeneration;
    }

    /**
     * Sets reactive power generation (MVar).
     * @param reactivePowerGeneration reactive power generation (MVar)
     */
    public void setReactivePowerGeneration(float reactivePowerGeneration) {
        this.reactivePowerGeneration = reactivePowerGeneration;
    }

    /**
     * Gets minimum permissible active power generation (MW).
     * <p>{@code Float.NaN} means undefined.
     * @return minimum permissible active power generation (MW)
     */
    public float getMinimumPermissibleActivePowerGeneration() {
        return minimumPermissibleActivePowerGeneration;
    }

    /**
     * Sets minimum permissible active power generation (MW).
     * <p>{@code Float.NaN} means undefined.
     * @param minimumPermissibleActivePowerGeneration minimum permissible active power generation (MW)
     */
    public void setMinimumPermissibleActivePowerGeneration(float minimumPermissibleActivePowerGeneration) {
        this.minimumPermissibleActivePowerGeneration = minimumPermissibleActivePowerGeneration;
    }

    /**
     * Gets maximum permissible active power generation (MW).
     * <p>{@code Float.NaN} means undefined.
     * @return maximum permissible active power generation (MW)
     */
    public float getMaximumPermissibleActivePowerGeneration() {
        return maximumPermissibleActivePowerGeneration;
    }

    /**
     * Sets maximum permissible active power generation (MW).
     * <p>{@code Float.NaN} means undefined.
     * @param maximumPermissibleActivePowerGeneration maximum permissible active power generation (MW)
     */
    public void setMaximumPermissibleActivePowerGeneration(float maximumPermissibleActivePowerGeneration) {
        this.maximumPermissibleActivePowerGeneration = maximumPermissibleActivePowerGeneration;
    }

    /**
     * Gets minimum permissible reactive power generation (MW).
     * <p>{@code Float.NaN} means undefined.
     * @return minimum permissible reactive power generation (MW)
     */
    public float getMinimumPermissibleReactivePowerGeneration() {
        return minimumPermissibleReactivePowerGeneration;
    }

    /**
     * Sets minimum permissible reactive power generation (MW).
     * <p>{@code Float.NaN} means undefined.
     * @param minimumPermissibleReactivePowerGeneration minimum permissible reactive power generation (MW)
     */
    public void setMinimumPermissibleReactivePowerGeneration(float minimumPermissibleReactivePowerGeneration) {
        this.minimumPermissibleReactivePowerGeneration = minimumPermissibleReactivePowerGeneration;
    }

    /**
     * Gets maximum permissible reactive power generation (MW).
     * <p>{@code Float.NaN} means undefined.
     * @return maximum permissible reactive power generation (MW)
     */
    public float getMaximumPermissibleReactivePowerGeneration() {
        return maximumPermissibleReactivePowerGeneration;
    }

    /**
     * Sets maximum permissible reactive power generation (MW).
     * <p>{@code Float.NaN} means undefined.
     * @param maximumPermissibleReactivePowerGeneration maximum permissible reactive power generation (MW)
     */
    public void setMaximumPermissibleReactivePowerGeneration(float maximumPermissibleReactivePowerGeneration) {
        this.maximumPermissibleReactivePowerGeneration = maximumPermissibleReactivePowerGeneration;
    }

    /**
     * Gets static of primary control (%).
     * <p>{@code Float.NaN} means undefined.
     * @return static of primary control (%)
     */
    public float getStaticOfPrimaryControl() {
        return staticOfPrimaryControl;
    }

    /**
     * Sets static of primary control (%).
     * <p>{@code Float.NaN} means undefined.
     * @param staticOfPrimaryControl static of primary control (%)
     */
    public void setStaticOfPrimaryControl(float staticOfPrimaryControl) {
        this.staticOfPrimaryControl = staticOfPrimaryControl;
    }

    /**
     * Gets nominal power for primary control (MW).
     * <p>{@code Float.NaN} means undefined.
     * @return nominal power for primary control (MW)
     */
    public float getNominalPowerPrimaryControl() {
        return nominalPowerPrimaryControl;
    }

    /**
     * Sets nominal power for primary control (MW).
     * <p>{@code Float.NaN} means undefined.
     * @param nominalPowerPrimaryControl nominal power for primary control (MW)
     */
    public void setNominalPowerPrimaryControl(float nominalPowerPrimaryControl) {
        this.nominalPowerPrimaryControl = nominalPowerPrimaryControl;
    }

    /**
     * Gets three phase short circuit power (MVA).
     * <p>{@code Float.NaN} means undefined.
     * @return three phase short circuit power (MVA)
     */
    public float getThreePhaseShortCircuitPower() {
        return threePhaseShortCircuitPower;
    }

    /**
     * Sets three phase short circuit power (MVA).
     * <p>{@code Float.NaN} means undefined.
     * @param threePhaseShortCircuitPower three phase short circuit power (MVA)
     */
    public void setThreePhaseShortCircuitPower(float threePhaseShortCircuitPower) {
        this.threePhaseShortCircuitPower = threePhaseShortCircuitPower;
    }

    /**
     * Gets X/R ratio.
     * <p>{@code Float.NaN} means undefined.
     * @return X/R ratio
     */
    public float getXrRatio() {
        return xrRatio;
    }

    /**
     * Sets X/R ratio.
     * <p>{@code Float.NaN} means undefined.
     * @param xrRatio X/R ratio
     */
    public void setXrRatio(float xrRatio) {
        this.xrRatio = xrRatio;
    }

    /**
     * Gets power plant type.
     * <p>{@code null} means undefined.
     * @return power plant type
     */
    public UctePowerPlantType getPowerPlantType() {
        return powerPlantType;
    }

    /**
     * Sets power plant type.
     * <p>{@code null} means undefined.
     * @param powerPlantType power plant type
     */
    public void setPowerPlantType(UctePowerPlantType powerPlantType) {
        this.powerPlantType = powerPlantType;
    }

    public boolean isGenerator() {
        // we consider there is a generator connected to the node if
        //   - node is regulating voltage
        //   - or active power generation is defined and non null
        //   - or reactive power generation is defined and non null
        //   - or active limits are both defined (min and max), one is non null and there are not equal
        //   - or reactive limits are both defined (min and max), one is non null and there are not equal
        return isRegulatingVoltage()
                || (!Float.isNaN(activePowerGeneration) && activePowerGeneration != 0)
                || (!Float.isNaN(reactivePowerGeneration) && reactivePowerGeneration != 0)
                || (!Float.isNaN(minimumPermissibleActivePowerGeneration)
                    && !Float.isNaN(maximumPermissibleActivePowerGeneration)
                    && (minimumPermissibleActivePowerGeneration != 0 || maximumPermissibleActivePowerGeneration != 0)
                    && minimumPermissibleActivePowerGeneration != maximumPermissibleActivePowerGeneration)
                || (!Float.isNaN(minimumPermissibleReactivePowerGeneration)
                    && !Float.isNaN(maximumPermissibleReactivePowerGeneration)
                    && (minimumPermissibleReactivePowerGeneration != 0 || maximumPermissibleReactivePowerGeneration != 0)
                    && minimumPermissibleReactivePowerGeneration != maximumPermissibleReactivePowerGeneration);
    }

    public boolean isRegulatingVoltage() {
        return typeCode == UcteNodeTypeCode.PU || typeCode == UcteNodeTypeCode.UT;
    }

    public boolean isRegulatingFrequency() {
        // we consider the node can regulate frequency if active power generation
        // is defined and if the node inject some power in the network
        return !Float.isNaN(activePowerGeneration) && activePowerGeneration < 0;
    }

    @Override
    public void fix() {
        if (isGenerator()) {
            fixActivePower();
            fixVoltage();
            fixReactivePower();
        }
    }

    private void fixActivePower() {
        // ---- ACTIVE POWER FIXES ----

        // active power is undefined
        if (Float.isNaN(activePowerGeneration)) {
            LOGGER.warn("Node {}: active power is undefined, set value to 0", code);
            activePowerGeneration = 0;
        }
        // undefined min reactive power
        if (Float.isNaN(minimumPermissibleActivePowerGeneration)) {
            LOGGER.warn("Node {}: minimum active power is undefined, set value to {}",
                    code, DEFAULT_POWER_LIMIT);
            minimumPermissibleActivePowerGeneration = DEFAULT_POWER_LIMIT;
        }
        // undefined max reactive power
        if (Float.isNaN(maximumPermissibleActivePowerGeneration)) {
            LOGGER.warn("Node {}: maximum active power is undefined, set value to {}",
                    code, -DEFAULT_POWER_LIMIT);
            maximumPermissibleActivePowerGeneration = -DEFAULT_POWER_LIMIT;
        }
        // inverted active power limits
        if (minimumPermissibleActivePowerGeneration < maximumPermissibleActivePowerGeneration) {
            LOGGER.warn("Node {}: active power limits are inverted ({}, {}), swap values",
                    code, minimumPermissibleActivePowerGeneration, maximumPermissibleActivePowerGeneration);
            float tmp = minimumPermissibleActivePowerGeneration;
            minimumPermissibleActivePowerGeneration = maximumPermissibleActivePowerGeneration;
            maximumPermissibleActivePowerGeneration = tmp;
        }
        // active power is out of limits
        if (activePowerGeneration < maximumPermissibleActivePowerGeneration) {
            LOGGER.warn("Node {}: active power {} under maximum permissible value {}, shift maximum permissible value",
                    code, activePowerGeneration, maximumPermissibleActivePowerGeneration);
            maximumPermissibleActivePowerGeneration = activePowerGeneration;
        }
        if (activePowerGeneration != 0 && activePowerGeneration > minimumPermissibleActivePowerGeneration) {
            LOGGER.warn("Node {}: active power {} above minimum permissible value {}, shift minimum permissible value",
                    code, activePowerGeneration, minimumPermissibleActivePowerGeneration);
            minimumPermissibleActivePowerGeneration = activePowerGeneration;
        }
        // flat active limits and not synchronous compensator
        // FIXME: Dead code?
        if (minimumPermissibleActivePowerGeneration == 0 && maximumPermissibleActivePowerGeneration == 0 && activePowerGeneration != 0) {
            LOGGER.warn("Node {}: flat active limits ({}), set values to [{}, {}]",
                    code, minimumPermissibleActivePowerGeneration, DEFAULT_POWER_LIMIT, -DEFAULT_POWER_LIMIT);
            minimumPermissibleActivePowerGeneration = DEFAULT_POWER_LIMIT;
            maximumPermissibleActivePowerGeneration = -DEFAULT_POWER_LIMIT;
        }
    }

    private void fixVoltage() {
        // ---- VOLTAGE FIXES ----

        // PV and undefined voltage, switch to PQ
        if (isRegulatingVoltage() && (Float.isNaN(voltageReference) || voltageReference < 0.0001)) {
            LOGGER.warn("Node {}: voltage is regulated, but voltage setpoint is null ({}), switch type code to {}",
                    code, voltageReference, UcteNodeTypeCode.PQ);
            typeCode = UcteNodeTypeCode.PQ;
        }
    }

    private void fixReactivePower() {
        // ---- REACTIVE POWER FIXES ----

        // PQ and undefined reactive power
        if (!isRegulatingVoltage() && Float.isNaN(reactivePowerGeneration)) {
            LOGGER.warn("Node {}: voltage is not regulated but reactive power is undefined, set value to 0", code);
            reactivePowerGeneration = 0;
        }
        // undefined min reactive power
        if (Float.isNaN(minimumPermissibleReactivePowerGeneration)) {
            LOGGER.warn("Node {}: minimum reactive power is undefined, set value to {}",
                    code, DEFAULT_POWER_LIMIT);
            minimumPermissibleReactivePowerGeneration = DEFAULT_POWER_LIMIT;
        }
        // undefined max reactive power
        if (Float.isNaN(maximumPermissibleReactivePowerGeneration)) {
            LOGGER.warn("Node {}: maximum reactive power is undefined, set value to {}",
                    code, -DEFAULT_POWER_LIMIT);
            maximumPermissibleReactivePowerGeneration = -DEFAULT_POWER_LIMIT;
        }
        // inverted reactive power limits
        if (minimumPermissibleReactivePowerGeneration < maximumPermissibleReactivePowerGeneration) {
            LOGGER.warn("Node {}: reactive power limits are inverted ({}, {}), swap values",
                    code, minimumPermissibleReactivePowerGeneration, maximumPermissibleReactivePowerGeneration);
            float tmp = minimumPermissibleReactivePowerGeneration;
            minimumPermissibleReactivePowerGeneration = maximumPermissibleReactivePowerGeneration;
            maximumPermissibleReactivePowerGeneration = tmp;
        }
        // reactive power is out of limits
        if (!Float.isNaN(reactivePowerGeneration) && reactivePowerGeneration < maximumPermissibleReactivePowerGeneration) {
            LOGGER.warn("Node {}: reactive power {} under maximum permissible value {}, shift maximum permissible value",
                    code, reactivePowerGeneration, maximumPermissibleReactivePowerGeneration);
            maximumPermissibleReactivePowerGeneration = reactivePowerGeneration;
        }
        if (!Float.isNaN(reactivePowerGeneration) && reactivePowerGeneration > minimumPermissibleReactivePowerGeneration) {
            LOGGER.warn("Node {}: reactive power {} above minimum permissible value {}, shift minimum permissible value",
                    code, reactivePowerGeneration, minimumPermissibleReactivePowerGeneration);
            minimumPermissibleReactivePowerGeneration = reactivePowerGeneration;
        }
        // reactive power limits are beyond DEFAULT_POWER_LIMIT
        if (minimumPermissibleReactivePowerGeneration > DEFAULT_POWER_LIMIT) {
            LOGGER.warn("Node {}: minimum reactive power is to high {}, set value to {}",
                    code, minimumPermissibleReactivePowerGeneration, DEFAULT_POWER_LIMIT);
        }
        if (maximumPermissibleReactivePowerGeneration < -DEFAULT_POWER_LIMIT) {
            LOGGER.warn("Node {}: maximum reactive power is to high {}, set value to {}",
                    code, maximumPermissibleReactivePowerGeneration, -DEFAULT_POWER_LIMIT);
        }
        // flat reactive power limits
        if (minimumPermissibleReactivePowerGeneration == maximumPermissibleReactivePowerGeneration) {
            LOGGER.warn("Node {}: flat reactive limits ({}), set values to [{}, {}]",
                    code, minimumPermissibleReactivePowerGeneration, DEFAULT_POWER_LIMIT, -DEFAULT_POWER_LIMIT);
            minimumPermissibleReactivePowerGeneration = DEFAULT_POWER_LIMIT;
            maximumPermissibleReactivePowerGeneration = -DEFAULT_POWER_LIMIT;
        }
    }

    @Override
    public String toString() {
        return code.toString();
    }

}
