/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.Collections;
import java.util.stream.Stream;

/**
 * A bus is a set of equipments connected together through a closed switch.
 * <p>It could be a configured object ot a result of a computation depending of the
 * context.</p>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Bus extends Identifiable<Bus> {

    /**
     * Get the voltage level to which the bus belongs.
     */
    VoltageLevel getVoltageLevel();

    /**
     * Get the voltage magnitude of the bus in kV.
     */
    double getV();

    /**
     * Set the voltage magnituge of the bus in kV.
     */
    Bus setV(double v);

    /**
     * Get the voltage angle of the bus in degree.
     */
    double getAngle();

    /**
     * Set the voltage angle of the bus in degree.
     */
    Bus setAngle(double angle);

    /**
     * Get the active power in MW injected by equipments connected to the bus.
     */
    double getP();

    /**
     * Get the reactive power in MVAR injected by equiments connected to the bus.
     */
    double getQ();

    default double getFictitiousP0() {
        return 0.0;
    }

    default Bus setFictitiousP0(double p0) {
        // do nothing
        return this;
    }

    default double getFictitiousQ0() {
        return 0.0;
    }

    default Bus setFictitiousQ0(double q0) {
        // do nothing
        return this;
    }

    /**
     * Get the connected component that the bus is part of.
     */
    Component getConnectedComponent();

    /**
     * Check if the bus belongs to the main connected component
     * @return true if the bus belongs to the main connected component, false otherwise
     */
    boolean isInMainConnectedComponent();

    /**
     * Get the synchronous component that the bus is part of.
     */
    Component getSynchronousComponent();

    /**
     * Check if the bus belongs to the main synchronous component
     * @return true if the bus belongs to the main synchronous component, false otherwise
     */
    boolean isInMainSynchronousComponent();

    /**
     * Get the number of terminals connected to this bus.
     */
    int getConnectedTerminalCount();

    /**
     * Get the AC lines connected to the bus.
     */
    Iterable<Line> getLines();

    /**
     * Get the AC lines connected to the bus.
     */
    Stream<Line> getLineStream();

    /**
     * Get 2 windings transformer connected to the bus.
     */
    Iterable<TwoWindingsTransformer> getTwoWindingsTransformers();

    /**
     * Get 2 windings transformer connected to the bus.
     */
    Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream();

    /**
     * Get 3 windings transformers connected to the bus.
     */
    Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers();

    /**
     * Get 3 windings transformers connected to the bus.
     */
    Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream();

    /**
     * Get generators connected to the bus.
     */
    Iterable<Generator> getGenerators();

    /**
     * Get generators connected to the bus.
     */
    Stream<Generator> getGeneratorStream();

    /**
     * Get batteries connected to the bus.
     */
    Iterable<Battery> getBatteries();

    /**
     * Get batteries connected to the bus.
     */
    Stream<Battery> getBatteryStream();

    /**
     * Get loads connected to the bus.
     */
    Iterable<Load> getLoads();

    /**
     * Get loads connected to the bus.
     */
    Stream<Load> getLoadStream();

    /**
     * Get shunt compensators connected to the bus.
     */
    Iterable<ShuntCompensator> getShuntCompensators();

    /**
     * Get shunt compensators connected to the bus.
     */
    Stream<ShuntCompensator> getShuntCompensatorStream();

    /**
     * Get dangling lines connected to the bus based on given filter.
     */
    Iterable<DanglingLine> getDanglingLines(DanglingLineFilter danglingLineFilter);

    /**
     * Get all dangling lines connected to the bus.
     */
    default Iterable<DanglingLine> getDanglingLines() {
        return getDanglingLines(DanglingLineFilter.ALL);
    }

    /**
     * Get dangling lines connected to the bus based on given filter.
     */
    Stream<DanglingLine> getDanglingLineStream(DanglingLineFilter danglingLineFilter);

     /**
     * Get all dangling lines connected to the bus.
     */
    default Stream<DanglingLine> getDanglingLineStream() {
        return getDanglingLineStream(DanglingLineFilter.ALL);
    }

    /**
     * Get static VAR compensators connected to the bus.
     */
    Iterable<StaticVarCompensator> getStaticVarCompensators();

    /**
     * Get static VAR compensators connected to the bus.
     */
    Stream<StaticVarCompensator> getStaticVarCompensatorStream();

    /**
     * Get LCC converter stations connected to the bus.
     */
    Iterable<LccConverterStation> getLccConverterStations();

    /**
     * Get LCC converter stations connected to the bus.
     */
    Stream<LccConverterStation> getLccConverterStationStream();

    /**
     * Get VSC converter stations connected to the bus.
     */
    Iterable<VscConverterStation> getVscConverterStations();

    /**
     * Get VSC converter stations connected to the bus.
     */
    Stream<VscConverterStation> getVscConverterStationStream();

    /**
     * Visit equipments connected to the bus.
     *
     * @param visitor a handler to be notified for each equipment connected at the bus
     */
    void visitConnectedEquipments(TopologyVisitor visitor);

    /**
     * Visit equipments connected or connectable to the bus.
     *
     * @param visitor a handler to be notified for each equipment
     */
    void visitConnectedOrConnectableEquipments(TopologyVisitor visitor);

    /**
     * Get an iterable of the terminals connected to the bus.
     */
    default Iterable<? extends Terminal> getConnectedTerminals() {
        return Collections.emptyList();
    }

    /**
     * Get a stream of the terminals connected to the bus.
     */
    default Stream<? extends Terminal> getConnectedTerminalStream() {
        return Stream.empty();
    }

    @Override
    default IdentifiableType getType() {
        return IdentifiableType.BUS;
    }
}
