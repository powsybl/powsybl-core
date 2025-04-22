/**
 * Copyright (c) 2025, Coreso SA (https://www.coreso.eu/) and TSCNET Services GmbH (https://www.tscnet.eu/)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.DcSwitch;
import com.powsybl.iidm.network.DcSwitchAdder;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcSwitchAdderImpl extends AbstractIdentifiableAdder<DcSwitchAdderImpl> implements DcSwitchAdder {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    private String dcNode1Id;
    private String dcNode2Id;
    private Boolean retained;
    private Boolean open;

    DcSwitchAdderImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef) {
        this.networkRef = ref;
        this.subnetworkRef = subnetworkRef;
    }

    @Override
    public DcSwitchAdder setDcNode1Id(String dcNode1Id) {
        this.dcNode1Id = dcNode1Id;
        return this;
    }

    @Override
    public DcSwitchAdder setDcNode2Id(String dcNode2Id) {
        this.dcNode2Id = dcNode2Id;
        return this;
    }

    @Override
    public DcSwitchAdder setOpen(boolean open) {
        this.open = open;
        return this;
    }

    @Override
    public DcSwitchAdder setRetained(boolean retained) {
        this.retained = retained;
        return this;
    }

    @Override
    public DcSwitch add() {
        String id = checkAndGetUniqueId();
        DcNode dcNode1 = getNetwork().getDcNode(this.dcNode1Id);
        DcNode dcNode2 = getNetwork().getDcNode(this.dcNode2Id);
        DcSwitch dcSwitch = new DcSwitchImpl(networkRef, subnetworkRef, id, getName(), isFictitious(), dcNode1, dcNode2, retained, open);
        getNetwork().getIndex().checkAndAdd(dcSwitch);
        getNetwork().getListeners().notifyCreation(dcSwitch);
        return dcSwitch;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "DC Switch";
    }
}
