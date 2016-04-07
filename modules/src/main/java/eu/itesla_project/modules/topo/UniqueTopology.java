/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.ShortIdDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UniqueTopology {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueTopology.class);

    public static class Switch {

        private final String id;

        private final PossibleTopology.Bus bus1;

        private final PossibleTopology.Bus bus2;

        public Switch(String id, PossibleTopology.Bus bus1, PossibleTopology.Bus bus2) {
            this.id = id;
            this.bus1 = bus1;
            this.bus2 = bus2;
        }

        public String getId() {
            return id;
        }

        public PossibleTopology.Bus getBus1() {
            return bus1;
        }

        public PossibleTopology.Bus getBus2() {
            return bus2;
        }

    }

    private final String substationId;

    private final Set<PossibleTopology.Bus> buses = new HashSet<>();

    private final Set<Switch> switches = new HashSet<>();

    public UniqueTopology(String substationId) {
        this.substationId = Objects.requireNonNull(substationId);
    }

    public Set<PossibleTopology.Bus> getBuses() {
        return buses;
    }

    public Set<Switch> getSwitches() {
        return switches;
    }

    public boolean containsEquipment(String eqId) {
        for (PossibleTopology.Bus b : buses) {
            for (PossibleTopology.Equipment eq : b.getEquipments()) {
                if (eq.getId().equals(eqId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void number(NumberingContext context) {
        for (PossibleTopology.Bus bus : buses) {
            bus.number(context);
        }
    }

    public void print(PrintStream out) {
        print(out, null);
    }

    public void print(PrintStream out, ShortIdDictionary dict) {
        out.println("substation " + substationId);
        for (PossibleTopology.Bus bus : buses) {
            out.println("    bus " + bus.toString(dict));
        }
        for (UniqueTopology.Switch s : switches) {
            out.println("    switch " + s.getId() + " " + s.getBus1().toString(dict) + "<->" + s.getBus2().toString(dict));
        }
    }

    private static String getBusId(PossibleTopology.Bus bus) {
        return "BUS" + bus.getNum();
    }

    public void apply(Network network) {
        final VoltageLevel vl = network.getVoltageLevel(substationId);
        if (vl == null) {
            throw new AssertionError("Voltage level " + substationId + " not found");
        }

        // store old buses id
        List<Bus> oldBuses = new ArrayList<>();
        for (Bus bus : vl.getBusBreakerView().getBuses()) {
            oldBuses.add(bus);
        }

        Map<PossibleTopology.Equipment, String> eq2bus = new HashMap<>();
        for (PossibleTopology.Bus bus : buses) {
            String busId = getBusId(bus);
            vl.getBusBreakerView().newBus()
                    .setId(busId)
                .add();

            for (PossibleTopology.Equipment eq : bus.getEquipments()) {
                eq2bus.put(eq, busId);
            }
        }

        // remove old switches
        vl.getBusBreakerView().removeAllSwitches();

        // map equipment of the topo history to terminal, this is tricky because of equipment connected at both sides
        // on the same substation
        BiMap<Terminal, PossibleTopology.Equipment> t2eq = HashBiMap.create();
        for (Bus bus : oldBuses) {

            final List<Terminal> terminals = new ArrayList<>();
            bus.visitConnectedOrConnectableEquipments(new TerminalTopologyVisitor() {
                @Override
                public void visitTerminal(Terminal t) {
                    terminals.add(t);
                }
            });

            for (Terminal t : terminals) {
                for (int i = 0; i < 2; i++) {
                    PossibleTopology.Equipment pt = new PossibleTopology.Equipment(t.getConnectable().getId(), i);
                    if (!t2eq.containsValue(pt) && eq2bus.containsKey(pt)) {
                        t2eq.put(t, pt);
                        break;
                    }
                }
            }

        }

        // move equipments to the new buses
        for (Map.Entry<Terminal, PossibleTopology.Equipment> e : t2eq.entrySet()) {
            Terminal t = e.getKey();
            PossibleTopology.Equipment eq = e.getValue();

            String newBusId = eq2bus.get(eq);
            if (newBusId == null) {
                throw new RuntimeException("Equipment " + eq + "not found in the history");
            }
            t.disconnect();
            t.getBusBreakerView().setConnectableBus(newBusId);
            t.connect();
            // reset state variables
            t.setP(Float.NaN).setQ(Float.NaN);
        }

        // remove old buses
        for (Bus oldBus : oldBuses) {
            vl.getBusBreakerView().removeBus(oldBus.getId());
        }

        // create new switches (open)
        for (UniqueTopology.Switch sw : switches) {
            vl.getBusBreakerView().newSwitch()
                    .setId(sw.getId())
                    .setBus1(getBusId(sw.getBus1()))
                    .setBus2(getBusId(sw.getBus2()))
                    .setOpen(true)
                .add();
        }

//        print();
//        vl.printTopology();
    }

}
