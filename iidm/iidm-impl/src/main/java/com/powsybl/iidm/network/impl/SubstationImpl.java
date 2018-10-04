/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class SubstationImpl extends AbstractIdentifiable<Substation> implements Substation {

    private Country country;

    private String tso;

    private final Ref<NetworkImpl> networkRef;

    private final Set<String> geographicalTags = new LinkedHashSet<>();

    private final Set<VoltageLevelExt> voltageLevels = new LinkedHashSet<>();

    SubstationImpl(String id, String name, Country country, String tso, Ref<NetworkImpl> networkRef) {
        super(id, name);
        this.country = country;
        this.tso = tso;
        this.networkRef = networkRef;
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.SUBSTATION;
    }

    @Override
    public Country getCountry() {
        return country;
    }

    @Override
    public SubstationImpl setCountry(Country country) {
        ValidationUtil.checkCountry(this, country);
        Country oldValue = this.country;
        this.country = country;
        getNetwork().getListeners().notifyUpdate(this, "country", oldValue.toString(), country.toString());
        return this;
    }

    @Override
    public String getTso() {
        return tso;
    }

    @Override
    public SubstationImpl setTso(String tso) {
        String oldValue = this.tso;
        this.tso = tso;
        getNetwork().getListeners().notifyUpdate(this, "tso", oldValue, tso);
        return this;
    }

    @Override
    public NetworkImpl getNetwork() {
        return networkRef.get();
    }

    void addVoltageLevel(VoltageLevelExt voltageLevel) {
        voltageLevels.add(voltageLevel);
    }

    @Override
    public VoltageLevelAdderImpl newVoltageLevel() {
        return new VoltageLevelAdderImpl(this);
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Collections.unmodifiableSet(voltageLevels);
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return voltageLevels.stream().map(Function.identity());
    }

    @Override
    public TwoWindingsTransformerAdderImpl newTwoWindingsTransformer() {
        return new TwoWindingsTransformerAdderImpl(this);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return FluentIterable.from(voltageLevels)
                .transformAndConcat(vl -> vl.getConnectables(TwoWindingsTransformer.class))
                .toSet();
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return voltageLevels.stream().flatMap(vl -> vl.getConnectableStream(TwoWindingsTransformer.class)).distinct();
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return voltageLevels.stream()
                .mapToInt(vl -> vl.getConnectableCount(TwoWindingsTransformer.class))
                .sum();
    }

    @Override
    public ThreeWindingsTransformerAdderImpl newThreeWindingsTransformer() {
        return new ThreeWindingsTransformerAdderImpl(this);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return FluentIterable.from(voltageLevels)
                .transformAndConcat(vl -> vl.getConnectables(ThreeWindingsTransformer.class))
                .toSet();
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return voltageLevels.stream().flatMap(vl -> vl.getConnectableStream(ThreeWindingsTransformer.class)).distinct();
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return voltageLevels.stream()
                .mapToInt(vl -> vl.getConnectableCount(ThreeWindingsTransformer.class))
                .sum();
    }

    @Override
    public Set<String> getGeographicalTags() {
        return Collections.unmodifiableSet(geographicalTags);
    }

    @Override
    public Substation addGeographicalTag(String tag) {
        if (tag == null) {
            throw new ValidationException(this, "geographical tag is null");
        }
        geographicalTags.add(tag);
        return this;
    }

    @Override
    protected String getTypeDescription() {
        return "Substation";
    }

}
