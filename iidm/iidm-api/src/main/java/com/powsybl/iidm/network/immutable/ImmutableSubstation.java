/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;

import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
final class ImmutableSubstation extends AbstractImmutableIdentifiable<Substation> implements Substation {

    ImmutableSubstation(Substation identifiable, ImmutableCacheIndex cache) {
        super(identifiable, cache);
    }

    @Override
    public Network getNetwork() {
        return cache.getNetwork();
    }

    @Override
    public Country getCountry() {
        return identifiable.getCountry();
    }

    @Override
    public Substation setCountry(Country country) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public String getTso() {
        return identifiable.getTso();
    }

    @Override
    public Substation setTso(String tso) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public VoltageLevelAdder newVoltageLevel() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Iterables.transform(identifiable.getVoltageLevels(), cache::getVoltageLevel);
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return identifiable.getVoltageLevelStream().map(cache::getVoltageLevel);
    }

    @Override
    public TwoWindingsTransformerAdder newTwoWindingsTransformer() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(identifiable.getTwoWindingsTransformers(), cache::getTwoWindingsTransformer);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return identifiable.getTwoWindingsTransformerStream().map(cache::getTwoWindingsTransformer);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return identifiable.getTwoWindingsTransformerCount();
    }

    @Override
    public ThreeWindingsTransformerAdder newThreeWindingsTransformer() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(identifiable.getThreeWindingsTransformers(), cache::getThreeWindingsTransformer);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return identifiable.getThreeWindingsTransformerStream().map(cache::getThreeWindingsTransformer);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return identifiable.getThreeWindingsTransformerCount();
    }

    @Override
    public Set<String> getGeographicalTags() {
        return identifiable.getGeographicalTags();
    }

    @Override
    public Substation addGeographicalTag(String tag) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public ContainerType getContainerType() {
        return identifiable.getContainerType();
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
