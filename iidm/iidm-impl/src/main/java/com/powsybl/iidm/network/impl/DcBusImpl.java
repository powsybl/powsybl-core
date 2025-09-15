/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcBusImpl extends AbstractDcTopologyVisitable<DcBus> implements DcBus {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    private final Set<DcNodeImpl> dcNodes;

    private boolean valid = true;

    DcBusImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, Set<DcNodeImpl> dcNodes) {
        super(id, name);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
        this.dcNodes = Objects.requireNonNull(dcNodes);
    }

    @Override
    public double getV() {
        checkValidity();
        for (DcNode n : dcNodes) {
            if (!Double.isNaN(n.getV())) {
                return n.getV();
            }
        }
        return Double.NaN;
    }

    @Override
    public DcBus setV(double v) {
        checkValidity();
        for (DcNode n : dcNodes) {
            n.setV(v);
        }
        return this;
    }

    void setConnectedComponentNumber(int connectedComponentNumber) {
        checkValidity();
        for (DcNodeImpl dcNode : dcNodes) {
            dcNode.setConnectedComponentNumber(connectedComponentNumber);
        }
    }

    void setDcComponentNumber(int dcComponentNumber) {
        checkValidity();
        for (DcNodeImpl dcNode : dcNodes) {
            dcNode.setDcComponentNumber(dcComponentNumber);
        }
    }

    @Override
    public Component getConnectedComponent() {
        checkValidity();
        for (DcNodeImpl dcNode : dcNodes) {
            Component cc = dcNode.getConnectedComponent();
            if (cc != null) {
                return cc;
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    @Override
    public boolean isInMainConnectedComponent() {
        var cc = getConnectedComponent();
        return cc != null && cc.getNum() == ComponentConstants.MAIN_NUM;
    }

    @Override
    public Component getDcComponent() {
        checkValidity();
        for (DcNodeImpl dcNode : dcNodes) {
            Component dcc = dcNode.getDcComponent();
            if (dcc != null) {
                return dcc;
            }
        }
        throw new IllegalStateException("Should not happen");
    }

    @Override
    public Iterable<DcNode> getDcNodes() {
        checkValidity();
        return Collections.unmodifiableCollection(dcNodes);
    }

    @Override
    public Stream<DcNode> getDcNodeStream() {
        checkValidity();
        return dcNodes.stream().map(Function.identity());
    }

    @Override
    public int getDcTerminalCount() {
        checkValidity();
        return dcNodes.stream().mapToInt(DcNode::getDcTerminalCount).sum();
    }

    @Override
    public List<DcTerminal> getDcTerminals() {
        checkValidity();
        return getDcTerminalStream().toList();
    }

    @Override
    public Stream<DcTerminal> getDcTerminalStream() {
        checkValidity();
        return dcNodes.stream().flatMap(DcNode::getDcTerminalStream);
    }

    @Override
    public int getConnectedDcTerminalCount() {
        checkValidity();
        return dcNodes.stream().mapToInt(DcNode::getConnectedDcTerminalCount).sum();
    }

    @Override
    public List<DcTerminal> getConnectedDcTerminals() {
        checkValidity();
        return getConnectedDcTerminalStream().toList();
    }

    @Override
    public Stream<DcTerminal> getConnectedDcTerminalStream() {
        checkValidity();
        return dcNodes.stream().flatMap(DcNode::getConnectedDcTerminalStream);
    }

    @Override
    public Network getParentNetwork() {
        return Optional.ofNullable((Network) subnetworkRef.get()).orElse(getNetwork());
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "DC Bus";
    }

    void invalidate() {
        valid = false;
        dcNodes.clear();
    }

    private void checkValidity() {
        if (!valid) {
            throw new PowsyblException("DcBus has been invalidated");
        }
    }
}
