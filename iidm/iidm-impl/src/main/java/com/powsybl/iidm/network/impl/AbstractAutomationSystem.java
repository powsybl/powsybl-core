/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.commons.util.fastutil.BooleanArrayListHack;
import com.powsybl.iidm.network.AutomationSystem;

import java.util.Objects;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
abstract class AbstractAutomationSystem<I extends AutomationSystem<I>> extends AbstractIdentifiable<I> implements AutomationSystem<I> {
    private final BooleanArrayListHack enabled;

    AbstractAutomationSystem(Ref<NetworkImpl> networkRef, String id, String name, boolean enabled) {
        super(id, name);
        Objects.requireNonNull(networkRef);

        int variantArraySize = networkRef.get().getVariantManager().getVariantArraySize();
        this.enabled = new BooleanArrayListHack(variantArraySize, enabled);
    }

    @Override
    public boolean isEnabled() {
        return enabled.getBoolean(getNetwork().getVariantIndex());
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(getNetwork().getVariantIndex(), enabled);
    }

    @Override
    public void extendVariantArraySize(int initVariantArraySize, int number, int sourceIndex) {
        super.extendVariantArraySize(initVariantArraySize, number, sourceIndex);
        enabled.growAndFill(number, enabled.getBoolean(sourceIndex));
    }

    @Override
    public void reduceVariantArraySize(int number) {
        super.reduceVariantArraySize(number);
        enabled.removeElements(number);
    }

    @Override
    public void deleteVariantArrayElement(int index) {
        super.deleteVariantArrayElement(index);
        // nothing to do
    }

    @Override
    public void allocateVariantArrayElement(int[] indexes, int sourceIndex) {
        super.allocateVariantArrayElement(indexes, sourceIndex);
        for (int index : indexes) {
            enabled.set(index, enabled.getBoolean(sourceIndex));
        }
    }
}
