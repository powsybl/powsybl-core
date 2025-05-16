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
import com.powsybl.iidm.network.DcNodeAdder;
import com.powsybl.iidm.network.ValidationUtil;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcNodeAdderImpl extends AbstractIdentifiableAdder<DcNodeAdderImpl> implements DcNodeAdder {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    private double nominalV = Double.NaN;

    DcNodeAdderImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef) {
        this.networkRef = ref;
        this.subnetworkRef = subnetworkRef;
    }

    @Override
    public DcNodeAdder setNominalV(double nominalV) {
        this.nominalV = nominalV;
        return this;
    }

    @Override
    public DcNode add() {
        String id = checkAndGetUniqueId();
        ValidationUtil.checkNominalV(this, nominalV);

        DcNode dcNode = new DcNodeImpl(networkRef, subnetworkRef, id, getName(), isFictitious(), nominalV);
        NetworkImpl network = networkRef.get();
        network.getIndex().checkAndAdd(dcNode);
        network.getListeners().notifyCreation(dcNode);
        return dcNode;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "DC Node";
    }
}
