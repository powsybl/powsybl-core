/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.AcDcConverter.ControlMode;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectIndex;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;

import java.util.*;

/**
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 * <p>
 * Importer from the DGS data model, for multi-terminal DC grids (a.k.a.
 * detailed HVDC).
 */
public final class HvdcDetailedConverter extends AbstractHvdcConverter {

    private final DcGridData gridData;

    // Small record that directly transcribes the DC data from the PowerFactory data
    // model for a connected subgrid
    private record DcGridData(
            Set<DataObject> dcElmLnes,
            Set<DataObject> dcElmTerms,
            Set<DataObject> acDcConverters) {

        /**
         * Create the data model by reading from the PowerFactory data models.
         * @param elmNets All the "networks" from the PowerFactory data model.
         * @return DC lines, DC terminals and converters.
         *
         * Selection is done based on the systype attribute of ElmTerm and of TypLne.
         */
        static DcGridData createGridData(List<DataObject> elmNets) {
            assert elmNets.isEmpty() || "ElmNet".equals(elmNets.getFirst().getDataClassName());

            List<DataObject> elmTerms = PowerFactoryImporter.gatherElmTerms(elmNets);
            assert elmTerms.isEmpty() || "ElmTerm".equals(elmTerms.getFirst().getDataClassName());
            List<DataObject> elmVscs = PowerFactoryImporter.gatherElmVscs(elmNets);
            assert elmVscs.isEmpty() || "ElmVsc".equals(elmVscs.getFirst().getDataClassName());

            Set<DataObject> dcElmLnes = new HashSet<>();
            Set<DataObject> dcElmTerms = new HashSet<>();
            Set<DataObject> usedVscs = new HashSet<>(elmVscs);

            // This test determines if DC elements may be expected in the model.
            if (!elmVscs.isEmpty()) {

                // Add DC lines
                List<DataObject> elmLines = elmNets.stream().flatMap(elmNet -> elmNet.search(".*.ElmLne").stream()).toList();
                for (DataObject elmLne : elmLines) {
                    DataObjectRef typLneRef = elmLne.getObjectAttributeValue("typ_id");
                    DataObject typLne = typLneRef.resolve()
                            .orElseThrow(() -> new PowerFactoryException("Missing line type in TypLne for ElmLne " + elmLne.getId() + "."));
                    int lineSysType = typLne.findIntAttributeValue("systype")
                            .orElseThrow(() -> new PowerFactoryException("Missing systype for TypLne " + typLne.getId() + "."));
                    if (lineSysType == 1) {
                        dcElmLnes.add(elmLne);
                    }
                }

                // Add DC terminals
                for (DataObject elmTerm : elmTerms) {
                    int termSystyp = elmTerm.findIntAttributeValue("systype")
                            .orElseThrow(() -> new PowerFactoryException("Missing systype for ElmTerm " + elmTerm.getId() + "."));
                    if (termSystyp == 1) {
                        dcElmTerms.add(elmTerm);
                    }
                }
            }
            return new DcGridData(dcElmLnes, dcElmTerms, usedVscs);
        }

    } // private record DcGridData

    public HvdcDetailedConverter(ImportContext importContext, Network network, List<DataObject> elmNets) {
        super(importContext, network);
        gridData = DcGridData.createGridData(elmNets);
    }

    @Override
    boolean isDcNode(DataObject elmTerm) {
        return gridData.dcElmTerms.contains(elmTerm);
    }

    @Override
    boolean isDcLink(DataObject elmLne) {
        return gridData.dcElmLnes.contains(elmLne);
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

        // add a DC ground if not present (necessary for load flow computation)
        addGroundWhereNeeded(network, gridData);
    }

    /**
     * Create Dc node in network. Not connected.
     *
     * @param terminal terminal to create DcNode from.
     * @param network  where to add the node.
     */
    private static void addNode(DataObject terminal, Network network) {

        assert "ElmTerm".equals(terminal.getDataClassName());

        network.newDcNode()
                .setId(idInNetworkString(terminal))
                .setName(terminal.getLocName())
                .setNominalV(terminal.getFloatAttributeValue("uknom"))
                .add();

        double uknom = terminal.getFloatAttributeValue("uknom");
        double unknom = terminal.getFloatAttributeValue("unknom");
        if (uknom != unknom) {
            throw new PowerFactoryException(
                    "ElmTerm " + terminal.getId() + " is part of a DC subgrid but its uknom and unknom are different.");
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

        // TODO see if possible to split.
        // Start with all converters in the network that correspond to a PowerFactory converter.
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
                final String groundIdString = converter.getId() + "Gnd";
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
            VoltageSourceConverter vsc = (VoltageSourceConverter) converter;
            if (remainingConverters.remove(vsc)) { // only need to process other DC node if not already visited
                TerminalNumber otherSide = terminalNumber == TerminalNumber.ONE ? TerminalNumber.TWO
                        : TerminalNumber.ONE;
                toProcess.add(vsc.getDcTerminal(otherSide).getDcNode()); // TODO check if getDcNode is the right
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
     * Create DC line in the network, retrieving endpoints and resistance
     * from the PowerFactory data model.
     *
     * @param line    DC line to be converted from the DGS to the PowSyBl data
     *                model.
     * @param network Network where to create the DC line.
     *                We suppose DC nodes have been created previously in the
     *                network.
     */
    private static void addLine(DataObject line, Network network) {
        // Find the nodes
        LineTerms lineTerminals = LineTerms.findDcLineEndsPowerFactory(line);

        String parentComponentName = "line " + line.getId(); // only for error message purpose
        DcNode dcNode1 = getSafeNodeForLine(lineTerminals.dcTerm1DataObject, network, parentComponentName);
        DcNode dcNode2 = getSafeNodeForLine(lineTerminals.dcTerm2DataObject, network, parentComponentName);
        assert dcNode1 != null && dcNode2 != null;

        // Get the data from the "types"
        double lineResistance = getLineResistance(line);

        // Create the line in the DC subnetwork
        network.newDcLine()
                .setId(idInNetworkString(line))
                .setDcNode1(dcNode1.getId())
                .setDcNode2(dcNode2.getId())
                .setR(lineResistance)
                .add();
    }

    /**
     * Return type for findDcLineEndsPowerFactory.
     * @param dcTerm1DataObject first terminal of the line in the PowerFactory data model.
     * @param dcTerm2DataObject second terminal of the line in the PowerFactory data model.
     */
    private record LineTerms(DataObject dcTerm1DataObject, DataObject dcTerm2DataObject) {
        /**
         * Find both ends of a line in the PowerFactory data model.
         * @param line DC line for which to find both nodes (terminals).
         * @return Ends of the line.
         */
        static LineTerms findDcLineEndsPowerFactory(DataObject line) {
            DataObject dcTerm1DataObject = null;
            DataObject dcTerm2DataObject = null;

            DataObjectIndex indexDataObjects = line.getIndex();
            for (DataObject cubicle : indexDataObjects.getDataObjectsByClass("StaCubic")) {
                Optional<DataObjectRef> elmLneRef = cubicle.findObjectAttributeValue(DataAttributeNames.OBJ_ID);
                if (elmLneRef.isPresent() && elmLneRef.get().getId() == line.getId()) {

                    DataObject tempTerminal = cubicle.getObjectAttributeValue("fold_id").resolve()
                            .orElseThrow(() -> new PowerFactoryException("Error getting terminal for line " + line.getId() + " cubicle " + cubicle.getId()));
                    int objBus = cubicle.getIntAttributeValue("obj_bus");

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

            if(dcTerm1DataObject == null || dcTerm2DataObject == null) {
                throw new PowerFactoryException("Missing terminal for line " + line.getId() + ".");
            }

            return new LineTerms(dcTerm1DataObject, dcTerm2DataObject);
        }
    }

    /**
     * Compute the line resistance from the line object and its line type in the PowerFactory data model.
     * @param line powerline we want to compute the resistance of.
     * @return Resistance (Ohm) of the line.
     *
     * Resistance = lineic resistance * distance / number of parallel lines.
     */
    private static double getLineResistance(DataObject line) {
        double lineLength = line.getFloatAttributeValue("dline"); // km
        int numberOfParallelLines = line.findIntAttributeValue("nlnum").orElse(1);

        DataObject lineType = line.getObjectAttributeValue("typ_id")
                .resolve()
                .orElseThrow(() -> new PowerFactoryException(
                        "Missing line type referenced by DC line " + line.getId() + "."));

        int lineSysType = lineType.findIntAttributeValue("systype").orElse(1);
        if (lineSysType != 1) {
            throw new PowerFactoryException("DC line " + line.getId() + " has type " + lineType.getId()
                    + " which not of system type systype DC.");
        }

        double rline = lineType.getFloatAttributeValue("rline"); // Ohm / km

        return rline * lineLength / numberOfParallelLines; // Ohm
    }

    private static DcNode getSafeNodeForLine(DataObject dcTermDataObject, Network network, String parentComponentName) {
        if  (dcTermDataObject == null) {
            throw new PowerFactoryException(parentComponentName + " is missing a node in the DGS files.");
        }
        String idInNetwork = idInNetworkString(dcTermDataObject);
        DcNode result = network.getDcNode(idInNetwork);
        if (result == null) {
            throw new PowerFactoryException("Missing node " + idInNetwork + " in network. Is terminal "
                    + dcTermDataObject.getId() + " in the ElmTerm table? This is for " + parentComponentName);
        }
        return result;
    }

    private static void addConverter(DataObject elmVsc, Network network, ImportContext importContext) {

        assert "ElmVsc".equals(elmVsc.getDataClassName());

        // Get the terminals on the PowerFactory side (DataObjects)
        VscConnections vscConnections = VscConnections.findVscConnectionsPowerFactory(elmVsc);

        // Get corresponding DC and AC nodes in the PowSyBl network
        DcNode dcNode1 = getSafeNodeForLine(vscConnections.dcTerminal1, network,
                "converter " + elmVsc.getId());
        DcNode dcNode2 = getSafeNodeForLine(vscConnections.dcTerminal2, network,
                "converter " + elmVsc.getId());

        assert dcNode1 != null && dcNode2 != null : "DC nodes should be initialized first";

        // From the list of terminals related to the elmVsc in the importContext
        List<NodeRef> acNodeRefList = importContext.objIdToNode.get(elmVsc.getId());
        NodeRef acNodeRef = acNodeRefList.stream().filter(noderef -> noderef.busIndexIn == 0).findFirst()
                .orElseThrow(() -> new PowerFactoryException("Missing AC terminal for Vsc " + elmVsc.getId() + "."));

        VoltageLevel voltageLevel = network.getVoltageLevel(acNodeRef.voltageLevelId);
        VoltageSourceConverterAdder converterAdder = voltageLevel.newVoltageSourceConverter()
                .setId(idInNetworkString(elmVsc));

        converterAdder.setNode1(acNodeRef.node);
        converterAdder.setIdleLoss(elmVsc.getFloatAttributeValue("Pnold"));
        converterAdder.setSwitchingLoss(0.0);
        converterAdder.setResistiveLoss(0.0);
        converterAdder.setControlMode(ControlMode.P_PCC); // TODO be smarter there.
        checkSameNominalVoltage(dcNode1, dcNode2);
        double targetV = vscConnections.dcNode1IsPlus ? dcNode1.getNominalV() : -dcNode1.getNominalV();
        converterAdder.setTargetVdc(targetV);
        converterAdder.setDcNode1(dcNode1.getId());
        converterAdder.setDcNode2(dcNode2.getId());
        converterAdder.setVoltageRegulatorOn(false); // TODO figure out which VSC controls the voltage
        converterAdder.setReactivePowerSetpoint(elmVsc.getFloatAttributeValue("qsetp")); // TODO check
        // sign
        // convention
        converterAdder.setTargetP(elmVsc.getFloatAttributeValue("psetp")); // TODO check sign convention
        final double voltageSetPoint = elmVsc.getFloatAttributeValue("usetp")
                * elmVsc.getFloatAttributeValue("Unom");
        converterAdder.setVoltageSetpoint(voltageSetPoint);
        converterAdder.add();

    }

    private static DataObject getParentTerminal(DataObject elmVsc, DataObject cubicle) {
        DataObject tempTerminal;
        try {
            tempTerminal = cubicle.getParent();
        } catch (NoSuchElementException e) {
            final String error = "When fetching the AC terminal of VSC (id "
                    + elmVsc.getId() + "):"
                    + "cubicle " + cubicle.getId() + " has no corresponding terminal.";
            throw new PowerFactoryException(error);
        }
        assert tempTerminal != null;
        return tempTerminal;
    }

    /**
     * Small record as return type of findVscConnectionsPowerFactory
     * meant to represent the AC node and DC nodes which are
     * terminals for an AC/DC converter.
     * @param acTerminal AC connection terminal.
     * @param dcTerminal1 DC connection terminal 1.
     * @param dcTerminal2 DC connection terminal 2.
     * @param dcNode1IsPlus true iff dcTerminal1 has polarity greater than dcTerminal2
     *                      (i.e. + over 0 or -, or 0 over -).
     */
    private record VscConnections(DataObject acTerminal,
                                  DataObject dcTerminal1,
                                  DataObject dcTerminal2,
                                  boolean dcNode1IsPlus) {
        /**
         * Fetch the connection terminals for the AC/DC converter.
         * @param elmVsc PowerFactory AC/DC converter.
         * @return VscConnections with connection data.
         * Helper routine for addConverter.
         */
        static VscConnections findVscConnectionsPowerFactory(DataObject elmVsc) {
            assert "ElmVsc".equals(elmVsc.getDataClassName());

            DataObject acTerminal = null;
            DataObject dcTerminal1 = null;
            DataObject dcTerminal2 = null;

            // figure out where it is connected on the AC side to get the voltage level.
            // get AC terminal and DC terminal 1 and DC terminal 2
            // for this we need to find 3 cubicles that reference the VSC
            // find all cubicles where Obj_id(p) is equal to the VSC id
            // get the terminal id through fold_id(p) -> this gives the AC bus (bus-breaker)
            // or node (node-breaker)
            // where the VSC should be connected.
            // hence the voltage level as well.

            for (DataObject cubicle : elmVsc.getIndex().getDataObjectsByClass("StaCubic")) {
                Optional<DataObjectRef> objId = cubicle.findObjectAttributeValue(DataAttributeNames.OBJ_ID);
                if (objId.isPresent() && objId.get().getId() == elmVsc.getId()) {
                    DataObject tempTerminal = getParentTerminal(elmVsc, cubicle);
                    int objBus = cubicle.findIntAttributeValue("obj_bus")
                            .orElseThrow(() -> new PowerFactoryException(
                                    "DGS Cubicle " + cubicle.getId() + " without obj_bus field."));
                    switch (objBus) {
                        case 0:
                            if (acTerminal != null) {
                                throw new PowerFactoryException(
                                        "Multiple AC cubicles for VSC " + elmVsc.getId() + ".");
                            }
                            acTerminal = tempTerminal;
                            break;
                        case 1:
                            if (dcTerminal1 != null) {
                                throw new PowerFactoryException(
                                        "Multiple DC cubicles 1 for VSC " + elmVsc.getId() + ".");
                            }
                            dcTerminal1 = tempTerminal;
                            break;
                        case 2:
                            if (dcTerminal2 != null) {
                                throw new PowerFactoryException(
                                        "Multiple DC cubicles 2 for VSC " + elmVsc.getId() + ".");
                            }
                            dcTerminal2 = tempTerminal;
                            break;
                        default:
                            throw new PowerFactoryException(
                                    "Invalid value " + objBus + " for obj_bus in cubicle " + cubicle.getId() + ".");
                    }
                }
            } // for (DataObject cubicle : elmVsc.getIndex().getDataObjectsByClass("StaCubic"))

            if (acTerminal == null) {
                throw new PowerFactoryException("When fetching the AC terminal of VSC (id "
                        + elmVsc.getId() + "): no correponding cubicle found.");
            }

            if (dcTerminal1 == null || dcTerminal2 == null) {
                throw new PowerFactoryException(
                        "Missing DC terminal for AC-DC converter " + elmVsc.getId() + ".");
            }

            // check which DC node is + and which is - (or neutral). This makes it possible
            // to get the sign of the DC command volatge.
            int iMinus1 = Integer.MIN_VALUE;
            int iMinus2 = Integer.MIN_VALUE;
            try {
                iMinus1 = dcTerminal1.getIntAttributeValue("iminus");
                iMinus2 = dcTerminal2.getIntAttributeValue("iminus");
            } catch (PowerFactoryException e) {
                throw new PowerFactoryException("Missing iminus field in $$ElmTerm table for DC terminal.");
            }

            if (iMinus1 == iMinus2) {
                throw new PowerFactoryException(
                        "Converter " + elmVsc.getId() + " has 2 DC terminals with the same polarities.");
            }

            boolean dcNode1IsPlus = iMinus1 == 2 && iMinus2 == 1 || iMinus1 == 0;
            return new VscConnections(acTerminal, dcTerminal1, dcTerminal2, dcNode1IsPlus);

        }

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
