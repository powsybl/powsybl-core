/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

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

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public int getNum() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public int getSize() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Iterable<Bus> getBuses() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public Stream<Bus> getBusStream() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
