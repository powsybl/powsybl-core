/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.InjectionAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractInjectionAdderAdapter<I extends InjectionAdder<I>> extends AbstractIdentifiableAdderAdapter<I> implements InjectionAdder<I> {

    protected AbstractInjectionAdderAdapter(I delegate, MergingViewIndex index) {
        super(delegate, index);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public I setNode(int node) {
        getDelegate().setNode(node);
        return (I) this;
    }

    @Override
    public I setBus(String bus) {
        getDelegate().setBus(bus);
        return (I) this;
    }

    @Override
    public I setConnectableBus(String connectableBus) {
        getDelegate().setConnectableBus(connectableBus);
        return (I) this;
    }
}
