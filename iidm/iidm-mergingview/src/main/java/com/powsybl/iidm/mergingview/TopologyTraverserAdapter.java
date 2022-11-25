/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.math.graph.TraverseResult;

import java.util.Objects;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
class TopologyTraverserAdapter implements Terminal.TopologyTraverser {

    private final Terminal.TopologyTraverser delegate;
    private final MergingViewIndex index;

    TopologyTraverserAdapter(Terminal.TopologyTraverser traverser, MergingViewIndex index) {
        this.delegate = Objects.requireNonNull(traverser);
        this.index = Objects.requireNonNull(index);
    }

    @Override
    public TraverseResult traverse(Terminal terminal, boolean connected) {
        return delegate.traverse(index.getTerminal(terminal), connected);
    }

    @Override
    public TraverseResult traverse(Switch aSwitch) {
        return delegate.traverse(index.getSwitch(aSwitch));
    }
}
