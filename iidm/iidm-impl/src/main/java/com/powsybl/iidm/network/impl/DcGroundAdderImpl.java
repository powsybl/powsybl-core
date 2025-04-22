/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.DcGround;
import com.powsybl.iidm.network.DcGroundAdder;
import com.powsybl.iidm.network.DcNode;

import java.util.Objects;

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
    public DcGroundAdder setDcNodeId(String dcNodeId) {
        this.dcNodeId = Objects.requireNonNull(dcNodeId);
        return this;
    }

    @Override
    public DcGroundAdder setConnected(boolean connected) {
        this.connected = connected;
        return this;
    }

    @Override
    public DcGround add() {
        // TODO
        //  check R > 0
        //  check dcNode found
        String id = checkAndGetUniqueId();
        DcGroundImpl dcGround = new DcGroundImpl(networkRef, subnetworkRef, id, getName(), isFictitious(), r);
        DcNode dcNode = getNetwork().getDcNode(this.dcNodeId);
        DcTerminalImpl dcTerminal = new DcTerminalImpl(networkRef, dcNode, connected);
        dcGround.addDcTerminal(dcTerminal);

        getNetwork().getIndex().checkAndAdd(dcGround);
        getNetwork().getListeners().notifyCreation(dcGround);
        return dcGround;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "DC Ground";
    }
}
