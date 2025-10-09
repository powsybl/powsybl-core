/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.iidm.network.*;

/**
 * A default implementation of {@link NetworkPredicate} that keeps everything.
 *
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public class DefaultNetworkPredicate implements NetworkPredicate {

    @Override
    public boolean test(Substation substation) {
        return true;
    }

    @Override
    public boolean test(VoltageLevel voltageLevel) {
        return true;
    }
}
