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
import com.powsybl.powerfactory.model.DataObjectIndex;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;

/**
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 *
 *         Importer from the DGS data model, for multi-terminal DC grids (a.k.a.
 *         detailed HVDC).
 */
public final class HvdcDetailedConverter extends AbstractHvdcConverter {

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

    public HvdcDetailedConverter(ImportContext importContext, Network network, List<DataObject> elmTerms, List<DataObject> elmVscs) {
        super(importContext, network);
        // TODO do stuff here
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
            addConverter(converter, network, getImportContext());
        }

        // create and connect lines
        for (DataObject line : gridData.dcElmLnes) {
            addLine(line, network);
        }

        // add a DC ground (necessary for load flow computation)
        addGroundWhereNeeded(network, gridData);
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
        Set<VoltageSourceConverter> remainingConverters = Collections.newSetFromMap(new IdentityHashMap<>());
        for (DataObject converterPowerFactory : gridData.acDcConverters) {
            VoltageSourceConverter converter = network
                    .getVoltageSourceConverter(idInNetworkString(converterPowerFactory));
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
                final String groundIdString = converter.getId() + "Gnd"; // TODO improve
                network.newDcGround()
                        .setId(groundIdString)
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

    /**
     * Create DC line in the network, retriving endpoints and resistance
     * from the PowerFactory data model.
     *
     * @param line    DC line to be converted from the DGS to the PowSyBl data
     *                model.
     * @param network Network where to create the DC line.
     *
     *                We suppose DC nodes have been created previously in the
     *                network.
     */
    private static void addLine(DataObject line, Network network) {
        // Find the nodes
        // TODO factorize out
        DataObject dcTerm1DataObject = null;
        DataObject dcTerm2DataObject = null;
        DataObjectIndex indexDataObjects = line.getIndex();
        for (DataObject cubicle : indexDataObjects.getDataObjectsByClass("StaCubic")) {
            Optional<DataObjectRef> objId = cubicle.findObjectAttributeValue(DataAttributeNames.OBJ_ID);
            if (objId.isPresent() && objId.get().getId() == line.getId()) {
                DataObject tempTerminal = null;
                // This may fail but it's ok that way
                long terminalId = cubicle.getLongAttributeValue("fold_id");
                int objBus = cubicle.getIntAttributeValue("obj_bus");

                try {
                    tempTerminal = indexDataObjects.getDataObjectById(terminalId).get();
                } catch (NoSuchElementException e) {
                    throw new PowerFactoryException(
                            "Error getting terminal " + terminalId + " for line " + line.getId() + ".");
                }
                assert tempTerminal != null;

                if (objBus != 0 && objBus != 1) {
                    throw new PowerFactoryException("Invalid value " + objBus
                            + " for obj_bus in cubicle related to DC line " + line.getId() + ". Expected 0 or 1");
                }

                if (objBus == 0 && dcTerm1DataObject == null) {
                    dcTerm1DataObject = tempTerminal;
                } else if (objBus == 1 && dcTerm2DataObject == null) {
                    dcTerm2DataObject = tempTerminal;
                } else {
                    throw new PowerFactoryException(
                            "Multiple cubicles with obj_bus = " + objBus + " related to DC line " + line.getId() + ".");
                }

            }
        }

        String parentComponentName = "line " + line.getId();
        DcNode dcNode1 = getSafeNodeForLine(dcTerm1DataObject, network, parentComponentName);
        DcNode dcNode2 = getSafeNodeForLine(dcTerm2DataObject, network, parentComponentName);
        assert dcNode1 != null && dcNode2 != null;

        // Get the data from the "types"
        double lineResistance = getLineResistance(line);

        // Create the line in the DC sub-network
        network.newDcLine()
                .setId(idInNetworkString(line))
                .setDcNode1(null)
                .setDcNode2(null)
                .setR(lineResistance)
                .add();
    }

    private static double getLineResistance(DataObject line) {
        double lineLength = line.getDoubleAttributeValue("dline"); // km
        int numberOfParallelLines = line.findIntAttributeValue("nlnum").orElse(1);

        DataObject lineType = line.getObjectAttributeValue("typ_id")
                .resolve()
                .orElseThrow(() -> new PowerFactoryException(
                        "Missing line type referenced by DC line " + line.getId() + "."));

        int lineSysType = lineType.findIntAttributeValue("systyp").orElse(1);
        if (lineSysType != 1) {
            throw new PowerFactoryException("DC line " + line.getId() + " has type " + lineType.getId()
                    + " which not of system type systyp DC.");
        }

        double rline = lineType.getDoubleAttributeValue("dline"); // Ohm / km

        return rline * lineLength / numberOfParallelLines; // Ohm
    }

    private static DcNode getSafeNodeForLine(DataObject dcTermDataObject, Network network, String parentComponentName) {
        String idInNetwork = idInNetworkString(dcTermDataObject);
        DcNode result = network.getDcNode(idInNetwork);
        if (result == null) {
            throw new PowerFactoryException("Missing node " + idInNetwork + " in network. Is terminal "
                    + dcTermDataObject.getId() + " in the ElmTerm table? This is for " + parentComponentName);
        }
        return result;
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
        DataObject acTerminalDataObject = null;
        DataObject dcTerminal1DataObject = null;
        DataObject dcTerminal2DataObject = null;
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
                        .orElseThrow(() -> new PowerFactoryException(
                                "DGS Cubicle " + cubicle.getId() + " without obj_bus field."));
                switch (objBus) {
                    case 0:
                        if (acTerminalDataObject != null) {
                            throw new PowerFactoryException(
                                    "Multiple AC cubicles for VSC " + converterPowerFactory.getId() + ".");
                        }
                        acTerminalDataObject = tempTerminal;
                        break;
                    case 1:
                        if (dcTerminal1DataObject != null) {
                            throw new PowerFactoryException(
                                    "Multiple DC cubicles 1 for VSC " + converterPowerFactory.getId() + ".");
                        }
                        dcTerminal1DataObject = tempTerminal;
                        break;
                    case 2:
                        if (dcTerminal2DataObject != null) {
                            throw new PowerFactoryException(
                                    "Multiple DC cubicles 2 for VSC " + converterPowerFactory.getId() + ".");
                        }
                        dcTerminal2DataObject = tempTerminal;
                        break;
                    default:
                        throw new PowerFactoryException(
                                "Invalid value " + objBus + " for obj_bus in cubicle " + cubicle.getId() + ".");
                }
            }
        } // for(DataObject cubicle :

        if (acTerminalDataObject == null) {
            throw new PowerFactoryException("When fetching the AC terminal of VSC (id "
                    + converterPowerFactory.getId() + "): no correponding cubicle found.");
        }

        if (dcTerminal1DataObject == null || dcTerminal2DataObject == null) {
            throw new PowerFactoryException(
                    "Missing DC terminal for AC-DC converter " + converterPowerFactory.getId() + ".");
        }

        // create

        // check which DC node is + and which is - (or neutral). This makes it possible
        // to get the sign of the DC command volatge.
        int iMinus1 = Integer.MIN_VALUE;
        int iMinus2 = Integer.MIN_VALUE;
        try {
            iMinus1 = dcTerminal1DataObject.getIntAttributeValue("iminus");
            iMinus2 = dcTerminal2DataObject.getIntAttributeValue("iminus");
        } catch (PowerFactoryException e) {
            throw new PowerFactoryException("Missing iminus field in $$ElmTerm table for DC terminal.");
        }

        if (iMinus1 == iMinus2) {
            throw new PowerFactoryException(
                    "Converter " + converterPowerFactory.getId() + " has 2 DC terminals with the same polarities.");
        }

        DcNode dcNode1 = getSafeNodeForLine(dcTerminal1DataObject, network,
                "converter " + converterPowerFactory.getId());
        DcNode dcNode2 = getSafeNodeForLine(dcTerminal2DataObject, network,
                "converter " + converterPowerFactory.getId());
        boolean dcNode1IsPlus = iMinus1 == 2 && iMinus2 == 1 || iMinus1 == 0;
        assert dcNode1 != null && dcNode2 != null : "DC nodes should be initialized first";

        NodeRef acNodeRef = importContext.elmTermIdToNode.get(acTerminalDataObject.getId());
        if (acNodeRef == null) {
            throw new PowerFactoryException("AC terminal " + acTerminalDataObject.getId()
                    + " not found in network when building converter " + converterPowerFactory.getId() + ".");
        }

        VoltageLevel voltageLevel = network.getVoltageLevel(acNodeRef.voltageLevelId);
        VoltageSourceConverterAdder converterAdder = voltageLevel.newVoltageSourceConverter()
                .setId(idInNetworkString(converterPowerFactory));

        converterAdder.setNode1(acNodeRef.node); // TODO check if/how to add bus (in bus/breaker mode ?)
        converterAdder.setIdleLoss(converterPowerFactory.getDoubleAttributeValue("Pnold"));
        converterAdder.setSwitchingLoss(0.0);
        converterAdder.setResistiveLoss(0.0);
        converterAdder.setControlMode(ControlMode.P_PCC); // TODO be smarter there.
        checkSameNominalVoltage(dcNode1, dcNode2);
        double targetV = dcNode1IsPlus ? dcNode1.getNominalV() : -dcNode1.getNominalV();
        converterAdder.setTargetVdc(targetV);
        converterAdder.setDcNode1(dcNode1.getId());
        converterAdder.setDcNode2(dcNode2.getId());
        converterAdder.setVoltageRegulatorOn(false); // TODO figure out which VSC controls the voltage
        converterAdder.setReactivePowerSetpoint(converterPowerFactory.getDoubleAttributeValue("qsetp")); // TODO check
                                                                                                         // sign
                                                                                                         // convention
        converterAdder.setTargetP(converterPowerFactory.getDoubleAttributeValue("psetp")); // TODO check sign convention
        final double voltageSetPoint = converterPowerFactory.getDoubleAttributeValue("usetp")
                * converterPowerFactory.getDoubleAttributeValue("Unom");
        converterAdder.setVoltageSetpoint(voltageSetPoint);
        converterAdder.add();

    }

    private static void checkSameNominalVoltage(DcNode dcNode1, DcNode dcNode2) {
        if (dcNode1.getNominalV() != dcNode2.getNominalV()) {
            throw new PowerFactoryException("DcNode " + dcNode1.getId() + " and DcNode " + dcNode2.getId()
                    + " are connected to the same convertor but have different nominal voltages.");
        }
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

}
