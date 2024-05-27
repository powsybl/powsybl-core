/**
 * Copyright (c) 2024, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.AreaType;

import java.util.Objects;

/**
 * @author Marine Guibert {@literal <marine.guibert at artelys.com>}
 * @author Valentin Mouradian {@literal <valentin.mouradian at artelys.com>}
 */
public class AreaTypeImpl extends AbstractIdentifiable<AreaType> implements AreaType {
    private final Ref<NetworkImpl> networkRef;

    public AreaTypeImpl(Ref<NetworkImpl> ref, String id, String name, boolean fictitious) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "AreaType";
    }

}
