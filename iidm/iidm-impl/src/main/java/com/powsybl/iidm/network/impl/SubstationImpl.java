/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.commons.ref.Ref;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class SubstationImpl extends AbstractIdentifiable<Substation> implements Substation {

    private Country country;

    private String tso;

    private final Ref<NetworkImpl> networkRef;
    private final Ref<SubnetworkImpl> subnetworkRef;

    private final Set<String> geographicalTags = new LinkedHashSet<>();

    private final Set<VoltageLevelExt> voltageLevels = new LinkedHashSet<>();

    private final Set<OverloadManagementSystemImpl> overloadManagementSystems = new LinkedHashSet<>();

    private boolean removed = false;

    SubstationImpl(String id, String name, boolean fictitious, Country country, String tso, Ref<NetworkImpl> networkRef, Ref<SubnetworkImpl> subnetworkRef) {
        super(id, name, fictitious);
        this.country = country;
        this.tso = tso;
        this.networkRef = networkRef;
        this.subnetworkRef = subnetworkRef;
    }

    Ref<SubnetworkImpl> getSubnetworkRef() {
        return subnetworkRef;
    }

    @Override
    public ContainerType getContainerType() {
        return ContainerType.SUBSTATION;
    }

    @Override
    public Optional<Country> getCountry() {
        return Optional.ofNullable(country);
    }

    @Override
    public Country getNullableCountry() {
        return country;
    }

    @Override
    public SubstationImpl setCountry(Country country) {
        String oldValue = Optional.ofNullable(this.country).map(Enum::toString).orElse("");
        this.country = country;
        getNetwork().getListeners().notifyUpdate(this, "country", oldValue, Optional.ofNullable(country).map(Enum::toString).orElse(""));
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
        if (removed) {
            throw new PowsyblException("Cannot access network of removed substation " + id);
        }
        return networkRef.get();
    }

    protected Ref<NetworkImpl> getNetworkRef() {
        return networkRef;
    }

    @Override
    public Network getParentNetwork() {
        return Optional.ofNullable((Network) subnetworkRef.get()).orElse(getNetwork());
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
        return Ints.checkedCast(getTwoWindingsTransformerStream()
                .count());
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
        return Ints.checkedCast(getThreeWindingsTransformerStream()
                .count());
    }

    void addOverloadManagementSystem(OverloadManagementSystemImpl overloadManagementSystem) {
        overloadManagementSystems.add(overloadManagementSystem);
    }

    @Override
    public OverloadManagementSystemAdderImpl newOverloadManagementSystem() {
        return new OverloadManagementSystemAdderImpl(this);
    }

    @Override
    public Iterable<OverloadManagementSystem> getOverloadManagementSystems() {
        return Collections.unmodifiableSet(overloadManagementSystems);
    }

    @Override
    public Stream<OverloadManagementSystem> getOverloadManagementSystemStream() {
        return overloadManagementSystems.stream().map(Function.identity());
    }

    @Override
    public int getOverloadManagementSystemCount() {
        return Ints.checkedCast(getOverloadManagementSystemStream().count());
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
        if (geographicalTags.add(tag)) {
            getNetwork().getListeners().notifyElementAdded(this, "geographicalTags", tag);
        }
        return this;
    }

    @Override
    protected String getTypeDescription() {
        return "Substation";
    }

    @Override
    public void remove() {
        Substations.checkRemovability(this);

        NetworkImpl network = getNetwork();
        network.getListeners().notifyBeforeRemoval(this);

        Set<VoltageLevelExt> vls = new HashSet<>(voltageLevels);
        for (VoltageLevelExt vl : vls) {
            // Remove all branches, transformers and HVDC lines
            List<Connectable> connectables = Lists.newArrayList(vl.getConnectables());
            for (Connectable connectable : connectables) {
                IdentifiableType type = connectable.getType();
                if (VoltageLevels.MULTIPLE_TERMINALS_CONNECTABLE_TYPES.contains(type)) {
                    connectable.remove();
                } else if (type == IdentifiableType.HVDC_CONVERTER_STATION) {
                    HvdcLine hvdcLine = getNetwork().getHvdcLine((HvdcConverterStation) connectable);
                    if (hvdcLine != null) {
                        hvdcLine.remove();
                    }
                }
            }

            // Then remove the voltage level (bus, switches and injections) from the network
            vl.remove();
        }

        // Remove the overload management systems
        removeOverloadManagementSystems();

        // Remove this substation from the network
        network.getIndex().remove(this);

        network.getListeners().notifyAfterRemoval(id);
        removed = true;
    }

    void removeOverloadManagementSystems() {
        overloadManagementSystems.forEach(OverloadManagementSystem::remove);
    }

    void remove(OverloadManagementSystemImpl overloadManagementSystem) {
        Objects.requireNonNull(overloadManagementSystem);
        overloadManagementSystems.remove(overloadManagementSystem);
    }

    void remove(VoltageLevelExt voltageLevelExt) {
        Objects.requireNonNull(voltageLevelExt);
        voltageLevels.remove(voltageLevelExt);
    }
}
