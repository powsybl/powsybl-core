/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Connectable;
import com.powsybl.iidm.network.ConnectableType;
import com.powsybl.iidm.network.Terminal;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague <mathieu.bague@rte-france.com>
 */
abstract class AbstractConnectableAdapter<I extends Connectable<I>> extends AbstractIdentifiableAdapter<I> implements Connectable<I> {

    protected AbstractConnectableAdapter(I delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public final ConnectableType getType() {
        return getDelegate().getType();
    }

    @Override
    public final List<? extends Terminal> getTerminals() {
        return getDelegate().getTerminals().stream()
                .map(getIndex()::getTerminal)
                .collect(Collectors.toList());
    }

    @Override
    public final void remove(boolean cleanDanglingSwitches) {
        throw MergingView.createNotImplementedException();
    }
}
