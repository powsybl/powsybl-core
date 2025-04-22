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
import com.powsybl.iidm.network.DcTerminal;

/**
 * @author Damien Jeandemange {@literal <damien.jeandemange at artelys.com>}
 */
public class DcGroundImpl extends AbstractDcConnectable<DcGround> implements DcGround {

    private double r;

    DcGroundImpl(Ref<NetworkImpl> ref, Ref<SubnetworkImpl> subnetworkRef, String id, String name, boolean fictitious, double r) {
        super(ref, subnetworkRef, id, name, fictitious);
        this.r = r;
    }

    @Override
    protected String getTypeDescription() {
        return "DC Ground";
    }

    @Override
    public DcTerminal getDcTerminal() {
        return this.dcTerminals.get(0);
    }

    @Override
    public double getR() {
        return this.r;
    }

    @Override
    public void setR(double r) {
        // todo checks
        this.r = r;
    }
}
