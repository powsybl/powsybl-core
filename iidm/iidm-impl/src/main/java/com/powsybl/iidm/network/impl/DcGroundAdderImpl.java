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
public class DcGroundAdderImpl extends AbstractIdentifiableAdder<DcGroundAdderImpl> implements DcGroundAdder {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;

    private double r = 0.; // defaults to zero ohms
    private String dcNodeId;
    private boolean connected = true; // defaults to connected

    DcGroundAdderImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef) {
        this.networkRef = ref;
        this.subnetworkRef = subnetworkRef;
    }

    @Override
    public DcGroundAdder setR(double r) {
        this.r = r;
        return this;
    }

    @Override
    public DcGroundAdder setDcNode(String dcNode) {
        this.dcNodeId = Objects.requireNonNull(dcNode);
        return this;
    }

    @Override
    public DcGroundAdder setConnected(boolean connected) {
        this.connected = connected;
        return this;
    }

    @Override
    public DcGround add() {
        String id = checkAndGetUniqueId();
        DcNode dcNode = ValidationUtil.checkAndGetDcNode(getNetwork().getParentNetwork(), this, dcNodeId, "dcNode");
        ValidationUtil.checkSameParentNetwork(this.getParentNetwork(), this, dcNode);
        ValidationUtil.checkDoubleParamPositive(this, r, DcGroundImpl.R_ATTRIBUTE);
        DcGroundImpl dcGround = new DcGroundImpl(networkRef, subnetworkRef, id, getName(), isFictitious(), r);
        DcTerminalImpl dcTerminal = new DcTerminalImpl(networkRef, null, dcNode, connected);
        dcGround.addDcTerminal(dcTerminal);
        getNetwork().getIndex().checkAndAdd(dcGround);
        getNetwork().getListeners().notifyCreation(dcGround);
        return dcGround;
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
        return "DC Ground";
    }
}
