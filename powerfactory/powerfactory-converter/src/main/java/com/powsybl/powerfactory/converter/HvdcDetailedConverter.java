/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.powerfactory.converter;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Optional;

import com.powsybl.iidm.network.AcDcConverter;
import com.powsybl.iidm.network.DcGround;
import com.powsybl.iidm.network.DcLine;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.DcTopologyVisitable;
import com.powsybl.iidm.network.DcTopologyVisitor;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TerminalNumber;
import com.powsybl.iidm.network.TwoSides;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageSourceConverter;
import com.powsybl.iidm.network.VoltageSourceConverterAdder;
import com.powsybl.iidm.network.AcDcConverter.ControlMode;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;

/**
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 *
 *         Importer from the DGS data model, for multi-terminal DC grids (a.k.a.
 *         detailed HVDC).
 */
public class HvdcDetailedConverter extends AbstractHvdcConverter {

    DcGridData gridData;

    // Small record that directly transcribes the DC data from the PowerFactory data
    // model for a connected subgrid
    private final record DcGridData(Map<DataObject, List<DataObject>> elmTermsConnectedToVscs,
            Set<DataObject> dcElmLnes,
            Set<DataObject> dcElmTerms, Set<DataObject> acDcConverters) {
        // Create the data model by reading from the PowerFactory data models
        static DcGridData createGridData(List<DataObject> elmTerms, List<DataObject> elmVscs) {
            Map<DataObject, List<DataObject>> elmTermsConnectedToVscs = new HashMap<>();
            Set<DataObject> dcElmLnes = new HashSet<>();
            Set<DataObject> dcElmTerms = new HashSet<>();
            Set<DataObject> usedVscs = new HashSet<>();

            // TODO implement
            if (!elmVscs.isEmpty()) {

                elmTermsConnectedToVscs = computeElmTermsConnectedToVscs(elmTerms, elmVscs);

            }
            return new DcGridData(elmTermsConnectedToVscs, dcElmLnes, dcElmTerms, usedVscs);
        }

        // Get all terminals connected to a converter
        private static Map<DataObject, List<DataObject>> computeElmTermsConnectedToVscs(List<DataObject> elmTerms,
                List<DataObject> elmVscs) {
            // TODO implement
            return null;
        }
    }

    HvdcDetailedConverter(ImportContext importContext, Network network) {
        super(importContext, network);
    }

    @Override
    boolean isDcNode(DataObject elmTerm) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isDcNode'");
    }

    @Override
    boolean isDcLink(DataObject elmLne) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isDcLink'");
    }

    @Override
    void create() {

        Network network = getNetwork();

        // Create nodes
        for (DataObject terminal : gridData.dcElmTerms) {
            addNode(terminal, network);
        }

        // create and connect converters
        for (DataObject converter : gridData.acDcConverters) {
            addConverter(converter, network, getImportContext()); // TODO implement
        }

        // create and connect lines
        for (DataObject line : gridData.dcElmLnes) {
            addLine(line, network); // TODO implement
        }

        // add a DC ground (necessary for load flow computation)
        addGroundWhereNeeded(network, gridData);

        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");

    }

    /**
     * Create Dc node in network. Not connected.
     * If the terminal has neutral polarity, it is connected to the ground.
     *
     * @param terminal terminal to create DcNode from.
     * @param network  where to add the node.
     */
    private static void addNode(DataObject terminal, Network network) {

        assert "ElmTerm".equals(terminal.getDataClassName());

        network.newDcNode()
                .setId(idInNetworkString(terminal))
                .setName(terminal.getLocName())
                .setNominalV(terminal.getDoubleAttributeValue("uknom"))
                .add();

        // sanity checks
        int systype = terminal.getIntAttributeValue("systype"); // throws if not present.
        if (systype != 1) {
            throw new PowerFactoryException(
                    "ElmTerm " + terminal.getId() + " is part of a DC subgrid but its systype is not equal to 1.");
        }

        double uknom = terminal.getDoubleAttributeValue("uknom");
        double unknom = terminal.getDoubleAttributeValue("unknom");
        if (uknom != unknom) {
            throw new PowerFactoryException(
                    "ElmTerm " + terminal.getId() + " is part of a DC subgrid but its uknom and unknom are different.");
        }

        // Add ground if relevant:
        int iminus = terminal.getIntAttributeValue("iminus");
        if (iminus == 2) {
            network.newDcGround()
                    .setId(idInNetworkStringGround(terminal))
                    .setDcNode(idInNetworkString(terminal))
                    .add();
        }
    }

    /**
     * Check each DC subgrid related to the conversion underway. If it has no
     * ground,
     * artificially add one to DC node 2 (minus by convention) of an arbitrary
     * converter.
     *
     * @param network AC-DC network
     */
    private static void addGroundWhereNeeded(Network network, DcGridData gridData) {
        // We must make sure that all converters have been considered, but we want to
        // avoid
        // going through them twice.
        // TODO fix ground id
        Set<VoltageSourceConverter> remainingConverters = Collections.newSetFromMap(new IdentityHashMap<>());
        for (DataObject converterDGS : gridData.acDcConverters) {
            VoltageSourceConverter converter = network.getVoltageSourceConverter(idInNetworkString(converterDGS));
            remainingConverters.add(converter);
        }

        while (!remainingConverters.isEmpty()) {
            // Tackle subgrids one by one
            // Discover new nodes and keep going while discovering
            HashSet<DcTopologyVisitable> visitedSubGridNodes = new HashSet<>(); // nodes already visited
            Deque<DcTopologyVisitable> toProcess = new ArrayDeque<>(); // nodes yet to be processed
            // pick an arbitrary converter. We will follow the subgrid from there
            VoltageSourceConverter converter = remainingConverters.iterator().next();
            remainingConverters.remove(converter);
            toProcess.add(converter.getDcTerminal1().getDcNode());
            toProcess.add(converter.getDcTerminal2().getDcNode());

            boolean hasGround = false;
            while (!toProcess.isEmpty()) {
                DcTopologyVisitable currentNode = toProcess.pop();
                if (visitedSubGridNodes.add(currentNode)) { // only consider yet unvisited nodes
                    ProcessNodeForAddGroundVisitor processor = new ProcessNodeForAddGroundVisitor(toProcess,
                            remainingConverters);
                    // Check if ground is present, remove connected converter from remaining list
                    // and add connected nodes (through lines and converters)
                    currentNode.visitConnectedEquipments(processor);
                    hasGround |= processor.connectedToGround;
                }
            } // while (!toProcess.isEmpty())

            if (!hasGround) { // add arbitrary ground
                network.newDcGround()
                        .setId(converter.getId() + "-ground")
                        .setDcNode(converter.getDcTerminal2().getDcNode().getId())
                        .add();
            }

        } // while(!remainingConverters.isEmpty())

    }

    private static class ProcessNodeForAddGroundVisitor implements DcTopologyVisitor {
        boolean connectedToGround;
        Deque<DcTopologyVisitable> toProcess;
        Set<VoltageSourceConverter> remainingConverters;

        public ProcessNodeForAddGroundVisitor(Deque<DcTopologyVisitable> toProcess,
                Set<VoltageSourceConverter> remainingConverters) {
            this.connectedToGround = false;
            this.toProcess = toProcess;
            this.remainingConverters = remainingConverters;
        }

        @Override
        public void visitAcDcConverter(AcDcConverter<?> converter, TerminalNumber terminalNumber) {
            // Remove connected converters from the list of related converters
            if (remainingConverters.remove(converter)) { // only need to process other DC node if not already visited
                TerminalNumber otherSide = terminalNumber == TerminalNumber.ONE ? TerminalNumber.TWO
                        : TerminalNumber.ONE;
                toProcess.add(converter.getDcTerminal(otherSide).getDcNode()); // TODO check if getDcNode is the right
                                                                              // method
            }
        }

        @Override
        public void visitDcGround(DcGround dcGround) {
            // Hooray, we have a ground ! But we must still consume all nodes and converters
            // in the subgrid
            connectedToGround = true;
        }

        @Override
        public void visitDcLine(DcLine dcLine, TwoSides side) {
            // add node on the other side
            TwoSides otherSide = side == TwoSides.ONE ? TwoSides.TWO : TwoSides.ONE;
            toProcess.add(dcLine.getDcTerminal(otherSide).getDcNode());
        }
    }

    private void addLine(DataObject line, Network network) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addLine'");
    }

    private static void addConverter(DataObject converterPowerFactory, Network network, ImportContext importContext) {

        assert "ElmVsc".equals(converterPowerFactory.getDataClassName());

        // figure out where it is connected on the AC side to get the voltage level.
        // get AC terminal and DC terminal 1 and DC terminal 2
        // for this we need to find 3 cubicles that reference the VSC
        // find all cubicles where Obj_id(p) is equal to the VSC id
        // get the terminal id through fold_id(p) -> this gives the AC bus (bus-breaker)
        // or node (node-breaker)
        // where the VSC should be connected.
        // hence the voltage level as well.
        // TODO refactor as (static) function
        DataObject acTerminalPowerFactory = null;
        DataObject dcTerminal1PowerFactory = null;
        DataObject dcTerminal2PowerFactory = null;
        for (DataObject cubicle : converterPowerFactory.getIndex().getDataObjectsByClass("StaCubic")) {
            Optional<DataObjectRef> objId = cubicle.findObjectAttributeValue(DataAttributeNames.OBJ_ID);
            if (objId.isPresent() && objId.get().getId() == converterPowerFactory.getId()) {
                DataObject tempTerminal = null;
                try {
                    tempTerminal = objId.get().resolve().get();
                } catch (NoSuchElementException e) {
                    final String error = "When fetching the AC terminal of VSC (id "
                            + converterPowerFactory.getId() + "):"
                            + "cubicle " + cubicle.getId() + " has no corresponding terminal.";
                    throw new PowerFactoryException(error);
                }
                assert tempTerminal != null;
                int objBus = cubicle.findIntAttributeValue("obj_bus")
                    .orElseThrow(() -> new PowerFactoryException("DGS Cubicle " + cubicle.getId() + " without obj_bus field."));
                switch (objBus) {
                    case 0:
                        if (acTerminalPowerFactory != null) {
                            throw new PowerFactoryException("Multiple AC cubicles for VSC " + converterPowerFactory.getId() + ".");
                        }
                        acTerminalPowerFactory = tempTerminal;
                        break;
                    case 1:
                        if (dcTerminal1PowerFactory != null) {
                            throw new PowerFactoryException("Multiple DC cubicles 1 for VSC " + converterPowerFactory.getId() + ".");
                        }
                        dcTerminal1PowerFactory = tempTerminal;
                        break;
                    case 2:
                        if (dcTerminal2PowerFactory != null) {
                            throw new PowerFactoryException("Multiple DC cubicles 2 for VSC " + converterPowerFactory.getId() + ".");
                        }
                        dcTerminal2PowerFactory = tempTerminal;
                        break;
                    default:
                        throw new PowerFactoryException("Invalid value " + objBus + " for obj_bus in cubicle " + cubicle.getId() + ".");
                }
            }
        } // for(DataObject cubicle :

        if (acTerminalPowerFactory == null) {
            throw new PowerFactoryException("When fetching the AC terminal of VSC (id "
                    + converterPowerFactory.getId() + "): no correponding cubicle found.");
        }

        // create.
        NodeRef acNodeRef = importContext.elmTermIdToNode.get(acTerminalPowerFactory.getId());
        DcNode dcNode1 = network.getDcNode(idInNetworkString(dcTerminal1PowerFactory));
        DcNode dcNode2 = network.getDcNode(idInNetworkString(dcTerminal2PowerFactory));
        assert dcNode1 != null && dcNode2 != null : "DC nodes should be initialized first";

        // Needed data
        // - IdleLoss
        // - SwitchingLoss
        // - ResistiveLoss
        // - ControlMode
        // - TargetVdc
        // - Id
        // - Bus1
        // - DcNode1
        // - DcNode2
        // - DcConnected1
        // - DcConnected2
        // - VoltageRegulatorOn
        // - ReactivePowerSetpoint
        VoltageLevel voltageLevel = network.getVoltageLevel(acNodeRef.voltageLevelId);
        VoltageSourceConverterAdder converterAdder = voltageLevel.newVoltageSourceConverter()
                .setId(idInNetworkString(converterPowerFactory));

        converterAdder.setIdleLoss(converterPowerFactory.getDoubleAttributeValue("Pnold"));
        converterAdder.setSwitchingLoss(0.0);
        converterAdder.setResistiveLoss(0.0);
        converterAdder.setControlMode(ControlMode.P_PCC); // TODO be smarter there.
        checkSameNominalVoltage(dcNode1, dcNode2); // TODO throw error if voltages are not the same.
        converterAdder.setTargetVdc(dcNode1.getNominalV());
        converterAdder.setDcNode1(dcNode1.getId());
        converterAdder.setDcNode2(dcNode2.getId());
        converterAdder.setVoltageRegulatorOn(false); // TODO figure out which VSC controls the voltage
        converterAdder.setReactivePowerSetpoint(converterPowerFactory.getDoubleAttributeValue("qsetp")); // TODO check sign convention
        converterAdder.setTargetP(converterPowerFactory.getDoubleAttributeValue("psetp")); // TODO check sign convention
        final double voltageSetPoint = converterPowerFactory.getDoubleAttributeValue("usetp") * converterPowerFactory.getDoubleAttributeValue("Unom");
        converterAdder.setVoltageSetpoint(voltageSetPoint);
        converterAdder.add();

    }

    private static void checkSameNominalVoltage(DcNode dcNode1, DcNode dcNode2) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'checkSameNominalVoltage'");
    }

    /**
     * Unique string Id in network from DataObject in DGS file. This guarantees some
     * consistency.
     *
     * @param component Component we want the id of.
     * @return Unique id in the AcDcNetwork.
     */
    private static String idInNetworkString(DataObject component) {
        return component.getLocName();
    }

    private static String idInNetworkStringGround(DataObject terminal) {
        return idInNetworkString(terminal) + "Gnd";
    }

    /**
     * Load the DC elements from the PowerFactory data model.
     *
     * @param elmTerms terminals
     * @param elmVscs  converters
     */
    void getGridData(List<DataObject> elmTerms, List<DataObject> elmVscs) {
        gridData = DcGridData.createGridData(elmTerms, elmVscs);
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'create'");
    }

}
