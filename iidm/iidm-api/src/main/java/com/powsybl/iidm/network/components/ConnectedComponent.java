/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.components;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Network;

import java.util.function.Predicate;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ConnectedComponent extends AbstractComponent {

    ConnectedComponent(Network network, int num, int size) {
        super(network, num, size);
    }

    @Override
    protected Predicate<Bus> getBusPredicate() {
        return bus -> bus.getConnectedComponent() == ConnectedComponent.this;
    }
}
