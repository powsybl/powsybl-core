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
public class DcSwitchAdderImpl extends AbstractIdentifiableAdder<DcSwitchAdderImpl> implements DcSwitchAdder {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    private DcSwitchKind kind;
    private String dcNode1Id;
    private String dcNode2Id;
    private Boolean open;

    DcSwitchAdderImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef) {
        this.networkRef = ref;
        this.subnetworkRef = subnetworkRef;
    }

    @Override
    public DcSwitchAdder setKind(DcSwitchKind kind) {
        this.kind = Objects.requireNonNull(kind);
        return this;
    }

    @Override
    public DcSwitchAdder setDcNode1(String dcNode1) {
        this.dcNode1Id = dcNode1;
        return this;
    }

    @Override
    public DcSwitchAdder setDcNode2(String dcNode2) {
        this.dcNode2Id = dcNode2;
        return this;
    }

    @Override
    public DcSwitchAdder setOpen(boolean open) {
        this.open = open;
        return this;
    }

    @Override
    public DcSwitch add() {
        String id = checkAndGetUniqueId();
        DcNode dcNode1 = ValidationUtil.checkAndGetDcNode(getNetwork().getParentNetwork(), this, dcNode1Id, "dcNode1");
        DcNode dcNode2 = ValidationUtil.checkAndGetDcNode(getNetwork().getParentNetwork(), this, dcNode2Id, "dcNode2");
        ValidationUtil.checkSameParentNetwork(this.getParentNetwork(), this, dcNode1, dcNode2);
        if (kind == null) {
            throw new ValidationException(this, "kind is not set");
        }
        if (open == null) {
            throw new ValidationException(this, "open is not set");
        }
        DcSwitch dcSwitch = new DcSwitchImpl(networkRef, subnetworkRef, id, getName(), isFictitious(), kind, dcNode1, dcNode2, open);
        getNetwork().getIndex().checkAndAdd(dcSwitch);
        getNetwork().getListeners().notifyCreation(dcSwitch);
        return dcSwitch;
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
        return "DC Switch";
    }
}
