/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.security.action;

import com.powsybl.iidm.network.StaticVarCompensator;

import java.util.Optional;
import java.util.OptionalDouble;

/**
 * @author Etienne Lesot <etienne.lesot@rte-france.com>
 */
public class StaticVarCompensatorAction extends AbstractAction {

    public static final String NAME = "STATIC_VAC_COMPENSATOR";
    private final String staticVarCompensatorId;
    private final StaticVarCompensator.RegulationMode regulationMode;
    private final String regulatingTerminal;
    private final Double voltageSetPoint;
    private final Double reactiveSetPoint;

    protected StaticVarCompensatorAction(String id, String staticVarCompensatorId,
                                         StaticVarCompensator.RegulationMode regulationMode,
                                         String regulatingTerminal, Double voltageSetPoint,
                                         Double reactiveSetPoint) {
        super(id);
        this.staticVarCompensatorId = staticVarCompensatorId;
        this.regulationMode = regulationMode;
        this.regulatingTerminal = regulatingTerminal;
        this.voltageSetPoint = voltageSetPoint;
        this.reactiveSetPoint = reactiveSetPoint;
    }

    @Override
    public String getType() {
        return NAME;
    }

    public String getStaticVarCompensatorId() {
        return staticVarCompensatorId;
    }

    public Optional<StaticVarCompensator.RegulationMode> getRegulationMode() {
        return Optional.ofNullable(regulationMode);
    }

    public Optional<String> getRegulatingTerminal() {
        return Optional.ofNullable(regulatingTerminal);
    }

    public OptionalDouble getVoltageSetPoint() {
        return voltageSetPoint == null ? OptionalDouble.empty() : OptionalDouble.of(voltageSetPoint);
    }

    public OptionalDouble getReactiveSetPoint() {
        return reactiveSetPoint == null ? OptionalDouble.empty() : OptionalDouble.of(reactiveSetPoint);
    }
}
