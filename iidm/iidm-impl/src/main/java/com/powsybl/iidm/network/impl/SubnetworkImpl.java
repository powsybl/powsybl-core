/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.iidm.network.*;
import com.powsybl.commons.ref.RefChain;
import com.powsybl.commons.ref.RefObj;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Miora Vedelago {@literal <miora.ralambotiana at rte-france.com>}
 */
public class SubnetworkImpl extends AbstractNetwork {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubnetworkImpl.class);

    /**
     * Reference to the root network, hence the parent network in this implementation (only one level of subnetworks).
     * This is used to easily update the root network in all equipments when detaching this subnetwork.
     * <p>This {@link RefChain} should reference the {@code ref} attribute of the root network in order for the {@link #flatten()}
     * method to work.</p>
     */
    private final RefChain<NetworkImpl> rootNetworkRef;

    /**
     * Reference to current subnetwork. This is used to easily update the subnetwork reference in substations / voltage
     * levels when detaching this subnetwork.
     */
    private final RefChain<SubnetworkImpl> ref;

    SubnetworkImpl(RefChain<NetworkImpl> rootNetworkRef, String id, String name, String sourceFormat) {
        super(id, name, sourceFormat);
        this.rootNetworkRef = Objects.requireNonNull(rootNetworkRef);
        this.ref = new RefChain<>(new RefObj<>(this));
    }

    SubnetworkImpl(RefChain<NetworkImpl> rootNetworkRef, RefChain<SubnetworkImpl> subnetworkRef, String id, String name, String sourceFormat, ZonedDateTime caseDate) {
        super(id, name, sourceFormat);
        this.rootNetworkRef = Objects.requireNonNull(rootNetworkRef);
        this.ref = Objects.requireNonNull(subnetworkRef);
        this.ref.setRef(new RefObj<>(this));
        setCaseDate(caseDate);
    }

    @Override
    public RefChain<NetworkImpl> getRootNetworkRef() {
        return rootNetworkRef;
    }

    protected RefChain<SubnetworkImpl> getRef() {
        return ref;
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
        return rootNetworkRef.get();
    }

    @Override
    public VariantManager getVariantManager() {
        return getNetwork().getVariantManager();
    }

    @Override
    public void allowReportNodeContextMultiThreadAccess(boolean allow) {
        getNetwork().allowReportNodeContextMultiThreadAccess(allow);
    }

    @Override
    public ReportNodeContext getReportNodeContext() {
        return getNetwork().getReportNodeContext();
    }

    private boolean contains(Identifiable<?> identifiable) {
        return identifiable == this ||
                identifiable != null && identifiable.getParentNetwork() == this;
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
        return getNetwork().getSubstationStream()
                           .filter(this::contains)
                           .map(s -> s.getCountry().orElse(null))
                           .filter(Objects::nonNull)
                           .distinct();
    }

    @Override
    public Iterable<String> getAreaTypes() {
        return getAreaTypeStream().toList();
    }

    @Override
    public Stream<String> getAreaTypeStream() {
        return getAreaStream().map(Area::getAreaType).distinct();
    }

    @Override
    public int getAreaTypeCount() {
        return (int) getAreaTypeStream().count();
    }

    @Override
    public AreaAdder newArea() {
        return new AreaAdderImpl(rootNetworkRef, ref);
    }

    @Override
    public Iterable<Area> getAreas() {
        return getAreaStream().toList();
    }

    @Override
    public Stream<Area> getAreaStream() {
        return getNetwork().getAreaStream().filter(this::contains);
    }

    @Override
    public Area getArea(String id) {
        Area area = getNetwork().getArea(id);
        return contains(area) ? area : null;
    }

    @Override
    public int getAreaCount() {
        return (int) getAreaStream().count();
    }

    @Override
    public SubstationAdder newSubstation() {
        return new SubstationAdderImpl(rootNetworkRef, ref);
    }

    @Override
    public Iterable<Substation> getSubstations() {
        return getSubstationStream().toList();
    }

    @Override
    public Stream<Substation> getSubstationStream() {
        return getNetwork().getSubstationStream().filter(this::contains);
    }

    @Override
    public int getSubstationCount() {
        return (int) getSubstationStream().count();
    }

    @Override
    public Iterable<Substation> getSubstations(Country country, String tsoId, String... geographicalTags) {
        return StreamSupport.stream(getNetwork().getSubstations(country, tsoId, geographicalTags).spliterator(), false)
                .filter(this::contains)
                .toList();
    }

    @Override
    public Iterable<Substation> getSubstations(String country, String tsoId, String... geographicalTags) {
        return StreamSupport.stream(getNetwork().getSubstations(country, tsoId, geographicalTags).spliterator(), false)
                .filter(this::contains)
                .toList();
    }

    @Override
    public Substation getSubstation(String id) {
        Substation s = getNetwork().getSubstation(id);
        return contains(s) ? s : null;
    }

    @Override
    public VoltageLevelAdder newVoltageLevel() {
        return new VoltageLevelAdderImpl(rootNetworkRef, ref);
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return getVoltageLevelStream().toList();
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return getNetwork().getVoltageLevelStream().filter(this::contains);
    }

    @Override
    public int getVoltageLevelCount() {
        return (int) getVoltageLevelStream().count();
    }

    @Override
    public VoltageLevel getVoltageLevel(String id) {
        VoltageLevel vl = getNetwork().getVoltageLevel(id);
        return contains(vl) ? vl : null;
    }

    @Override
    public LineAdder newLine() {
        return getNetwork().newLine(id);
    }

    @Override
    public LineAdder newLine(Line line) {
        return getNetwork().newLine(id, line);
    }

    @Override
    public Iterable<Line> getLines() {
        return getLineStream().toList();
    }

    @Override
    public Branch<?> getBranch(String branchId) {
        Branch<?> b = getNetwork().getBranch(branchId);
        return contains(b) ? b : null;
    }

    @Override
    public Iterable<Branch> getBranches() {
        return getBranchStream().toList();
    }

    @Override
    public Stream<Branch> getBranchStream() {
        return getNetwork().getBranchStream().filter(this::contains);
    }

    @Override
    public int getBranchCount() {
        return (int) getBranchStream().count();
    }

    @Override
    public Stream<Line> getLineStream() {
        return getNetwork().getLineStream().filter(this::contains);
    }

    @Override
    public int getLineCount() {
        return (int) getLineStream().count();
    }

    @Override
    public Line getLine(String id) {
        Line l = getNetwork().getLine(id);
        return contains(l) ? l : null;
    }

    @Override
    public VoltageAngleLimitAdder newVoltageAngleLimit() {
        return getNetwork().newVoltageAngleLimit(id);
    }

    @Override
    public Iterable<VoltageAngleLimit> getVoltageAngleLimits() {
        return getVoltageAngleLimitsStream().toList();
    }

    @Override
    public Stream<VoltageAngleLimit> getVoltageAngleLimitsStream() {
        return getNetwork().getVoltageAngleLimitsStream()
                .filter(val -> contains(val.getTerminalFrom().getVoltageLevel()) && contains(val.getTerminalTo().getVoltageLevel()));
    }

    @Override
    public VoltageAngleLimit getVoltageAngleLimit(String id) {
        VoltageAngleLimitImpl val = (VoltageAngleLimitImpl) getNetwork().getVoltageAngleLimit(id);
        boolean valInSubnetwork = val != null
                && contains(val.getTerminalFrom().getVoltageLevel())
                && contains(val.getTerminalTo().getVoltageLevel());
        return valInSubnetwork ? val : null;
    }

    @Override
    public TieLineAdder newTieLine() {
        return getNetwork().newTieLine(id);
    }

    @Override
    public Iterable<TieLine> getTieLines() {
        return getTieLineStream().toList();
    }

    @Override
    public Stream<TieLine> getTieLineStream() {
        return getNetwork().getTieLineStream().filter(this::contains);
    }

    @Override
    public int getTieLineCount() {
        return (int) getTieLineStream().count();
    }

    @Override
    public TieLine getTieLine(String id) {
        TieLine t = getNetwork().getTieLine(id);
        return contains(t) ? t : null;
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return getTwoWindingsTransformerStream().toList();
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return getNetwork().getTwoWindingsTransformerStream().filter(this::contains);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return (int) getTwoWindingsTransformerStream().count();
    }

    @Override
    public TwoWindingsTransformer getTwoWindingsTransformer(String id) {
        TwoWindingsTransformer twt = getNetwork().getTwoWindingsTransformer(id);
        return contains(twt) ? twt : null;
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return getThreeWindingsTransformerStream().toList();
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return getNetwork().getThreeWindingsTransformerStream().filter(this::contains);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return (int) getThreeWindingsTransformerStream().count();
    }

    @Override
    public ThreeWindingsTransformer getThreeWindingsTransformer(String id) {
        ThreeWindingsTransformer twt = getNetwork().getThreeWindingsTransformer(id);
        return contains(twt) ? twt : null;
    }

    @Override
    public Iterable<OverloadManagementSystem> getOverloadManagementSystems() {
        return getOverloadManagementSystemStream().toList();
    }

    @Override
    public Stream<OverloadManagementSystem> getOverloadManagementSystemStream() {
        return getNetwork().getOverloadManagementSystemStream().filter(this::contains);
    }

    @Override
    public int getOverloadManagementSystemCount() {
        return (int) getOverloadManagementSystemStream().count();
    }

    @Override
    public OverloadManagementSystem getOverloadManagementSystem(String id) {
        OverloadManagementSystem oms = getNetwork().getOverloadManagementSystem(id);
        return contains(oms) ? oms : null;
    }

    @Override
    public Iterable<Generator> getGenerators() {
        return getGeneratorStream().toList();
    }

    @Override
    public Stream<Generator> getGeneratorStream() {
        return getNetwork().getGeneratorStream().filter(this::contains);
    }

    @Override
    public int getGeneratorCount() {
        return (int) getGeneratorStream().count();
    }

    @Override
    public Generator getGenerator(String id) {
        Generator g = getNetwork().getGenerator(id);
        return contains(g) ? g : null;
    }

    @Override
    public Iterable<Battery> getBatteries() {
        return getBatteryStream().toList();
    }

    @Override
    public Stream<Battery> getBatteryStream() {
        return getNetwork().getBatteryStream().filter(this::contains);
    }

    @Override
    public int getBatteryCount() {
        return (int) getBatteryStream().count();
    }

    @Override
    public Battery getBattery(String id) {
        Battery b = getNetwork().getBattery(id);
        return contains(b) ? b : null;
    }

    @Override
    public Iterable<Load> getLoads() {
        return getLoadStream().toList();
    }

    @Override
    public Stream<Load> getLoadStream() {
        return getNetwork().getLoadStream().filter(this::contains);
    }

    @Override
    public int getLoadCount() {
        return (int) getLoadStream().count();
    }

    @Override
    public Load getLoad(String id) {
        Load l = getNetwork().getLoad(id);
        return contains(l) ? l : null;
    }

    @Override
    public Iterable<ShuntCompensator> getShuntCompensators() {
        return getShuntCompensatorStream().toList();
    }

    @Override
    public Stream<ShuntCompensator> getShuntCompensatorStream() {
        return getNetwork().getShuntCompensatorStream().filter(this::contains);
    }

    @Override
    public int getShuntCompensatorCount() {
        return (int) getShuntCompensatorStream().count();
    }

    @Override
    public ShuntCompensator getShuntCompensator(String id) {
        ShuntCompensator s = getNetwork().getShuntCompensator(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter) {
        return getDanglingLineStream(danglingLineFilter).toList();
    }

    @Override
    public Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter) {
        return getNetwork().getDanglingLineStream().filter(this::contains).filter(danglingLineFilter.getPredicate());
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
        DanglingLine dl = getNetwork().getDanglingLine(id);
        return contains(dl) ? dl : null;
    }

    @Override
    public Iterable<StaticVarCompensator> getStaticVarCompensators() {
        return getStaticVarCompensatorStream().toList();
    }

    @Override
    public Stream<StaticVarCompensator> getStaticVarCompensatorStream() {
        return getNetwork().getStaticVarCompensatorStream().filter(this::contains);
    }

    @Override
    public int getStaticVarCompensatorCount() {
        return (int) getStaticVarCompensatorStream().count();
    }

    @Override
    public StaticVarCompensator getStaticVarCompensator(String id) {
        StaticVarCompensator s = getNetwork().getStaticVarCompensator(id);
        return contains(s) ? s : null;
    }

    @Override
    public Switch getSwitch(String id) {
        Switch s = getNetwork().getSwitch(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<Switch> getSwitches() {
        return getSwitchStream().toList();
    }

    @Override
    public Stream<Switch> getSwitchStream() {
        return getNetwork().getSwitchStream().filter(this::contains);
    }

    @Override
    public int getSwitchCount() {
        return (int) getSwitchStream().count();
    }

    @Override
    public BusbarSection getBusbarSection(String id) {
        BusbarSection b = getNetwork().getBusbarSection(id);
        return contains(b) ? b : null;
    }

    @Override
    public Iterable<BusbarSection> getBusbarSections() {
        return getBusbarSectionStream().toList();
    }

    @Override
    public Stream<BusbarSection> getBusbarSectionStream() {
        return getNetwork().getBusbarSectionStream().filter(this::contains);
    }

    @Override
    public int getBusbarSectionCount() {
        return (int) getBusbarSectionStream().count();
    }

    @Override
    public Iterable<HvdcConverterStation<?>> getHvdcConverterStations() {
        return getHvdcConverterStationStream().toList();
    }

    @Override
    public Stream<HvdcConverterStation<?>> getHvdcConverterStationStream() {
        return getNetwork().getHvdcConverterStationStream().filter(this::contains);
    }

    @Override
    public int getHvdcConverterStationCount() {
        return (int) getHvdcConverterStationStream().count();
    }

    @Override
    public HvdcConverterStation<?> getHvdcConverterStation(String id) {
        HvdcConverterStation<?> s = getNetwork().getHvdcConverterStation(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<LccConverterStation> getLccConverterStations() {
        return getLccConverterStationStream().toList();
    }

    @Override
    public Stream<LccConverterStation> getLccConverterStationStream() {
        return getNetwork().getLccConverterStationStream().filter(this::contains);
    }

    @Override
    public int getLccConverterStationCount() {
        return (int) getLccConverterStationStream().count();
    }

    @Override
    public LccConverterStation getLccConverterStation(String id) {
        LccConverterStation s = getNetwork().getLccConverterStation(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<VscConverterStation> getVscConverterStations() {
        return getVscConverterStationStream().toList();
    }

    @Override
    public Stream<VscConverterStation> getVscConverterStationStream() {
        return getNetwork().getVscConverterStationStream().filter(this::contains);
    }

    @Override
    public int getVscConverterStationCount() {
        return (int) getVscConverterStationStream().count();
    }

    @Override
    public VscConverterStation getVscConverterStation(String id) {
        VscConverterStation s = getNetwork().getVscConverterStation(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<HvdcLine> getHvdcLines() {
        return getHvdcLineStream().toList();
    }

    @Override
    public Stream<HvdcLine> getHvdcLineStream() {
        return getNetwork().getHvdcLineStream().filter(this::contains);
    }

    @Override
    public int getHvdcLineCount() {
        return (int) getHvdcLineStream().count();
    }

    @Override
    public HvdcLine getHvdcLine(String id) {
        HvdcLine l = getNetwork().getHvdcLine(id);
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
        return getNetwork().newHvdcLine(id);
    }

    @Override
    public Ground getGround(String id) {
        Ground s = getNetwork().getGround(id);
        return contains(s) ? s : null;
    }

    @Override
    public Iterable<Ground> getGrounds() {
        return getGroundStream().toList();
    }

    @Override
    public Stream<Ground> getGroundStream() {
        return getNetwork().getGroundStream().filter(this::contains);
    }

    @Override
    public int getGroundCount() {
        return (int) getGroundStream().count();
    }

    @Override
    public Identifiable<?> getIdentifiable(String id) {
        Identifiable<?> i = getNetwork().getIdentifiable(id);
        return contains(i) ? i : null;
    }

    @Override
    public Collection<Identifiable<?>> getIdentifiables() {
        return getNetwork().getIdentifiables().stream().filter(this::contains).toList();
    }

    @Override
    public <C extends Connectable> Iterable<C> getConnectables(Class<C> clazz) {
        return getConnectableStream(clazz).toList();
    }

    @Override
    public <C extends Connectable> Stream<C> getConnectableStream(Class<C> clazz) {
        return getNetwork().getConnectableStream(clazz).filter(this::contains);
    }

    @Override
    public <C extends Connectable> int getConnectableCount(Class<C> clazz) {
        return (int) getConnectableStream(clazz).count();
    }

    @Override
    public Iterable<Connectable> getConnectables() {
        return getConnectableStream().toList();
    }

    @Override
    public Stream<Connectable> getConnectableStream() {
        return getNetwork().getConnectableStream().filter(this::contains);
    }

    @Override
    public Connectable<?> getConnectable(String id) {
        Connectable<?> c = getNetwork().getConnectable(id);
        return contains(c) ? c : null;
    }

    @Override
    public int getConnectableCount() {
        return (int) getConnectableStream().count();
    }

    class BusBreakerViewImpl extends AbstractNetwork.AbstractBusBreakerViewImpl {
        @Override
        public Bus getBus(String id) {
            Bus b = getNetwork().getBusBreakerView().getBus(id);
            return contains(b) ? b : null;
        }
    }

    private final BusBreakerViewImpl busBreakerView = new BusBreakerViewImpl();

    @Override
    public BusBreakerView getBusBreakerView() {
        return busBreakerView;
    }

    class BusViewImpl extends AbstractNetwork.AbstractBusViewImpl {

        @Override
        public Bus getBus(String id) {
            Bus b = getNetwork().getBusView().getBus(id);
            return contains(b) ? b : null;
        }

        @Override
        public Collection<Component> getConnectedComponents() {
            return getNetwork().getBusView().getConnectedComponents().stream()
                    .filter(c -> c.getBusStream().anyMatch(SubnetworkImpl.this::contains))
                    .map(c -> (Component) new Subcomponent(c, SubnetworkImpl.this))
                    .toList();
        }

        @Override
        public Collection<Component> getSynchronousComponents() {
            return getNetwork().getBusView().getSynchronousComponents().stream()
                    .filter(c -> c.getBusStream().anyMatch(SubnetworkImpl.this::contains))
                    .map(c -> (Component) new Subcomponent(c, SubnetworkImpl.this))
                    .toList();
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

    @Override
    public Network detach() {
        Set<Identifiable<?>> boundaryElements = getBoundaryElements();
        checkDetachable(boundaryElements, true);

        long start = System.currentTimeMillis();

        // Remove tie-lines
        boundaryElements.stream()
                .filter(DanglingLine.class::isInstance)
                .map(DanglingLine.class::cast)
                .map(DanglingLine::getTieLine)
                .filter(Optional::isPresent)
                .forEach(t -> t.get().remove(true));

        // Create a new NetworkImpl and transfer the extensions to it
        NetworkImpl detachedNetwork = new NetworkImpl(getId(), getNameOrId(), getSourceFormat());
        transferExtensions(this, detachedNetwork);
        transferProperties(this, detachedNetwork);

        // Memorize the network identifiables/voltageAngleLimits before moving references (to use them later)
        Collection<Identifiable<?>> identifiables = getIdentifiables();
        Iterable<VoltageAngleLimit> vals = getVoltageAngleLimits();

        // Move the substations and voltageLevels to the new network
        ref.setRef(new RefObj<>(null));

        // Remove the old subnetwork from the subnetworks list of the current parent network
        NetworkImpl previousRootNetwork = rootNetworkRef.get();
        previousRootNetwork.removeFromSubnetworks(getId());

        // Change root network back reference
        rootNetworkRef.setRef(detachedNetwork.getRef());

        // Remove all the identifiers from the parent's index and add them to the detached network's index
        for (Identifiable<?> i : identifiables) {
            previousRootNetwork.getIndex().remove(i);
            if (i != this) {
                detachedNetwork.getIndex().checkAndAdd(i);
            }
        }
        for (VoltageAngleLimit val : vals) {
            previousRootNetwork.getVoltageAngleLimitsIndex().remove(val.getId());
            detachedNetwork.getVoltageAngleLimitsIndex().put(val.getId(), val);
        }

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
        if (getNetwork().getVariantManager().getVariantArraySize() != 1) {
            if (throwsException) {
                throw new PowsyblException("Detaching from multi-variants network is not supported");
            }
            return false;
        }
        if (boundaryElements.stream().anyMatch(Predicate.not(SubnetworkImpl::isSplittable))) {
            if (throwsException) {
                throw new PowsyblException("Un-splittable boundary elements prevent the subnetwork to be detached: "
                        + boundaryElements.stream().filter(Predicate.not(SubnetworkImpl::isSplittable)).map(Identifiable::getId).collect(Collectors.joining(", ")));
            }
            return false;
        }
        if (getNetwork().getVoltageAngleLimitsStream().anyMatch(this::isBoundary)) {
            if (throwsException) {
                throw new PowsyblException("VoltageAngleLimits prevent the subnetwork to be detached: "
                        + getNetwork().getVoltageAngleLimitsStream().filter(this::isBoundary).map(VoltageAngleLimit::getId).collect(Collectors.joining(", ")));
            }
            return false;
        }
        return true;
    }

    private static boolean isSplittable(Identifiable<?> identifiable) {
        return identifiable.getType() == IdentifiableType.DANGLING_LINE;
    }

    @Override
    public Set<Identifiable<?>> getBoundaryElements() {
        // transformers cannot link different subnetworks for the moment.
        Stream<Line> lines = getNetwork().getLineStream().filter(i -> i.getParentNetwork() == getNetwork());
        Stream<DanglingLine> danglingLineStream = getDanglingLineStream();
        Stream<HvdcLine> hvdcLineStream = getNetwork().getHvdcLineStream().filter(i -> i.getParentNetwork() == getNetwork());

        return Stream.of(lines, danglingLineStream, hvdcLineStream)
                .flatMap(Function.identity())
                .map(o -> (Identifiable<?>) o)
                .filter(this::isBoundaryElement)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isBoundaryElement(Identifiable<?> identifiable) {
        return switch (identifiable.getType()) {
            case LINE, TIE_LINE -> isBoundary((Branch<?>) identifiable);
            case HVDC_LINE -> isBoundary((HvdcLine) identifiable);
            case DANGLING_LINE -> isBoundary((DanglingLine) identifiable);
            default -> false;
        };
    }

    @Override
    public void flatten() {
        throw new UnsupportedOperationException("Subnetworks cannot be flattened.");
    }

    private boolean isBoundary(Branch<?> branch) {
        return isBoundary(branch.getTerminal1(), branch.getTerminal2());
    }

    private boolean isBoundary(HvdcLine hvdcLine) {
        return isBoundary(hvdcLine.getConverterStation1().getTerminal(),
                hvdcLine.getConverterStation2().getTerminal());
    }

    private boolean isBoundary(DanglingLine danglingLine) {
        return danglingLine.getTieLine()
                .map(this::isBoundary)
                .orElse(true);
    }

    private boolean isBoundary(VoltageAngleLimit val) {
        return isBoundary(val.getTerminalFrom(), val.getTerminalTo());
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
        return getNetwork().runValidationChecks();
    }

    @Override
    public ValidationLevel runValidationChecks(boolean throwsException) {
        return getNetwork().runValidationChecks(throwsException);
    }

    @Override
    public ValidationLevel runValidationChecks(boolean throwsException, ReportNode reportNode) {
        return getNetwork().runValidationChecks(throwsException, reportNode);
    }

    @Override
    public ValidationLevel getValidationLevel() {
        return getNetwork().getValidationLevel();
    }

    @Override
    public Network setMinimumAcceptableValidationLevel(ValidationLevel validationLevel) {
        return getNetwork().setMinimumAcceptableValidationLevel(validationLevel);
    }

    @Override
    public Stream<Identifiable<?>> getIdentifiableStream(IdentifiableType identifiableType) {
        return getNetwork().getIdentifiableStream(identifiableType).filter(this::contains);
    }
}
