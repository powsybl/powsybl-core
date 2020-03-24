/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.util.ComponentImpl;

import java.lang.ref.WeakReference;
import java.util.Objects;

/**
 * @author Thomas ADAM <tadam at silicom.fr>
 */
public class SynchronousComponentsManager extends AbstractComponentsManager<ComponentImpl> {

    public SynchronousComponentsManager(Network network) {
        super(network);
    }

    @Override
    public String getComponentLabel() {
        return "Synchronous";
    }

    @Override
    public void setComponentNumber(Bus bus, int num) {
        Objects.requireNonNull(bus);
        bus.setSynchronousComponentNumber(num);
    }

    @Override
    public ComponentImpl createComponent(int num, int size) {
        return new ComponentImpl(num, size, new WeakReference<>(network));
    }
}
