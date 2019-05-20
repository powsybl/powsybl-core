/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.immutable;

import com.google.common.collect.Iterables;
import com.powsybl.iidm.network.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * An immutable {@link Substation}.
 * It is a read-only object, any modification on it will throw a runtime exception.
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
final class ImmutableSubstation extends AbstractImmutableIdentifiable<Substation> implements Substation {

    ImmutableSubstation(Substation identifiable, ImmutableCacheIndex cache) {
        super(identifiable, cache);
    }

    /**
     * {@inheritDoc}
     * @return Returns an {@link ImmutableNetwork}
     */
    @Override
    public Network getNetwork() {
        return cache.getNetwork();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Country> getCountry() {
        return identifiable.getCountry();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public Substation setCountry(Country country) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTso() {
        return identifiable.getTso();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public Substation setTso(String tso) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public VoltageLevelAdder newVoltageLevel() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * VoltageLevels are wrapped in {@link ImmutableVoltageLevel}.
     */
    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Iterables.transform(identifiable.getVoltageLevels(), cache::getVoltageLevel);
    }

    /**
     * {@inheritDoc}
     * VoltageLevels are wrapped in {@link ImmutableVoltageLevel}.
     */
    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return identifiable.getVoltageLevelStream().map(cache::getVoltageLevel);
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public TwoWindingsTransformerAdder newTwoWindingsTransformer() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * TwoWindingsTransformers are wrapped in {@link ImmutableTwoWindingsTransformer}.
     */
    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(identifiable.getTwoWindingsTransformers(), cache::getTwoWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     * TwoWindingsTransformers are wrapped in {@link ImmutableTwoWindingsTransformer}.
     */
    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return identifiable.getTwoWindingsTransformerStream().map(cache::getTwoWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTwoWindingsTransformerCount() {
        return identifiable.getTwoWindingsTransformerCount();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public ThreeWindingsTransformerAdder newThreeWindingsTransformer() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     * ThreeWindingsTransformers are wrapped in {@link ImmutableThreeWindingsTransformer}.
     */
    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(identifiable.getThreeWindingsTransformers(), cache::getThreeWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     * ThreeWindingsTransformers are wrapped in {@link ImmutableThreeWindingsTransformer}.
     */
    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return identifiable.getThreeWindingsTransformerStream().map(cache::getThreeWindingsTransformer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getThreeWindingsTransformerCount() {
        return identifiable.getThreeWindingsTransformerCount();
    }

    /**
     * {@inheritDoc}
     * @return an unmodifiable set of tag's string
     */
    @Override
    public Set<String> getGeographicalTags() {
        return Collections.unmodifiableSet(new HashSet<>(identifiable.getGeographicalTags()));
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public Substation addGeographicalTag(String tag) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ContainerType getContainerType() {
        return identifiable.getContainerType();
    }

    /**
     * Mutative operation is not allowed. It will throw an exception in runtime.
     * @return
     */
    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }
}
