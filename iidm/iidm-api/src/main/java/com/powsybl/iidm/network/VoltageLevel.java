/**
 * Copyright (c) 2016-2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.iidm.network.util.ShortIdDictionary;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

/**
 * A voltage level is a collection of equipments located in the same substation
 * and at the same base voltage.
 * <p>A voltage level contains a topology model, i.e. an object that describes
 * how equipments are connected together.
 * <h3>Topology model:</h3>
 * A voltage level may have two kinds of topology model depending on what level
 * of detail we want to have ({@link #getTopologyKind}):
 * <ul>
 *   <li>node/breaker model: this is the most detailed way to describe a topology.
 *       All elements are physical ones: busbar sections, breakers and disconnectors.
 *       A node in a node/breaker context means "connection node" and not topological
 *       node or bus.</li>
 *   <li>bus/breaker model: this is an aggregated form of the topology made of buses
 *       and breakers. A bus is the aggregation of busbar sections and closed switches.</li>
 * </ul>
 * <h3>Topology view:</h3>
 * A topology model can be managed through the 3 following views ordered from the
 * most detailed to the less detailed:
 * <ul>
 *   <li>node/breaker view</li>
 *   <li>bus/breaker view</li>
 *   <li>bus only view</li>
 * </ul>
 * <p>Depending on the topology model kind of the voltage level a view can have
 * the status:
 * <ul>
 *   <li>N/A, it doesn't make sense to take view that is more detailed than the
 *       model. An exception is thrown when a method is called on an N/A view</li>
 *   <li>modifiable, when the view has the same level of detail than the model</li>
 *   <li>readable only, because the view is a result of a computation on the
 *       topology model</li>
 * </ul>
 * <p>The view status is summarized in the following table:
 * <table border="1">
 *   <tr>
 *     <th colspan="2" rowspan="2"></th>
 *     <th colspan="2">Topology model</th>
 *   </tr>
 *   <tr>
 *     <th>node/breaker</th>
 *     <th>bus/breaker</th>
 *   </tr>
 *   <tr>
 *     <th rowspan="3">Topology view</th>
 *     <th>node/breaker</th>
 *     <td>modifiable</td>
 *     <td>N/A</td>
 *   </tr>
 *   <tr>
 *     <th>bus/breaker</th>
 *     <td>readable</td>
 *     <td>modifiable</td>
 *   </tr>
 *   <tr>
 *     <th>bus</th>
 *     <td>readable</td>
 *     <td>readable</td>
 *   </tr>
 * </table>
 * <h3>Creating a substation with a node/breaker topology model:</h3>
 * The substation of the example has two voltage levels VL1 and VL2 described by a
 * node/breaker topology model. The first voltage level VL1 has 2 busbar sections
 * BBS1 and BBS2 , a generator GN, a load LD and a coupler BR3 between busbar
 * section BBS1 and BBS2. The second voltage level VL2 has a single busbar
 * section BBS3 a line LN and is connected to voltage level VL1 through
 * transformer TR.
 * <p>Here is a diagram of the substation:
 * <div>
 *    <object data="doc-files/nodeBreakerTopology.svg" type="image/svg+xml"></object>
 * </div>
 * The node/breaker topology model is stored inside the voltage level as a graph
 * where connection nodes are the vertices and switches are the edges.
 * <p>The next diagram shows how to map the subtation topology to a graph.
 * <div>
 *    <object data="doc-files/nodeBreakerTopologyGraph.svg" type="image/svg+xml"></object>
 * </div>
 * Each voltage level has its own topology graph. Voltage level VL1 has 8
 * connection nodes. Generator GN is connected to node 1, load LD to node 5,
 * busbar sections BBS1 and BBS2 to node 3 and 4. 2, 6 and 7 are internal
 * connection nodes. Voltage level VL2 has 3 nodes, line LN is connected to
 * node 1, busbar section BBS3 to node 2. Transformer TR is connected
 * to node 8 of voltage level 400Kv and node 3 of voltage level 225Kv. Plain
 * edges represent closed switches. Dashed edges reprensent opened switches.
 * Green edges will disappear during the bus/breaker topology computation
 * whereas pink edges (like in this case 3<->4) will be retained whatever their
 * position are (see {@link Switch#isRetained()}).
 *<p>The following code shows how to create the substation with a node/breaker
 *   topology model.
 * <pre>
 *    Network n = ...
 *    Substation s = ...
 *    VoltageLevel vl1 = s.newVoltageLevel()
 *        .setId("VL1")
 *        .setTopologyKind(TopologyKind.NODE_BREAKER)
 *        .add();
 *    vl1.getNodeBreakerView().setNodeCount(8);
 *    // create busbar sections BBS1 and BBS2
 *    vl1.getNodeBreakerView().newBusbarSection()
 *        .setId("BBS1")
 *        .add();
 *    vl1.getNodeBreakerView().newBusbarSection()
 *        .setId("BBS2")
 *        .add();
 *    // create generator GN
 *    vl1.newGenerator()
 *        .setId("GN")
 *        .setNode(1)
 *        ...
 *        .add();
 *    // create load LD
 *    vl1.newLoad()
 *        .setId("LD")
 *        .setNode(5)
 *        ...
 *        .add();
 *    // connect generator GN by creating breaker BR1 and disconnectors DI1 and DI2
 *    vl1.getNodeBreakerView().newBreaker()
 *        .setId("BR1")
 *        .setOpen(false)
 *        .setNode1(1)
 *        .setNode1(2)
 *        .add();
 *    vl1.getNodeBreakerView().newDisconnector()
 *        .setId("DI1")
 *        .setOpen(false)
 *        .setNode1(2)
 *        .setNode1(3)
 *        .add();
 *    vl1.getNodeBreakerView().newDisconnector()
 *        .setId("DI2")
 *        .setOpen(true)
 *        .setNode1(2)
 *        .setNode1(4)
 *        .add();
 *    // connect load LD
 *    ...
 *    // create busbar coupler BR3
 *    vl1.getNodeBreakerView().newBreaker()
 *        .setId("BR3")
 *        .setOpen(false)
 *        .setRetained(true) // retain this breaker in the bus/breaker topology!!!
 *        .setNode1(3)
 *        .setNode2(4)
 *        .add();
 *
 *    VoltageLevel vl2 = s.newVoltageLevel()
 *        .setId("VL2")
 *        .setTopologyKind(TopologyKind.NODE_BREAKER)
 *        .add();
 *    vl2.getNodeBreakerView().setNodeCount(3);
 *    // create busbar section BBS3
 *    vl2.getNodeBreakerView().newBusbarSection()
 *        .setId("BBS3")
 *        .add();
 *    // create line LN
 *    n.newLine()
 *        .setId("LN")
 *        .setVoltageLevel1("VL2")
 *        .setNode1(1)
 *        .setVoltageLevel2(...)
 *        .setNode2(...)
 *        ...
 *        .add();
 *
 *    // create transformer TR
 *    s.newTwoWindingsTransformer()
 *        .setId("TR")
 *        .setVoltageLevel1("VL1")
 *        .setNode1(8)
 *        .setVoltageLevel2("VL2")
 *        .setNode2(3)
 *        ...
 *        .add();
 * </pre>
 *
 * <p>The following diagram shows computed bus/breaker topology. Compared to
 * node/breaker topology, only remains equipements (GN, LD, TR, LN), and switches
 * flagged as retained (BR3). Equipments are now connected through buses
 * (B1 and B2).
 * <div>
 *    <object data="doc-files/busBreakerTopology.svg" type="image/svg+xml"></object>
 * </div>
 * <p>To get a bus/breaker view on the substation voltage level VL1 use
 * {@link VoltageLevel#getBusBreakerView}.
 * The following code shows how to get buses and breakers of the bus/breaker
 * view in voltage level VL1.
 * <pre>
 *    // VL1 contains 2 buses in the bus/breaker view
 *    Iterator&lt;Bus&gt; itB = vl1.getBusBreakerView().getBuses().iterator();
 *
 *    // first bus connects nodes 1, 2, 3, 5, 6
 *    Bus b1 = itB.next();
 *    // ... and consequently generator GN and load LD
 *    Generator gn = b1.getGenerators().iterator().next();
 *    Load ld = b1.getLoads().iterator().next();
 *
 *    // bus/breaker view can also be accessed from an equipment
 *    Bus alsoB1 = gn.getTerminal().getBusBreakerView.getBus();
 *
 *    // second bus connects nodes 4, 7, 8
 *    Bus b2 = itB.next();
 *    TwoWindingsTransformer tr = b2.getTwoWindingsTransformer().iterator().next();
 *
 *    // VL1 contains 1 switch in the bus/breaker view
 *    Iterator&lt;Switch&gt; itS = vl1.getBusBreakerView().getSwitches().iterator();
 *    Switch br3 = itS.next();
 * </pre>
 *
 * <p>The following diagram shows computed bus topology. Compared to bus/breaker
 * topology, there is no switches anymore. Only remains equipements (GN, LD, TR, LN)
 * connected through buses.
 * <div>
 *    <object data="doc-files/busTopology.svg" type="image/svg+xml"></object>
 * </div>
 * <p>To get a bus view one the substation voltage level VL1 use
 * {@link VoltageLevel#getBusView}.
 * The following code shows how to get buses of the bus view in voltage level VL1.
 * <pre>
 *    // VL1 contains 1 buses in the bus view
 *    Iterator&lt;Bus&gt; itB = vl1.getBusView().getBuses();
 *
 *    // the bus connects all the equipements of voltage level VL1
 *    Bus b1 = itB.next();
 * </pre>
 * <h3>Creating a substation with a bus/breaker topology model:</h3>
 * Instead of creating VL1 and VL3 with a node/breaker topology model, we can
 * directly create them in a simpler bus/breaker topology model. It can be
 * very useful when data source only contains bus/branch data link in DEF or CIM
 * format.
 * <p>The following code shows how to create the substation with a bus/breaker
 * topology model.
 * <pre>
 *    VoltageLevel vl1 = s.newVoltageLevel()
 *        .setId("VL1")
 *        .setTopologyKind(TopologyKind.BUS_BREAKER)
 *        .add();
 *    // create busbar sections BBS1 and BBS2
 *    vl1.getBusBreakerView().newBus()
 *        .setId("B1")
 *        .add();
 *    vl1.getBusBreakerView().newBus()
 *        .setId("B2")
 *        .add();
 *    // create generator GN
 *    vl1.newGenerator()
 *        .setId("GN")
 *        .setBus("B1")
 *        ...
 *        .add();
 *    // create load LD
 *    vl1.newLoad()
 *        .setId("LD")
 *        .setBus("B1")
 *        ...
 *        .add();
 *     // create busbar coupler BR3
 *     vl1.getBusBreakerView().newBreaker()
 *        .setId("BR3")
 *        .setOpen(false)
 *        .setBus1("B1")
 *        .setBus2("B2")
 *        .add();
 *
 *    VoltageLevel vl2 = s.newVoltageLevel()
 *        .setId("VL2")
 *        .setTopologyKind(TopologyKind.BUS_BREAKER)
 *        .add();
 *    vl2.getBusBreakerView().newBus()
 *        .setId("B3")
 *        .add();
 *    // create line LN
 *    n.newLine()
 *        .setId("LN")
 *        .setVoltageLevel1("VL2");
 *        .setBus1("B3")
 *        .setVoltageLevel2(...);
 *        .setBus2(...)
 *        ...
 *        .add();
 *
 *    // create transformer TR
 *    s.newTwoWindingsTransformer()
 *        .setId("TR")
 *        .setVoltageLevel1("VL1");
 *        .setBus1("B2")
 *        .setVoltageLevel2("VL2");
 *        .setBus2("B3")
 *        ...
 *        .add();
 * </pre>
 * <p>Warning: in that case the node/breaker view status on voltage level VL1 and VL2
 * is N/A.
 *
 * <p>To create a voltage level, see {@link VoltageLevelAdder}
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @see VoltageLevelAdder
 */
public interface VoltageLevel extends Container<VoltageLevel> {

    /**
     * A node/breaker view of the topology.
     */
    interface NodeBreakerView {

        interface SwitchAdder extends IdentifiableAdder<SwitchAdder> {

            SwitchAdder setNode1(int node1);

            SwitchAdder setNode2(int node2);

            SwitchAdder setKind(SwitchKind kind);

            SwitchAdder setKind(String kind);

            SwitchAdder setOpen(boolean open);

            SwitchAdder setRetained(boolean retained);

            SwitchAdder setFictitious(boolean fictitious);

            Switch add();

        }

        interface InternalConnectionAdder extends IdentifiableAdder<InternalConnectionAdder> {

            InternalConnectionAdder setNode1(int node1);

            InternalConnectionAdder setNode2(int node2);

            void add();
        }

        /**
         * Get the number of node.
         */
        int getNodeCount();

        /**
         * Get the list of nodes.
         */
        int[] getNodes();

        /**
         * Set the number of node.
         */
        NodeBreakerView setNodeCount(int count);

        /**
         * Get a builder to create a new switch.
         */
        SwitchAdder newSwitch();

        /**
         * Get a builder to create a new switch.
         */
        InternalConnectionAdder newInternalConnection();

        /**
         * Get a builder to create a new breaker.
         */
        SwitchAdder newBreaker();

        /**
         * Get a builder to create a new disconnector.
         */
        SwitchAdder newDisconnector();

        /**
         * Get the first node to which a switch is connected.
         *
         * @param switchId the id of the switch
         * @throws PowsyblException if switch is not found
         */
        int getNode1(String switchId);

        /**
         * Get the second node to which a switch is connected.
         *
         * @param switchId the id of the switch
         * @throws com.powsybl.commons.PowsyblException if switch is not found
         */
        int getNode2(String switchId);

        /**
         * Get the terminal corresponding to the {@param node}.
         * May return null.
         *
         * @throws PowsyblException if node is not found.
         */
        Terminal getTerminal(int node);

        /**
         * Get the first terminal corresponding to the {@param switchId}.
         * May return null.
         *
         * @throws PowsyblException if switch is not found.
         */
        Terminal getTerminal1(String switchId);

        /**
         * Get the second terminal corresponding to the {@param switchId}.
         * May return null.
         *
         * @throws PowsyblException if switch is not found.
         */
        Terminal getTerminal2(String switchId);

        /**
         * Get a switch.
         *
         * @param switchId the id the switch
         * @return the switch or <code>null</code> if not found
         */
        Switch getSwitch(String switchId);

        /**
         * Get switches.
         */
        Iterable<Switch> getSwitches();

        /**
         * Get switches.
         */
        Stream<Switch> getSwitchStream();

        /**
         * Get the switch count.
         */
        int getSwitchCount();

        /**
         * Remove a switch.
         * @param switchId the switch id
         */
        void removeSwitch(String switchId);

        /**
         * Get a builder to create a new busbar section.
         */
        BusbarSectionAdder newBusbarSection();

        /**
         * Get busbar sections.
         */
        Iterable<BusbarSection> getBusbarSections();

        /**
         * Get busbar sections.
         */
        Stream<BusbarSection> getBusbarSectionStream();

        /**
         * Get the busbar section count.
         */
        int getBusbarSectionCount();

        /**
         * Get a busbar section.
         *
         * @param id the id of the busbar section
         */
        BusbarSection getBusbarSection(String id);

        interface Traverser {
            boolean traverse(int node1, Switch sw, int node2);
        }

        /**
         * Performs a depth-first traversal of the topology graph,
         * starting from {@param node}.
         * The {@param traverser} callback is called every time an edge is traversed.
         */
        void traverse(int node, Traverser traverser);
    }

    /**
     * A bus/breaker view of the topology.
     */
    interface BusBreakerView {

        interface SwitchAdder extends IdentifiableAdder<SwitchAdder> {

            SwitchAdder setBus1(String bus1);

            SwitchAdder setBus2(String bus2);

            SwitchAdder setOpen(boolean open);

            SwitchAdder setFictitious(boolean fictitious);

            Switch add();

        }

        /**
         * Get buses.
         * <p>
         * Depends on the working state if topology kind is NODE_BREAKER.
         * @see StateManager
         */
        Iterable<Bus> getBuses();

        /**
         * Get buses.
         * <p>
         * Depends on the working state if topology kind is NODE_BREAKER.
         * @see StateManager
         */
        Stream<Bus> getBusStream();

        /**
         * Get a bus.
         * <p>
         * Depends on the working state if topology kind is NODE_BREAKER.
         * @param id the id of the bus.
         * @return the bus or <code>null</code> if not found
         * @see StateManager
         */
        Bus getBus(String id);

        /**
         * Get a builder to create a new bus.
         * @throws com.powsybl.commons.PowsyblException if the topology kind is NODE_BREAKER
         */
        BusAdder newBus();

        /**
         * Remove a bus.
         * @param busId the bus id
         */
        void removeBus(String busId);

        /**
         * Remove all buses.
         */
        void removeAllBuses();

        /**
         * Get switches.
         */
        Iterable<Switch> getSwitches();

        /**
         * Get switches.
         */
        Stream<Switch> getSwitchStream();

        /**
         * Get the switch count
         */
        int getSwitchCount();

        /**
         * Remove a switch.
         * @param switchId the switch id
         */
        void removeSwitch(String switchId);

        /**
         * Remove all switches.
         */
        void removeAllSwitches();

        /**
         * Get the first bus to which the switch is connected.
         * <p>
         * Depends on the working state if topology kind is NODE_BREAKER.
         * @param switchId the id of the switch
         * @throws com.powsybl.commons.PowsyblException if switch is not found
         * @see StateManager
         */
        Bus getBus1(String switchId);

        /**
         * Get the second bus to which the switch is connected.
         * <p>
         * Depends on the working state if topology kind is NODE_BREAKER.
         * @param switchId the id of the switch
         * @throws com.powsybl.commons.PowsyblException if switch is not found
         * @see StateManager
         */
        Bus getBus2(String switchId);

        /**
         * Get a switch.
         *
         * @param switchId the id of the switch
         * @return the switch or <code>null</code> if not found
         */
        Switch getSwitch(String switchId);

        /**
         * Get a builer to create a new switch.
         * @throws com.powsybl.commons.PowsyblException if the topology kind is NODE_BREAKER
         */
        SwitchAdder newSwitch();

    }

    /**
     * A bus view of the substation topology.
     */
    interface BusView {

        /**
         * Get buses.
         * <p>
         * Depends on the working state.
         * @see StateManager
         */
        Iterable<Bus> getBuses();

        /**
         * Get buses.
         * <p>
         * Depends on the working state.
         * @see StateManager
         */
        Stream<Bus> getBusStream();

        /**
         * Get a bus.
         *<p>
         * Depends on the working state.
         * @param id the id of the bus.
         * @return the bus or <code>null</code> if not found
         * @see StateManager
         */
        Bus getBus(String id);

        /**
         * Get the merged bus that includes the given configured bus or Busbar section.
         *<p>
         * Depends on the working state.
         * @param configuredBusId The id of the configured bus or busbar section.
         * @return the merged bus or <code>null</code> if not found
         * @see StateManager
         */
        Bus getMergedBus(String configuredBusId);
    }

    /**
     * Topology traversal handler
     */
    interface TopologyTraverser {

        /**
         * Called when a terminal in encountered.
         *
         * @param terminal the encountered terminal
         * @param connected in bus/breaker topology, give the terminal connection status
         * @return true to continue the graph traversal, false otherwise
         */
        boolean traverse(Terminal terminal, boolean connected);

        /**
         * Called when a switch in encountered
         *
         * @param aSwitch the encountered switch
         * @return true to continue the graph traversal, false otherwise
         */
        boolean traverse(Switch aSwitch);

    }

    /**
     * Get the substation to which the voltage level belongs.
     */
    Substation getSubstation();

    /**
     * Get the nominal voltage in KV.
     */
    double getNominalV();

    VoltageLevel setNominalV(double nominalV);

    /**
     * Get the low voltage limit in KV.
     * @return the low voltage limit or NaN if undefined
     */
    double getLowVoltageLimit();

    /**
     * Set the low voltage limit in KV.
     * @param lowVoltageLimit the low voltage limit in KV
     */
    VoltageLevel setLowVoltageLimit(double lowVoltageLimit);

    /**
     * Get the high voltage limit in KV.
     * @return the high voltage limit or NaN if undefined
     */
    double getHighVoltageLimit();

    /**
     * Set the high voltage limit in KV.
     * @param highVoltageLimit the high voltage limit in KV
     */
    VoltageLevel setHighVoltageLimit(double highVoltageLimit);

    /**
     * Get an equipment connected to this substation voltage level.
     * @param id the equipment id
     * @param aClass
     * @return the equipment
     */
    <T extends Connectable> T getConnectable(String id, Class<T> aClass);

    /**
     * Get an Iterable on all the equipments connected to this substation voltage level for a given type.
     * @param clazz equipments type
     * @return all the equipments of the given type
     */
    <T extends Connectable> Iterable<T> getConnectables(Class<T> clazz);

    /**
     * Get a Stream on all the equipments connected to this substation voltage level for a given type.
     * @param clazz equipments type
     * @return all the equipments of the given type
     */
    <T extends Connectable> Stream<T> getConnectableStream(Class<T> clazz);

    /**
     * Count the equipments connected to this substation voltage level for a given type.
     * @param clazz equipments type
     * @return all the equipment of the given type
     */
    <T extends Connectable> int getConnectableCount(Class<T> clazz);

    /**
     * Get an Iterable on all the equipments connected to this substation voltage level.
     * @return all the equipments
     */
    Iterable<Connectable> getConnectables();

    /**
     * Get a Stream on all the equipments connected to this substation voltage level.
     * @return all the equipments
     */
    Stream<Connectable> getConnectableStream();

    /**
     * Count the equipments connected to this substation voltage level.
     * @return all the equipments
     */
    int getConnectableCount();

    /**
     * Get a builder to create a new generator.
     */
    GeneratorAdder newGenerator();

    /**
     * Get generators.
     */
    Iterable<Generator> getGenerators();

    /**
     * Get generators.
     */
    Stream<Generator> getGeneratorStream();

    /**
     * Get generator count.
     */
    int getGeneratorCount();

    /**
     * Get a builder to create a new load.
     */
    LoadAdder newLoad();

    /**
     * Get loads.
     */
    Iterable<Load> getLoads();

    /**
     * Get loads.
     */
    Stream<Load> getLoadStream();

    /**
     * Get switches.
     */
    Iterable<Switch> getSwitches();

    /**
     * Get switch count.
     */
    int getSwitchCount();

    /**
     * Get load count.
     */
    int getLoadCount();

    /**
     * @deprecated Use {@link #newShuntCompensator()} instead.
     */
    @Deprecated
    default ShuntCompensatorAdder newShunt() {
        throw new UnsupportedOperationException("deprecated");
    }

    /**
     * @deprecated Use {@link #getShuntCompensators()} instead.
     */
    @Deprecated
    default Iterable<ShuntCompensator> getShunts() {
        throw new UnsupportedOperationException("deprecated");
    }

    /**
     * @deprecated Use {@link #getShuntCompensatorStream()} instead.
     */
    @Deprecated
    default Stream<ShuntCompensator> getShuntStream() {
        throw new UnsupportedOperationException("deprecated");
    }

    /**
     * @deprecated Use {@link #getShuntCompensatorCount()} instead.
     */
    @Deprecated
    default int getShuntCount() {
        throw new UnsupportedOperationException("deprecated");
    }

    /**
     * Get a builder to create a new compensator shunt.
     */
    default ShuntCompensatorAdder newShuntCompensator() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Get compensator shunts.
     */
    default Iterable<ShuntCompensator> getShuntCompensators() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Get compensator shunts.
     */
    default Stream<ShuntCompensator> getShuntCompensatorStream() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Get shunt count.
     */
    default int getShuntCompensatorCount() {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /**
     * Get a builder to create a new dangling line.
     */
    DanglingLineAdder newDanglingLine();

    /**
     * Get dangling lines.
     */
    Iterable<DanglingLine> getDanglingLines();

    /**
     * Get dangling lines.
     */
    Stream<DanglingLine> getDanglingLineStream();

    /**
     * Get dangling line count.
     */
    int getDanglingLineCount();

    /**
     * Get a builder to create a new static var compensator.
     */
    StaticVarCompensatorAdder newStaticVarCompensator();

    /**
     * Get static var compensators.
     */
    Iterable<StaticVarCompensator> getStaticVarCompensators();

    /**
     * Get static var compensators.
     */
    Stream<StaticVarCompensator> getStaticVarCompensatorStream();

    /**
     * Get static var compensator count.
     */
    int getStaticVarCompensatorCount();

    /**
     * Get a builder to create a new VSC converter station connected to this voltage level.
     * @return a builder to create a new VSC converter
     */
    VscConverterStationAdder newVscConverterStation();

    /**
     * Get all VSC converter stations connected to this voltage level.
     * @return all VSC converter stations connected to this voltage level
     */
    Iterable<VscConverterStation> getVscConverterStations();

    /**
     * Get all VSC converter stations connected to this voltage level.
     * @return all VSC converter stations connected to this voltage level
     */
    Stream<VscConverterStation> getVscConverterStationStream();

    /**
     * Get VSC converter stations count connected to this voltage level.
     * @return VSC converter stations count connected to this voltage level
     */
    int getVscConverterStationCount();

    /**
     * Get a builder to create a new LCC converter station connected to this voltage level.
     * @return a builder to create a new LCC converter
     */
    LccConverterStationAdder newLccConverterStation();

    /**
     * Get all LCC converter stations connected to this voltage level.
     * @return all LCC converter stations connected to this voltage level
     */
    Iterable<LccConverterStation> getLccConverterStations();

    /**
     * Get all LCC converter stations connected to this voltage level.
     * @return all LCC converter stations connected to this voltage level
     */
    Stream<LccConverterStation> getLccConverterStationStream();

    /**
     * Get LCC converter stations count connected to this voltage level.
     * @return LCC converter stations count connected to this voltage level
     */
    int getLccConverterStationCount();

    /**
     * Visit equipments of the voltage level.
     * @param visitor
     */
    void visitEquipments(TopologyVisitor visitor);

    /**
     * Get the kind of topology.
     */
    TopologyKind getTopologyKind();

    /**
     * Get a node/breaker view of the topology.
     *
     * @return a node/breaker view of the topology
     */
    NodeBreakerView getNodeBreakerView();

    /**
     * Get a bus/breaker view of the topology.
     *
     * @return a bus/breaker view of the topology
     */
    BusBreakerView getBusBreakerView();

    /**
     * Get a bus view of the topology.
     *
     * @return a bus view of the topology
     */
    BusView getBusView();

    /**
     * Print an ASCII representation of the topology on the standard ouput.
     */
    void printTopology();

    /**
     * Print an ASCII representation of the topology on a stream.
     *
     * @param out the stream
     */
    void printTopology(PrintStream out, ShortIdDictionary dict);

    /**
     * Export in a file the topology in DOT format (Graphviz).
     *
     * @param filename the file name
     */
    void exportTopology(String filename) throws IOException;

    /**
     * Export the topology in DOT format (Graphviz).
     *
     * @param outputStream a writer
     */
    void exportTopology(OutputStream outputStream) throws IOException;

}
