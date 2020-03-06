/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Component;

import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class ComponentAdapter extends AbstractAdapter<Component> implements Component {

    ComponentAdapter(final Component delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public int getNum() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public int getSize() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Iterable<Bus> getBuses() {
        throw MergingView.createNotImplementedException();
    }

    @Override
    public Stream<Bus> getBusStream() {
        throw MergingView.createNotImplementedException();
    }

}
