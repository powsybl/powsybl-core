/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.Ref;

import java.util.*;
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

    @Override
    public void remove() {
        Substations.checkRemovability(this);

        Set<VoltageLevelExt> vls = new HashSet<>(voltageLevels);
        for (VoltageLevelExt vl : vls) {
            // Remove all branches, transformers and HVDC lines
            List<Connectable> connectables = Lists.newArrayList(vl.getConnectables());
            for (Connectable connectable : connectables) {
                ConnectableType type = connectable.getType();
                if (VoltageLevels.MULTIPLE_TERMINALS_CONNECTABLE_TYPES.contains(type)) {
                    connectable.remove();
                } else if (type == ConnectableType.HVDC_CONVERTER_STATION) {
                    HvdcLine hvdcLine = getNetwork().getHvdcLine((HvdcConverterStation) connectable);
                    if (hvdcLine != null) {
                        hvdcLine.remove();
                    }
                }
            }

            // Then remove the voltage level (bus, switches and injections) from the network
            vl.remove();
        }

        // Remove this substation from the network
        getNetwork().getObjectStore().remove(this);

        getNetwork().getListeners().notifyRemoval(this);
    }

    void remove(VoltageLevelExt voltageLevelExt) {
        Objects.requireNonNull(voltageLevelExt);
        voltageLevels.remove(voltageLevelExt);
    }
}
