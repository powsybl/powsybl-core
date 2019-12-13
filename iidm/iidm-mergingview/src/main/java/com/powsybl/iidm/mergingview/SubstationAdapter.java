/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
class SubstationAdapter extends AbstractIdentifiableAdapter<Substation> implements Substation {

    SubstationAdapter(final Substation delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public VoltageLevelAdder newVoltageLevel() {
        return new VoltageLevelAdderAdapter(getDelegate().newVoltageLevel(), getIndex());
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return getDelegate().getVoltageLevelStream()
                            .map(getIndex()::getVoltageLevel);
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Iterables.transform(getDelegate().getVoltageLevels(),
                                   getIndex()::getVoltageLevel);
    }

    @Override
    public TwoWindingsTransformerAdderAdapter newTwoWindingsTransformer() {
        return new TwoWindingsTransformerAdderAdapter(getDelegate().newTwoWindingsTransformer(), getIndex());
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(getDelegate().getTwoWindingsTransformers(),
                                   getIndex()::getTwoWindingsTransformer);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getDelegate().getTwoWindingsTransformerStream()
                            .map(getIndex()::getTwoWindingsTransformer);
    }

    @Override
    public ThreeWindingsTransformerAdderAdapter newThreeWindingsTransformer() {
        return new ThreeWindingsTransformerAdderAdapter(getDelegate().newThreeWindingsTransformer(), getIndex());
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(getDelegate().getThreeWindingsTransformers(),
                                   getIndex()::getThreeWindingsTransformer);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getDelegate().getThreeWindingsTransformerStream()
                            .map(getIndex()::getThreeWindingsTransformer);
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public ContainerType getContainerType() {
        return getDelegate().getContainerType();
    }

    @Override
    public Optional<Country> getCountry() {
        return getDelegate().getCountry();
    }

    @Override
    public Country getNullableCountry() {
        return getDelegate().getNullableCountry();
    }

    @Override
    public Substation setCountry(final Country country) {
        getDelegate().setCountry(country);
        return this;
    }

    @Override
    public String getTso() {
        return getDelegate().getTso();
    }

    @Override
    public Substation setTso(final String tso) {
        getDelegate().setTso(tso);
        return this;
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return getDelegate().getTwoWindingsTransformerCount();
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return getDelegate().getThreeWindingsTransformerCount();
    }

    @Override
    public Set<String> getGeographicalTags() {
        return getDelegate().getGeographicalTags();
    }

    @Override
    public Substation addGeographicalTag(final String tag) {
        getDelegate().addGeographicalTag(tag);
        return this;
    }
}
