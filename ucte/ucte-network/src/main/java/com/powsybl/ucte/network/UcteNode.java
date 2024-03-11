/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.report.ReportNode;
import com.powsybl.commons.report.TypedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class UcteNode implements UcteRecord, Comparable<UcteNode> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteNode.class);

    private static final double DEFAULT_POWER_LIMIT = 9999;
    private static final double LOW_VOLTAGE_FACTOR = 0.8;
    private static final double HIGH_VOLTAGE_FACTOR = 1.2;
    private static final double LOW_NOMINAL_VOLTAGE = 110;

    private UcteNodeCode code;
    private String geographicalName;
    private UcteNodeStatus status;
    private UcteNodeTypeCode typeCode;
    private double voltageReference;
    private double activeLoad;
    private double reactiveLoad;
    private double activePowerGeneration;
    private double reactivePowerGeneration;
    private double minimumPermissibleActivePowerGeneration;
    private double maximumPermissibleActivePowerGeneration;
    private double minimumPermissibleReactivePowerGeneration;
    private double maximumPermissibleReactivePowerGeneration;
    private double staticOfPrimaryControl;
    private double nominalPowerPrimaryControl;
    private double threePhaseShortCircuitPower;
    private double xrRatio;
    private UctePowerPlantType powerPlantType;

    public UcteNode(UcteNodeCode code, String geographicalName, UcteNodeStatus status, UcteNodeTypeCode typeCode,
                    double voltageReference, double activeLoad, double reactiveLoad, double activePowerGeneration,
                    double reactivePowerGeneration, double minimumPermissibleActivePowerGeneration,
                    double maximumPermissibleActivePowerGeneration, double minimumPermissibleReactivePowerGeneration,
                    double maximumPermissibleReactivePowerGeneration, double staticOfPrimaryControl,
                    double nominalPowerPrimaryControl, double threePhaseShortCircuitPower, double xrRatio,
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
    public double getVoltageReference() {
        return voltageReference;
    }

    /**
     * Sets voltage reference value (0 not allowed) in kV.
     * @param voltageReference voltage reference value
     */
    public void setVoltageReference(double voltageReference) {
        this.voltageReference = voltageReference;
    }

    /**
     * Gets active load (MW).
     * @return active load (MW)
     */
    public double getActiveLoad() {
        return activeLoad;
    }

    /**
     * Gets active load (MW).
     * @param activeLoad active load (MW)
     */
    public void setActiveLoad(double activeLoad) {
        this.activeLoad = activeLoad;
    }

    /**
     * Gets reactive load (MVar).
     * @return reactive load (MVar)
     */
    public double getReactiveLoad() {
        return reactiveLoad;
    }

    /**
     * Sets reactive load (MVar).
     * @param reactiveLoad reactive load (MVar)
     */
    public void setReactiveLoad(double reactiveLoad) {
        this.reactiveLoad = reactiveLoad;
    }

    /**
     * Gets active power generation (MW).
     * @return active power generation (MW)
     */
    public double getActivePowerGeneration() {
        return activePowerGeneration;
    }

    /**
     * Sets active power generation (MW).
     * @param activePowerGeneration active power generation (MW)
     */
    public void setActivePowerGeneration(double activePowerGeneration) {
        this.activePowerGeneration = activePowerGeneration;
    }

    /**
     * Gets reactive power generation (MVar).
     * @return reactive power generation (MVar)
     */
    public double getReactivePowerGeneration() {
        return reactivePowerGeneration;
    }

    /**
     * Sets reactive power generation (MVar).
     * @param reactivePowerGeneration reactive power generation (MVar)
     */
    public void setReactivePowerGeneration(double reactivePowerGeneration) {
        this.reactivePowerGeneration = reactivePowerGeneration;
    }

    /**
     * Gets minimum permissible active power generation (MW).
     * <p>{@code Double.NaN} means undefined.
     * @return minimum permissible active power generation (MW)
     */
    public double getMinimumPermissibleActivePowerGeneration() {
        return minimumPermissibleActivePowerGeneration;
    }

    /**
     * Sets minimum permissible active power generation (MW).
     * <p>{@code Double.NaN} means undefined.
     * @param minimumPermissibleActivePowerGeneration minimum permissible active power generation (MW)
     */
    public void setMinimumPermissibleActivePowerGeneration(double minimumPermissibleActivePowerGeneration) {
        this.minimumPermissibleActivePowerGeneration = minimumPermissibleActivePowerGeneration;
    }

    /**
     * Gets maximum permissible active power generation (MW).
     * <p>{@code Double.NaN} means undefined.
     * @return maximum permissible active power generation (MW)
     */
    public double getMaximumPermissibleActivePowerGeneration() {
        return maximumPermissibleActivePowerGeneration;
    }

    /**
     * Sets maximum permissible active power generation (MW).
     * <p>{@code Double.NaN} means undefined.
     * @param maximumPermissibleActivePowerGeneration maximum permissible active power generation (MW)
     */
    public void setMaximumPermissibleActivePowerGeneration(double maximumPermissibleActivePowerGeneration) {
        this.maximumPermissibleActivePowerGeneration = maximumPermissibleActivePowerGeneration;
    }

    /**
     * Gets minimum permissible reactive power generation (MW).
     * <p>{@code Double.NaN} means undefined.
     * @return minimum permissible reactive power generation (MW)
     */
    public double getMinimumPermissibleReactivePowerGeneration() {
        return minimumPermissibleReactivePowerGeneration;
    }

    /**
     * Sets minimum permissible reactive power generation (MW).
     * <p>{@code Double.NaN} means undefined.
     * @param minimumPermissibleReactivePowerGeneration minimum permissible reactive power generation (MW)
     */
    public void setMinimumPermissibleReactivePowerGeneration(double minimumPermissibleReactivePowerGeneration) {
        this.minimumPermissibleReactivePowerGeneration = minimumPermissibleReactivePowerGeneration;
    }

    /**
     * Gets maximum permissible reactive power generation (MW).
     * <p>{@code Double.NaN} means undefined.
     * @return maximum permissible reactive power generation (MW)
     */
    public double getMaximumPermissibleReactivePowerGeneration() {
        return maximumPermissibleReactivePowerGeneration;
    }

    /**
     * Sets maximum permissible reactive power generation (MW).
     * <p>{@code Double.NaN} means undefined.
     * @param maximumPermissibleReactivePowerGeneration maximum permissible reactive power generation (MW)
     */
    public void setMaximumPermissibleReactivePowerGeneration(double maximumPermissibleReactivePowerGeneration) {
        this.maximumPermissibleReactivePowerGeneration = maximumPermissibleReactivePowerGeneration;
    }

    /**
     * Gets static of primary control (%).
     * <p>{@code Double.NaN} means undefined.
     * @return static of primary control (%)
     */
    public double getStaticOfPrimaryControl() {
        return staticOfPrimaryControl;
    }

    /**
     * Sets static of primary control (%).
     * <p>{@code Double.NaN} means undefined.
     * @param staticOfPrimaryControl static of primary control (%)
     */
    public void setStaticOfPrimaryControl(double staticOfPrimaryControl) {
        this.staticOfPrimaryControl = staticOfPrimaryControl;
    }

    /**
     * Gets nominal power for primary control (MW).
     * <p>{@code Double.NaN} means undefined.
     * @return nominal power for primary control (MW)
     */
    public double getNominalPowerPrimaryControl() {
        return nominalPowerPrimaryControl;
    }

    /**
     * Sets nominal power for primary control (MW).
     * <p>{@code Double.NaN} means undefined.
     * @param nominalPowerPrimaryControl nominal power for primary control (MW)
     */
    public void setNominalPowerPrimaryControl(double nominalPowerPrimaryControl) {
        this.nominalPowerPrimaryControl = nominalPowerPrimaryControl;
    }

    /**
     * Gets three-phase short-circuit power (MVA).
     * <p>{@code Double.NaN} means undefined.
     * @return three-phase short-circuit power (MVA)
     */
    public double getThreePhaseShortCircuitPower() {
        return threePhaseShortCircuitPower;
    }

    /**
     * Sets three-phase short-circuit power (MVA).
     * <p>{@code Double.NaN} means undefined.
     * @param threePhaseShortCircuitPower three-phase short-circuit power (MVA)
     */
    public void setThreePhaseShortCircuitPower(double threePhaseShortCircuitPower) {
        this.threePhaseShortCircuitPower = threePhaseShortCircuitPower;
    }

    /**
     * Gets X/R ratio.
     * <p>{@code Double.NaN} means undefined.
     * @return X/R ratio
     */
    public double getXrRatio() {
        return xrRatio;
    }

    /**
     * Sets X/R ratio.
     * <p>{@code Double.NaN} means undefined.
     * @param xrRatio X/R ratio
     */
    public void setXrRatio(double xrRatio) {
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
                || !Double.isNaN(activePowerGeneration) && activePowerGeneration != 0
                || !Double.isNaN(reactivePowerGeneration) && reactivePowerGeneration != 0
                || !Double.isNaN(minimumPermissibleActivePowerGeneration)
                    && !Double.isNaN(maximumPermissibleActivePowerGeneration)
                    && (minimumPermissibleActivePowerGeneration != 0 || maximumPermissibleActivePowerGeneration != 0)
                    && minimumPermissibleActivePowerGeneration != maximumPermissibleActivePowerGeneration
                || !Double.isNaN(minimumPermissibleReactivePowerGeneration)
                    && !Double.isNaN(maximumPermissibleReactivePowerGeneration)
                    && (minimumPermissibleReactivePowerGeneration != 0 || maximumPermissibleReactivePowerGeneration != 0)
                    && minimumPermissibleReactivePowerGeneration != maximumPermissibleReactivePowerGeneration;
    }

    public boolean isRegulatingVoltage() {
        return typeCode == UcteNodeTypeCode.PU || typeCode == UcteNodeTypeCode.UT;
    }

    public boolean isRegulatingFrequency() {
        // we consider the node can regulate frequency if active power generation
        // is defined and if the node inject some power in the network
        return !Double.isNaN(activePowerGeneration) && activePowerGeneration < 0;
    }

    @Override
    public void fix(ReportNode reportNode) {
        if (isGenerator()) {
            fixActivePower(reportNode);
            fixVoltage(reportNode);
            fixReactivePower(reportNode);
        }
    }

    private void fixActivePower(ReportNode reportNode) {
        // ---- ACTIVE POWER FIXES ----

        // active power is undefined
        if (Double.isNaN(activePowerGeneration)) {

            reportNode.newReportNode()
                .withMessageTemplate("activePowerUndefined", "Node ${node}: active power is undefined, set value to 0")
                .withUntypedValue("node", code.toString())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
            LOGGER.warn("Node {}: active power is undefined, set value to 0", code);
            activePowerGeneration = 0;
        }
        // undefined min reactive power
        if (Double.isNaN(minimumPermissibleActivePowerGeneration)) {
            LOGGER.info("Node {}: minimum active power is undefined, set value to {}",
                    code, DEFAULT_POWER_LIMIT);
            minimumPermissibleActivePowerGeneration = DEFAULT_POWER_LIMIT;
        }
        // undefined max reactive power
        if (Double.isNaN(maximumPermissibleActivePowerGeneration)) {
            LOGGER.info("Node {}: maximum active power is undefined, set value to {}",
                    code, -DEFAULT_POWER_LIMIT);
            maximumPermissibleActivePowerGeneration = -DEFAULT_POWER_LIMIT;
        }
        // inverted active power limits
        if (minimumPermissibleActivePowerGeneration < maximumPermissibleActivePowerGeneration) {
            LOGGER.warn("Node {}: active power limits are inverted ({}, {}), swap values",
                    code, minimumPermissibleActivePowerGeneration, maximumPermissibleActivePowerGeneration);
            double tmp = minimumPermissibleActivePowerGeneration;
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

    private void fixVoltage(ReportNode reportNode) {
        // ---- VOLTAGE FIXES ----

        // PV and undefined voltage, switch to PQ
        if (isRegulatingVoltage() && (Double.isNaN(voltageReference) || voltageReference < 0.0001)) {
            reportNode.newReportNode()
                .withMessageTemplate("PvUndefinedVoltage", "Node ${node}: voltage is regulated, but voltage setpoint is null (${voltageReference}), switch type code to PQ")
                .withUntypedValue("node", code.toString())
                .withUntypedValue("voltageReference", voltageReference)
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
            LOGGER.warn("Node {}: voltage is regulated, but voltage setpoint is null ({}), switch type code to {}",
                    code, voltageReference, UcteNodeTypeCode.PQ);
            typeCode = UcteNodeTypeCode.PQ;
        }

        // PV and incoherent voltage reference
        if (isRegulatingVoltage()) {
            double nominalVoltage = code.getVoltageLevelCode().getVoltageLevel();
            if (nominalVoltage > LOW_NOMINAL_VOLTAGE && (voltageReference < LOW_VOLTAGE_FACTOR * nominalVoltage
                    || voltageReference > HIGH_VOLTAGE_FACTOR * nominalVoltage)) {
                LOGGER.warn("Node {}: voltage is regulated, but voltage setpoint is too far for nominal voltage ({} kV)",
                        code, voltageReference);
            }
        }
    }

    private void fixReactivePower(ReportNode reportNode) {
        // ---- REACTIVE POWER FIXES ----

        // PQ and undefined reactive power
        if (!isRegulatingVoltage() && Double.isNaN(reactivePowerGeneration)) {
            reportNode.newReportNode()
                .withMessageTemplate("PqUndefinedReactivePower", "Node ${node}: voltage is not regulated but reactive power is undefined, set value to 0")
                .withUntypedValue("node", code.toString())
                .withSeverity(TypedValue.WARN_SEVERITY)
                .add();
            LOGGER.warn("Node {}: voltage is not regulated but reactive power is undefined, set value to 0", code);
            reactivePowerGeneration = 0;
        }
        // undefined min reactive power
        if (Double.isNaN(minimumPermissibleReactivePowerGeneration)) {
            LOGGER.info("Node {}: minimum reactive power is undefined, set value to {}",
                    code, DEFAULT_POWER_LIMIT);
            minimumPermissibleReactivePowerGeneration = DEFAULT_POWER_LIMIT;
        }
        // undefined max reactive power
        if (Double.isNaN(maximumPermissibleReactivePowerGeneration)) {
            LOGGER.info("Node {}: maximum reactive power is undefined, set value to {}",
                    code, -DEFAULT_POWER_LIMIT);
            maximumPermissibleReactivePowerGeneration = -DEFAULT_POWER_LIMIT;
        }
        // inverted reactive power limits
        if (minimumPermissibleReactivePowerGeneration < maximumPermissibleReactivePowerGeneration) {
            LOGGER.warn("Node {}: reactive power limits are inverted ({}, {}), swap values",
                    code, minimumPermissibleReactivePowerGeneration, maximumPermissibleReactivePowerGeneration);
            double tmp = minimumPermissibleReactivePowerGeneration;
            minimumPermissibleReactivePowerGeneration = maximumPermissibleReactivePowerGeneration;
            maximumPermissibleReactivePowerGeneration = tmp;
        }
        // reactive power is out of limits
        if (!Double.isNaN(reactivePowerGeneration) && reactivePowerGeneration < maximumPermissibleReactivePowerGeneration) {
            LOGGER.warn("Node {}: reactive power {} under maximum permissible value {}, shift maximum permissible value",
                    code, reactivePowerGeneration, maximumPermissibleReactivePowerGeneration);
            maximumPermissibleReactivePowerGeneration = reactivePowerGeneration;
        }
        if (!Double.isNaN(reactivePowerGeneration) && reactivePowerGeneration > minimumPermissibleReactivePowerGeneration) {
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof UcteNode ucteNode) {
            return this.compareTo(ucteNode) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public int compareTo(UcteNode ucteNode) {
        if (ucteNode == null) {
            throw new IllegalArgumentException("ucteNode should not be null");
        }
        return this.getCode().toString().compareTo(ucteNode.getCode().toString());
    }
}
