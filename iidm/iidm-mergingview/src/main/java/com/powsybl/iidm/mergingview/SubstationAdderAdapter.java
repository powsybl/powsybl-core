/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.SubstationAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class SubstationAdderAdapter implements SubstationAdder {

    private final SubstationAdder internal;

    private final MergingViewIndex index;

    SubstationAdderAdapter(final SubstationAdder internal, final MergingViewIndex index) {
        this.internal = internal;
        this.index = index;
    }

    @Override
    public SubstationAdapter add() {
        return index.getSubstation(internal.add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public SubstationAdderAdapter setId(final String id) {
        this.internal.setId(id);
        return this;
    }

    @Override
    public SubstationAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        this.internal.setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public SubstationAdderAdapter setName(final String name) {
        this.internal.setName(name);
        return this;
    }

    @Override
    public SubstationAdderAdapter setCountry(final Country country) {
        this.internal.setCountry(country);
        return this;
    }

    @Override
    public SubstationAdderAdapter setTso(final String tso) {
        this.internal.setTso(tso);
        return this;
    }

    @Override
    public SubstationAdderAdapter setGeographicalTags(final String... tags) {
        this.internal.setGeographicalTags(tags);
        return this;
    }
}
