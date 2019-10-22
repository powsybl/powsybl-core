/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.List;

import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.ConnectableType;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class BusbarSectionAdapter extends AbstractIdentifiableAdapter<BusbarSection> implements BusbarSection {

    protected BusbarSectionAdapter(final BusbarSection delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Not implemented methods -------
    // -------------------------------
    @Override
    public TerminalAdapter getTerminal() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public ConnectableType getType() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public List<? extends TerminalAdapter> getTerminals() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public void remove() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getV() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

    @Override
    public double getAngle() {
        throw MergingView.NOT_IMPLEMENTED_EXCEPTION;
    }

}
