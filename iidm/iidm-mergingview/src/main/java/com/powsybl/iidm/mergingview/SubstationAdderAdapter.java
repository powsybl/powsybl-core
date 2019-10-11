/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import java.util.Objects;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.SubstationAdder;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class SubstationAdderAdapter implements SubstationAdder {

    private final SubstationAdder delegate;

    private final MergingViewIndex index;

    SubstationAdderAdapter(final SubstationAdder delegate, final MergingViewIndex index) {
        this.delegate = Objects.requireNonNull(delegate, "delegate is null");
        this.index = Objects.requireNonNull(index, "merging view index is null");
    }

    @Override
    public SubstationAdapter add() {
        return index.getSubstation(delegate.add());
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public SubstationAdderAdapter setId(final String id) {
        delegate.setId(id);
        return this;
    }

    @Override
    public SubstationAdderAdapter setEnsureIdUnicity(final boolean ensureIdUnicity) {
        delegate.setEnsureIdUnicity(ensureIdUnicity);
        return this;
    }

    @Override
    public SubstationAdderAdapter setName(final String name) {
        delegate.setName(name);
        return this;
    }

    @Override
    public SubstationAdderAdapter setCountry(final Country country) {
        delegate.setCountry(country);
        return this;
    }

    @Override
    public SubstationAdderAdapter setTso(final String tso) {
        delegate.setTso(tso);
        return this;
    }

    @Override
    public SubstationAdderAdapter setGeographicalTags(final String... tags) {
        delegate.setGeographicalTags(tags);
        return this;
    }
}
