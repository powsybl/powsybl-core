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

    /**
     * Throw a {@link com.powsybl.commons.PowsyblException} if this substation contains at least one {@link Branch} or
     * one {@link ThreeWindingsTransformer} or one {@link HvdcConverterStation} linked to a voltage level outside this
     * substation.
     */
    private void checkRemovability() {
        String errorMessage = "The substation " + getId() + " is still connected to another substation";

        for (VoltageLevelExt vl : voltageLevels) {
            for (Connectable connectable : vl.getConnectables()) {
                if (connectable instanceof Branch) {
                    Branch branch = (Branch) connectable;
                    Substation s1 = branch.getTerminal1().getVoltageLevel().getSubstation();
                    Substation s2 = branch.getTerminal2().getVoltageLevel().getSubstation();
                    if ((s1 != this) || (s2 != this)) {
                        throw new AssertionError(errorMessage);
                    }
                } else if (connectable instanceof ThreeWindingsTransformer) {
                    ThreeWindingsTransformer twt = (ThreeWindingsTransformer) connectable;
                    Substation s1 = twt.getLeg1().getTerminal().getVoltageLevel().getSubstation();
                    Substation s2 = twt.getLeg2().getTerminal().getVoltageLevel().getSubstation();
                    Substation s3 = twt.getLeg3().getTerminal().getVoltageLevel().getSubstation();
                    if ((s1 != this) || (s2 != this) || (s3 != this)) {
                        throw new AssertionError(errorMessage);
                    }
                } else if (connectable instanceof HvdcConverterStation) {
                    HvdcLine hvdcLine = getNetwork().getHvdcLine((HvdcConverterStation) connectable);
                    if (hvdcLine != null) {
                        Substation s1 = hvdcLine.getConverterStation1().getTerminal().getVoltageLevel().getSubstation();
                        Substation s2 = hvdcLine.getConverterStation2().getTerminal().getVoltageLevel().getSubstation();
                        if ((s1 != this) || (s2 != this)) {
                            throw new AssertionError(errorMessage);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void remove() {
        checkRemovability();

        Set<ConnectableType> types = EnumSet.of(
                ConnectableType.LINE,
                ConnectableType.TWO_WINDINGS_TRANSFORMER,
                ConnectableType.THREE_WINDINGS_TRANSFORMER);

        Set<VoltageLevelExt> vls = new HashSet<>(voltageLevels);
        for (VoltageLevelExt vl : vls) {
            // Remove all branches, transformers and HVDC lines
            List<Connectable> connectables = Lists.newArrayList(vl.getConnectables());
            for (Connectable connectable : connectables) {
                ConnectableType type = connectable.getType();
                if (types.contains(type)) {
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
