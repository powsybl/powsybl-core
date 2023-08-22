/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.impl.util.RefChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public class SubnetworkImpl extends AbstractNetwork {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubnetworkImpl.class);

    private final NetworkImpl parent;

    SubnetworkImpl(NetworkImpl parent, String id, String name, String sourceFormat) {
        super(id, name, sourceFormat);
        this.parent = Objects.requireNonNull(parent);
    }

    @Override
    public final Collection<Network> getSubnetworks() {
        return Collections.emptyList();
    }

    @Override
    public final Network getSubnetwork(String id) {
        return null;
    }

    @Override
    public NetworkImpl getNetwork() {
        return parent;
    }

    @Override
    public VariantManager getVariantManager() {
        return parent.getVariantManager();
    }

    private boolean contains(Identifiable<?> identifiable) {
        return identifiable != null && identifiable.getParentNetwork() == this;
    }

    @Override
    public Set<Country> getCountries() {
        return getCountryStream().collect(Collectors.toSet());
    }

    @Override
    public int getCountryCount() {
        return (int) getCountryStream().count();
    }

    private Stream<Country> getCountryStream() {
        return parent.getSubstationStream()
                .filter(this::contains)
                .map(s -> s.getCountry().orElse(null))
                .filter(Objects::nonNull);
    }

    @Override
    public SubstationAdder newSubstation() {
        return parent.newSubstation(id);
    }

    @Override
    public Iterable<Substation> getSubstations() {
        return getSubstationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return parent.getSubstationStream().filter(this::contains);
    }

    @Override
    public int getSubstationCount() {
        return (int) getSubstationStream().count();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return StreamSupport.stream(parent.getSubstations(country, tsoId, geographicalTags).spliterator(), false)
                .filter(this::contains)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<Substation> getSubstations(String country, String tsoId, String... geographicalTags) {
        return StreamSupport.stream(parent.getSubstations(country, tsoId, geographicalTags).spliterator(), false)
                .filter(this::contains)
                .collect(Collectors.toList());
    }

    @Override
    public Substation getSubstation(String id) {
        Substation s = parent.getSubstation(id);
        return contains(s) ? s : null;
    }

    @Override
    public VoltageLevelAdder newVoltageLevel() {
        return parent.newVoltageLevel(id);
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return getVoltageLevelStream().collect(Collectors.toList());
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return parent.getVoltageLevelStream().filter(this::contains);
    }

    @Override
    public int getVoltageLevelCount() {
        return (int) getVoltageLevelStream().count();
    }

    @Override
    public VoltageLevel getVoltageLevel(String id) {
        VoltageLevel vl = parent.getVoltageLevel(id);
        return contains(vl) ? vl : null;
    }

    @Override
    public LineAdder newLine() {
        return parent.newLine(id);
    }

    @Override
    public Iterable<Line> getLines() {
        return getLineStream().collect(Collectors.toList());
    }

    @Override
    public Branch<?> getBranch(String branchId) {
        Branch<?> b = parent.getBranch(branchId);
        return contains(b) ? b : null;
    }

    @Override
    public Iterable<Branch> getBranches() {
        return getBranchStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return parent.getBranchStream().filter(this::contains);
    }

    @Override
    public int getBranchCount() {
        return (int) getBranchStream().count();
    }

    @Override
    public Stream<Line> getLineStream() {
        return parent.getLineStream().filter(this::contains);
    }

    @Override
    public int getLineCount() {
        return (int) getLineStream().count();
    }

    @Override
    public Line getLine(String id) {
        Line l = parent.getLine(id);
        return contains(l) ? l : null;
    }

    @Override
    public TieLineAdder newTieLine() {
        return parent.newTieLine(id);
    }

    @Override
    public Iterable<TieLine> getTieLines() {
        return getTieLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<TieLine> getTieLineStream() {
        return parent.getTieLineStream().filter(this::contains);
    }

    @Override
    public int getTieLineCount() {
        return (int) getTieLineStream().count();
    }

    @Override
    public TieLine getTieLine(String id) {
        TieLine t = parent.getTieLine(id);
        return contains(t) ? t : null;
    }

    @Override
    public TwoWindingsTransformerAdder newTwoWindingsTransformer() {
        return parent.newTwoWindingsTransformer(id);
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getTwoWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return parent.getTwoWindingsTransformerStream().filter(this::contains);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return (int) getTwoWindingsTransformerStream().count();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        TwoWindingsTransformer twt = parent.getTwoWindingsTransformer(id);
        return contains(twt) ? twt : null;
    }

    @Override
    public ThreeWindingsTransformerAdder newThreeWindingsTransformer() {
        return parent.newThreeWindingsTransformer(id);
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getThreeWindingsTransformerStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return parent.getThreeWindingsTransformerStream().filter(this::contains);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return (int) getThreeWindingsTransformerStream().count();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        ThreeWindingsTransformer twt = parent.getThreeWindingsTransformer(id);
        return contains(twt) ? twt : null;
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getGeneratorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return parent.getGeneratorStream().filter(this::contains);
    }

    @Override
    public int getGeneratorCount() {
        return (int) getGeneratorStream().count();
    }

    @Override
    public Generator getGenerator(String id) {
        Generator g = parent.getGenerator(id);
        return contains(g) ? g : null;
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getBatteryStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return parent.getBatteryStream().filter(this::contains);
    }

    @Override
    public int getBatteryCount() {
        return (int) getBatteryStream().count();
    }

    @Override
    public Battery getBattery(String id) {
        Battery b = parent.getBattery(id);
        return contains(b) ? b : null;
    }

    @Override
    public Iterable<Load> getLoads() {
        return getLoadStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Load> getLoadStream() {
        return parent.getLoadStream().filter(this::contains);
    }

    @Override
    public int getLoadCount() {
        return (int) getLoadStream().count();
    }

    @Override
    public Load getLoad(String id) {
        Load l = parent.getLoad(id);
        return contains(l) ? l : null;
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getShuntCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return parent.getShuntCompensatorStream().filter(this::contains);
    }

    @Override
    public int getShuntCompensatorCount() {
        return (int) getShuntCompensatorStream().count();
    }

    @Override
    public ShuntCompensator getShuntCompensator(String id) {
        ShuntCompensator s = parent.getShuntCompensator(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream(danglingLineFilter).collect(Collectors.toList());
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        return parent.getDanglingLineStream().filter(this::contains).filter(danglingLineFilter.getPredicate());
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines() {
        return getDanglingLines(DanglingLineFilter.ALL);
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream() {
        return getDanglingLineStream(DanglingLineFilter.ALL);
    }

    @Override
    public int getDanglingLineCount() {
        return (int) getDanglingLineStream().count();
    }

    @Override
    public DanglingLine getDanglingLine(String id) {
        DanglingLine dl = parent.getDanglingLine(id);
        return contains(dl) ? dl : null;
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getStaticVarCompensatorStream().collect(Collectors.toList());
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return parent.getStaticVarCompensatorStream().filter(this::contains);
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return (int) getStaticVarCompensatorStream().count();
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(String id) {
        StaticVarCompensator s = parent.getStaticVarCompensator(id);
        return contains(s) ? s : null;
    }

    @Override
    public Switch getSwitch(String id) {
        Switch s = parent.getSwitch(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return getSwitchStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return parent.getSwitchStream().filter(this::contains);
    }

    @Override
    public int getSwitchCount() {
        return (int) getSwitchStream().count();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        BusbarSection b = parent.getBusbarSection(id);
        return contains(b) ? b : null;
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return getBusbarSectionStream().collect(Collectors.toList());
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return parent.getBusbarSectionStream().filter(this::contains);
    }

    @Override
    public int getBusbarSectionCount() {
        return (int) getBusbarSectionStream().count();
    }

    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return getHvdcConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return parent.getHvdcConverterStationStream().filter(this::contains);
    }

    @Override
    public int getHvdcConverterStationCount() {
        return (int) getHvdcConverterStationStream().count();
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(String id) {
        HvdcConverterStation<?> s = parent.getHvdcConverterStation(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return parent.getLccConverterStationStream().filter(this::contains);
    }

    @Override
    public int getLccConverterStationCount() {
        return (int) getLccConverterStationStream().count();
    }

    @Override
    public LccConverterStation getLccConverterStation(String id) {
        LccConverterStation s = parent.getLccConverterStation(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getVscConverterStationStream().collect(Collectors.toList());
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return parent.getVscConverterStationStream().filter(this::contains);
    }

    @Override
    public int getVscConverterStationCount() {
        return (int) getVscConverterStationStream().count();
    }

    @Override
    public VscConverterStation getVscConverterStation(String id) {
        VscConverterStation s = parent.getVscConverterStation(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return getHvdcLineStream().collect(Collectors.toList());
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return parent.getHvdcLineStream().filter(this::contains);
    }

    @Override
    public int getHvdcLineCount() {
        return (int) getHvdcLineStream().count();
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        HvdcLine l = parent.getHvdcLine(id);
        return contains(l) ? l : null;
    }

    @Override
    public HvdcLine getHvdcLine(HvdcConverterStation converterStation) {
        if (converterStation.getParentNetwork() == this) {
            return getHvdcLineStream()
                    .filter(l -> l.getConverterStation1() == converterStation || l.getConverterStation2() == converterStation)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    @Override
    public HvdcLineAdder newHvdcLine() {
        return parent.newHvdcLine(id);
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        Identifiable<?> i = parent.getIdentifiable(id);
        return contains(i) ? i : null;
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return parent.getIdentifiables().stream().filter(this::contains).collect(Collectors.toList());
    }

    @Override
    public <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        return getConnectableStream(clazz).collect(Collectors.toList());
    }

    @Override
    public <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        return parent.getConnectableStream(clazz).filter(this::contains);
    }

    @Override
    public <C extends Connectable> int getConnectableCount(Class<C> clazz) {
        return (int) getConnectableStream(clazz).count();
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return getConnectableStream().collect(Collectors.toList());
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return parent.getConnectableStream().filter(this::contains);
    }

    @Override
    public Connectable<?> getConnectable(String id) {
        Connectable<?> c = parent.getConnectable(id);
        return contains(c) ? c : null;
    }

    @Override
    public int getConnectableCount() {
        return (int) getConnectableStream().count();
    }

    class BusBreakerViewImpl implements BusBreakerView {

        @Override
        public Iterable<Bus> getBuses() {
            return getBusStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return parent.getBusBreakerView().getBusStream().filter(SubnetworkImpl.this::contains);
        }

        @Override
        public Iterable<Switch> getSwitches() {
            return getSwitchStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Switch> getSwitchStream() {
            return parent.getBusBreakerView().getSwitchStream().filter(SubnetworkImpl.this::contains);
        }

        @Override
        public int getSwitchCount() {
            return (int) getSwitchStream().count();
        }

        @Override
        public Bus getBus(String id) {
            Bus b = parent.getBusBreakerView().getBus(id);
            return contains(b) ? b : null;
        }
    }

    private final BusBreakerViewImpl busBreakerView = new BusBreakerViewImpl();

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    class BusViewImpl implements BusView {

        @Override
        public Iterable<Bus> getBuses() {
            return getBusStream().collect(Collectors.toList());
        }

        @Override
        public Stream<Bus> getBusStream() {
            return parent.getBusView().getBusStream().filter(SubnetworkImpl.this::contains);
        }

        @Override
        public Bus getBus(String id) {
            Bus b = parent.getBusView().getBus(id);
            return contains(b) ? b : null;
        }

        @Override
        public Collection<Component> getConnectedComponents() {
            return parent.getBusView().getConnectedComponents().stream()
                    .filter(c -> c.getBusStream().anyMatch(SubnetworkImpl.this::contains))
                    .map(c -> new Subcomponent(c, SubnetworkImpl.this))
                    .collect(Collectors.toList());
        }

        @Override
        public Collection<Component> getSynchronousComponents() {
            return parent.getBusView().getSynchronousComponents().stream()
                    .filter(c -> c.getBusStream().anyMatch(SubnetworkImpl.this::contains))
                    .map(c -> new Subcomponent(c, SubnetworkImpl.this))
                    .collect(Collectors.toList());
        }
    }

    private final BusViewImpl busView = new BusViewImpl();

    @Override
    public BusView getBusView() {
        return busView;
    }

    /**
     * {@inheritDoc}
     * <p>This operation is not allowed on a subnetwork.</p>
     * <p>This method throws an {@link UnsupportedOperationException}</p>
     */
    @Override
    public Network createSubnetwork(String subnetworkId, String name, String sourceFormat) {
        throw new UnsupportedOperationException("Inner subnetworks are not yet supported");
    }

    /**
     * {@inheritDoc}
     * <p>This operation is not allowed on a subnetwork.</p>
     * <p>This method throws an {@link UnsupportedOperationException}</p>
     */
    @Override
    public void merge(Network other) {
        throw new UnsupportedOperationException("Network " + id + " is already a subnetwork: not supported");
    }

    /**
     * {@inheritDoc}
     * <p>This operation is not allowed on a subnetwork.</p>
     * <p>This method throws an {@link UnsupportedOperationException}</p>
     */
    @Override
    public void merge(Network... others) {
        throw new UnsupportedOperationException("Network " + id + " is already a subnetwork: not supported");
    }

    @Override
    public Network detach() {
        Set<Identifiable<?>> boundaryElements = getBoundaryElements();
        checkDetachable(boundaryElements, true);

        long start = System.currentTimeMillis();

        // Remove tie-lines
        boundaryElements.stream()
                .filter(i -> i.getType() == IdentifiableType.TIE_LINE)
                .map(TieLineImpl.class::cast)
                .forEach(t -> t.remove(true));

        // Create a new NetworkImpl and transfer the extensions to it
        NetworkImpl detachedNetwork = new NetworkImpl(getId(), getNameOrId(), getSourceFormat());
        transferExtensions(this, detachedNetwork);

        // Memorize the network identifiables before moving references (to use them latter)
        Collection<Identifiable<?>> identifiables = getIdentifiables();

        // Move the substations and voltageLevels to the new network
        getSubstationStream().forEach(s -> ((SubstationImpl) s).setSubnetwork(null));
        getVoltageLevelStream().forEach(v -> ((AbstractVoltageLevel) v).setSubnetwork(null));

        // Change network back reference of the network objects
        RefChain<NetworkImpl> refChain = parent.removeRef(getId());
        refChain.setRef(detachedNetwork.getRef());

        // Remove the old subnetwork from the subnetworks list of the current parent network
        parent.removeFromSubnetworks(getId());

        // Remove all the identifiers from the parent's index and add them to the detached network's index
        identifiables.forEach(i -> {
            parent.getIndex().remove(i);
            detachedNetwork.getIndex().checkAndAdd(i);
        });

        // We don't control that regulating terminals and phase/ratio regulation terminals are in the same subnetwork
        // as their network elements (generators, PSTs, ...). It is unlikely that those terminals and their elements
        // are in different subnetworks but nothing prevents it. For now, we ignore this case, but it may be necessary
        // to handle it later. If so, note that there are 2 possible cases:
        // - the element is in the subnetwork to detach and its regulating or phase/ratio regulation terminal is not
        // - the terminal is in the subnetwork, but not its element (this is trickier)

        LOGGER.info("Detaching of {} done in {} ms", id, System.currentTimeMillis() - start);
        return detachedNetwork;
    }

    /**
     * {@inheritDoc}
     * <p>For now, only tie-lines can be split (HVDC lines may be supported later).</p>
     */
    @Override
    public boolean isDetachable() {
        return checkDetachable(getBoundaryElements(), false);
    }

    private boolean checkDetachable(Set<Identifiable<?>> boundaryElements, boolean throwsException) {
        if (parent.getVariantManager().getVariantArraySize() != 1) {
            if (throwsException) {
                throw new PowsyblException("Detaching from multi-variants network is not supported");
            }
            return false;
        }
        if (boundaryElements.stream().anyMatch(Predicate.not(SubnetworkImpl::isSplittable))) {
            if (throwsException) {
                throw new PowsyblException("Some un-splittable boundary elements prevent the subnetwork to be detached");
            }
            return false;
        }
        return true;
    }

    private static boolean isSplittable(Identifiable<?> identifiable) {
        return identifiable.getType() == IdentifiableType.TIE_LINE;
    }

    @Override
    public Set<Identifiable<?>> getBoundaryElements() {
        // transformers cannot link different subnetworks for the moment.
        Stream<Line> lines = parent.getLineStream();
        Stream<TieLine> tieLineStream = parent.getTieLineStream();
        Stream<HvdcLine> hvdcLineStream = parent.getHvdcLineStream();

        return Stream.of(lines, tieLineStream, hvdcLineStream)
                .flatMap(Function.identity())
                .map(o -> (Identifiable<?>) o)
                // Boundary elements are not entirely contained by the subnetwork => they are registered in the parent network
                .filter(i -> i.getParentNetwork() == parent)
                .filter(this::isBoundaryElement)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isBoundaryElement(Identifiable<?> identifiable) {
        switch (identifiable.getType()) {
            case LINE:
            case TIE_LINE:
                return isBoundary((Branch<?>) identifiable);
            case HVDC_LINE:
                return isBoundary((HvdcLine) identifiable);
            default:
                return false;
        }
    }

    private boolean isBoundary(Branch<?> branch) {
        return isBoundary(branch.getTerminal1(), branch.getTerminal2());
    }

    private boolean isBoundary(HvdcLine hvdcLine) {
        return isBoundary(hvdcLine.getConverterStation1().getTerminal(),
                hvdcLine.getConverterStation2().getTerminal());
    }

    private boolean isBoundary(Terminal terminal1, Terminal terminal2) {
        boolean containsVoltageLevel1 = contains(terminal1.getVoltageLevel());
        boolean containsVoltageLevel2 = contains(terminal2.getVoltageLevel());
        return containsVoltageLevel1 && !containsVoltageLevel2 ||
                !containsVoltageLevel1 && containsVoltageLevel2;
    }

    /**
     * {@inheritDoc}
     * <p>This method throws an {@link PowsyblException}.</p>
     * <p>Motivation: The listeners apply to the whole network (root + subnetworks). Thus, in a network with several subnetworks,
     * if calling <code>addListener</code> on a subnetwork registers the given listener on the root network, changes
     * on another subnetwork will also be reported to the listener. This is counterintuitive and could lead to confusion.
     * To avoid that, we don't allow to add or remove listeners from a subnetwork.</p>
     */
    @Override
    public void addListener(NetworkListener listener) {
        throw new PowsyblException("Listeners are not managed at subnetwork level." +
                " Add this listener to the parent network '" + getNetwork().getId() + "'");
    }

    /**
     * {@inheritDoc}
     * <p>This method throws an {@link PowsyblException}.</p>
     * <p>Motivation: The listeners apply to the whole network (root + subnetworks). Thus, in a network with several subnetworks,
     * if calling <code>addListener</code> on a subnetwork registers the given listener on the root network, changes
     * on another subnetwork will also be reported to the listener. This is counterintuitive and could lead to confusion.
     * To avoid that, we don't allow to add or remove listeners from a subnetwork.</p>
     */
    @Override
    public void removeListener(NetworkListener listener) {
        throw new PowsyblException("Listeners are not managed at subnetwork level." +
                " Remove this listener to the parent network '" + getNetwork().getId() + "'");
    }

    @Override
    public ValidationLevel runValidationChecks() {
        return parent.runValidationChecks();
    }

    @Override
    public ValidationLevel runValidationChecks(boolean throwsException) {
        return parent.runValidationChecks(throwsException);
    }

    @Override
    public ValidationLevel runValidationChecks(boolean throwsException, Reporter reporter) {
        return parent.runValidationChecks(throwsException, reporter);
    }

    @Override
    public ValidationLevel getValidationLevel() {
        return parent.getValidationLevel();
    }

    @Override
    public Network setMinimumAcceptableValidationLevel(ValidationLevel validationLevel) {
        return parent.setMinimumAcceptableValidationLevel(validationLevel);
    }

    @Override
    public Stream<Identifiable<?>> getIdentifiableStream(IdentifiableType identifiableType) {
        return parent.getIdentifiableStream(identifiableType).filter(this::contains);
    }
}
