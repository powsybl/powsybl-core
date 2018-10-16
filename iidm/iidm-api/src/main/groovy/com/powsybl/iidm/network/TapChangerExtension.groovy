/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

/**
 * @author Yichen Tang <yichen.tang at rte-france.com>
 */
public class TapChangerExtension {

    static int getStepCount(TapChanger self) {
        return self.getTapCount();
    }

    static TapChanger.Tap getStep(TapChanger self, int tapPosition) {
        return self.getTap(tapPosition);
    }

    static TapChanger.Tap getCurrentStep(TapChanger self) {
        return self.getCurrentTap();
    }
}
