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
 * - increase or decrease the targetV of a generator.
 * - move to voltage control or remove from voltage control.
 * - change targetQ of a generator that is not a voltage controller.
 * - change targetV of a generator that is a voltage controller.
 *
 * @author Hadrien Godard <hadrien.godard@artelys.com>
 * @author Anne Tilloy <anne.tilloy@rte-france.com>
 */
public class GeneratorAction extends AbstractAction {

    public static final String NAME = "GENERATOR";

    private final String generatorId;
    private Boolean activePowerRelativeValue; // true if it is a relative variation, false if it is a new targetP
    private Double activePowerValue; // could be a new targetP if relativeVariation equals false or a relative variation of targetP.
    private Boolean voltageRegulatorOn;
    private Double targetV; // absolute value only.
    private Double targetQ; // absolute value only.

    public GeneratorAction(String id, String generatorId) {
        super(id);
        this.generatorId = Objects.requireNonNull(generatorId);
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

    public GeneratorAction setActivePowerRelativeValue(Boolean activePowerRelativeValue) {
        this.activePowerRelativeValue = activePowerRelativeValue;
        return this;
    }

    public Optional<Double> getActivePowerValue() {
        return Optional.ofNullable(activePowerValue);
    }

    public GeneratorAction setActivePowerValue(Double activePowerValue) {
        this.activePowerValue = activePowerValue;
        return this;
    }

    public Optional<Boolean> isVoltageRegulatorOn() {
        return Optional.ofNullable(voltageRegulatorOn);
    }

    public GeneratorAction setVoltageRegulatorOn(Boolean voltageRegulatorOn) {
        this.voltageRegulatorOn = voltageRegulatorOn;
        return this;
    }

    public Optional<Double> getTargetV() {
        return Optional.ofNullable(targetV);
    }

    public GeneratorAction setTargetV(Double targetV) {
        this.targetV = targetV;
        return this;
    }

    public Optional<Double> getTargetQ() {
        return Optional.ofNullable(targetQ);
    }

    public GeneratorAction setTargetQ(Double targetQ) {
        this.targetQ = targetQ;
        return this;
    }
}
