/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ComponentAdapter extends AbstractAdapter<Component> implements Component {

    protected ComponentAdapter(final Component delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Iterable<Bus> getBuses() {
        return Collections.unmodifiableSet(getBusStream().collect(Collectors.toSet()));
    }

    @Override
    public Stream<Bus> getBusStream() {
        return getDelegate().getBusStream().map(getIndex()::getBus);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public int getNum() {
        return getDelegate().getNum();
    }

    @Override
    public int getSize() {
        return getDelegate().getSize();
    }
}
