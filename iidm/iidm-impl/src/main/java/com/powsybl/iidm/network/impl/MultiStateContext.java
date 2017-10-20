/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class MultiStateContext implements StateContext {

    private int index;

    MultiStateContext(int initialStateIndex) {
        this.index = initialStateIndex;
    }

    @Override
    public int getStateIndex() {
        if (index == -1) {
            throw new PowsyblException("State not set");
        }
        return index;
    }

    @Override
    public void setStateIndex(int index) {
        this.index = index;
    }

    @Override
    public void resetIfStateIndexIs(int index) {
        if (this.index == index) {
            this.index = -1;
        }
    }
}
