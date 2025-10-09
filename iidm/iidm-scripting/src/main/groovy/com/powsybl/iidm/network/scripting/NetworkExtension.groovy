/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.iidm.network.Network

/**
 * @author Chamseddine BENHAMED {@literal <chamseddine.benhamed at rte-france.com>}
 */
class NetworkExtension {
    static Object getShunt(Network self, String id) {
       self.getShuntCompensator(id)
    }

    static Object getShunts(Network self) {
        self.getShuntCompensators()
    }

    static Object getShuntStream(Network self) {
        self.getShuntCompensatorStream()
    }

    static Object getShuntCount(Network self) {
        self.getShuntCompensatorCount()
    }
}
