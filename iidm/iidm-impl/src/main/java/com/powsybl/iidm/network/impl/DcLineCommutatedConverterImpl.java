/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcLineCommutatedConverterImpl extends AbstractDcConverter<DcLineCommutatedConverter> implements DcLineCommutatedConverter {

    public static final String REACTIVE_MODEL = "reactiveModel";
    public static final String POWER_FACTOR = "powerFactor";
    private ReactiveModel reactiveModel;

    private double powerFactor;

    DcLineCommutatedConverterImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious,
                                  double idleLoss, double switchingLoss, double resistiveLoss,
                                  TerminalExt pccTerminal, ControlMode controlMode, double targetP, double targetVdc, ReactiveModel reactiveModel, double powerFactor) {
        super(ref, id, name, fictitious, idleLoss, switchingLoss, resistiveLoss,
                pccTerminal, controlMode, targetP, targetVdc);
        this.reactiveModel = reactiveModel;
        this.powerFactor = powerFactor;
    }

    @Override
    protected String getTypeDescription() {
        return "DC Line Commutated Converter";
    }

    @Override
    public ReactiveModel getReactiveModel() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, REACTIVE_MODEL);
        return this.reactiveModel;
    }

    @Override
    public DcLineCommutatedConverter setReactiveModel(ReactiveModel reactiveModel) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, REACTIVE_MODEL);
        ValidationUtil.checkLccReactiveModel(this, reactiveModel);
        ReactiveModel oldValue = this.reactiveModel;
        this.reactiveModel = reactiveModel;
        getNetwork().getListeners().notifyUpdate(this, REACTIVE_MODEL, oldValue, reactiveModel);
        return this;
    }

    @Override
    public double getPowerFactor() {
        ValidationUtil.checkAccessOfRemovedEquipment(this.id, this.removed, POWER_FACTOR);
        return this.powerFactor;
    }

    @Override
    public DcLineCommutatedConverter setPowerFactor(double powerFactor) {
        ValidationUtil.checkModifyOfRemovedEquipment(this.id, this.removed, POWER_FACTOR);
        ValidationUtil.checkPositivePowerFactor(this, powerFactor);
        double oldValue = this.powerFactor;
        this.powerFactor = powerFactor;
        getNetwork().getListeners().notifyUpdate(this, POWER_FACTOR, oldValue, powerFactor);
        return this;
    }

    @Override
    protected DcLineCommutatedConverter self() {
        return this;
    }
}
