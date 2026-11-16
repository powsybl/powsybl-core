/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class MultiVariantContext implements VariantContext {

    private int index;

    MultiVariantContext(int index) {
        this.index = index;
    }

    @Override
    public int getVariantIndex() {
        if (index == -1) {
            throw new PowsyblException("Variant index not set");
        }
        return index;
    }

    @Override
    public void setVariantIndex(int index) {
        this.index = index;
    }

    @Override
    public void resetIfVariantIndexIs(int index) {
        if (this.index == index) {
            this.index = -1;
        }
    }

    @Override
    public boolean isIndexSet() {
        return this.index != -1;
    }
}
