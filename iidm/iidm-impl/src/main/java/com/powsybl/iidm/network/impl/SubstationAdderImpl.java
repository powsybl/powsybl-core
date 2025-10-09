/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.ref.Ref;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SubstationAdder;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SubstationAdderImpl extends AbstractIdentifiableAdder<SubstationAdderImpl> implements SubstationAdder {

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;

    private Country country;

    private String tso;

    private String[] tags;

    SubstationAdderImpl(Ref<NetworkImpl> networkRef, Ref<SubnetworkImpl> subnetworkRef) {
        this.networkRef = networkRef;
        this.subnetworkRef = subnetworkRef;
    }

    @Override
    protected NetworkImpl getNetwork() {
        return networkRef.get();
    }

    @Override
    protected String getTypeDescription() {
        return "Substation";
    }

    @Override
    public SubstationAdder setCountry(Country country) {
        this.country = country;
        return this;
    }

    @Override
    public SubstationAdder setTso(String tso) {
        this.tso = tso;
        return this;
    }

    @Override
    public SubstationAdder setGeographicalTags(String... tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public Substation add() {
        String id = checkAndGetUniqueId();
        SubstationImpl substation = new SubstationImpl(id, getName(), isFictitious(), country, tso, networkRef, subnetworkRef);
        if (tags != null) {
            for (String tag : tags) {
                substation.addGeographicalTag(tag);
            }
        }
        getNetwork().getIndex().checkAndAdd(substation);
        getNetwork().getListeners().notifyCreation(substation);
        return substation;
    }

}
