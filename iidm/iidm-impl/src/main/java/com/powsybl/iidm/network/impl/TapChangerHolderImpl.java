/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.TapChanger;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class TapChangerHolderImpl implements TapChangerOwner {

    private Validable validable;

    private TapChanger tapChanger;

    TapChangerHolderImpl(Validable validable) {
        this.validable = Objects.requireNonNull(validable);
    }

    public TapChanger getTapChanger() {
        return tapChanger;
    }

    public <T extends TapChanger> T getTapChanger(Class<T> type) {
        if (type == null) {
            throw new IllegalArgumentException("type is null");
        }
        if (type.isInstance(tapChanger)) {
            return type.cast(tapChanger);
        } else {
            throw new ValidationException(validable, "incorrect reactive limits type "
                    + type.getName() + ", expected " + tapChanger.getClass());
        }
    }

    @Override
    public void setTapChanger(TapChanger tapChanger) {
        this.tapChanger = Objects.requireNonNull(tapChanger);
    }
}
