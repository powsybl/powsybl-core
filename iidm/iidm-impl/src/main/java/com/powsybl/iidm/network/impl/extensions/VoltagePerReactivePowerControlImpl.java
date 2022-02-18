/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.extensions.VoltagePerReactivePowerControl;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import gnu.trove.list.array.TDoubleArrayList;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Anne Tilloy <anne.tilloy at rte-france.com>
 */
public class VoltagePerReactivePowerControlImpl extends AbstractMultiVariantIdentifiableExtension<StaticVarCompensator> implements VoltagePerReactivePowerControl {

    private TDoubleArrayList slope;

    public VoltagePerReactivePowerControlImpl(StaticVarCompensator svc, double slope) {
        super(svc);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.slope = new TDoubleArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.slope.add(checkSlope(slope));
        }
    }

    @Override
    public double getSlope() {
        return slope.get(getVariantIndex());
    }

    public VoltagePerReactivePowerControl setSlope(double slope) {
        this.slope.set(getVariantIndex(), checkSlope(slope));
        return this;
    }

    private double checkSlope(double slope) {
        if (Double.isNaN(slope)) {
            throw new PowsyblException("Undefined value for slope");
        }
        if (slope < 0) {
            throw new PowsyblException("Slope value of SVC " + getExtendable().getId() + " must be positive: " + slope);
        }
        return slope;
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        slope.ensureCapacity(slope.size() + number);
        for (int i = 0; i < number; ++i) {
            slope.add(slope.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        slope.remove(slope.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            slope.set(index, slope.get(sourceIndex));
        }
    }
}
