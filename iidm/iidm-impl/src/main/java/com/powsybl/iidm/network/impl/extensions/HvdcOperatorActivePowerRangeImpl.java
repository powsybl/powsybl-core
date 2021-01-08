/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;

import java.util.Objects;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class HvdcOperatorActivePowerRangeImpl extends AbstractExtension<HvdcLine> implements HvdcOperatorActivePowerRange {

    /**
     * Operator active power range from the converter station 1 to the converter station 2 in MW.
     */
    private float oprFromCS1toCS2;

    /**
     * Operator active power range from the converter station 2 to the converter station 1 in MW.
     */
    private float oprFromCS2toCS1;

    public HvdcOperatorActivePowerRangeImpl(HvdcLine hvdcLine, float oprFromCS1toCS2, float oprFromCS2toCS1) {
        super(hvdcLine);
        this.oprFromCS1toCS2 = checkOPR(oprFromCS1toCS2, hvdcLine.getConverterStation1(), hvdcLine.getConverterStation2());
        this.oprFromCS2toCS1 = checkOPR(oprFromCS2toCS1, hvdcLine.getConverterStation2(), hvdcLine.getConverterStation1());
    }

    @Override
    public float getOprFromCS1toCS2() {
        return oprFromCS1toCS2;
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS1toCS2(float oprFromCS1toCS2) {
        this.oprFromCS1toCS2 = checkOPR(oprFromCS1toCS2, getExtendable().getConverterStation1(), getExtendable().getConverterStation2());
        return this;
    }

    @Override
    public float getOprFromCS2toCS1() {
        return oprFromCS2toCS1;
    }

    @Override
    public HvdcOperatorActivePowerRangeImpl setOprFromCS2toCS1(float oprFromCS2toCS1) {
        this.oprFromCS2toCS1 = checkOPR(oprFromCS2toCS1, getExtendable().getConverterStation1(), getExtendable().getConverterStation2());
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
        return Float.compare(that.oprFromCS1toCS2, oprFromCS1toCS2) == 0 &&
                Float.compare(that.oprFromCS2toCS1, oprFromCS2toCS1) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(oprFromCS1toCS2, oprFromCS2toCS1);
    }
}
