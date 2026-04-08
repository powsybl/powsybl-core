/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl.extensions;

import com.google.auto.service.AutoService;
import com.powsybl.commons.extensions.ExtensionAdderProvider;
import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.extensions.ConnectablePosition;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
//Ideally this class should not be generic because it is used
//through ServiceLoader which doesn't understand generics
@AutoService(ExtensionAdderProvider.class)
public class ConnectablePositionAdderImplProvider<C extends Connectable<C>> implements
        ExtensionAdderProvider<C, ConnectablePosition<C>, ConnectablePositionAdderImpl<C>> {

    @Override
    public String getImplementationName() {
        return "Default";
    }

    @Override
    public Class<ConnectablePositionAdderImpl> getAdderClass() {
        return ConnectablePositionAdderImpl.class;
    }

    @Override
    public ConnectablePositionAdderImpl<C> newAdder(C connectable) {
        return new ConnectablePositionAdderImpl<>(connectable);
    }

}
