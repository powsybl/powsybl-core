/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.impl.util.Ref;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SubstationAdder;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SubstationAdderImpl extends AbstractIdentifiableAdder<SubstationAdderImpl> implements SubstationAdder {

    private final Ref<NetworkImpl> networkRef;

    private Country country;

    private String tso;

    private String[] tags;

    SubstationAdderImpl(Ref<NetworkImpl> networkRef) {
        this.networkRef = networkRef;
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
        ValidationUtil.checkCountry(this, country);
        SubstationImpl substation = new SubstationImpl(id, getName(), country, tso, networkRef);
        if (tags != null) {
            for (String tag : tags) {
                substation.addGeographicalTag(tag);
            }
        }
        getNetwork().getObjectStore().checkAndAdd(substation);
        getNetwork().getListeners().notifyCreation(substation);
        return substation;
    }

}
