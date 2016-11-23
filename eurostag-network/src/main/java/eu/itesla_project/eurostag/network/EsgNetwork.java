/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.eurostag.network;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class EsgNetwork {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsgNetwork.class);

    public static final String VERSION = "5.1";

    private static final float MIN_REACTIVE_RANGE = 1f;

    private final Map<String, EsgArea> areas = new LinkedHashMap<>();
    private final Map<String, EsgNode> nodes = new LinkedHashMap<>();
    private final Map<String, EsgLine> lines = new LinkedHashMap<>();
    private final Map<String, EsgDetailedTwoWindingTransformer> detailedTwoWindingTransformers = new LinkedHashMap<>();
    private final Map<String, EsgDissymmetricalBranch> dissymmetricalBranches = new LinkedHashMap<>();
    private final Map<String, EsgCouplingDevice> couplingDevices = new LinkedHashMap<>();
    private final Map<String, EsgGenerator> generators = new LinkedHashMap<>();
    private final Map<String, EsgLoad> loads = new LinkedHashMap<>();
    private final Map<String, EsgCapacitorOrReactorBank> capacitorsOrReactorBanks = new LinkedHashMap<>();
    private final Map<String, EsgStaticVarCompensator> staticVarCompensators = new LinkedHashMap<>();

    private void checkBranchName(EsgBranchName name) {
        if (getNode(name.getNode1Name().toString()) == null) {
            throw new RuntimeException("Line '" + name + "' reference an unknown connection node '" + name.getNode1Name() + "'");
        }
        if (getNode(name.getNode2Name().toString()) == null) {
            throw new RuntimeException("Line '" + name + "' reference an unknown connection node '" + name.getNode2Name() + "'");
        }
    }

    public void checkConsistency() {
        // check there is at least one node and a slack bus
        if (nodes.size() < 1) {
            throw new RuntimeException("Network must have at least one node");
        }
        int slackBusCount = 0;
        for (EsgNode node : nodes.values()) {
            if (node.isSlackBus()) {
                slackBusCount++;
            }
        }
        if (slackBusCount == 0) {
            throw new RuntimeException("Network must have at least one slack bus");
        }
        for (EsgNode node :  getNodes()) {
            if (getArea(node.getArea().toString()) == null) {
                throw new RuntimeException("Node '" + node.getName() + "' reference an unknown area '" + node.getArea() + "'");
            }
        }
        for (EsgLine line : getLines()) {
            checkBranchName(line.getName());
        }
        for (EsgCouplingDevice device : getCouplingDevices()) {
            checkBranchName(device.getName());
        }
        for (EsgDissymmetricalBranch branch : getDissymmetricalBranches()) {
            checkBranchName(branch.getName());
        }
        for (EsgDetailedTwoWindingTransformer transformer : getDetailedTwoWindingTransformers()) {
            checkBranchName(transformer.getName());
            if (transformer.getZbusr() != null && getNode(transformer.getZbusr().toString()) == null) {
                throw new RuntimeException("Transformer '" + transformer.getName() +  "' reference an unknown regulating node '"
                        + transformer.getZbusr() + "'");
            }
        }
        for (EsgLoad load : getLoads()) {
            if (getNode(load.getZnodlo().toString()) == null) {
                throw new RuntimeException("Load '" + load.getZnamlo() + "' reference an unknown connection node '"
                        + load.getZnodlo() + "'");
            }
        }
        for (EsgGenerator generator : getGenerators()) {
            if (getNode(generator.getZnodge().toString()) == null) {
                throw new RuntimeException("Generator '" + generator.getZnamge() + "' reference an unknown connection node '"
                        + generator.getZnodge() + "'");
            }
            if (getNode(generator.getZregnoge().toString()) == null) {
                throw new RuntimeException("Generator '" + generator.getZnamge() + "' reference an unknown regulating node '"
                        + generator.getZregnoge() + "'");
            }
        }
        for (EsgCapacitorOrReactorBank bank : getCapacitorOrReactorBanks()) {
            if (getNode(bank.getZnodba().toString()) == null) {
                throw new RuntimeException("Capacitor or reactor bank '" + bank.getZnamba() + "' reference an unknown connection node '"
                        + bank.getZnodba() + "'");
            }
        }
        for (EsgStaticVarCompensator svc : getStaticVarCompensators()) {
            if (getNode(svc.getZnodsvc().toString()) == null) {
                throw new RuntimeException("Static VAR compensator '" + svc.getZnamsvc() +"' reference an unknown connection node '"
                        + svc.getZnodsvc() + "'");
            }
        }

        // Fix generator small reactive range issue
        List<String> minReactiveRangePb = new ArrayList<>();
        for (EsgGenerator g : getGenerators()) {
            if (g.getXregge() == EsgRegulatingMode.REGULATING && Math.abs(g.getQgmax() - g.getQgmin()) < MIN_REACTIVE_RANGE) {
                minReactiveRangePb.add(g.getZnamge().toString());
                g.setXregge(EsgRegulatingMode.NOT_REGULATING);
            }
        }
        if (minReactiveRangePb.size() > 0) {
            LOGGER.warn("Reactive range too small, switch regulator off: " + minReactiveRangePb);
        }

        // Fix target voltage consistency issue
        // Eurostag error message example:
        // ERR-0194.0350:LE GEN CURBH6G0 ESSAIE D'IMPOSER UNE TENSION AU NOEUD BARNAP71 AUQUEL UN AUTRE EQUIPEMENT A DEJA IMPOSE UNE AUTRE TENSION
        Multimap<String, EsgGenerator> generatorsConnectedToSameNode = HashMultimap.create();
        for (EsgGenerator g : getGenerators()) {
            if (g.getXregge() == EsgRegulatingMode.REGULATING) {
                generatorsConnectedToSameNode.put(g.getZnodge().toString(), g);
            }
        }
        for (Map.Entry<String, Collection<EsgGenerator>> e : generatorsConnectedToSameNode.asMap().entrySet()) {
            String nodeName = e.getKey();
            Collection<EsgGenerator> generators = e.getValue();
            Set<Float> targetVoltageSet = generators.stream()
                    .map(EsgGenerator::getVregge)
                    .collect(Collectors.toSet());
            if (targetVoltageSet.size() > 1) {
                Collection<EsgGenerator> connectedGenerators = generators.stream()
                        .filter(g -> g.getXgenest() == EsgConnectionStatus.CONNECTED)
                        .collect(Collectors.toList());
                targetVoltageSet = connectedGenerators.stream()
                        .map(EsgGenerator::getVregge)
                        .collect(Collectors.toSet());
                if (targetVoltageSet.size() > 0) {
                    if (targetVoltageSet.size() == 1) {
                        Collection<EsgGenerator> diconnectedGenerators = generators.stream()
                                .filter(g -> g.getXgenest() == EsgConnectionStatus.NOT_CONNECTED)
                                .collect(Collectors.toList());
                        LOGGER.warn("Fix target voltage of disconnected generators {} to be consistent with target voltage ({} Kv) of other generators connected to the same node ({})",
                                diconnectedGenerators.stream().map(EsgGenerator::getZnamge).collect(Collectors.toList()),
                                targetVoltageSet.iterator().next(), nodeName);
                        float vregge = targetVoltageSet.iterator().next();
                        for (EsgGenerator g : diconnectedGenerators) {
                            g.setVregge(vregge);
                        }
                    } else {
                        throw new RuntimeException(connectedGenerators.size() + " generators ("
                                + connectedGenerators.stream().map(EsgGenerator::getZnamge).collect(Collectors.toList())
                                + ") are connected to a same node (" + nodeName + ") and try to impose a different target voltage: "
                                + targetVoltageSet);
                    }
                }
            }
        }

        // check there is no regulating transformer connected to same bus with a different target voltage
        Multimap<Esg8charName, EsgDetailedTwoWindingTransformer> transformersByRegulatedNode = HashMultimap.create();
        for (EsgDetailedTwoWindingTransformer transfo : getDetailedTwoWindingTransformers()) {
            if (transfo.getXregtr() == EsgDetailedTwoWindingTransformer.RegulatingMode.VOLTAGE) {
                transformersByRegulatedNode.put(transfo.getZbusr(), transfo);
            }
        }
        for (Map.Entry<Esg8charName, Collection<EsgDetailedTwoWindingTransformer>> e : transformersByRegulatedNode.asMap().entrySet()) {
            Esg8charName regulatedNode = e.getKey();
            Collection<EsgDetailedTwoWindingTransformer> transformers = e.getValue();
            Set<Float> targetVoltageSet = transformers.stream()
                    .map(EsgDetailedTwoWindingTransformer::getVoltr)
                    .collect(Collectors.toSet());
            if (targetVoltageSet.size() > 1) {
                float chosenTargetVoltage = targetVoltageSet.stream().min(Float::compare).get();
                LOGGER.warn("Fix target voltage of transformers {} connected to same regulating bus {}: {} -> {}",
                        transformers.stream().map(EsgDetailedTwoWindingTransformer::getName).collect(Collectors.toList()),
                        regulatedNode, targetVoltageSet, chosenTargetVoltage);
                for (EsgDetailedTwoWindingTransformer transformer : transformers) {
                    transformer.setVoltr(chosenTargetVoltage);
                }
            }
        }
    }

    public Collection<EsgArea> getAreas() {
        return areas.values();
    }

    public EsgArea getArea(String name) {
        return areas.get(name);
    }

    public void addArea(EsgArea area) {
        if (areas.containsKey(area.getName().toString())) {
            throw new IllegalArgumentException("Area '" + area.getName() + "' already exists");
        }
        areas.put(area.getName().toString(), area);
    }
    
    public void removeArea(String area) {
        if (!areas.containsKey(area)) {
        	throw new IllegalArgumentException("Area '" + area + "' doesn't exists");
        } 
        areas.remove(area);
    }

    public Collection<EsgNode> getNodes() {
        return nodes.values();
    }

    public EsgNode getNode(String name) {
        return nodes.get(name);
    }

    public void addNode(EsgNode node) {
        if (nodes.containsKey(node.getName().toString())) {
            throw new IllegalArgumentException("Node '" + node.getName() + "' already exists");
        }
        nodes.put(node.getName().toString(), node);
    }
    
    public void removeNode(String node) {
        if (!nodes.containsKey(node)) {
            throw new IllegalArgumentException("Node '" + node + "' doesn't exists");
        }
        nodes.remove(node);
    }

    public Collection<EsgLine> getLines() {
        return lines.values();
    }

    public EsgLine getLine(String name) {
        return lines.get(name);
    }

    public void addLine(EsgLine line) {
        if (lines.containsKey(line.getName().toString())) {
            throw new IllegalArgumentException("Line '" + line.getName() + "' already exists");
        }
        lines.put(line.getName().toString(), line);
    }
    
    public void removeLine(String line) {
        if (!lines.containsKey(line)) {
            throw new IllegalArgumentException("Line '" + line + "' doesn't exists");
        }
        lines.remove(line);
    }

    public Collection<EsgDetailedTwoWindingTransformer> getDetailedTwoWindingTransformers() {
        return detailedTwoWindingTransformers.values();
    }

    public EsgDetailedTwoWindingTransformer getDetailedTwoWindingTransformer(String name) {
        return detailedTwoWindingTransformers.get(name);
    }

    public void addDetailedTwoWindingTransformer(EsgDetailedTwoWindingTransformer transformer) {
        if (detailedTwoWindingTransformers.containsKey(transformer.getName().toString())) {
            throw new IllegalArgumentException("Detailed 2 winding transformer '" + transformer.getName() + "' already exists");
        }
        detailedTwoWindingTransformers.put(transformer.getName().toString(), transformer);
    }
    
    public void removeDetailedTwoWindingTransformer(String transformer) {
        if (!detailedTwoWindingTransformers.containsKey(transformer)) {
            throw new IllegalArgumentException("Detailed 2 winding transformer '" + transformer + "' doesn't exists");
        }
        detailedTwoWindingTransformers.remove(transformer);
    }

    public Collection<EsgDissymmetricalBranch> getDissymmetricalBranches() {
        return dissymmetricalBranches.values();
    }

    public EsgDissymmetricalBranch getDissymmetricalBranch(String name) {
        return dissymmetricalBranches.get(name);
    }

    public void addDissymmetricalBranch(EsgDissymmetricalBranch branch) {
        if (dissymmetricalBranches.containsKey(branch.getName().toString())) {
            throw new IllegalArgumentException("Dissymmetrical branch '" + branch.getName() + "' already exists");
        }
        dissymmetricalBranches.put(branch.getName().toString(), branch);
    }
    
    public void removeDissymmetricalBranch(String branch) {
        if (!dissymmetricalBranches.containsKey(branch)) {
            throw new IllegalArgumentException("Dissymmetrical branch '" + branch + "' doesn't exists");
        }
        dissymmetricalBranches.remove(branch);
    }

    public Collection<EsgCouplingDevice> getCouplingDevices() {
        return couplingDevices.values();
    }

    public EsgCouplingDevice getCouplingDevice(String name) {
        return couplingDevices.get(name);
    }

    public void addCouplingDevice(EsgCouplingDevice device) {
        if (couplingDevices.containsKey(device.getName().toString())) {
            throw new IllegalArgumentException("Coupling device '" + device.getName() + "' already exists");
        }
        couplingDevices.put(device.getName().toString(), device);
    }
    
    public void removeCouplingDevice(String device) {
        if (!couplingDevices.containsKey(device)) {
            throw new IllegalArgumentException("Coupling device '" + device + "' doesn't exists");
        }
        couplingDevices.remove(device);
    }

    public Collection<EsgGenerator> getGenerators() {
        return generators.values();
    }

    public EsgGenerator getGenerator(String name) {
        return generators.get(name);
    }

    public void addGenerator(EsgGenerator generator) {
        if (generators.containsKey(generator.getZnamge().toString())) {
            throw new IllegalArgumentException("Generator '" + generator.getZnamge() + "' already exists");
        }
        generators.put(generator.getZnamge().toString(), generator);
    }
    
    public void removeGenerator(String generator) {
        if (!generators.containsKey(generator)) {
            throw new IllegalArgumentException("Generator '" + generator + "' doesn't exists");
        }
        generators.remove(generator);
    }

    public Collection<EsgLoad> getLoads() {
        return loads.values();
    }

    public EsgLoad getLoad(String name) {
        return loads.get(name);
    }

    public void addLoad(EsgLoad load) {
        if (loads.containsKey(load.getZnamlo().toString())) {
            throw new IllegalArgumentException("Load '" + load.getZnamlo() + "' already exists");
        }
        loads.put(load.getZnamlo().toString(), load);
    }
    
    public void removeLoad(String load) {
        if (!loads.containsKey(load)) {
            throw new IllegalArgumentException("Load '" + load + "' doesn't exists");
        }
        loads.remove(load);
    }

    public Collection<EsgCapacitorOrReactorBank> getCapacitorOrReactorBanks() {
        return capacitorsOrReactorBanks.values();
    }

    public EsgCapacitorOrReactorBank getCapacitorOrReactorBank(String name) {
        return capacitorsOrReactorBanks.get(name);
    }

    public void addCapacitorsOrReactorBanks(EsgCapacitorOrReactorBank bank) {
        if (capacitorsOrReactorBanks.containsKey(bank.getZnamba().toString())) {
            throw new IllegalArgumentException("Capacitor or reactor bank '" + bank.getZnamba() + "' already exists");
        }
        capacitorsOrReactorBanks.put(bank.getZnamba().toString(), bank);
    }
    
    public void removeCapacitorsOrReactorBanks(String bank) {
        if (!capacitorsOrReactorBanks.containsKey(bank)) {
            throw new IllegalArgumentException("Capacitor or reactor bank '" + bank + "' doesn't exists");
        }
        capacitorsOrReactorBanks.remove(bank);
    }

    public Collection<EsgStaticVarCompensator> getStaticVarCompensators() {
        return staticVarCompensators.values();
    }

    public void addStaticVarCompensator(EsgStaticVarCompensator svc) {
        if (staticVarCompensators.containsKey(svc.getZnamsvc().toString())) {
            throw new IllegalArgumentException("Static VAR compensator '" + svc + "' already exists");
        }
        staticVarCompensators.put(svc.getZnamsvc().toString(), svc);
    }
}
