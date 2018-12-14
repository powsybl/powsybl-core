/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

/**
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
class EurostagNetworkFilter implements NetworkFilter {

    @Override
    public boolean accept(Substation substation) {
        return substation.getId().equals("P1");
    }

    @Override
    public boolean accept(VoltageLevel voltageLevel) {
        return true;
    }

    @Override
    public boolean accept(Line line) {
        return false;
    }

    @Override
    public boolean accept(TwoWindingsTransformer transformer) {
        return true;
    }

    @Override
    public boolean accept(ThreeWindingsTransformer transformer) {
        return true;
    }

    @Override
    public boolean accept(HvdcLine hvdcLine) {
        return true;
    }
}
