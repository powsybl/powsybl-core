/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.network.components;

import com.powsybl.iidm.network.Bus;

import java.util.function.Predicate;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
public abstract class AbstractSynchronousComponent extends AbstractComponent {

    protected AbstractSynchronousComponent(int num, int size) {
        super(num, size);
    }

    @Override
    protected Predicate<Bus> getBusPredicate() {
        return bus -> bus.getSynchronousComponent() == AbstractSynchronousComponent.this;
    }
}
