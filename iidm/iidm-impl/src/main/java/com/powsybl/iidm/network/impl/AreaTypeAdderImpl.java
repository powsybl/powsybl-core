/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.AreaType;
import com.powsybl.iidm.network.AreaTypeAdder;
import com.powsybl.iidm.network.impl.util.Ref;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaTypeAdderImpl extends AbstractIdentifiableAdder<AreaTypeAdderImpl> implements AreaTypeAdder {

    private final Ref<NetworkImpl> networkRef;

    AreaTypeAdderImpl(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
    }

    @Override
    public AreaTypeAdder copy(AreaType otherAreaType) {
        this.setId(otherAreaType.getId())
                .setName(otherAreaType.getNameOrId())
                .setFictitious(otherAreaType.isFictitious());
        return this;
    }

    @Override
    public AreaTypeImpl add() {
        String id = checkAndGetUniqueId();
        AreaTypeImpl areaType = new AreaTypeImpl(networkRef, id, getName(), isFictitious());
        getNetwork().getIndex().checkAndAdd(areaType);
        getNetwork().getListeners().notifyCreation(areaType);
        return areaType;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "AreaType";
    }
}
