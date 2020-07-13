/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * A power generator.
 *
 * **Characteristics**
 *
 * | Attribute | Type | Unit | Required | Default value | Description |
 * | --------- | ---- | ---- | -------- | ------------- | ----------- |
 * | Id | String | - | yes | - | The ID of the generator |
 * | Name | String | - | no | - | The name of the generator |
 * | EnergySource | `EnergySource` | - | yes | `OTHER` | The energy source |
 * | MinP | double | MW | yes | - | The minimal active power |
 * | MaxP | double | MW | yes | - | The maximum active power |
 * | RegulatingTerminal | `Terminal` | - | no | The generator's terminal | The terminal used for regulation |
 * | VoltageRegulatorOn | boolean | - | yes | - | The voltage regulator status |
 * | TargetP | double | MW | yes | - | The active power target |
 * | TargetQ | double | MVAr | only if `VoltageRegulatorOn` is set to `false` | - | The reactive power target |
 * | TargetV | double | kV | only if `VoltageRegulatorOn` is set to `true` | - | The voltage target |
 * | RatedS | double | MVA | yes | - | The rated nominal power |
 * | ReactiveLimits | - | - | no | min/max | Operational limits of the generator (P/Q/U diagram) |
 *
 *<p>
 * To create a generator, see {@link GeneratorAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see GeneratorAdder
 * @see MinMaxReactiveLimits
 * @see ReactiveCapabilityCurve
 */
public interface Generator extends Injection<Generator>, ReactiveLimitsHolder {

    /**
     * Get the energy source.
     */
    EnergySource getEnergySource();

    Generator setEnergySource(EnergySource energySource);

    /**
     * Get the maximal active power in MW.
     */
    double getMaxP();

    /**
     * Set the maximal active power in MW.
     */
    Generator setMaxP(double maxP);

    /**
     * Get the minimal active power in MW.
     */
    double getMinP();

    /**
     * Set the minimal active power in MW.
     */
    Generator setMinP(double minP);

    /**
     * Get the voltage regulator status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    boolean isVoltageRegulatorOn();

    /**
     * Set the voltage regulator status.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Generator setVoltageRegulatorOn(boolean voltageRegulatorOn);

    /**
     * Get the terminal used for regulation.
     * @return the terminal used for regulation
     */
    Terminal getRegulatingTerminal();

    Generator setRegulatingTerminal(Terminal regulatingTerminal);

    /**
     * Get the voltage target in kV.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetV();

    /**
     * Set the voltage target in kV.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Generator setTargetV(double targetV);

    /**
     * Get the active power target in MW.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetP();

    /**
     * Set the active power target in MW.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Generator setTargetP(double targetP);

    /**
     * Get the reactive power target in MVAR.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    double getTargetQ();

    /**
     * Set the reactive power target in MVAR.
     * <p>
     * Depends on the working variant.
     * @see VariantManager
     */
    Generator setTargetQ(double targetQ);

    /**
     * Get the rated nominal power (apparent power rating) in MVA.
     * @return the rated nominal power in MVA or NaN if not defined
     */
    double getRatedS();

    Generator setRatedS(double ratedS);
}
