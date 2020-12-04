/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.extensions2.LoadDetail;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import gnu.trove.list.array.TFloatArrayList;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public class LoadDetailImpl extends AbstractMultiVariantIdentifiableExtension<Load> implements LoadDetail {

    private final TFloatArrayList fixedActivePower;

    private final TFloatArrayList fixedReactivePower;

    private final TFloatArrayList variableActivePower;

    private final TFloatArrayList variableReactivePower;

    public LoadDetailImpl(Load load, float fixedActivePower, float fixedReactivePower,
                          float variableActivePower, float variableReactivePower) {
        super(load);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.fixedActivePower = new TFloatArrayList(variantArraySize);
        this.fixedReactivePower = new TFloatArrayList(variantArraySize);
        this.variableActivePower = new TFloatArrayList(variantArraySize);
        this.variableReactivePower = new TFloatArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.fixedActivePower.add(checkPower(fixedActivePower, "Invalid fixedActivePower"));
            this.fixedReactivePower.add(checkPower(fixedReactivePower, "Invalid fixedReactivePower"));
            this.variableActivePower.add(checkPower(variableActivePower, "Invalid variableActivePower"));
            this.variableReactivePower.add(checkPower(variableReactivePower, "Invalid variableReactivePower"));
        }
    }

    public float getFixedActivePower() {
        return fixedActivePower.get(getVariantIndex());
    }

    @Override
    public LoadDetail setFixedActivePower(float fixedActivePower) {
        checkPower(fixedActivePower, "Invalid fixedActivePower");
        this.fixedActivePower.set(getVariantIndex(), fixedActivePower);
        return this;
    }

    @Override
    public float getFixedReactivePower() {
        return fixedReactivePower.get(getVariantIndex());
    }

    @Override
    public LoadDetail setFixedReactivePower(float fixedReactivePower) {
        checkPower(fixedReactivePower, "Invalid fixedReactivePower");
        this.fixedReactivePower.set(getVariantIndex(), fixedReactivePower);
        return this;
    }

    @Override
    public float getVariableActivePower() {
        return variableActivePower.get(getVariantIndex());
    }

    @Override
    public LoadDetail setVariableActivePower(float variableActivePower) {
        checkPower(variableActivePower, "Invalid variableActivePower");
        this.variableActivePower.set(getVariantIndex(), variableActivePower);
        return this;
    }

    @Override
    public float getVariableReactivePower() {
        return variableReactivePower.get(getVariantIndex());
    }

    @Override
    public LoadDetail setVariableReactivePower(float variableReactivePower) {
        checkPower(variableReactivePower, "Invalid variableReactivePower");
        this.variableReactivePower.set(getVariantIndex(), variableReactivePower);
        return this;
    }

    private static float checkPower(float power, String errorMessage) {
        if (Float.isNaN(power)) {
            throw new IllegalArgumentException(errorMessage);
        }
        return power;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        fixedActivePower.ensureCapacity(fixedActivePower.size() + number);
        fixedReactivePower.ensureCapacity(fixedReactivePower.size() + number);
        variableActivePower.ensureCapacity(variableActivePower.size() + number);
        variableReactivePower.ensureCapacity(variableReactivePower.size() + number);
        for (int i = 0; i < number; ++i) {
            fixedActivePower.add(fixedActivePower.get(sourceIndex));
            fixedReactivePower.add(fixedReactivePower.get(sourceIndex));
            variableActivePower.add(variableActivePower.get(sourceIndex));
            variableReactivePower.add(variableReactivePower.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        fixedActivePower.remove(fixedActivePower.size() - number, number);
        fixedReactivePower.remove(fixedReactivePower.size() - number, number);
        variableActivePower.remove(variableActivePower.size() - number, number);
        variableReactivePower.remove(variableReactivePower.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
        // TODO: maybe set default/undefined values for deleted variant index
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            fixedActivePower.set(index, fixedActivePower.get(sourceIndex));
            fixedReactivePower.set(index, fixedReactivePower.get(sourceIndex));
            variableActivePower.set(index, variableActivePower.get(sourceIndex));
            variableReactivePower.set(index, variableReactivePower.get(sourceIndex));
        }
    }
}
