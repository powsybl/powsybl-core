/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.DcLine;
import com.powsybl.iidm.network.DcLineAdder;
import com.powsybl.iidm.network.DcNode;

import java.util.Objects;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcLineAdderImpl extends AbstractIdentifiableAdder<DcLineAdderImpl> implements DcLineAdder {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;

    private double r = Double.NaN;
    private String dcNode1Id;
    private Boolean connected1;
    private String dcNode2Id;
    private Boolean connected2;

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

    @Override
    public DcLine add() {
        String id = checkAndGetUniqueId();
        DcNode dcNode1 = getNetwork().getDcNode(this.dcNode1Id);
        DcNode dcNode2 = getNetwork().getDcNode(this.dcNode2Id);
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

    @Override
    protected String getTypeDescription() {
        return "DC Line";
    }
}
