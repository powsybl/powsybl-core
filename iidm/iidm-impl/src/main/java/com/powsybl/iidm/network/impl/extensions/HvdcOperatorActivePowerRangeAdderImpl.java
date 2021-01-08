/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.impl.extensions;

import com.powsybl.commons.extensions.AbstractExtensionAdder;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRange;
import com.powsybl.iidm.network.extensions.HvdcOperatorActivePowerRangeAdder;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 */
public class HvdcOperatorActivePowerRangeAdderImpl extends AbstractExtensionAdder<HvdcLine, HvdcOperatorActivePowerRange>
        implements HvdcOperatorActivePowerRangeAdder {

    /**
     * Operator active power range from the converter station 1 to the converter station 2 in MW.
     */
    private float oprFromCS1toCS2;

    /**
     * Operator active power range from the converter station 2 to the converter station 1 in MW.
     */
    private float oprFromCS2toCS1;

    public HvdcOperatorActivePowerRangeAdderImpl(HvdcLine hvdcLine) {
        super(hvdcLine);
    }

    @Override
    protected HvdcOperatorActivePowerRange createExtension(HvdcLine hvdcLine) {
        return new HvdcOperatorActivePowerRangeImpl(hvdcLine, oprFromCS1toCS2, oprFromCS2toCS1);
    }

    @Override
    public HvdcOperatorActivePowerRangeAdder withOprFromCS1toCS2(float oprFromCS1toCS2) {
        this.oprFromCS1toCS2 = oprFromCS1toCS2;
        return this;
    }

    @Override
    public HvdcOperatorActivePowerRangeAdder withOprFromCS2toCS1(float oprFromCS2toCS1) {
        this.oprFromCS2toCS1 = oprFromCS2toCS1;
        return this;
    }
}
