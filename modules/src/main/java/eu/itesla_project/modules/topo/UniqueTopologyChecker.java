/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import com.google.common.collect.Sets;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.util.Networks;
import eu.itesla_project.iidm.network.util.ShortIdDictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UniqueTopologyChecker {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueTopologyChecker.class);

    private final Network network;

    private final TopologyHistory topologyHistory;

    private final Map<String, UniqueTopology> uniqueTopos;

    private final ShortIdDictionary dict;

    public UniqueTopologyChecker(Network network, TopologyHistory topologyHistory, Map<String, UniqueTopology> uniqueTopos, ShortIdDictionary dict) {
        this.network = Objects.requireNonNull(network);
        this.topologyHistory = Objects.requireNonNull(topologyHistory);
        this.uniqueTopos = Objects.requireNonNull(uniqueTopos);
        this.dict = dict;
    }

    public UniqueTopologyChecker(Network network, TopologyHistory topologyHistory, Map<String, UniqueTopology> uniqueTopos) {
        this(network, topologyHistory, uniqueTopos, null);
    }

    private static Set<Set<String>> toTopoSet(VoltageLevel vl, ShortIdDictionary dict) {
        Set<Set<String>> sets = new HashSet<>();
        for (Bus b : vl.getBusView().getBuses()) {
            final Set<String> set = new HashSet<>();
            b.visitConnectedEquipments(new EquipmentTopologyVisitor() {

                @Override
                public void visitEquipment(Connectable eq) {
                    set.add(dict != null ? dict.getShortId(eq.getId()) : eq.getId());
                }
            });
            sets.add(set);
        }
        return sets;
    }

    // has to be consistent with BusBreakerVoltageLevel.CalculatedBusTopology.isBusValid
    private static boolean isBusValid(PossibleTopology.Bus bus) {
        int feederCount = 0;
        int branchCount = 0;
        for (PossibleTopology.Equipment eq : bus.getEquipments()) {
            switch (eq.getType()) {
                case LINE:
                case TWO_WINDINGS_TRANSFORMER:
                case THREE_WINDINGS_TRANSFORMER:
                    branchCount++;
                    feederCount++;
                    break;

                case DANGLING_LINE:
                case LOAD:
                case GENERATOR:
                case SHUNT_COMPENSATOR:
                    feederCount++;
                    break;

                case BUSBAR_SECTION: // must not happen in a bus/breaker topology
                default:
                    throw new AssertionError();
            }
        }
        return Networks.isBusValid(feederCount, branchCount);
    }

    private static Set<Set<String>> toTopoSet(PossibleTopology.Substation ps, ShortIdDictionary dict) {
        Set<Set<String>> sets = new HashSet<>();
        for (PossibleTopology.Bus b : ps.getBuses()) {
            if (isBusValid(b)) {
                Set<String> set = new HashSet<>();
                for (PossibleTopology.Equipment eq : b.getEquipments()) {
                    set.add(dict != null ? dict.getShortId(eq.getId()) : eq.getId());
                }
                sets.add(set);
            }
        }
        return sets;
    }

    public void check() {
        int errors = 0;
        // create a test state
        String topoCheckStateId = "topocheck";
        String oldState = network.getStateManager().getWorkingStateId();
        network.getStateManager().cloneState(oldState, topoCheckStateId);
        network.getStateManager().setWorkingState(topoCheckStateId);
        try {
            // try to apply all possible configurations to the unique topology and check it is equal to the corresponding
            // historical nodal topology
            for (TopologyChoice topologyChoice : topologyHistory.getTopologyChoices()) {
                for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                    for (PossibleTopology.Substation possibleSubstation : possibleTopology.getMetaSubstation().getSubstations()) {
                        VoltageLevel vl = network.getVoltageLevel(possibleSubstation.getId());
                        if (vl == null) {
                            continue;
                        }
                        for (Map.Entry<String, Boolean> entry : possibleSubstation.getSwitches().entrySet()) {
                            String switchId = entry.getKey();
                            boolean open = entry.getValue();
                            if (open) {
                                vl.getBusBreakerView().openSwitch(switchId);
                            } else {
                                vl.getBusBreakerView().closeSwitch(switchId);
                            }
                        }

                        Set<Set<String>> topoSet = toTopoSet(possibleSubstation, dict);
                        Set<Set<String>> topoSet2 = toTopoSet(vl, dict);
                        if (!topoSet.equals(topoSet2)) {
                            LOGGER.error("Inconsistent unique topology of substation {}", possibleSubstation.getId());
                            LOGGER.error("    history topo set {}: {}", possibleTopology.getTopoHash(), topoSet);
                            LOGGER.error("    network topo set: {}", topoSet2);
                            LOGGER.error("    topo set diff : {}", Sets.symmetricDifference(topoSet, topoSet2));
                            LOGGER.error("    possible topo:");
                            possibleSubstation.print(System.out, 0, dict);
                            LOGGER.error("    unique topo:");
                            uniqueTopos.get(possibleSubstation.getId()).print(System.out, dict);
                            vl.printTopology(System.out, dict);
                            errors++;
                        }
                    }
                }
            }
        } finally {
            network.getStateManager().removeState(topoCheckStateId);
            network.getStateManager().setWorkingState(oldState);
        }
        if (errors > 0) {
            throw new RuntimeException("Check failed for " + errors + " substations");
        }
    }
}
