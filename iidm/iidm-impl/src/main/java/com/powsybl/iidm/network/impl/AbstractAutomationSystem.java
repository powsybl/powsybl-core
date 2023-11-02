/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.util.trove.TBooleanArrayList;
import com.powsybl.iidm.network.AutomationSystem;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
abstract class AbstractAutomationSystem<I extends AutomationSystem<I>> extends AbstractIdentifiable<I> implements AutomationSystem<I> {

    private final TBooleanArrayList enabled;

    AbstractAutomationSystem(String id, String name, boolean enabled) {
        super(id, name);
        int variantArraySize = getNetwork().getVariantManager().getVariantArraySize();
        this.enabled = new TBooleanArrayList(variantArraySize);
        for (int i = 0; i < variantArraySize; i++) {
            this.enabled.add(enabled);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled.get(getNetwork().getVariantIndex());
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(getNetwork().getVariantIndex(), enabled);
    }
}
