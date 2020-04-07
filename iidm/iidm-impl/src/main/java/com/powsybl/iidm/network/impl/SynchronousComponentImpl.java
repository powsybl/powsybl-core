/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.function.Predicate;

/**
 * @author Thomas ADAM <tadam at silicom.fr>
 */
class SynchronousComponentImpl extends AbstractComponentImpl implements Component {

    SynchronousComponentImpl(int num, int size, Ref<NetworkImpl> networkRef) {
        super(num, size, networkRef);
    }

    @Override
    protected Predicate<Bus> getBusPredicate() {
        return bus -> bus.getSynchronousComponent() == SynchronousComponentImpl.this;
    }
}
