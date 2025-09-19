/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import com.powsybl.iidm.network.*;

import java.util.List;
import java.util.stream.Stream;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public abstract class AbstractDcTopologyVisitable<I extends Identifiable<I>> extends AbstractIdentifiable<I> {

    AbstractDcTopologyVisitable(String id, String name) {
        super(id, name);
    }

    AbstractDcTopologyVisitable(String id, String name, boolean fictitious) {
        super(id, name, fictitious);
    }

    abstract List<DcTerminal> getConnectedDcTerminals();

    abstract Stream<DcTerminal> getConnectedDcTerminalStream();

    public Iterable<DcGround> getDcGrounds() {
        return getDcConnectables(DcGround.class);
    }

    public Stream<DcGround> getDcGroundStream() {
        return getDcConnectableStream(DcGround.class);
    }

    public Iterable<DcLine> getDcLines() {
        return getDcConnectables(DcLine.class);
    }

    public Stream<DcLine> getDcLineStream() {
        return getDcConnectableStream(DcLine.class);
    }

    public Iterable<LineCommutatedConverter> getLineCommutatedConverters() {
        return getDcConnectables(LineCommutatedConverter.class);
    }

    public Stream<LineCommutatedConverter> getLineCommutatedConverterStream() {
        return getDcConnectableStream(LineCommutatedConverter.class);
    }

    public Iterable<VoltageSourceConverter> getVoltageSourceConverters() {
        return getDcConnectables(VoltageSourceConverter.class);
    }

    public Stream<VoltageSourceConverter> getVoltageSourceConverterStream() {
        return getDcConnectableStream(VoltageSourceConverter.class);
    }

    private <C extends DcConnectable<C>> Iterable<C> getDcConnectables(Class<C> clazz) {
        return FluentIterable.from(getConnectedDcTerminals())
                .transform(DcTerminal::getDcConnectable)
                .filter(clazz);
    }

    protected <C extends DcConnectable<C>> Stream<C> getDcConnectableStream(Class<C> clazz) {
        return getConnectedDcTerminalStream()
                .map(DcTerminal::getDcConnectable)
                .filter(clazz::isInstance)
                .map(clazz::cast);
    }
}
