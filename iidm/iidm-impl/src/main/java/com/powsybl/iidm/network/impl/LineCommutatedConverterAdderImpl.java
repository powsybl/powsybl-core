/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class LineCommutatedConverterAdderImpl extends AbstractAcDcConverterAdder<LineCommutatedConverterAdderImpl> implements LineCommutatedConverterAdder {

    private LineCommutatedConverter.ReactiveModel reactiveModel = LineCommutatedConverter.ReactiveModel.FIXED_POWER_FACTOR;

    private double powerFactor = 0.5;

    LineCommutatedConverterAdderImpl(VoltageLevelExt voltageLevel) {
        super(voltageLevel);
    }

    @Override
    protected String getTypeDescription() {
        return "AC/DC Line Commutated Converter";
    }

    @Override
    public LineCommutatedConverterAdder setReactiveModel(LineCommutatedConverter.ReactiveModel reactiveModel) {
        this.reactiveModel = reactiveModel;
        return this;
    }

    @Override
    public LineCommutatedConverterAdder setPowerFactor(double powerFactor) {
        this.powerFactor = powerFactor;
        return this;
    }

    @Override
    public LineCommutatedConverter add() {
        // TODO checks
        // TODO / note: dcNodes and voltage level must be in same network
        String id = checkAndGetUniqueId();
        super.preCheck();
        ValidationUtil.checkPositivePowerFactor(this, powerFactor);
        ValidationUtil.checkLccReactiveModel(this, reactiveModel);
        LineCommutatedConverterImpl dcCsConverter = new LineCommutatedConverterImpl(voltageLevel.getNetworkRef(), id, getName(), isFictitious(),
                idleLoss, switchingLoss, resistiveLoss,
                pccTerminal, controlMode, targetP, targetVdc,
                reactiveModel, powerFactor);
        super.checkAndAdd(dcCsConverter);
        return dcCsConverter;
    }

    @Override
    protected LineCommutatedConverterAdderImpl self() {
        return this;
    }
}
