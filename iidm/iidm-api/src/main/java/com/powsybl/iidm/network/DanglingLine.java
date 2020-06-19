/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A dangling line to model boundaries (X nodes).
 * <p>A dangling line is a component that aggregates a line chunk and a constant
 * power injection (fixed p0, q0).
 * <div>
 *    <object data="doc-files/danglingLine.svg" type="image/svg+xml"></object>
 * </div>
 * Electrical characteritics (r, x, g, b) corresponding to a percent of the
 * orginal line.
 * <p>r, x, g, b have to be consistent with the declared length of the dangling
 * line.
 * <p>To create a dangling line, see {@link DanglingLineAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see DanglingLineAdder
 */
public interface DanglingLine extends Injection<DanglingLine>, ReactiveLimitsHolder {

    /**
     * Get the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getP0();

    /**
     * Set the constant active power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    DanglingLine setP0(double p0);

    /**
     * Get the constant reactive power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    double getQ0();

    /**
     * Set the constant reactive power in MW.
     * <p>Depends on the working variant.
     * @see VariantManager
     */
    DanglingLine setQ0(double q0);

    /**
     * Get the series resistance in &#937;.
     */
    double getR();

    /**
     * Set the series resistance in &#937;.
     */
    DanglingLine setR(double r);

    /**
     * Get the series reactance in &#937;.
     */
    double getX();

    /**
     * Set the series reactance in &#937;.
     */
    DanglingLine setX(double x);

    /**
     * Get the shunt conductance in S.
     */
    double getG();

    /**
     * Set the shunt conductance in S.
     */
    DanglingLine setG(double g);

    /**
     * Get the shunt susceptance in S.
     */
    double getB();

    /**
     * Set the shunt susceptance in S.
     */
    DanglingLine setB(double b);

    /**
     * <p>Get the active power setpoint in MW.</p>
     * <p>The active power setpoint follows a load sign convention.</p>
     * <p>Depends on the working variant.</p>
     * @return the reactive power setpoint
     */
    default double getActivePowerSetpoint() {
        return Double.NaN;
    }

    /**
     * <p>Set the active power setpoint in MW.</p>
     * <p>Depends on the working variant.</p>
     * @param activePowerSetpoint the active power setpoint
     * @return this to allow method chaining
     */
    default DanglingLine setActivePowerSetpoint(double activePowerSetpoint) {
        return this;
    }

    /**
     * <p>Get the reactive power setpoint in MVAR.</p>
     * <p>The reactive power setpoint follows a load sign convention.</p>
     * <p>Depends on the working variant.</p>
     * @return the reactive power setpoint
     */
    default double getReactivePowerSetpoint() {
        return Double.NaN;
    }

    /**
     * <p>Set the reactive power setpoint in MVAR.</p>
     * <p>Depends on the working variant.</p>
     * @param reactivePowerSetpoint the reactive power setpoint
     * @return this to allow method chaining
     */
    default DanglingLine setReactivePowerSetpoint(double reactivePowerSetpoint) {
        return this;
    }

    /**
     * Get the voltage regulation status.
     */
    default boolean isVoltageRegulationOn() {
        return false;
    }

    /**
     * Set the voltage regulation status.
     */
    default DanglingLine setVoltageRegulationOn(boolean voltageRegulationOn) {
        return this;
    }

    /**
     * <p>Get the voltage setpoint in Kv.</p>
     * <p>Depends on the working variant.</p>
     * @return the voltage setpoint
     */
    default double getVoltageSetpoint() {
        return Double.NaN;
    }

    /**
     * <p>Set the voltage setpoint in Kv.</p>
     * <p>Depends on the working variant.</p>
     * @param voltageSetpoint the voltage setpoint
     * @return this to allow method chaining
     */
    default DanglingLine setVoltageSetpoint(double voltageSetpoint) {
        return this;
    }

    /**
     * Get the UCTE Xnode code corresponding to this dangling line in the case
     * where the line is a boundary, return null otherwise.
     */
    String getUcteXnodeCode();

    CurrentLimits getCurrentLimits();

    CurrentLimitsAdder newCurrentLimits();

}
