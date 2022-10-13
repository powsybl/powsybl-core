/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.action;

import java.util.Objects;
import java.util.Optional;

/**
 * An action to:
 * - change the targetP of a generator, either by specifying a new absolute value (MW) or a relative change (MW).
 * - enable or disable the generator voltage control.
 * - change targetQ of a generator (MVar).
 * - change targetV of a generator (kV).
 *
 * @author Hadrien Godard <hadrien.godard@artelys.com>
 * @author Anne Tilloy <anne.tilloy@rte-france.com>
 */
public class GeneratorAction extends AbstractAction {

    public static final String NAME = "GENERATOR";

    private final String generatorId;
    private final Boolean activePowerRelativeValue; // true if it is a relative variation of targetP, false if it is a new targetP
    private final Double activePowerValue; // could be a new targetP if relativeVariation equals false or a relative variation of targetP.
    private final Boolean voltageRegulatorOn; // to change voltage control.
    private final Double targetV; // absolute value only.
    private final Double targetQ; // absolute value only.

    /**
     * @param id the id of the action.
     * @param generatorId the id of the generator on which the action would be applied.
     * @param activePowerRelativeValue True if the generator targetP variation is relative, False if absolute.
     * @param activePowerValue The new generator targetP (MW) if activePowerRelativeValue equals False, otherwise the relative variation of generator targetP (MW).
     * @param voltageRegulatorOn The new generator voltage regulator status.
     * @param targetV The new generator targetV (kV).
     * @param targetQ The new generator targetQ (MVar).
     */
    public GeneratorAction(String id, String generatorId, Boolean activePowerRelativeValue, Double activePowerValue,
                           Boolean voltageRegulatorOn, Double targetV, Double targetQ) {
        super(id);
        this.generatorId = Objects.requireNonNull(generatorId);
        this.activePowerRelativeValue = activePowerRelativeValue;
        this.activePowerValue = activePowerValue;
        this.voltageRegulatorOn = voltageRegulatorOn;
        this.targetV = targetV;
        this.targetQ = targetQ;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getGeneratorId() {
        return generatorId;
    }

    public Optional<Boolean> isActivePowerRelativeValue() {
        return Optional.ofNullable(activePowerRelativeValue);
    }

    public Optional<Double> getActivePowerValue() {
        return Optional.ofNullable(activePowerValue);
    }

    public Optional<Boolean> isVoltageRegulatorOn() {
        return Optional.ofNullable(voltageRegulatorOn);
    }

    public Optional<Double> getTargetV() {
        return Optional.ofNullable(targetV);
    }

    public Optional<Double> getTargetQ() {
        return Optional.ofNullable(targetQ);
    }

}
