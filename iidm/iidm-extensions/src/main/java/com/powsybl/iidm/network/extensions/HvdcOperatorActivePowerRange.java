/*
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 */

package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.HvdcLine;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface HvdcOperatorActivePowerRange extends Extension<HvdcLine> {

    @Override
    default String getName() {
        return "hvdcOperatorActivePowerRange";
    }

    float getOprFromCS1toCS2();

    HvdcOperatorActivePowerRange setOprFromCS1toCS2(float oprFromCS1toCS2);

    float getOprFromCS2toCS1();

    HvdcOperatorActivePowerRange setOprFromCS2toCS1(float oprFromCS2toCS1);
}
