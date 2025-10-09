/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.extensions;

import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Connectable;

/**
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
public interface ConnectablePositionAdder<C extends Connectable<C>>
        extends ExtensionAdder<C, ConnectablePosition<C>> {

    default Class<ConnectablePosition> getExtensionClass() {
        return ConnectablePosition.class;
    }

    interface FeederAdder<C extends Connectable<C>> {

        public FeederAdder<C> withName(String name);

        public FeederAdder<C> withOrder(int order);

        public FeederAdder<C> withDirection(ConnectablePosition.Direction direction);

        public ConnectablePositionAdder<C> add();

    }

    FeederAdder<C> newFeeder();

    FeederAdder<C> newFeeder1();

    FeederAdder<C> newFeeder2();

    FeederAdder<C> newFeeder3();

}
