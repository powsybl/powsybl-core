/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.topo;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eu.itesla_project.commons.jaxb.IntervalAdapter;
import eu.itesla_project.iidm.network.*;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@XmlRootElement
public class TopologyHistory {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopologyHistory.class);

    public static boolean MOVE_ISOLATED_LOADS = true;

    public static final String FICTIVE_PATTERN = "fict";

    private static Path getPath(Path dir, Interval histoInterval, double correlationThreshold) {
        return dir.resolve("topology-history-" + Double.toString(correlationThreshold) + "-" + histoInterval.getStart() + "-" + histoInterval.getEnd() + ".xml");
    }

    public static TopologyHistory load(Path dir, Interval histoInterval, double threshold) {
        Path file = getPath(dir, histoInterval, threshold);
        if (Files.exists(file)) {
            LOGGER.info("Loading topology history cache {}...", file);
            try {
                JAXBContext context = JAXBContext.newInstance(TopologyHistory.class);
                Unmarshaller m = context.createUnmarshaller();
                try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                    return (TopologyHistory) m.unmarshal(reader);
                }
            } catch (JAXBException|IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @XmlAttribute(required=true)
    @XmlJavaTypeAdapter(type=Interval.class, value=IntervalAdapter.class)
    private final Interval histoInterval;

    @XmlAttribute(required=true)
    private double threshold;

    @XmlElement(name="topologyChoice")
    private final List<TopologyChoice> topologyChoices = new ArrayList<>();

    public TopologyHistory(Interval histoInterval, double threshold) {
        this.histoInterval = histoInterval;
        this.threshold = threshold;
    }

    // for JAXB
    public TopologyHistory() {
        this(null, 0);
    }

    public Interval getHistoInterval() {
        return histoInterval;
    }

    public double getThreshold() {
        return threshold;
    }

    public List<TopologyChoice> getTopologyChoices() {
        return topologyChoices;
    }

    public void number() {
        NumberingContext context = new NumberingContext();
        for (TopologyChoice topologyChoice : topologyChoices) {
            topologyChoice.number(context);
        }
    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream out) {
        out.println("topoHisto");
        for (TopologyChoice topologyChoice : topologyChoices) {
            topologyChoice.print(out, 4);
        }
    }

    public void save(Writer writer) {
        try {
            JAXBContext context = JAXBContext.newInstance(TopologyHistory.class);
            Marshaller m = context.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(this, writer);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(Path dir) {
        try (Writer writer = Files.newBufferedWriter(getPath(dir, histoInterval, threshold), StandardCharsets.UTF_8)) {
            save(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public float[] getMeanProbabilityRange() {
        float[] rangeSum = new float[2];
        float[] rangeCount = new float[2];
        for (TopologyChoice topologyChoice : topologyChoices) {
            topologyChoice.getPossibleTopologies().stream()
                    .mapToDouble(PossibleTopology::getProbability)
                    .min()
                    .ifPresent(d -> {
                        rangeSum[0] += d;
                        rangeCount[0]++;
                    });
            topologyChoice.getPossibleTopologies().stream()
                    .mapToDouble(PossibleTopology::getProbability)
                    .max()
                    .ifPresent(d -> {
                        rangeSum[1] += d;
                        rangeCount[1]++;
                    });
        }
        rangeSum[0] /= rangeCount[0];
        rangeSum[1] /= rangeCount[1];
        return rangeSum;
    }

    private void mergeDanglingLines(Network network) {
        // do on historical data same line merging as in reference network
        Map<String, String> lineIdToMergedLineId = new HashMap<>();
        for (Line l : network.getLines()) {
            if (l.isTieLine()) {
                TieLine tieLine = (TieLine) l;
                String lineId1 = tieLine.getHalf1().getId();
                String lineId2 = tieLine.getHalf2().getId();
                lineIdToMergedLineId.put(lineId1, tieLine.getId());
                lineIdToMergedLineId.put(lineId2, tieLine.getId());
            }
        }

        Set<String> mergedLineIdsFound = new HashSet<>();

        for (TopologyChoice topologyChoice : topologyChoices) {
            for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    for (PossibleTopology.Bus bus : substation.getBuses()) {
                        for (PossibleTopology.Equipment eq : bus.getEquipments()) {
                            String mergedLineId = lineIdToMergedLineId.get(eq.getId());
                            if (mergedLineId != null) {
                                eq.setId(mergedLineId);
                                mergedLineIdsFound.add(mergedLineId);
                            }
                        }
                    }
                }
            }
        }

        if (mergedLineIdsFound.size() > 0) {
            LOGGER.debug("{} merged line have been found in the history", mergedLineIdsFound.size());
            LOGGER.trace("Detailed list of merged line have been found in the history: {}",
                    mergedLineIdsFound.size(), mergedLineIdsFound);
        }
    }

    private void fixEquipmentsNotInReferenceNetwork(Network network, Set<String> excludedTopoIds, Set<String> excludedEquipmentIds) {
        Set<String> substationsRemoved = new HashSet<>();
        Set<String> fictiveEquimentsRemoved = new HashSet<>();
        Set<String> equimentsRemoved = new HashSet<>();
        Set<String> equipmentsNotFound = new HashSet<>();
        int removedTopoCount = 0;
        for (TopologyChoice topologyChoice : topologyChoices) {
            for (Iterator<PossibleTopology> itP = topologyChoice.getPossibleTopologies().iterator(); itP.hasNext();) {
                PossibleTopology possibleTopology = itP.next();
                boolean removeTopo = false;
                for (Iterator<PossibleTopology.Substation> itS = possibleTopology.getMetaSubstation().getSubstations().iterator(); itS.hasNext();) {
                    PossibleTopology.Substation substation = itS.next();
                    VoltageLevel vl = network.getVoltageLevel(substation.getId());
                    if (vl == null) {
                        substationsRemoved.add(substation.getId());
                        itS.remove();
                    } else {
                        for (PossibleTopology.Bus bus : substation.getBuses()) {
                            for (Iterator<PossibleTopology.Equipment> itEq = bus.getEquipments().iterator(); itEq.hasNext();) {
                                PossibleTopology.Equipment eq = itEq.next();
                                Connectable obj = vl.getConnectable(eq.getId(), Connectable.class);
                                if (obj == null) {
                                    if (eq.getId().contains(FICTIVE_PATTERN)) {
                                        fictiveEquimentsRemoved.add(eq.getId());
                                        itEq.remove();
                                    } else {
                                        equipmentsNotFound.add(eq.getId());
                                        if (topologyChoice.getPossibleTopologies().size() == 1) {
                                            // In the case where there is only one possible topo remaining we for sure
                                            // cannot remove so in that case it is best to remove the equipment
                                            equimentsRemoved.add(eq.getId());
                                            itEq.remove();
                                        } else {
                                            removeTopo = true;
                                        }
                                    }
                                    excludedEquipmentIds.add(eq.getId());
                                } else {
                                    // set equipment type
                                    eq.setType(obj.getType());
                                }
                            }
                        }
                    }
                }
                if (removeTopo) {
                    excludedTopoIds.add(possibleTopology.getTopoHash());
                    itP.remove();
                    removedTopoCount++;
                }
            }
        }
        if (substationsRemoved.size() > 0) {
            LOGGER.debug("{} substations have been removed from history because not found in reference network", substationsRemoved.size());
            LOGGER.trace("Detailed list of removed substation: {}", substationsRemoved);
        }
        if (equipmentsNotFound.size() > 0) {
            LOGGER.debug("{} equipments of the history have not been found in reference network, resulting to the removal of {} possible topologies",
                    equipmentsNotFound.size(), removedTopoCount);
            LOGGER.trace("Detailed list of equipments not found: {}", equipmentsNotFound);
        }
        if (equimentsRemoved.size() > 0) {
            LOGGER.debug("{} equipments have been removed because not found in reference network", equimentsRemoved.size());
            LOGGER.trace("Detailed list of removed equipments: {}", equimentsRemoved);
        }
        if (fictiveEquimentsRemoved.size() > 0) {
            LOGGER.debug("{} fictive equipments have been removed because not found in reference network", fictiveEquimentsRemoved.size());
        }
    }

    private boolean removeEquipmentsOfMainConnectedComponent(int iteration, Set<String> excludedEquipmentIds) {
        TopologyHistoryConnectedComponentsAnalyser ccAnalyser = new TopologyHistoryConnectedComponentsAnalyser(this);
        Set<String> equipmentIdsInMainCC = ccAnalyser.analyse();
        Set<String> equipmentIdsOutOfMainCC = new HashSet<>();

        // remove substation out of main connected component from all possible topologies
        for (TopologyChoice topologyChoice : topologyChoices) {
            for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    for (PossibleTopology.Bus bus : substation.getBuses()) {
                        for (Iterator<PossibleTopology.Equipment> it = bus.getEquipments().iterator(); it.hasNext();) {
                            PossibleTopology.Equipment equipment = it.next();
                            if (!equipmentIdsInMainCC.contains(equipment.getId())) {
                                it.remove();
                                equipmentIdsOutOfMainCC.add(equipment.getId());
                            }
                        }
                    }
                }
            }
        }

        excludedEquipmentIds.addAll(equipmentIdsOutOfMainCC);

        if (equipmentIdsOutOfMainCC.size() > 0) {
            LOGGER.debug("Iteration {}: {} equipments removed because out of main connected component",
                    iteration, equipmentIdsOutOfMainCC.size());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Iteration {}: detailed list of equipments removed because out of main connected component: {}",
                        iteration, equipmentIdsOutOfMainCC);
            }
            return true;
        }
        return false;
    }

    private boolean fixBranchesAlwaysDisconnectAtOneSide(int iteration, Network network) {
        // avoid branches always disconnected at one side => connect it to an isolated bus on disconnected side
        Multimap<String, String> branch2substations = HashMultimap.create();
        for (TopologyChoice topologyChoice : topologyChoices) {
            for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    for (PossibleTopology.Bus bus : substation.getBuses()) {
                        for (PossibleTopology.Equipment eq : bus.getEquipments()) {
                            if (eq.isBranch(false)) {
                                branch2substations.put(eq.getId(), substation.getId());
                            }
                        }
                    }
                }
            }
        }
        Set<String> branchesAlwaysDisconnectedAtOneSide = new HashSet<>();
        Multimap<String, String> substation2branches = HashMultimap.create();
        for (Map.Entry<String, Collection<String>> entry : branch2substations.asMap().entrySet()) {
            String branchId = entry.getKey();
            if (entry.getValue().size() == 1) {
                String substationId = entry.getValue().iterator().next();
                TwoTerminalsConnectable branch = network.getLine(branchId);
                if (branch == null) {
                    branch = network.getTwoWindingsTransformer(branchId);
                }
                if (branch == null) {
                    throw new RuntimeException();
                }
                if (branch.getTerminal1().getVoltageLevel() != branch.getTerminal2().getVoltageLevel()) {
                    String otherSubstationId;
                    if (branch.getTerminal1().getVoltageLevel().getId().equals(substationId)) {
                        otherSubstationId = branch.getTerminal2().getVoltageLevel().getId();
                    } else if (branch.getTerminal2().getVoltageLevel().getId().equals(substationId)) {
                        otherSubstationId = branch.getTerminal1().getVoltageLevel().getId();
                    } else {
                        throw new RuntimeException();
                    }
                    substation2branches.put(otherSubstationId, branchId);
                    branchesAlwaysDisconnectedAtOneSide.add(branchId);
                }
            }
        }

        for (TopologyChoice topologyChoice : topologyChoices) {
            for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    if (substation2branches.containsKey(substation.getId())) {
                        VoltageLevel vl = network.getVoltageLevel(substation.getId());
                        for (String branchId : substation2branches.asMap().get(substation.getId())) {
                            PossibleTopology.Equipment eq = new PossibleTopology.Equipment(branchId);
                            Connectable obj = vl.getConnectable(eq.getId(), Connectable.class);
                            eq.setType(obj.getType());
                            substation.getBuses().add(new PossibleTopology.Bus(eq));
                        }
                    }
                }
            }
        }
        if (branchesAlwaysDisconnectedAtOneSide.size() > 0) {
            LOGGER.debug("Iteration {}: {} branches are always disconnected at one side, a fictive bus (isolated) has been added to disconnected side",
                    iteration, branchesAlwaysDisconnectedAtOneSide.size());
            LOGGER.trace("Iteration {}: detailed list of branches always disconnected at one side: {}",
                    iteration, branchesAlwaysDisconnectedAtOneSide);
            return true;
        }
        return false;
    }

    private boolean removeLowProbabilityPossibleTopologies(int iteration, double probabilityThreshold, Set<String> excludedTopoIds) {
        Multimap<String, String> removedPerMetaSubstation = HashMultimap.create();
        Map<String, Integer> topologyCountPerMetaSubstationBefore = new HashMap<>();
        for (TopologyChoice topologyChoice : topologyChoices) {
            // skip lowest probability topologies but skip one at least...
            Collections.sort(topologyChoice.getPossibleTopologies(), PossibleTopology.COMPARATOR);
            int count = topologyChoice.getPossibleTopologies().size();
            topologyCountPerMetaSubstationBefore.put(topologyChoice.getPossibleTopologies().iterator().next().getMetaSubstation().getId(), count);
            int removedCount = 0;
            for (Iterator<PossibleTopology> it = topologyChoice.getPossibleTopologies().iterator(); it.hasNext() && topologyChoice.getPossibleTopologies().size() > 1;) {
                PossibleTopology possibleTopology = it.next();
                if (possibleTopology.getProbability() < probabilityThreshold && removedCount < count - 1) {
                    removedPerMetaSubstation.put(possibleTopology.getMetaSubstation().getId(), possibleTopology.getTopoHash());
                    it.remove();
                    excludedTopoIds.add(possibleTopology.getTopoHash());
                    removedCount++;
                }
            }
            if (topologyChoice.getPossibleTopologies().isEmpty()) {
                throw new RuntimeException("Empty topo choice");
            }
        }
        int removedTopoCount = removedPerMetaSubstation.asMap().entrySet().stream().mapToInt(e -> e.getValue().size()).sum();
        if (removedTopoCount > 0) {
            LOGGER.debug("Iteration {}: {} possible topologies removed because very low probability (< {})",
                    iteration, removedTopoCount, probabilityThreshold);
            if (LOGGER.isTraceEnabled()) {
                for (Map.Entry<String, Collection<String>> entry : removedPerMetaSubstation.asMap().entrySet()) {
                    String metaSubstationId = entry.getKey();
                    LOGGER.trace("Iteration {}: remove {} possible topologies on {} of meta substation {} because of very low probability",
                            iteration, entry.getValue().size(), topologyCountPerMetaSubstationBefore.get(metaSubstationId), metaSubstationId);
                }
            }
            return true;
        }
        return false;
    }

    private boolean removeDuplicatedPossibleTopologies(int iteration, Set<String> excludedTopoIds) {
        Map<String, Integer> duplicateTopoCount= new HashMap<>();
        Map<String, Integer> initialTopoCount = new HashMap<>();
        for (TopologyChoice topologyChoice : topologyChoices) {
            if (topologyChoice.getPossibleTopologies().size() > 0) {
                String metaSubstationId = topologyChoice.getPossibleTopologies().get(0).getMetaSubstation().getId();
                initialTopoCount.put(metaSubstationId, topologyChoice.getPossibleTopologies().size());
                for (Iterator<PossibleTopology> it = topologyChoice.getPossibleTopologies().iterator(); it.hasNext();) {
                    PossibleTopology possibleTopology = it.next();
                    // remove duplicated topologies
                    boolean duplicated = false;
                    for (PossibleTopology other : topologyChoice.getPossibleTopologies()) {
                        if (possibleTopology != other
                                && possibleTopology.getMetaSubstation().equals(other.getMetaSubstation())
                                && possibleTopology.getProbability() < other.getProbability()) {
                            duplicated = true;
                            break;
                        }
                    }
                    if (duplicated) {
                        if (duplicateTopoCount.containsKey(metaSubstationId)) {
                            duplicateTopoCount.put(metaSubstationId, duplicateTopoCount.get(metaSubstationId) + 1);
                        } else {
                            duplicateTopoCount.put(metaSubstationId, 1);
                        }
                        excludedTopoIds.add(possibleTopology.getTopoHash());
                        it.remove();
                    }
                }
            }
        }
        int removedTopoCount = duplicateTopoCount.entrySet().stream().mapToInt(Map.Entry::getValue).sum();
        if (removedTopoCount > 0) {
            LOGGER.debug("Iteration {}: {} duplicated possible topologies have been removed",
                    iteration, removedTopoCount);
            if (LOGGER.isTraceEnabled()) {
                for (Map.Entry<String, Integer> entry : duplicateTopoCount.entrySet()) {
                    String metaSubstationId = entry.getKey();
                    int count = entry.getValue();
                    int initCount = initialTopoCount.get(metaSubstationId);
                    if (count == initCount) {
                        throw new RuntimeException("No more possible topo for meta substation " + metaSubstationId
                                + " after removing all duplicates");
                    }
                    LOGGER.trace("Iteration {}: remove {} duplicated possible topology on {} in meta substation {}",
                            iteration, count, initCount, metaSubstationId);
                }
            }
            return true;
        }
        return false;
    }

    private List<PossibleTopology.Equipment> getIsolatedLoads(PossibleTopology.Bus bus) {
        List<PossibleTopology.Equipment> loads = new ArrayList<>();
        int branchCount = 0;
        for (PossibleTopology.Equipment equipment : bus.getEquipments()) {
            if (equipment.isBranch()) {
                branchCount++;
            } else if (equipment.getType() == ConnectableType.LOAD) {
                loads.add(equipment);
            }
        }
        if (loads.size() > 0 && branchCount == 0) {
            return loads;
        } else {
            return null;
        }
    }

    private boolean moveIsolatedLoads(TopologyChoice topologyChoice, PossibleTopology possibleTopology, Set<String> movedIsolatedLoads) {
        for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
            List<PossibleTopology.Equipment> isolatedLoads = new ArrayList<>();
            for (Iterator<PossibleTopology.Bus> it = substation.getBuses().iterator(); it.hasNext();) {
                PossibleTopology.Bus bus = it.next();
                List<PossibleTopology.Equipment> busIsolatedLoads = getIsolatedLoads(bus);
                if (busIsolatedLoads != null) {
                    isolatedLoads.addAll(busIsolatedLoads);
                    it.remove();
                }
            }
            if (isolatedLoads.size() > 0) {
                if (substation.getBuses().isEmpty()) {
                    LOGGER.warn("No more bus to move isolated loads {}", isolatedLoads.stream().map(PossibleTopology.Equipment::getId).collect(Collectors.toList()));
                    return false;
                }
                for (PossibleTopology.Equipment isolatedLoad : isolatedLoads) {
                    Map<String, Integer> branchesConnectedToThisIsolatedLoadCounter = new HashMap<>();
                    // find in the other possible topos to which branch it is likely to be connected
                    for (PossibleTopology otherPossibleTopology : topologyChoice.getPossibleTopologies()) {
                        for (PossibleTopology.Substation otherSubstation : otherPossibleTopology.getMetaSubstation().getSubstations()) {
                            for (PossibleTopology.Bus otherBus : otherSubstation.getBuses()) {
                                if (otherBus.getEquipments().contains(isolatedLoad)) {
                                    for (PossibleTopology.Equipment otherEq : otherBus.getEquipments()) {
                                        if (otherEq.isBranch()) {
                                            if (branchesConnectedToThisIsolatedLoadCounter.containsKey(otherEq.getId())) {
                                                branchesConnectedToThisIsolatedLoadCounter.put(otherEq.getId(), branchesConnectedToThisIsolatedLoadCounter.get(otherEq.getId()) + 1);
                                            } else {
                                                branchesConnectedToThisIsolatedLoadCounter.put(otherEq.getId(), 1);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    String branchId = branchesConnectedToThisIsolatedLoadCounter.entrySet().stream()
                            .sorted((o1, o2) -> o2.getValue() - o1.getValue())
                            .findFirst()
                            .get()
                            .getKey();
                    PossibleTopology.Bus mostProbableBus = substation.findEquipmentBus(branchId);
                    if (mostProbableBus == null) {
                        throw new RuntimeException("No most probable bus found in substation " + substation.getId());
                    }

                    mostProbableBus.getEquipments().add(isolatedLoad);
                    movedIsolatedLoads.add(isolatedLoad.getId());
                }
            }
        }
        return true;
    }

    private boolean moveIsolatedLoads(int iteration, Set<String> excludedTopoIds) {
        Set<String> movedIsolatedLoads = new HashSet<>();
        int removedTopoCount = 0;
        for (TopologyChoice topologyChoice : topologyChoices) {
            String metaSubstationId = topologyChoice.getPossibleTopologies().iterator().next().getMetaSubstation().getId();
            for (Iterator<PossibleTopology> it = topologyChoice.getPossibleTopologies().iterator(); it.hasNext();) {
                PossibleTopology possibleTopology = it.next();
                if (!moveIsolatedLoads(topologyChoice, possibleTopology, movedIsolatedLoads)) {
                    it.remove();
                    removedTopoCount++;
                    excludedTopoIds.add(possibleTopology.getTopoHash());
                }
            }
            if (topologyChoice.getPossibleTopologies().isEmpty()) {
                throw new RuntimeException("Oups, no more possible topo for " + topologyChoice.getClusterId() + " (" + metaSubstationId + ")");
            }
        }
        boolean fixed = false;
        if (movedIsolatedLoads.size() > 0) {
            LOGGER.debug("Iteration {}: {} isolated loads have been moved to most probable bus",
                    iteration, movedIsolatedLoads.size());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Iteration {}: detailed list of isolated loads moved to most probable bus: {}",
                        iteration, movedIsolatedLoads);
            }
            fixed = true;
        }
        if (removedTopoCount > 0) {
            LOGGER.debug("Iteration {}: {} possible topologies have been removed because of isolated load",
                    iteration, removedTopoCount);
            fixed = true;
        }
        return fixed;
    }

    private boolean removePossibleTopologiesWithIsolatedLoads(int iteration, Set<String> excludedTopoIds) {
        int removedTopoCount = 0;
        for (TopologyChoice topologyChoice : topologyChoices) {
            String metaSubstationId = topologyChoice.getPossibleTopologies().iterator().next().getMetaSubstation().getId();
            for (Iterator<PossibleTopology> it = topologyChoice.getPossibleTopologies().iterator(); it.hasNext();) {
                PossibleTopology possibleTopology = it.next();
                EXIT: for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    for (PossibleTopology.Bus bus : substation.getBuses()) {
                        if (getIsolatedLoads(bus) != null) {
                            removedTopoCount++;
                            excludedTopoIds.add(possibleTopology.getTopoHash());
                            it.remove();
                            break EXIT;
                        }
                    }
                }
            }
            if (topologyChoice.getPossibleTopologies().isEmpty()) {
                throw new RuntimeException("Oups, no more possible topo for " + topologyChoice.getClusterId() + " (" + metaSubstationId + ")");
            }
        }
        if (removedTopoCount > 0) {
            LOGGER.debug("Iteration {}: {} possible topologies have been removed because of isolated load",
                    iteration, removedTopoCount);
            return true;
        }
        return false;
    }

    private boolean removeEmptyBuses(int iteration) {
        Set<String> substationIdsWhereAbusHasBeenRemoved = new HashSet<>();
        for (TopologyChoice topologyChoice : topologyChoices) {
            for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    for (Iterator<PossibleTopology.Bus> it = substation.getBuses().iterator(); it.hasNext();) {
                        PossibleTopology.Bus bus = it.next();
                        if (bus.getEquipments().isEmpty()) {
                            substationIdsWhereAbusHasBeenRemoved.add(substation.getId());
                            it.remove();
                        }
                    }
                }
            }
        }
        if (substationIdsWhereAbusHasBeenRemoved.size() > 0) {
            LOGGER.trace("Iteration {}: an empty bus has been removed in substations: {}",
                    iteration, substationIdsWhereAbusHasBeenRemoved);
            return true;
        }
        return false;
    }

    private boolean fixDisconnectedEquipments(int iteration) {
        boolean fixed = false;

        Set<String> injectionsConnectedToFictiveBus = new HashSet<>();

        for (TopologyChoice topologyChoice : topologyChoices) {
            // list of injections connected at least one time per substation
            Multimap<String, PossibleTopology.Equipment> equipmentsPerSubstation = HashMultimap.create();
            for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    for (PossibleTopology.Bus bus : substation.getBuses()) {
                        for (PossibleTopology.Equipment eq : bus.getEquipments()) {
                            equipmentsPerSubstation.put(substation.getId(), eq);
                        }
                    }
                }
            }

            for (PossibleTopology possibleTopology : topologyChoice.getPossibleTopologies()) {
                for (PossibleTopology.Substation substation : possibleTopology.getMetaSubstation().getSubstations()) {
                    for (PossibleTopology.Equipment equipment : equipmentsPerSubstation.get(substation.getId())) {
                        if (!substation.containsEquipment(equipment)) {
                            // create a fictive bus and connect the equipment
                            PossibleTopology.Bus fictBus = new PossibleTopology.Bus();
                            fictBus.getEquipments().add(equipment);
                            substation.getBuses().add(fictBus);
                            injectionsConnectedToFictiveBus.add(equipment.getId());
                            fixed = true;
                        }
                    }
                }
            }
        }

        if (injectionsConnectedToFictiveBus.size() > 0) {
            LOGGER.debug("Iteration {}: {} equipments have been connected to a fictive bus in some possible topos",
                    iteration, injectionsConnectedToFictiveBus.size());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("Iteration {}: detailed list of equipments connected to a fictive bus in some possible topos: {}",
                        iteration, injectionsConnectedToFictiveBus);
            }
        }

        return fixed;
    }

    public void clean(Network network, double probabilityThreshold, Set<String> excludedTopoIds, Set<String> excludedEquipmentIds) {
        mergeDanglingLines(network);

        // count initial possible topologies
        int intialTopoCount = topologyChoices.stream().mapToInt(topologyChoice -> topologyChoice.getPossibleTopologies().size()).sum();
        fixEquipmentsNotInReferenceNetwork(network, excludedTopoIds, excludedEquipmentIds);

        boolean fixed;
        int iteration  = 0;
        do {
            fixed = fixDisconnectedEquipments(iteration);
            fixed |= fixBranchesAlwaysDisconnectAtOneSide(iteration, network);
            fixed |= removeEquipmentsOfMainConnectedComponent(iteration, excludedEquipmentIds);
            fixed |= MOVE_ISOLATED_LOADS ? moveIsolatedLoads(iteration, excludedTopoIds) : removePossibleTopologiesWithIsolatedLoads(iteration, excludedTopoIds);
            fixed |= removeDuplicatedPossibleTopologies(iteration, excludedTopoIds);
            fixed |= removeLowProbabilityPossibleTopologies(iteration, probabilityThreshold, excludedTopoIds);
            fixed |= removeEmptyBuses(iteration);
            iteration++;
        } while (fixed);

        LOGGER.info("{} possible topologies on {} have been excluded from history", excludedTopoIds.size(), intialTopoCount);
    }

    @Override
    public TopologyHistory clone() {
        TopologyHistory clone = new TopologyHistory(histoInterval, threshold);
        for (TopologyChoice topologyChoice : topologyChoices) {
            clone.getTopologyChoices().add(topologyChoice.clone());
        }
        return clone;
    }
}
