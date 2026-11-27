/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.util.fastutil.ExtendedFloatArrayList;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;

import java.util.Objects;

/**
 * @author Jérémy Labous {@literal <jlabous at silicom.fr>}
 * @author Paul Bui-Quang {@literal <paul.buiquang at rte-france.com>}
 */
public class HvdcOperatorActivePowerRangeImpl extends AbstractMultiVariantIdentifiableExtension<HvdcLine> implements HvdcOperatorActivePowerRange {

    /**
     * Operator active power range from the converter station 1 to the converter station 2 in MW.
     */
    private final ExtendedFloatArrayList oprFromCS1toCS2;

    /**
     * Operator active power range from the converter station 2 to the converter station 1 in MW.
     */
    private final ExtendedFloatArrayList oprFromCS2toCS1;

    public HvdcOperatorActivePowerRangeImpl(HvdcLine hvdcLine, float oprFromCS1toCS2, float oprFromCS2toCS1) {
        super(hvdcLine);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        checkOPR(oprFromCS1toCS2, hvdcLine.getConverterStation1(), hvdcLine.getConverterStation2());
        checkOPR(oprFromCS2toCS1, hvdcLine.getConverterStation2(), hvdcLine.getConverterStation1());
        this.oprFromCS1toCS2 = new ExtendedFloatArrayList(variantArraySize, oprFromCS1toCS2);
        this.oprFromCS2toCS1 = new ExtendedFloatArrayList(variantArraySize, oprFromCS2toCS1);
    }

    @Override
    public float getOprFromCS1toCS2() {
        return oprFromCS1toCS2.getFloat(getVariantIndex());
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS1toCS2(float oprFromCS1toCS2) {
        this.oprFromCS1toCS2.set(getVariantIndex(), checkOPR(oprFromCS1toCS2, getExtendable().getConverterStation1(),
                getExtendable().getConverterStation2()));
        return this;
    }

    @Override
    public float getOprFromCS2toCS1() {
        return oprFromCS2toCS1.getFloat(getVariantIndex());
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS2toCS1(float oprFromCS2toCS1) {
        this.oprFromCS2toCS1.set(getVariantIndex(), checkOPR(oprFromCS2toCS1, getExtendable().getConverterStation1(),
                getExtendable().getConverterStation2()));
        return this;
    }

    private float checkOPR(float opr, HvdcConverterStation<?> from, HvdcConverterStation<?> to) {
        if (!Float.isNaN(opr) && opr < 0) {
            String message = "OPR from " + from.getId() + " to " + to.getId() + " must be greater than 0 (current value " + Float.toString(opr) + ").";
            throw new IllegalArgumentException(message);
        }
        return opr;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HvdcOperatorActivePowerRangeImpl that = (HvdcOperatorActivePowerRangeImpl) o;
        return Float.compare(that.getOprFromCS1toCS2(), getOprFromCS1toCS2()) == 0 &&
                Float.compare(that.getOprFromCS2toCS1(), getOprFromCS2toCS1()) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOprFromCS1toCS2(), getOprFromCS2toCS1());
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        oprFromCS1toCS2.growAndFill(number, oprFromCS1toCS2.getFloat(sourceIndex));
        oprFromCS2toCS1.growAndFill(number, oprFromCS2toCS1.getFloat(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        oprFromCS1toCS2.removeElements(number);
        oprFromCS2toCS1.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            oprFromCS1toCS2.set(index, oprFromCS1toCS2.getFloat(sourceIndex));
            oprFromCS2toCS1.set(index, oprFromCS2toCS1.getFloat(sourceIndex));
        }
    }
}
