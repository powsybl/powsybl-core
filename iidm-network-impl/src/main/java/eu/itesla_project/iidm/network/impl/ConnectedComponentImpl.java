/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.impl.util.Ref;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import eu.itesla_project.iidm.network.Bus;
import eu.itesla_project.iidm.network.ConnectedComponent;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ConnectedComponentImpl implements ConnectedComponent {

    private final int num;

    private final int size;

    private final Ref<NetworkImpl> networkRef;

    ConnectedComponentImpl(int num, int size, Ref<NetworkImpl> networkRef) {
        this.num = num;
        this.size = size;
        this.networkRef = networkRef;
    }

    @Override
    public int getNum() {
        return num;
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public Iterable<Bus> getBuses() {
        return Iterables.filter(networkRef.get().getBusView().getBuses(), new Predicate<Bus>() {
            @Override
            public boolean apply(Bus bus) {
                return bus.getConnectedComponent() == ConnectedComponentImpl.this;
            }
        });
    }

}
