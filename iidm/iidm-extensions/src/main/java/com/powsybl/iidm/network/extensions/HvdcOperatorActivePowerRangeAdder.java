/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.HvdcLine;

/**
 * @author Jérémy Labous <jlabous at silicom.fr>
 * @author Paul Bui-Quang <paul.buiquang at rte-france.com>
 */
public interface HvdcOperatorActivePowerRangeAdder extends ExtensionAdder<HvdcLine, HvdcOperatorActivePowerRange> {

    @Override
    default Class<HvdcOperatorActivePowerRange> getExtensionClass() {
        return HvdcOperatorActivePowerRange.class;
    }

    HvdcOperatorActivePowerRangeAdder withOprFromCS1toCS2(float oprFromCS1toCS2);

    HvdcOperatorActivePowerRangeAdder withOprFromCS2toCS1(float oprFromCS2toCS1);
}
