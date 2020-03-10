/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;

import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ComponentAdapter implements Component {

    private final MergingViewIndex index;
    private final int num;
    private final int size;

    ComponentAdapter(MergingViewIndex index, int num, int size) {
        this.index = index;
        this.num = num;
        this.size = size;
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
        return Iterables.filter(index.getView().getBusView().getBuses(), bus -> bus.getConnectedComponent() == ComponentAdapter.this);
    }

    @Override
    public Stream<Bus> getBusStream() {
        return index.getView().getBusView().getBusStream().filter(bus -> bus.getConnectedComponent() == ComponentAdapter.this);
    }

}
