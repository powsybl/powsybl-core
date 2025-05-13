/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.*;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcLineAdderImpl extends AbstractIdentifiableAdder<DcLineAdderImpl> implements DcLineAdder {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;

    private double r = Double.NaN;
    private String dcNode1Id;
    private boolean connected1 = true;
    private String dcNode2Id;
    private boolean connected2 = true;

    DcLineAdderImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef) {
        this.networkRef = ref;
        this.subnetworkRef = subnetworkRef;
    }

    @Override
    public DcLineAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public DcLineAdder setDcNode1Id(String dcNode1Id) {
        this.dcNode1Id = Objects.requireNonNull(dcNode1Id);
        return this;
    }

    @Override
    public DcLineAdder setConnected1(boolean connected1) {
        this.connected1 = connected1;
        return this;
    }

    @Override
    public DcLineAdder setDcNode2Id(String dcNode2Id) {
        this.dcNode2Id = Objects.requireNonNull(dcNode2Id);
        return this;
    }

    @Override
    public DcLineAdder setConnected2(boolean connected2) {
        this.connected2 = connected2;
        return this;
    }

    private DcNode checkAndGetDcNode(String dcNodeId, String attribute) {
        if (dcNodeId == null) {
            throw new ValidationException(this, attribute + " is not set");
        }
        DcNode dcNode = getNetwork().getParentNetwork().getDcNode(dcNodeId);
        if (dcNode == null) {
            throw new ValidationException(this, "DcNode '" + dcNodeId + "' not found");
        }
        return dcNode;
    }

    private void checkSameParentNetwork(DcNode dcNode1, DcNode dcNode2) {
        if (dcNode1.getParentNetwork() != dcNode2.getParentNetwork()) {
            throw new ValidationException(this, "DC Nodes '" + dcNode1.getId() + "' and '" + dcNode2.getId() +
                    "' are in different networks '" + dcNode1.getParentNetwork().getId() + "' and '" + dcNode2.getParentNetwork().getId() + "'");
        }
        if (getParentNetwork() != dcNode1.getParentNetwork()) {
            throw new ValidationException(this, "DC Nodes '" + dcNode1.getId() + "' and '" + dcNode2.getId() +
                    "' are in network '" + dcNode1.getParentNetwork().getId() + "' but DC Line is in '" + getParentNetwork().getId() + "'");
        }
    }

    @Override
    public DcLine add() {
        String id = checkAndGetUniqueId();
        DcNode dcNode1 = checkAndGetDcNode(this.dcNode1Id, "dcNode1Id");
        DcNode dcNode2 = checkAndGetDcNode(this.dcNode2Id, "dcNode2Id");
        checkSameParentNetwork(dcNode1, dcNode2);
        ValidationUtil.checkRStrictlyPositive(this, this.r);
        DcLineImpl dcLine = new DcLineImpl(networkRef, subnetworkRef, id, getName(), isFictitious(), this.r);
        DcTerminalImpl dcTerminal1 = new DcTerminalImpl(networkRef, dcNode1, connected1);
        DcTerminalImpl dcTerminal2 = new DcTerminalImpl(networkRef, dcNode2, connected2);
        dcLine.addDcTerminal(dcTerminal1);
        dcLine.addDcTerminal(dcTerminal2);
        getNetwork().getIndex().checkAndAdd(dcLine);
        getNetwork().getListeners().notifyCreation(dcLine);
        return dcLine;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    private Network getParentNetwork() {
        return Optional.ofNullable((Network) subnetworkRef.get()).orElse(getNetwork());
    }

    @Override
    protected String getTypeDescription() {
        return "DC Line";
    }
}
