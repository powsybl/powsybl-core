/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.util.fastutil.ExtendedDoubleArrayList;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions.LoadDetail;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;

/**
 * @author Miora Ralambotiana {@literal <miora.ralambotiana at rte-france.com>}
 */
public class LoadDetailImpl extends AbstractMultiVariantIdentifiableExtension<Load> implements LoadDetail {

    private final ExtendedDoubleArrayList fixedActivePower;

    private final ExtendedDoubleArrayList fixedReactivePower;

    private final ExtendedDoubleArrayList variableActivePower;

    private final ExtendedDoubleArrayList variableReactivePower;

    public LoadDetailImpl(Load load, double fixedActivePower, double fixedReactivePower,
                double variableActivePower, double variableReactivePower) {
        super(load);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        checkPower(fixedActivePower, "Invalid fixedActivePower", load);
        checkPower(fixedReactivePower, "Invalid fixedReactivePower", load);
        checkPower(variableActivePower, "Invalid variableActivePower", load);
        checkPower(variableReactivePower, "Invalid variableReactivePower", load);
        this.fixedActivePower = new ExtendedDoubleArrayList(variantArraySize, fixedActivePower);
        this.fixedReactivePower = new ExtendedDoubleArrayList(variantArraySize, fixedReactivePower);
        this.variableActivePower = new ExtendedDoubleArrayList(variantArraySize, variableActivePower);
        this.variableReactivePower = new ExtendedDoubleArrayList(variantArraySize, variableReactivePower);
    }

    public double getFixedActivePower() {
        return fixedActivePower.getDouble(getVariantIndex());
    }

    @Override
    public LoadDetail setFixedActivePower(double fixedActivePower) {
        checkPower(fixedActivePower, "Invalid fixedActivePower", this.getExtendable());
        this.fixedActivePower.set(getVariantIndex(), fixedActivePower);
        return this;
    }

    @Override
    public double getFixedReactivePower() {
        return fixedReactivePower.getDouble(getVariantIndex());
    }

    @Override
    public LoadDetail setFixedReactivePower(double fixedReactivePower) {
        checkPower(fixedReactivePower, "Invalid fixedReactivePower", this.getExtendable());
        this.fixedReactivePower.set(getVariantIndex(), fixedReactivePower);
        return this;
    }

    @Override
    public double getVariableActivePower() {
        return variableActivePower.getDouble(getVariantIndex());
    }

    @Override
    public LoadDetail setVariableActivePower(double variableActivePower) {
        checkPower(variableActivePower, "Invalid variableActivePower", this.getExtendable());
        this.variableActivePower.set(getVariantIndex(), variableActivePower);
        return this;
    }

    @Override
    public double getVariableReactivePower() {
        return variableReactivePower.getDouble(getVariantIndex());
    }

    @Override
    public LoadDetail setVariableReactivePower(double variableReactivePower) {
        checkPower(variableReactivePower, "Invalid variableReactivePower", this.getExtendable());
        this.variableReactivePower.set(getVariantIndex(), variableReactivePower);
        return this;
    }

    private static void checkPower(double power, String errorMessage, Load load) {
        if (Double.isNaN(power)) {
            throw new IllegalArgumentException(String.format("%s (%s) for load %s",
                errorMessage,
                power,
                load.getId()));
        }
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        fixedActivePower.growAndFill(number, fixedActivePower.getDouble(sourceIndex));
        fixedReactivePower.growAndFill(number, fixedReactivePower.getDouble(sourceIndex));
        variableActivePower.growAndFill(number, variableActivePower.getDouble(sourceIndex));
        variableReactivePower.growAndFill(number, variableReactivePower.getDouble(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        fixedActivePower.removeElements(number);
        fixedReactivePower.removeElements(number);
        variableActivePower.removeElements(number);
        variableReactivePower.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
        // TODO: maybe set default/undefined values for deleted variant index
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            fixedActivePower.set(index, fixedActivePower.getDouble(sourceIndex));
            fixedReactivePower.set(index, fixedReactivePower.getDouble(sourceIndex));
            variableActivePower.set(index, variableActivePower.getDouble(sourceIndex));
            variableReactivePower.set(index, variableReactivePower.getDouble(sourceIndex));
        }
    }
}
