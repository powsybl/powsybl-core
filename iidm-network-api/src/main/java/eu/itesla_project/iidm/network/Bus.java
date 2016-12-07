/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network;

import java.util.stream.Stream;

/**
 * A bus is a set of equipemnts connected together through a closed switch.
 *
 * It could be a configured object ot a result of a computation depending of the
 * context.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Bus extends Identifiable<Bus> {

    /**
     * Get the voltage level to which the bus belongs.
     */
    VoltageLevel getVoltageLevel();

    /**
     * Get the voltage magnitude of the bus in kV.
     */
    float getV();

    /**
     * Set the voltage magnituge of the bus in kV.
     */
    Bus setV(float v);

    /**
     * Get the voltage angle of the bus in degree.
     */
    float getAngle();

    /**
     * Set the voltage angle of the bus in degree.
     */
    Bus setAngle(float angle);

    /**
     * Get the active power in MW injected by equipments connected to the bus.
     */
    float getP();

    /**
     * Get the reactive power in MVAR injected by equiments connected to the bus.
     */
    float getQ();

    /**
     * Get the connected component that the bus is part of.
     */
    ConnectedComponent getConnectedComponent();

    /**
     * Check if the bus belongs to the main connected component
     * @return true if the bus belongs to the main connected component, false otherwise
     */
    boolean isInMainConnectedComponent();

    /**
     * Get the AC lines connected to the bus.
     */
    Iterable<Line> getLines();

    /**
     * Get the AC lines connected to the bus.
     */
    Stream<Line> getLinesStream();

    /**
     * Get 2 windings transformer connected to the bus.
     */
    Iterable<TwoWindingsTransformer> getTwoWindingTransformers();

    /**
     * Get 2 windings transformer connected to the bus.
     */
    Stream<TwoWindingsTransformer> getTwoWindingTransformersStream();

    /**
     * Get 3 windings transformers connected to the bus.
     */
    Iterable<ThreeWindingsTransformer> getThreeWindingTransformers();

    /**
     * Get 3 windings transformers connected to the bus.
     */
    Stream<ThreeWindingsTransformer> getThreeWindingTransformersStream();

    /**
     * Get generators connected to the bus.
     */
    Iterable<Generator> getGenerators();

    /**
     * Get generators connected to the bus.
     */
    Stream<Generator> getGeneratorsStream();

    /**
     * Get loads connected to the bus.
     */
    Iterable<Load> getLoads();

    /**
     * Get loads connected to the bus.
     */
    Stream<Load> getLoadsStream();

    /**
     * Get shunt compensators connected to the bus.
     */
    Iterable<ShuntCompensator> getShunts();

    /**
     * Get shunt compensators connected to the bus.
     */
    Stream<ShuntCompensator> getShuntsStream();

    /**
     * Get dangling lines connected to the bus.
     */
    Iterable<DanglingLine> getDanglingLines();

    /**
     * Get dangling lines connected to the bus.
     */
    Stream<DanglingLine> getDanglingLinesStream();

    /**
     * Get static VAR compensators connected to the bus.
     */
    Iterable<StaticVarCompensator> getStaticVarCompensators();

    /**
     * Get static VAR compensators connected to the bus.
     */
    Stream<StaticVarCompensator> getStaticVarCompensatorsStream();

    /**
     * Get LCC converter stations connected to the bus.
     */
    Iterable<LccConverterStation> getLccConverterStations();

    /**
     * Get LCC converter stations connected to the bus.
     */
    Stream<LccConverterStation> getLccConverterStationsStream();

    /**
     * Get VSC converter stations connected to the bus.
     */
    Iterable<VscConverterStation> getVscConverterStations();

    /**
     * Get VSC converter stations connected to the bus.
     */
    Stream<VscConverterStation> getVscConverterStationsStream();

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

}
