/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.impl.AbstractMultiVariantIdentifiableExtension;
import gnu.trove.list.array.TFloatArrayList;

import java.util.Objects;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public class HvdcOperatorActivePowerRangeImpl extends AbstractMultiVariantIdentifiableExtension<HvdcLine> implements HvdcOperatorActivePowerRange {

    /**
     * Operator active power range from the converter station 1 to the converter station 2 in MW.
     */
    private TFloatArrayList oprFromCS1toCS2;

    /**
     * Operator active power range from the converter station 2 to the converter station 1 in MW.
     */
    private TFloatArrayList oprFromCS2toCS1;

    public HvdcOperatorActivePowerRangeImpl(HvdcLine hvdcLine, float oprFromCS1toCS2, float oprFromCS2toCS1) {
        super(hvdcLine);
        int variantArraySize = getVariantManagerHolder().getVariantManager().getVariantArraySize();
        this.oprFromCS1toCS2 = new TFloatArrayList(variantArraySize);
        this.oprFromCS2toCS1 = new TFloatArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.oprFromCS1toCS2.add(checkOPR(oprFromCS1toCS2, hvdcLine.getConverterStation1(), hvdcLine.getConverterStation2()));
            this.oprFromCS2toCS1.add(checkOPR(oprFromCS2toCS1, hvdcLine.getConverterStation2(), hvdcLine.getConverterStation1()));
        }
    }

    @Override
    public float getOprFromCS1toCS2() {
        return oprFromCS1toCS2.get(getVariantIndex());
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS1toCS2(float oprFromCS1toCS2) {
        this.oprFromCS1toCS2.set(getVariantIndex(), checkOPR(oprFromCS1toCS2, getExtendable().getConverterStation1(),
                getExtendable().getConverterStation2()));
        return this;
    }

    @Override
    public float getOprFromCS2toCS1() {
        return oprFromCS2toCS1.get(getVariantIndex());
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS2toCS1(float oprFromCS2toCS1) {
        this.oprFromCS2toCS1.set(getVariantIndex(), checkOPR(oprFromCS2toCS1, getExtendable().getConverterStation1(),
                getExtendable().getConverterStation2()));
        return this;
    }

    private float checkOPR(float opr, HvdcConverterStation<?> from, HvdcConverterStation<?> to) {
        if ((!Float.isNaN(opr)) && (opr < 0)) {
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
        return Float.compare(that.oprFromCS1toCS2.get(that.getVariantManagerHolder().getVariantIndex()),
                oprFromCS1toCS2.get(getVariantIndex())) == 0 &&
                Float.compare(that.oprFromCS2toCS1.get(that.getVariantManagerHolder().getVariantIndex()),
                        oprFromCS2toCS1.get(getVariantIndex())) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oprFromCS1toCS2.get(getVariantIndex()), oprFromCS2toCS1.get(getVariantIndex()));
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        oprFromCS1toCS2.ensureCapacity(oprFromCS1toCS2.size() + number);
        oprFromCS2toCS1.ensureCapacity(oprFromCS2toCS1.size() + number);
        for (int i = 0; i < number; ++i) {
            oprFromCS1toCS2.add(oprFromCS1toCS2.get(sourceIndex));
            oprFromCS2toCS1.add(oprFromCS2toCS1.get(sourceIndex));
        }
    }

    @Override
    public void reduceVariantArraySize(int number) {
        oprFromCS1toCS2.remove(oprFromCS1toCS2.size() - number, number);
        oprFromCS2toCS1.remove(oprFromCS2toCS1.size() - number, number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        // Does nothing
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        for (int index : indexes) {
            oprFromCS1toCS2.set(index, oprFromCS1toCS2.get(sourceIndex));
            oprFromCS2toCS1.set(index, oprFromCS2toCS1.get(sourceIndex));
        }
    }
}
