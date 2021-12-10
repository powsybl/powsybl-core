/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.SubstationAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class SubstationAdderAdapter extends AbstractIdentifiableAdderAdapter<SubstationAdder> implements SubstationAdder {

    SubstationAdderAdapter(final SubstationAdder delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public Substation add() {
        checkAndSetUniqueId();
        return getIndex().getSubstation(getDelegate().add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public SubstationAdder setCountry(final Country country) {
        getDelegate().setCountry(country);
        return this;
    }

    @Override
    public SubstationAdder setTso(final String tso) {
        getDelegate().setTso(tso);
        return this;
    }

    @Override
    public SubstationAdder setGeographicalTags(final String... tags) {
        getDelegate().setGeographicalTags(tags);
        return this;
    }
}
