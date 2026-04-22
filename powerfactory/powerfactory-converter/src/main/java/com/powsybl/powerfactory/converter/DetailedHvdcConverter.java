/**
 * Copyright (c) 2026, SuperGrid Institute (https://www.supergrid-institute.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.powerfactory.converter;

import com.powsybl.iidm.network.AcDcConverter.ControlMode;
import com.powsybl.iidm.network.DcNode;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.VoltageSourceConverterAdder;
import static com.powsybl.powerfactory.converter.DataAttributeNames.*;

import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.powerfactory.converter.PowerFactoryImporter.ImportContext;
import com.powsybl.powerfactory.model.DataObject;
import com.powsybl.powerfactory.model.DataObjectRef;
import com.powsybl.powerfactory.model.PowerFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Landry Huet {@literal <landry.huet at supergrid-institute.com>}
 * <p>
 * Importer from the DGS data model, for multi-terminal DC grids (a.k.a.
 * detailed HVDC).
 */
final class DetailedHvdcConverter extends AbstractHvdcConverter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DetailedHvdcConverter.class);

    private final DcGridData gridData;

    // Small record that directly transcribes the DC data from the PowerFactory data
    // model for a connected subgrid
    private record DcGridData(Set<DataObject> dcElmLnes,
                              Set<DataObject> dcElmTerms,
                              Set<DataObject> acDcConverters,
                              Set<DataObject> elmGndswt) {

        /**
         * Create the data model by reading from the PowerFactory data models.
         *
         * @param elmNets All the "networks" from the PowerFactory data model.
         * @return DC lines, DC terminals and converters.
         * <p>
         * Selection is done based on the systp attribute of ElmTerm and of TypLne.
         */
        static DcGridData createGridData(List<DataObject> elmNets) {
            assert elmNets.isEmpty() || ELMNET.equals(elmNets.getFirst().getDataClassName());

            List<DataObject> elmTerms = PowerFactoryImporter.gatherElmTerms(elmNets);
            assert elmTerms.isEmpty() || ELMTERM.equals(elmTerms.getFirst().getDataClassName());
            List<DataObject> elmVscs = PowerFactoryImporter.gatherElmVscs(elmNets);
            assert elmVscs.isEmpty() || "ElmVsc".equals(elmVscs.getFirst().getDataClassName());

            // Add DC lines
            Set<DataObject> dcElmLnes = new HashSet<>();
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
            Set<DataObject> dcElmTerms = new HashSet<>();
            for (DataObject elmTerm : elmTerms) {
                int termSystyp = elmTerm.findIntAttributeValue("systype").orElseThrow(() -> new PowerFactoryException("Missing systype for ElmTerm " + elmTerm.getId() + "."));
                if (termSystyp == 1) {
                    dcElmTerms.add(elmTerm);
                }
            }

            Set<DataObject> usedVscs = new HashSet<>(elmVscs);

            // Add grounds
            Set<DataObject> elmGndswt = elmNets.stream().flatMap(elmNet -> elmNet.search(".*.ElmGndswt").stream()).collect(Collectors.toSet());

            return new DcGridData(dcElmLnes, dcElmTerms, usedVscs, elmGndswt);
        }

    } // private record DcGridData

    DetailedHvdcConverter(ImportContext importContext, Network network, List<DataObject> elmNets) {
        super(importContext, network);
        gridData = DcGridData.createGridData(elmNets);
        final int nRead = gridData.acDcConverters.size()
                + gridData.dcElmLnes.size()
                + gridData.dcElmTerms.size();
        LOGGER.info("{} data objects read from the DGS file for detailed DC data .", nRead);
    }

    @Override
    boolean isDcNode(DataObject elmTerm) {
        return gridData.dcElmTerms.contains(elmTerm);
    }

    @Override
    boolean isDcLink(DataObject elmLne) {
        return gridData.dcElmLnes.contains(elmLne);
    }

    /**
     * Record representation of the StaCubic content.
     * @param busIndexIn // obj_bud in the DGS file - connector id for the equipment.
     * @param dcNodeId   // id of the DcNode in the PowSyBl DC network.
     */
    private record DcNodeRef(int busIndexIn, String dcNodeId) {
    }

    @Override
    void create() {

        LOGGER.info("Creating the DC sub-networks.");

        Network network = getNetwork();

        // Equipment sets are sorted by id to keep a deterministic order and simplify testing

        // Create nodes
        LOGGER.debug("Creating {} DC nodes.", gridData.dcElmTerms.size());
        gridData.dcElmTerms.stream()
                .sorted(Comparator.comparing(DataObject::getId))
                .forEachOrdered(terminal -> addDcNode(terminal, network));

        // Map: id of the equipment in the DGS file
        // -> list of connections to terminals (in DGS) and DC nodes (in PowSyBl), by bus.
        // This makes it possible to compute the map once for all components, and then use it to establish connections
        // everywhere.
        final Map<Long, List<DcNodeRef>> objIdDcNodeRef = mapConnectedObjToDcNode(getImportContext(), gridData);
        objIdDcNodeRefSanityCheck(objIdDcNodeRef, network, gridData.dcElmTerms);

        // create and connect converters
        LOGGER.debug("Creating {} VSCs.", gridData.acDcConverters.size());
        gridData.acDcConverters.stream()
                .sorted(Comparator.comparing(DataObject::getId))
                .forEachOrdered(converter -> addAcDcConverter(converter, network, objIdDcNodeRef));

        // create and connect lines
        LOGGER.debug("Creating {} DC lines.", gridData.dcElmLnes.size());
        gridData.dcElmLnes.stream()
                .sorted(Comparator.comparing(DataObject::getId))
                .forEachOrdered(line -> addDcLine(line, network, objIdDcNodeRef));

        // create and connect grounds
        LOGGER.debug("Creating {} DC grounds.", 0);
        gridData.elmGndswt.stream()
                .sorted(Comparator.comparing(DataObject::getId))
                .forEachOrdered(gnd -> addDcGround(gnd, network, objIdDcNodeRef));

        LOGGER.debug("DC subnetworks created.");
    }

    /**
     * Compute map equipment id -> (bus_id, DcNode id)
     * @param importContext to store unfound objects in StaCubic.
     * @param dcGridData where to fetch the ElmTerm.
     * @return map.
     */
    private static Map<Long, List<DcNodeRef>> mapConnectedObjToDcNode(ImportContext importContext, DcGridData dcGridData) {

        // Map: id of the equipment in the DGS file
        // -> list of connections to DC nodes (in network), by bus.
        Map<Long, List<DcNodeRef>> objIdDcNodeRef = new HashMap<>();

        for (DataObject elmTerm : dcGridData.dcElmTerms) {
            for (DataObject staCubic : elmTerm.getChildrenByClass("StaCubic")) {
                DataObject connectedObj = staCubic.findObjectAttributeValue("obj_id")
                        .flatMap(DataObjectRef::resolve)
                        .orElse(null);

                if (connectedObj == null) {
                    importContext.cubiclesObjectNotFound.add(staCubic);
                    continue;
                }

                int busIndexIn = staCubic.getIntAttributeValue("obj_bus");
                String dcNodeId = idInNetworkString(elmTerm);
                objIdDcNodeRef.computeIfAbsent(connectedObj.getId(), k -> new ArrayList<>()).add(new DcNodeRef(busIndexIn, dcNodeId));
            }
        }

        return objIdDcNodeRef;
    }

    /**
     * Check that all referenced nodes from the map actually exist in the network.
     * If this is not the case, point out the missing terminal, which is likely missing in the DGS file.
     * This should most likely be caught before, but we should not rely on it.
     * @param objIdDcNodeRef map to check.
     * @param network where the nodes should be present.
     * @param elmTerms set of terminals, to look up the terminal in the DGS file.
     * @throws PowerFactoryException if a referenced node is absent from the network.
     */
    private static void objIdDcNodeRefSanityCheck(Map<Long, List<DcNodeRef>> objIdDcNodeRef, Network network, Set<DataObject> elmTerms) {
        assert elmTerms != null;
        assert elmTerms.isEmpty() || ELMTERM.equals(elmTerms.iterator().next().getDataClassName());
        for (var entry : objIdDcNodeRef.entrySet()) {
            for (DcNodeRef nodeRef : entry.getValue()) {
                if (network.getDcNode(nodeRef.dcNodeId()) == null) {
                    DataObject elmTerm = elmTerms.stream().filter(term -> idInNetworkString(term).equals(nodeRef.dcNodeId())).findFirst().orElse(null);
                    assert elmTerm != null;
                    throw new PowerFactoryException("ElmTerm "
                            + elmTerm.getId() + " is referenced by equipment "
                            + entry.getKey()
                            + " but was not created in the network. ");
                }
            }
        }
    }

    /**
     * Add a ground object to the network
     * @param elmGndswt PowerFactory data object.
     * @param network network to amend.
     */
    private static void addDcGround(DataObject elmGndswt, Network network, Map<Long, List<DcNodeRef>> objIdDcNodeRef) {

        int onOff = elmGndswt.findIntAttributeValue("on_off").orElse(1);
        // ciEarthed is disregarded.

        if (onOff == 0) {
            return;
        }

        String gndId = idInNetworkString(elmGndswt);
        assert gndId != null;

        // Find terminal connected to ground
        List<String> dcNodesId = getAndCheckDcNodes(elmGndswt, 1, objIdDcNodeRef);

        network.newDcGround().setId(gndId)
                .setDcNode(dcNodesId.getFirst())
                .add();
    }

    /**
     * Create Dc node in network. Not connected.
     *
     * @param terminal terminal to create DcNode from.
     * @param network  where to add the node.
     */
    private static void addDcNode(DataObject terminal, Network network) {

        assert ELMTERM.equals(terminal.getDataClassName());

        network.newDcNode().setId(idInNetworkString(terminal)).setName(terminal.getLocName()).setNominalV(terminal.getFloatAttributeValue("uknom")).add();

        double uknom = terminal.getFloatAttributeValue("uknom");
        double unknom = terminal.getFloatAttributeValue("unknom");
        if (uknom != unknom) {
            throw new PowerFactoryException("ElmTerm " + terminal.getId() + " is part of a DC subgrid but its uknom and unknom are different.");
        }
    }

    /**
     * Fetch the ids of DcNodes in the network, ordered by bus id.
     * We use this opportunity to check that the correct number of terminals is present in the DGS file.
     * @param elm equipment in the DGS file.
     * @param expectedNumDcNodes number of expected terminals.
     * @param objIdDcNodeRef map equipment -> (bus_id, node id)
     * @return list of dc node ids (in network), sorted by bus_id.
     */
    private static List<String> getAndCheckDcNodes(DataObject elm, int expectedNumDcNodes, Map<Long, List<DcNodeRef>> objIdDcNodeRef) {
        List<DcNodeRef> dcNodeRefs = objIdDcNodeRef.get(elm.getId());
        if (dcNodeRefs == null || dcNodeRefs.size() != expectedNumDcNodes) {
            throw new PowerFactoryException("Inconsistent number (" + (dcNodeRefs != null ? dcNodeRefs.size() : 0) + ") of dcNodes for '" + elm.getId() + "'");
        }
        return dcNodeRefs.stream()
                .sorted(Comparator.comparing(dcNodeRef -> dcNodeRef.busIndexIn))
                .map(DcNodeRef::dcNodeId)
                .toList();
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
    private static void addDcLine(DataObject line, Network network, Map<Long, List<DcNodeRef>> objIdDcNodeRef) {
        // Find the nodes
        List<String> dcNodesIds = getAndCheckDcNodes(line, 2, objIdDcNodeRef);

        // Get the data from the "types"
        double lineResistance = getLineResistance(line);

        // Create the line in the DC subnetwork
        network.newDcLine()
                .setId(idInNetworkString(line))
                .setDcNode1(dcNodesIds.getFirst())
                .setDcNode2(dcNodesIds.getLast())
                .setR(lineResistance).add();
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

    private record PowerFactoryAcDcConverterParameters(int iAcdc,
                                                       double uSetVoltageDcPu,
                                                       double unomDc,
                                                       double usetpPu,
                                                       double uNom,
                                                       double qsetp,
                                                       double psetp,
                                                       double pnold,
                                                       double swtLossFactor,
                                                       double resLossFactor) {
    }

    private static PowerFactoryAcDcConverterParameters fetchPowerFactoryAcDcConverterParameters(DataObject elmVsc) {
        int iAcdc = elmVsc.getIntAttributeValue("i_acdc");
        double uSetVoltageDcPu = elmVsc.findFloatAttributeValue("usetpdc").orElse(Float.NaN);
        double unomDc = elmVsc.getFloatAttributeValue("Unomdc"); // Nominal DC voltage in kV.

        double usetpPu = elmVsc.findFloatAttributeValue("usetp").orElse(Float.NaN); // AC Voltage setpoint in pu.
        double uNom = elmVsc.findFloatAttributeValue("Unom").orElse(Float.NaN);

        double qsetp = elmVsc.findFloatAttributeValue("qsetp").orElse(Float.NaN); // Reactive power in Mvar
        double psetp = elmVsc.findFloatAttributeValue("psetp").orElse(Float.NaN);

        // Loss model
        double pnold = elmVsc.findFloatAttributeValue("Pnold").orElse(0.0F); // No-load losses in kW
        double swtLossFactor = elmVsc.findFloatAttributeValue("swtLossFactor").orElse(0.0F); // Switching loss factor in kW/A
        double resLossFactor = elmVsc.findFloatAttributeValue("resLossFactor").orElse(0.0F);

        return new PowerFactoryAcDcConverterParameters(iAcdc,
                uSetVoltageDcPu, // pu
                unomDc, // kV
                usetpPu, // pu
                uNom, // kV
                qsetp, // MVar
                psetp, // MW
                pnold, // kW (warning !)
                swtLossFactor, // kW / A (warning !)
                resLossFactor); // Ohm
    }

    /**
     * Add converter to the PowSyBl network
     *
     * @param elmVsc        Converter to add (PowerFactory data model)
     * @param network       PowSyBl network with all DC nodes already added.
     */
    private void addAcDcConverter(DataObject elmVsc, Network network, Map<Long, List<DcNodeRef>> objIdDcNodeRef) {

        assert "ElmVsc".equals(elmVsc.getDataClassName());
        assert network.getDcNodeCount() > 0;

        // Fetch data from the ElmVsc DataObject
        PowerFactoryAcDcConverterParameters pfParams = fetchPowerFactoryAcDcConverterParameters(elmVsc);

        // Manage the control logic
        ControlMode controlMode;
        boolean acVoltageRegulation;
        switch (pfParams.iAcdc) {
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
            case 0, 1, 2, 7, 8: // 0, 1, 2, 7 and 8 are supported in PowerFactory but not yet in PowSyBl
            default:
                throw new PowerFactoryException("Unsupported value " + pfParams.iAcdc + " for VSC " + elmVsc.getId() + ".");
        }

        List<String> dcNodesIds = getAndCheckDcNodes(elmVsc, 2, objIdDcNodeRef);

        // From the list of terminals related to the elmVsc in the importContext
        NodeRef acNodeRef = findNodes(elmVsc).stream().filter(noderef -> noderef.busIndexIn == 0)
                .findFirst()
                .orElseThrow(() -> new PowerFactoryException("Missing AC terminal for Vsc " + elmVsc.getId() + "."));

        VoltageLevel voltageLevel = network.getVoltageLevel(acNodeRef.voltageLevelId);
        VoltageSourceConverterAdder converterAdder = voltageLevel.newVoltageSourceConverter().setId(idInNetworkString(elmVsc));

        // Connect
        converterAdder.setNode1(acNodeRef.node);
        checkSameNominalVoltage(network.getDcNode(dcNodesIds.getFirst()), network.getDcNode(dcNodesIds.getLast()));
        converterAdder.setDcNode1(dcNodesIds.getFirst());
        converterAdder.setDcNode2(dcNodesIds.getLast());

        converterAdder.setControlMode(controlMode);

        double targetVdc = pfParams.uSetVoltageDcPu * pfParams.unomDc;
        if (!Double.isFinite(targetVdc) && controlMode == ControlMode.V_DC) {
            throw new PowerFactoryException("VSC " + elmVsc.getId() + " has control mode V_DC but unspecified target V_DC.");
        }
        converterAdder.setTargetVdc(targetVdc);

        double voltageSetPointAc = pfParams.usetpPu * pfParams.uNom;
        if (!Double.isFinite(voltageSetPointAc) && acVoltageRegulation) {
            throw new PowerFactoryException("VSC " + elmVsc.getId() + " has V_AC control but unspecified target V_AC.");
        }

        // Loss model
        double idleLoss = computeIdleLoss(controlMode, pfParams.pnold, pfParams.uSetVoltageDcPu);
        converterAdder.setIdleLoss(idleLoss);
        double switchingLossPowSyBl = pfParams.swtLossFactor / 1000; // convert from kW / A to MW / A
        converterAdder.setSwitchingLoss(switchingLossPowSyBl);
        converterAdder.setResistiveLoss(pfParams.resLossFactor);

        // AC Power and voltage regulation
        // WARNING There is a different sign convention between PowerFactory and PowSyBl:
        // In PowerFactory, power flows from the DC network to the AC network,
        // while in PowSyBl power flows towards the converter
        if (!Double.isFinite(pfParams.qsetp) && !acVoltageRegulation) {
            throw new PowerFactoryException("VSC " + elmVsc.getId() + " has Q control but undefined Q.");
        }
        if (!Double.isFinite(pfParams.psetp) && controlMode == ControlMode.P_PCC) {
            throw new PowerFactoryException("VSC " + elmVsc.getId() + " has P control but undefined P.");
        }
        converterAdder.setTargetP(-pfParams.psetp);

        addVoltageRegulation(converterAdder, acVoltageRegulation, voltageSetPointAc, pfParams);
        converterAdder.add();
    }

    private static void addVoltageRegulation(VoltageSourceConverterAdder converterAdder, boolean acVoltageRegulation, double voltageSetPointAc, PowerFactoryAcDcConverterParameters pfParams) {
        RegulationMode regulationMode;
        double targetValue;
        if (acVoltageRegulation) {
            regulationMode = RegulationMode.VOLTAGE;
            targetValue = voltageSetPointAc;
        } else {
            regulationMode = RegulationMode.REACTIVE_POWER;
            targetValue = -pfParams.qsetp;
        }
        converterAdder.newVoltageRegulation()
            .withMode(regulationMode)
            .withTargetValue(targetValue)
            .add();
    }

    private static double computeIdleLoss(ControlMode controlMode, double pnold, double usetpDcPu) {
        // The PowerFactory model considers the idle loss to be equal to Ud^2, while it is constant in Open Load Flow.
        // approximation: if there is a set DC voltage, then we consider it is always met.
        // Otherwise, we consider that the DC voltage is equal to the nominal voltage.
        // Furthermore, idleLoss is converted from kW to MW.
        if (controlMode == ControlMode.V_DC) {
            return pnold / 1000 * usetpDcPu * usetpDcPu; // MW
        } else {
            return pnold / 1000; // MW
        }
    }

    private static void checkSameNominalVoltage(DcNode dcNode1, DcNode dcNode2) {
        if (dcNode1.getNominalV() != dcNode2.getNominalV()) {
            throw new PowerFactoryException("DcNode " + dcNode1.getId() + " and DcNode " + dcNode2.getId() + " are connected to the same convertor but have different nominal voltages.");
        }
    }

    /**
     * Unique string id in network from DataObject in DGS file. This guarantees some
     * consistency.
     *
     * @param component Component we want the id of.
     * @return Unique id in the AcDcNetwork.
     */
    private static String idInNetworkString(DataObject component) {
        return component.getLocName();
    }

}
