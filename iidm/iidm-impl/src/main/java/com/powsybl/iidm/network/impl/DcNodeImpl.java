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
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.Network;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcNodeImpl extends AbstractIdentifiable<DcNode> implements DcNode {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;
    protected boolean removed = false;
    private double nominalV;

    DcNodeImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, double nominalV) {
        super(id, name, fictitious);
        this.networkRef = Objects.requireNonNull(ref);
        this.subnetworkRef = subnetworkRef;
        this.nominalV = nominalV;
    }

    @Override
    protected String getTypeDescription() {
        return "DC Node";
    }

    void throwIfRemoved(String attribute) {
        if (removed) {
            throw new PowsyblException("Cannot access " + attribute + " of removed equipment " + id);
        }
    }

    @Override
    public Network getParentNetwork() {
        throwIfRemoved("network");
        return Optional.ofNullable((Network) subnetworkRef.get()).orElse(getNetwork());
    }

    @Override
    public NetworkImpl getNetwork() {
        throwIfRemoved("network");
        return networkRef.get();
    }

    @Override
    public double getNominalV() {
        return this.nominalV;
    }

    @Override
    public DcNode setNominalV(double nominalV) {
        this.nominalV = nominalV;
        return this;
    }

    @Override
    public void remove() {
        NetworkImpl network = getNetwork();

        network.getListeners().notifyBeforeRemoval(this);

        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        removed = true;
    }
}
