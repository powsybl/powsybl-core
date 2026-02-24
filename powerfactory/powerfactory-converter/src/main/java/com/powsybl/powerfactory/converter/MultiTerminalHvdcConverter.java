/**
 * Copyright (c) 2025, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.AcDcConverter.ControlMode;
import com.powsybl.iidm.network.*;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectIndex;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 * <p>
 * Importer from the DGS data model, for multi-terminal DC grids (a.k.a.
 * detailed HVDC).
 */
public final class MultiTerminalHvdcConverter extends AbstractHvdcConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractHvdcConverter.class);

    private final DcGridData gridData;

    // Small record that directly transcribes the DC data from the PowerFactory data
    // model for a connected subgrid
    private record DcGridData(List<DataObject> dcElmLnes, List<DataObject> dcElmTerms,
                              List<DataObject> acDcConverters, List<DataObject> elmGndswt) {

        /**
         * Create the data model by reading from the PowerFactory data models.
         *
         * @param elmNets All the "networks" from the PowerFactory data model.
         * @return DC lines, DC terminals and converters.
         * <p>
         * Selection is done based on the systp attribute of ElmTerm and of TypLne.
         */
        static DcGridData createGridData(List<DataObject> elmNets) {
            assert elmNets.isEmpty() || "ElmNet".equals(elmNets.getFirst().getDataClassName());

            List<DataObject> elmTerms = PowerFactoryImporter.gatherElmTerms(elmNets);
            assert elmTerms.isEmpty() || "ElmTerm".equals(elmTerms.getFirst().getDataClassName());
            List<DataObject> elmVscs = PowerFactoryImporter.gatherElmVscs(elmNets);
            assert elmVscs.isEmpty() || "ElmVsc".equals(elmVscs.getFirst().getDataClassName());

            List<DataObject> dcElmLnes = new ArrayList<>();
            List<DataObject> dcElmTerms = new ArrayList<>();
            List<DataObject> usedVscs = new ArrayList<>(elmVscs);

            // Add DC lines
            List<DataObject> elmLines = elmNets.stream().flatMap(elmNet -> elmNet.search(".*.ElmLne").stream()).toList();
            for (DataObject elmLne : elmLines) {
                DataObjectRef typLneRef = elmLne.getObjectAttributeValue("typ_id");
                DataObject typLne = typLneRef.resolve().orElseThrow(() -> new PowerFactoryException("Missing line type in TypLne for ElmLne " + elmLne.getId() + "."));
                int lineSysType = typLne.findIntAttributeValue("systp").orElseThrow(() -> new PowerFactoryException("Missing systp for TypLne " + typLne.getId() + "."));
                if (lineSysType == 1) {
                    dcElmLnes.add(elmLne);
                }
            }

            // Add DC terminals
            for (DataObject elmTerm : elmTerms) {
                int termSystyp = elmTerm.findIntAttributeValue("systype").orElseThrow(() -> new PowerFactoryException("Missing systype for ElmTerm " + elmTerm.getId() + "."));
                if (termSystyp == 1) {
                    dcElmTerms.add(elmTerm);
                }
            }

            // Add grounds
            List<DataObject> elmGndswt = elmNets.stream().flatMap(elmNet -> elmNet.search(".*.ElmGndswt").stream()).toList();

            return new DcGridData(dcElmLnes, dcElmTerms, usedVscs, elmGndswt);
        }

    } // private record DcGridData

    public MultiTerminalHvdcConverter(ImportContext importContext, Network network, List<DataObject> elmNets) {
        super(importContext, network);
        gridData = DcGridData.createGridData(elmNets);
        final int nRead = gridData.acDcConverters.size()
                + gridData.dcElmLnes.size()
                + gridData.dcElmTerms.size();
        LOGGER.info("{} data objects read from the DGS file for detailed HVDC data .", nRead);
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

        LOGGER.info("Creating the DC sub-networks.");

        Network network = getNetwork();

        // Create nodes
        LOGGER.debug("Creating {} DC nodes.", gridData.dcElmTerms.size());
        for (DataObject terminal : gridData.dcElmTerms) {
            addNode(terminal, network);
        }

        // create and connect converters
        LOGGER.debug("Creating {} DC converters.", gridData.acDcConverters.size());
        for (DataObject converter : gridData.acDcConverters) {
            addConverter(converter, network, getImportContext());
        }

        // create and connect lines
        LOGGER.debug("Creating {} DC lines.", gridData.dcElmLnes.size());
        for (DataObject line : gridData.dcElmLnes) {
            addLine(line, network);
        }

        // create and connect grounds
        LOGGER.debug("Creating {} DC grounds.", 0);
        for (DataObject gnd : gridData.elmGndswt) {
            addGround(gnd, network);
        }

        LOGGER.debug("DC subnetworks created.");
    }

    /**
     * Add a ground object to the network
     * @param elmGndswt PowerFactory data object.
     * @param network network to amend.
     */
    private static void addGround(DataObject elmGndswt, Network network) {

        int onOff = elmGndswt.findIntAttributeValue("on_off").orElse(1);
        int ciEarthed = elmGndswt.findIntAttributeValue("ciEarthed").orElse(1);

        if (onOff == 0 || ciEarthed == 0) {
            return;
        }

        String gndId = idInNetworkString(elmGndswt);

        // Find terminal connected to ground
        String elmId = getGroundNodeId(elmGndswt);

        assert gndId != null;
        assert elmId != null;

        network.newDcGround().setId(gndId)
                .setDcNode(elmId)
                .add();
    }

    /**
     * Fetch the node id corresponding to the ground element in ElmGndswt.
     * @param elmGndswt ground element in PowerFactory data model.
     * @return node name in the PowSyBl data model.
     */
    private static String getGroundNodeId(DataObject elmGndswt) {
        DataObjectIndex indexDataObjects = elmGndswt.getIndex();
        for (DataObject cubicle : indexDataObjects.getDataObjectsByClass("StaCubic")) {
            Optional<DataObjectRef> elmGndswtRef = cubicle.findObjectAttributeValue(DataAttributeNames.OBJ_ID);
            if (elmGndswtRef.isPresent() && elmGndswtRef.get().getId() == elmGndswt.getId()) {
                DataObjectRef elmTermRef = cubicle.getObjectAttributeValue("fold_id");
                DataObject elmTerm = elmTermRef.resolve()
                        .orElseThrow(() -> new PowerFactoryException("Cannot find terminal " + elmTermRef.getId()
                                + " corresponding to ElmGndswt " + elmGndswt.getId()));
                return idInNetworkString(elmTerm);
            }
        }

        throw new PowerFactoryException("No cubicle corresponding to ElmGndswt " + elmGndswt.getId());
    }

    /**
     * Create Dc node in network. Not connected.
     *
     * @param terminal terminal to create DcNode from.
     * @param network  where to add the node.
     */
    private static void addNode(DataObject terminal, Network network) {

        assert "ElmTerm".equals(terminal.getDataClassName());

        network.newDcNode().setId(idInNetworkString(terminal)).setName(terminal.getLocName()).setNominalV(terminal.getFloatAttributeValue("uknom")).add();

        double uknom = terminal.getFloatAttributeValue("uknom");
        double unknom = terminal.getFloatAttributeValue("unknom");
        if (uknom != unknom) {
            throw new PowerFactoryException("ElmTerm " + terminal.getId() + " is part of a DC subgrid but its uknom and unknom are different.");
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
        network.newDcLine().setId(idInNetworkString(line)).setDcNode1(dcNode1.getId()).setDcNode2(dcNode2.getId()).setR(lineResistance).add();
    }

    /**
     * Return type for findDcLineEndsPowerFactory.
     *
     * @param dcTerm1DataObject first terminal of the line in the PowerFactory data model.
     * @param dcTerm2DataObject second terminal of the line in the PowerFactory data model.
     */
    private record LineTerms(DataObject dcTerm1DataObject, DataObject dcTerm2DataObject) {
        /**
         * Find both ends of a line in the PowerFactory data model.
         *
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

                    DataObject tempTerminal = cubicle.getObjectAttributeValue("fold_id").resolve().orElseThrow(() -> new PowerFactoryException("Error getting terminal for line " + line.getId() + " cubicle " + cubicle.getId()));
                    int objBus = cubicle.getIntAttributeValue("obj_bus");

                    if (objBus != 0 && objBus != 1) {
                        throw new PowerFactoryException("Invalid value " + objBus + " for obj_bus in cubicle related to DC line " + line.getId() + ". Expected 0 or 1");
                    }

                    if (objBus == 0 && dcTerm1DataObject == null) {
                        dcTerm1DataObject = tempTerminal;
                    } else if (objBus == 1 && dcTerm2DataObject == null) {
                        dcTerm2DataObject = tempTerminal;
                    } else {
                        throw new PowerFactoryException("Multiple cubicles with obj_bus = " + objBus + " related to DC line " + line.getId() + ".");
                    }

                }
            }

            if (dcTerm1DataObject == null || dcTerm2DataObject == null) {
                throw new PowerFactoryException("Missing terminal for line " + line.getId() + ".");
            }

            return new LineTerms(dcTerm1DataObject, dcTerm2DataObject);
        }
    }

    /**
     * Compute the line resistance from the line object and its line type in the PowerFactory data model.
     *
     * @param line powerline we want to compute the resistance of.
     * @return Resistance (Ohm) of the line.
     * <p>
     * Resistance = lineic resistance * distance / number of parallel lines.
     */
    private static double getLineResistance(DataObject line) {
        double lineLength = line.getFloatAttributeValue("dline"); // km
        int numberOfParallelLines = line.findIntAttributeValue("nlnum").orElse(1);

        DataObject lineType = line.getObjectAttributeValue("typ_id").resolve().orElseThrow(() -> new PowerFactoryException("Missing line type referenced by DC line " + line.getId() + "."));

        int lineSysType = lineType.findIntAttributeValue("systype").orElse(1);
        if (lineSysType != 1) {
            throw new PowerFactoryException("DC line " + line.getId() + " has type " + lineType.getId() + " which not of system type systype DC.");
        }

        double rline = lineType.getFloatAttributeValue("rline"); // Ohm / km

        return rline * lineLength / numberOfParallelLines; // Ohm
    }

    /**
     * Convert from PowerFactory terminal to DcNode in the PowSyBl network.
     *
     * @param dcTermDataObject    terminal to find as DC node.
     * @param network             PowSyBl network.
     * @param parentComponentName Name of the component where the node belong, purely for the error message.
     * @return DcNode inside the network.
     */
    private static DcNode getSafeNodeForLine(DataObject dcTermDataObject, Network network, String parentComponentName) {
        if (dcTermDataObject == null) {
            throw new PowerFactoryException(parentComponentName + " is missing a node in the DGS files.");
        }
        String idInNetwork = idInNetworkString(dcTermDataObject);
        DcNode result = network.getDcNode(idInNetwork);
        if (result == null) {
            throw new PowerFactoryException("Missing node " + idInNetwork + " in network. Is terminal " + dcTermDataObject.getId() + " in the ElmTerm table? This is for " + parentComponentName);
        }
        return result;
    }

    /**
     * Add converter to the PowSyBl network
     *
     * @param elmVsc        Converter to add (PowerFactory data model)
     * @param network       PowSyBl network with all DC nodes already added.
     * @param importContext Import context with mapping object id -> corresponding nodes
     */
    private static void addConverter(DataObject elmVsc, Network network, ImportContext importContext) {
        // TODO refactor to break down.

        assert "ElmVsc".equals(elmVsc.getDataClassName());
        assert network.getDcNodeCount() > 0;

        // Get the terminals on the PowerFactory side (DataObjects)
        VscConnections vscConnections = VscConnections.findVscConnectionsPowerFactory(elmVsc);

        // Get corresponding DC and AC nodes in the PowSyBl network
        DcNode dcNode1 = getSafeNodeForLine(vscConnections.dcTerminal1, network, "converter " + elmVsc.getId());
        DcNode dcNode2 = getSafeNodeForLine(vscConnections.dcTerminal2, network, "converter " + elmVsc.getId());

        assert dcNode1 != null && dcNode2 != null : "DC nodes should be initialized first";

        int iAcdc = elmVsc.getIntAttributeValue("i_acdc");
        double uSetPowerDcPu = elmVsc.findFloatAttributeValue("usetpdc").orElse(Float.NaN);
        double unomDc = elmVsc.getFloatAttributeValue("Unomdc"); // Nominal DC voltage in kV.

        // From the list of terminals related to the elmVsc in the importContext
        List<NodeRef> acNodeRefList = importContext.objIdToNode.get(elmVsc.getId());
        NodeRef acNodeRef = acNodeRefList.stream().filter(noderef -> noderef.busIndexIn == 0).findFirst().orElseThrow(() -> new PowerFactoryException("Missing AC terminal for Vsc " + elmVsc.getId() + "."));

        double usetpPu = elmVsc.findFloatAttributeValue("usetp").orElse(Float.NaN); // AC Voltage setpoint in pu.
        double uNom = elmVsc.findFloatAttributeValue("Unom").orElse(Float.NaN);

        double qsetp = elmVsc.findFloatAttributeValue("qsetp").orElse(Float.NaN); // Reactive power in Mvar
        double psetp = elmVsc.findFloatAttributeValue("psetp").orElse(Float.NaN);

        // Loss model
        double pnold = elmVsc.findFloatAttributeValue("Pnold").orElse(0.0F); // No-load losses in kW
        double swtLossFactor = elmVsc.findFloatAttributeValue("swtLossFactor").orElse(0.0F); // Switching loss factor in kW/A
        double resLossFactor = elmVsc.findFloatAttributeValue("resLossFactor").orElse(0.0F);

        VoltageLevel voltageLevel = network.getVoltageLevel(acNodeRef.voltageLevelId);
        VoltageSourceConverterAdder converterAdder = voltageLevel.newVoltageSourceConverter().setId(idInNetworkString(elmVsc));

        // Connect
        converterAdder.setNode1(acNodeRef.node);
        checkSameNominalVoltage(dcNode1, dcNode2);
        converterAdder.setDcNode1(dcNode1.getId());
        converterAdder.setDcNode2(dcNode2.getId());

        // Control logic
        ControlMode controlMode;
        boolean acVoltageRegulation;
        switch (iAcdc) {
            case 3:
                controlMode = ControlMode.V_DC;
                acVoltageRegulation = false;
                break;
            case 4:
                controlMode = ControlMode.P_PCC;
                acVoltageRegulation = true;
                break;
            case 5:
                controlMode = ControlMode.P_PCC;
                acVoltageRegulation = false;
                break;
            case 6:
                controlMode = ControlMode.V_DC;
                acVoltageRegulation = true;
                break;
            case 0: // 0, 1, 2, 7 and 8 are supported in PowerFactory but not yet in PowSyBl
            case 1:
            case 2:
            case 7:
            case 8:
            default:
                throw new PowerFactoryException("Unsupported value " + iAcdc + " for VSC " + elmVsc.getId() + ".");
        }
        converterAdder.setControlMode(controlMode);
        converterAdder.setVoltageRegulatorOn(acVoltageRegulation);

        double targetVdc = uSetPowerDcPu * unomDc;
        if (!Double.isFinite(targetVdc) && controlMode == ControlMode.V_DC) {
            throw new PowerFactoryException("VSC " + elmVsc.getId() + " has control mode V_DC but unspecified target V_DC.");
        }
        converterAdder.setTargetVdc(targetVdc);

        double voltageSetPointAc = usetpPu * uNom;
        if (!Double.isFinite(voltageSetPointAc) && acVoltageRegulation) {
            throw new PowerFactoryException("VSC " + elmVsc.getId() + " has V_AC control but unspecified target V_AC.");
        }
        converterAdder.setVoltageSetpoint(voltageSetPointAc);

        // Loss model
        double idleLoss = getIdleLoss(controlMode, pnold, uSetPowerDcPu);
        converterAdder.setIdleLoss(idleLoss);
        converterAdder.setSwitchingLoss(swtLossFactor);
        converterAdder.setResistiveLoss(resLossFactor);

        // AC Power and voltage regulation
        if (!Double.isFinite(qsetp) && !acVoltageRegulation) {
            throw new PowerFactoryException("VSC " + elmVsc.getId() + " has Q control but undefined Q.");
        }
        converterAdder.setReactivePowerSetpoint(qsetp); // TODO check sign convention
        if (!Double.isFinite(psetp) && controlMode == ControlMode.P_PCC) {
            throw new PowerFactoryException("VSC " + elmVsc.getId() + " has P control but undefined P.");
        }
        converterAdder.setTargetP(psetp); // TODO check sign convention

        converterAdder.add();
    }

    private static double getIdleLoss(ControlMode controlMode, double pnold, double usetpDcPu) {
        double idleLoss = 0.0; // MW
        // The PowerFactory model considers the idle loss to be equal to Ud^2, while it is constant in Open Load Flow.
        // approximation: if there is a set DC voltage, then we consider it is always met.
        // Otherwise, we consider that the DC voltage is equal to the nominal voltage.
        if (controlMode == ControlMode.V_DC) {
            idleLoss = pnold / 1000 * usetpDcPu * usetpDcPu; // MW
        } else {
            idleLoss = pnold / 1000; // MW
        }
        return idleLoss;
    }

    /**
     * Convenience routine to safely fetch the terminal of a cubicle
     *
     * @param elmVsc  converter (for error message only)
     * @param cubicle to get the parent terminal of.
     * @return parent terminal.
     */
    private static DataObject getParentTerminal(DataObject elmVsc, DataObject cubicle) {
        DataObject tempTerminal;
        try {
            tempTerminal = cubicle.getParent();
        } catch (NoSuchElementException e) {
            final String error = "When fetching a terminal of VSC (id " + elmVsc.getId() + "):" + "cubicle " + cubicle.getId() + " has no corresponding terminal.";
            throw new PowerFactoryException(error);
        }
        assert tempTerminal != null;
        return tempTerminal;
    }

    /**
     * Small record as return type of findVscConnectionsPowerFactory
     * meant to represent the AC node and DC nodes which are
     * terminals for an AC/DC converter.
     *
     * @param dcTerminal1   DC connection terminal 1.
     * @param dcTerminal2   DC connection terminal 2.
     * @param dcNode1IsPlus true iff dcTerminal1 has polarity greater than dcTerminal2
     *                      (i.e. + over 0 or -, or 0 over -).
     */
    private record VscConnections(DataObject dcTerminal1, DataObject dcTerminal2, boolean dcNode1IsPlus) {
        /**
         * Fetch the connection terminals for the AC/DC converter.
         *
         * @param elmVsc PowerFactory AC/DC converter.
         * @return VscConnections with connection data.
         * Helper routine for addConverter.
         */
        static VscConnections findVscConnectionsPowerFactory(DataObject elmVsc) {
            assert "ElmVsc".equals(elmVsc.getDataClassName());

            DataObject dcTerminal1 = null;
            DataObject dcTerminal2 = null;

            // figure out the DC terminal 1 and DC terminal 2 of the converter.
            // for this we need to find 2 cubicles that reference the VSC
            // find all cubicles where Obj_id(p) is equal to the VSC id
            // with obj_id = 1 and 2.

            for (DataObject cubicle : elmVsc.getIndex().getDataObjectsByClass("StaCubic")) {
                Optional<DataObjectRef> objId = cubicle.findObjectAttributeValue(DataAttributeNames.OBJ_ID);
                if (objId.isPresent() && objId.get().getId() == elmVsc.getId()) {
                    int objBus = cubicle.findIntAttributeValue("obj_bus").orElseThrow(() -> new PowerFactoryException("DGS Cubicle " + cubicle.getId() + " without obj_bus field."));
                    switch (objBus) {
                        case 0:
                            break;
                        case 1:
                            if (dcTerminal1 != null) {
                                throw new PowerFactoryException("Multiple DC cubicles 1 for VSC " + elmVsc.getId() + ".");
                            }
                            dcTerminal1 = getParentTerminal(elmVsc, cubicle);
                            break;
                        case 2:
                            if (dcTerminal2 != null) {
                                throw new PowerFactoryException("Multiple DC cubicles 2 for VSC " + elmVsc.getId() + ".");
                            }
                            dcTerminal2 = getParentTerminal(elmVsc, cubicle);
                            break;
                        default:
                            throw new PowerFactoryException("Invalid value " + objBus + " for obj_bus in cubicle " + cubicle.getId() + ".");
                    }
                }
            } // for (DataObject cubicle : elmVsc.getIndex().getDataObjectsByClass("StaCubic"))

            if (dcTerminal1 == null || dcTerminal2 == null) {
                throw new PowerFactoryException("Missing DC terminal for AC-DC converter " + elmVsc.getId() + ".");
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
                throw new PowerFactoryException("Converter " + elmVsc.getId() + " has 2 DC terminals with the same polarities.");
            }

            boolean dcNode1IsPlus = iMinus1 == 2 && iMinus2 == 1 || iMinus1 == 0;

            return new VscConnections(dcTerminal1, dcTerminal2, dcNode1IsPlus);

        }

    }

    private static void checkSameNominalVoltage(DcNode dcNode1, DcNode dcNode2) {
        if (dcNode1.getNominalV() != dcNode2.getNominalV()) {
            throw new PowerFactoryException("DcNode " + dcNode1.getId() + " and DcNode " + dcNode2.getId() + " are connected to the same convertor but have different nominal voltages.");
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

}
