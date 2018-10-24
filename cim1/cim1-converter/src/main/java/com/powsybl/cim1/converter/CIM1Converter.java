/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cim1.converter;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.powsybl.entsoe.util.EntsoeFileName;
import com.powsybl.iidm.network.*;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.Pseudograph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Supplier;

/**
 * Building of an IIDM network model based on a CIM network model
 *
 * @author Olivier Bretteville <olivier.bretteville at rte-france.com>
 */
class CIM1Converter implements CIM1Constants {

    private static final Logger LOGGER = LoggerFactory.getLogger(CIM1Converter.class);

    private static final String CIM_ENTSOE_PROFILE_1ST_EDITION_VERSION = "IEC61970CIM14v02";

    private static final String XNODE_V_PROPERTY = "xnode_v";
    private static final String XNODE_ANGLE_PROPERTY = "xnode_angle";

    private final cim1.model.CIMModel cimModel;

    private final String fileName;

    private final CIM1ConverterConfig config;

    private final CIM1NamingStrategy namingStrategy;

    private final Map<Bus, cim1.model.SvVoltage> svVoltages = new IdentityHashMap<>();

    private final List<cim1.model.Switch> lowImpedanceLines = new ArrayList<>();

    CIM1Converter(cim1.model.CIMModel cimModel, String fileName, CIM1ConverterConfig config) {
        this.cimModel = Objects.requireNonNull(cimModel);
        this.fileName = Objects.requireNonNull(fileName);
        if (fileName.isEmpty()) {
            throw new IllegalArgumentException("File name is null");
        }
        this.config = Objects.requireNonNull(config);
        namingStrategy = config.getNamingStrategyFactory().create(cimModel);
    }

    private final Multimap<String, Terminal> terminalMapping = HashMultimap.create();

    private final Map<cim1.model.TopologicalNode, cim1.model.EnergyConsumer> boundaryXNodes = new IdentityHashMap<>();

    private final Map<cim1.model.TopologicalNode, List<cim1.model.ConductingEquipment>> mergedXNodes = new IdentityHashMap<>();

    private void addTerminalMapping(cim1.model.TopologicalNode tn, Terminal t) {
        terminalMapping.put(tn.getId(), t);
    }

    private Terminal getTerminalMapping(cim1.model.TopologicalNode tn) {
        Collection<Terminal> terminals = terminalMapping.get(tn.getId());
        if (terminals.isEmpty()) {
            throw new CIM1Exception("Cannot find an IIDM terminal for CIM topological node "
                    + tn.getId());
        }
        return terminals.iterator().next();
    }

    private static boolean isXNode(cim1.model.TopologicalNode tn) {
        return tn.isFromBoundary() || tn.getConnectivityNodeContainer() == null;
    }

    private static String findUcteXnodeCode(cim1.model.TopologicalNode tn) {
        // the xnode name is contained in the description field, starting
        // from the letter X until ; character
        int pos1 = tn.getDescription().indexOf('X');
        int pos2 = tn.getDescription().indexOf(';');
        if (pos1 == -1 || pos2 == -1) {
            throw new CIM1Exception("Cannot find Xnode name from topological node description field '"
                    + tn.getDescription() + "'");
        }
        return tn.getDescription().substring(pos1, pos2);
    }

    private static float[] getVoltageLimits(cim1.model.VoltageLevel vl, Set<String> noOperationalLimitInOperationalLimitSet) {
        float lowVoltageLimit = Float.NaN;
        float highVoltageLimit = Float.NaN;
        if (vl.getTopologicalNode() != null) {
            for (cim1.model.TopologicalNode tn : vl.getTopologicalNode()) {
                if (tn.getTerminal() != null) {
                    for (cim1.model.Terminal t : tn.getTerminal()) {
                        if (t.getOperationalLimitSet() != null) {
                            for (cim1.model.OperationalLimitSet ols : t.getOperationalLimitSet()) {
                                if (ols.getOperationalLimitValue() == null) {
                                    noOperationalLimitInOperationalLimitSet.add(ols.getId());
                                    continue;
                                }
                                for (cim1.model.OperationalLimit ol : ols.getOperationalLimitValue()) {
                                    cim1.model.OperationalLimitType olt = ol.getOperationalLimitType();
                                    float value;
                                    switch (olt.getName()) {
                                        case "LowVoltage":
                                            value = ((cim1.model.VoltageLimit) ol).getValue();
                                            if (Float.isNaN(lowVoltageLimit)) {
                                                lowVoltageLimit = value;
                                            } else {
                                                lowVoltageLimit = Math.min(lowVoltageLimit, value);
                                            }
                                            break;
                                        case "HighVoltage":
                                            value = ((cim1.model.VoltageLimit) ol).getValue();
                                            if (Float.isNaN(highVoltageLimit)) {
                                                highVoltageLimit = value;
                                            } else {
                                                highVoltageLimit = Math.max(highVoltageLimit, value);
                                            }
                                            break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new float[] {lowVoltageLimit, highVoltageLimit};
    }

    private static void createCurrentLimits(cim1.model.Terminal t, Supplier<CurrentLimitsAdder> owner, Set<String> noOperationalLimitInOperationalLimitSet) {
        if (t.getOperationalLimitSet() != null) {
            CurrentLimitsAdder cla = owner.get();
            boolean foundCurrentLimits = false;
            for (cim1.model.OperationalLimitSet ols : t.getOperationalLimitSet()) {
                if (ols.getOperationalLimitValue() == null) {
                    noOperationalLimitInOperationalLimitSet.add(ols.getId());
                    continue;
                }
                for (cim1.model.OperationalLimit ol : ols.getOperationalLimitValue()) {
                    cim1.model.OperationalLimitType olt = ol.getOperationalLimitType();
                    if (ol instanceof  cim1.model.CurrentLimit) {
                        float value = ((cim1.model.CurrentLimit) ol).getValue();
                        if (value <= 0) {
                            LOGGER.warn("Invalid current limit {} for {}", value, ols.getId());
                        } else {
                            foundCurrentLimits = true;
                            switch (olt.getName()) {
                                case "PATL":
                                    cla.setPermanentLimit(value);
                                    break;
                                case "TATL":
                                    if (olt.getDirection() != cim1.model.OperationalLimitDirectionKind.absoluteValue) {
                                        throw new CIM1Exception("Direction not supported " + olt.getDirection());
                                    }
                                    int acceptableDuration = (int) olt.getAcceptableDuration();
                                    cla.beginTemporaryLimit()
                                            .setName(Integer.toString(acceptableDuration))
                                            .setValue(value)
                                            .setAcceptableDuration(acceptableDuration)
                                            .endTemporaryLimit();
                            }
                        }
                    }
                }
            }
            if (foundCurrentLimits) {
                cla.add();
            }
        }
    }

    private void createLine(Network network, cim1.model.ACLineSegment l, Set<String> noOperationalLimitInOperationalLimitSet) {
        cim1.model.Terminal t1 = l.getTerminals().get(0);
        cim1.model.Terminal t2 = l.getTerminals().get(1);
        // t1 and t2 respect sequenceNumber
        if (t1.getSequenceNumber() == 2) {
            cim1.model.Terminal t = t1;
            t1 = t2;
            t2 = t;
        }
        cim1.model.TopologicalNode tn1 = t1.getTopologicalNode();
        cim1.model.TopologicalNode tn2 = t2.getTopologicalNode();
        String voltageLevelId1 = namingStrategy.getId(tn1.getConnectivityNodeContainer());
        String voltageLevelId2 = namingStrategy.getId(tn2.getConnectivityNodeContainer());
        cim1.model.SvPowerFlow svpf1 = t1.getSvPowerFlow();
        cim1.model.SvPowerFlow svpf2 = t2.getSvPowerFlow();
        float r = l.getR();
        float x = l.getX();
        float b = l.getBch();
        float g = l.getGch();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Create line {} between bus {} and bus {}",
                    namingStrategy.getId(l), namingStrategy.getId(tn1), namingStrategy.getId(tn2));
        }
        final Line line = network.newLine()
                .setId(namingStrategy.getId(l))
                .setName(namingStrategy.getName(l))
                .setEnsureIdUnicity(false)
                .setBus1(t1.isConnected() ? namingStrategy.getId(tn1) : null)
                .setBus2(t2.isConnected() ? namingStrategy.getId(tn2) : null)
                .setConnectableBus1(namingStrategy.getId(tn1))
                .setConnectableBus2(namingStrategy.getId(tn2))
                .setVoltageLevel1(voltageLevelId1)
                .setVoltageLevel2(voltageLevelId2)
                .setR(r)
                .setX(x)
                .setG1(g / 2)
                .setG2(g / 2)
                .setB1(b / 2)
                .setB2(b / 2)
            .add();
        addTerminalMapping(tn1, line.getTerminal1());
        addTerminalMapping(tn2, line.getTerminal2());

        createCurrentLimits(t1, line::newCurrentLimits1, noOperationalLimitInOperationalLimitSet);
        createCurrentLimits(t2, line::newCurrentLimits2, noOperationalLimitInOperationalLimitSet);

        if (svpf1 != null) {
            line.getTerminal1().setP(svpf1.getP()).setQ(svpf1.getQ());
        }
        if (svpf2 != null) {
            line.getTerminal2().setP(svpf2.getP()).setQ(svpf2.getQ());
        }
    }

    /* xnode is on side 2 */
    private void createDanglingLine(Network network, cim1.model.ACLineSegment l,
                                    cim1.model.Terminal t1, cim1.model.Terminal t2,
                                    cim1.model.TopologicalNode tn1, cim1.model.TopologicalNode tn2,
                                    cim1.model.EnergyConsumer ec2,
                                    Set<String> noOperationalLimitInOperationalLimitSet) {
        assert isXNode(tn2) && !isXNode(tn1);
        String voltageLevelId1 = namingStrategy.getId(tn1.getConnectivityNodeContainer());
        VoltageLevel voltageLevel1 = network.getVoltageLevel(voltageLevelId1);
        boolean ect2isConnected = true;
        float p0 = 0;
        float q0 = 0;
        if (ec2 != null) {
            cim1.model.Terminal ect2 = ec2.getTerminals().get(0);
            cim1.model.SvPowerFlow ect2svpf = ect2.getSvPowerFlow();
            ect2isConnected = ect2.isConnected();
            p0 = ect2svpf.getP();
            q0 = ect2svpf.getQ();
        }
        cim1.model.SvPowerFlow svpf1 = t1.getSvPowerFlow();
        cim1.model.SvVoltage svv2 = tn2.getSvVoltage();
        float r = l.getR();
        float x = l.getX();
        float b = l.getBch();
        float g = l.getGch();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Create dangling line {} connected to bus {}", namingStrategy.getId(l), namingStrategy.getId(tn1));
        }
        boolean connected = t1.isConnected() && t2.isConnected() && ect2isConnected;
        DanglingLine dl = voltageLevel1.newDanglingLine()
                .setId(namingStrategy.getId(l))
                .setName(namingStrategy.getName(l))
                .setEnsureIdUnicity(false)
                .setBus(connected ? namingStrategy.getId(tn1) : null)
                .setConnectableBus(namingStrategy.getId(tn1))
                .setR(r)
                .setX(x)
                .setG(g)
                .setB(b)
                .setUcteXnodeCode(findUcteXnodeCode(tn2))
                .setP0(p0)
                .setQ0(q0)
            .add();
        addTerminalMapping(tn1, dl.getTerminal());

        createCurrentLimits(t1, dl::newCurrentLimits, noOperationalLimitInOperationalLimitSet);

        if (svpf1 != null) {
            dl.getTerminal().setP(svpf1.getP()).setQ(svpf1.getQ());
        }
        // for debug only
        if (svv2 != null) {
            dl.getProperties().setProperty(XNODE_V_PROPERTY, Float.toString(svv2.getV()));
            dl.getProperties().setProperty(XNODE_ANGLE_PROPERTY, Float.toString(svv2.getAngle()));
        }
    }

    /**
     * xnode is on side 2 of l1
     *
     *         l1       l2
     *    *---------*---------*
     *    t1       xnode      t2
     *    tn1                 tn2
     */
    private void createMergedLine(Network network,
                                  cim1.model.ACLineSegment l1, cim1.model.ACLineSegment l2,
                                  cim1.model.Terminal t1, cim1.model.TopologicalNode tn1,
                                  cim1.model.TopologicalNode xn,
                                  Set<String> noOperationalLimitInOperationalLimitSet) {

        cim1.model.Terminal t2;
        cim1.model.TopologicalNode tn2;
        if (l2.getTerminals().get(0).getTopologicalNode() == xn) {
            t2 = l2.getTerminals().get(1);
            tn2 = t2.getTopologicalNode();
        } else if (l2.getTerminals().get(1).getTopologicalNode() == xn) {
            t2 = l2.getTerminals().get(0);
            tn2 = t2.getTopologicalNode();
        } else {
            throw new AssertionError();
        }
        String voltageLevelId1 = namingStrategy.getId(tn1.getConnectivityNodeContainer());
        String voltageLevelId2 = namingStrategy.getId(tn2.getConnectivityNodeContainer());
        cim1.model.SvPowerFlow svpf1 = t1.getSvPowerFlow();
        cim1.model.SvPowerFlow svpf2 = t2.getSvPowerFlow();
        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Create merged line {} between bus {} and bus {}",
                    namingStrategy.getId(l1), namingStrategy.getId(tn1), namingStrategy.getId(xn));
        }
        Line line = network.newTieLine()
                .setId(namingStrategy.getId(l1) + " + " + namingStrategy.getId(l2))
                .setName(namingStrategy.getName(l1) + " + " + namingStrategy.getName(l2))
                .setEnsureIdUnicity(false)
                .setBus1(t1.isConnected() ? tn1.getId() : null)
                .setBus2(t2.isConnected() ? tn2.getId() : null)
                .setConnectableBus1(namingStrategy.getId(tn1))
                .setConnectableBus2(namingStrategy.getId(tn2))
                .setVoltageLevel1(voltageLevelId1)
                .setVoltageLevel2(voltageLevelId2)
                .line1().setId(namingStrategy.getId(l1))
                        .setName(namingStrategy.getName(l1))
                        .setR(l1.getR())
                        .setX(l1.getX())
                        .setG1(l1.getGch() / 2)
                        .setG2(l1.getGch() / 2)
                        .setB1(l1.getBch() / 2)
                        .setB2(l1.getBch() / 2)
                        .setXnodeP(0)
                        .setXnodeQ(0)
                .line2().setId(namingStrategy.getId(l2))
                        .setName(namingStrategy.getName(l2))
                        .setR(l2.getR())
                        .setX(l2.getX())
                        .setG1(l2.getGch() / 2)
                        .setG2(l2.getGch() / 2)
                        .setB1(l2.getBch() / 2)
                        .setB2(l2.getBch() / 2)
                        .setXnodeP(0)
                        .setXnodeQ(0)
                .setUcteXnodeCode(findUcteXnodeCode(xn))
            .add();
        addTerminalMapping(tn1, line.getTerminal1());
        addTerminalMapping(tn2, line.getTerminal2());

        createCurrentLimits(t1, line::newCurrentLimits1, noOperationalLimitInOperationalLimitSet);
        createCurrentLimits(t2, line::newCurrentLimits2, noOperationalLimitInOperationalLimitSet);

        if (svpf1 != null) {
            line.getTerminal1().setP(svpf1.getP()).setQ(svpf1.getQ());
        }
        if (svpf2 != null) {
            line.getTerminal2().setP(svpf2.getP()).setQ(svpf2.getQ());
        }
    }

    private void createLines(Network network, Set<String> noOperationalLimitInOperationalLimitSet) {
        for (cim1.model.ACLineSegment l : cimModel.getId_ACLineSegment().values()) {
            cim1.model.Terminal t1 = l.getTerminals().get(0);
            cim1.model.Terminal t2 = l.getTerminals().get(1);
            cim1.model.TopologicalNode tn1 = t1.getTopologicalNode();
            cim1.model.TopologicalNode tn2 = t2.getTopologicalNode();
            if (isXNode(tn1)) {
                if (boundaryXNodes.containsKey(tn1)) {
                    // side 1 of the line is connected to a XNODE. Only one load
                    // is connected to the XNODE => replace the line, the XNODE
                    // and the load by an IIDM dangling line
                    cim1.model.EnergyConsumer ec1 = boundaryXNodes.get(tn1);
                    createDanglingLine(network, l, t2, t1, tn2, tn1, ec1, noOperationalLimitInOperationalLimitSet);
                }
                    // nothing to do because merged line has already been created
                    // by test on tn2
            } else if (isXNode(tn2)) {
                if (boundaryXNodes.containsKey(tn2)) {
                    // side 2 of the line is connected to a XNODE. Only one load
                    // is connected to the XNODE => replace the line, the XNODE
                    // and the load by an IIDM dangling line
                    cim1.model.EnergyConsumer ec2 = boundaryXNodes.get(tn2);
                    createDanglingLine(network, l, t1, t2, tn1, tn2, ec2, noOperationalLimitInOperationalLimitSet);
                } else {
                    // side 2 of the line is connected to a XNODE. Another line
                    // is connected to the XNODE and no non null injection is
                    // connected to the XNODE => replace both lines and the XNODE
                    // by an IIDM line
                    List<cim1.model.ConductingEquipment> linesToMerge = mergedXNodes.get(tn2);
                    cim1.model.ACLineSegment l2 = (cim1.model.ACLineSegment) (linesToMerge.get(0) == l ? linesToMerge.get(1) : linesToMerge.get(0));
                    createMergedLine(network, l, l2, t1, tn1, tn2, noOperationalLimitInOperationalLimitSet);
                }
            } else {
                createLine(network, l, noOperationalLimitInOperationalLimitSet);
            }
        }
    }

    private static double getStepXforAsymmetrical(double xStepMin, double xStepMax,
                                                 double alpha, double alphaMax, double theta) {
        double numer = Math.sin(theta) - Math.tan(alphaMax) * Math.cos(theta);
        double denom = Math.sin(theta) - Math.tan(alpha) * Math.cos(theta);
        return xStepMin + (xStepMax - xStepMin)
                * Math.pow(Math.tan(alpha) / Math.tan(alphaMax) * numer / denom, 2);
    }

    private static double getStepXforSymmetrical(double xStepMin, double xStepMax,
                                                double alpha, double alphaMax) {
        return xStepMin + (xStepMax - xStepMin)
                * Math.pow(Math.sin(alpha / 2) / Math.sin(alphaMax / 2), 2);
    }

    private void createPhaseTapChanger(cim1.model.PhaseTapChanger ptc, cim1.model.Terminal t1, cim1.model.Terminal t2,
                                       TwoWindingsTransformer transfo) {
        int lowStep = ptc.getLowStep();
        int highStep = ptc.getHighStep();
        int neutralStep = ptc.getNeutralStep();
        if (neutralStep < lowStep || neutralStep > highStep) {
            throw new CIM1Exception("Malformed ratio tap changer: neutral step ("
                    + neutralStep + ") isn't between low (" + lowStep + ") and high ("
                    + highStep + ")");
        }
        int position = (int) ptc.svTapStep.getContinuousPosition();

        PhaseTapChangerAdder ptca = transfo.newPhaseTapChanger()
                .setLowTapPosition(lowStep)
                .setTapPosition(position);

        double du0 = ptc.neutralU / ptc.transformerWinding.ratedU;
        if (Math.abs(du0) > 0.5) {
            du0 = 0;
        }

        float du;
        if (ptc.voltageStepIncrementOutOfPhaseIsSet() && ptc.getVoltageStepIncrementOutOfPhase() != 0) {
            du = (config.isInvertVoltageStepIncrementOutOfPhase() ? -1 : 1) * ptc.getVoltageStepIncrementOutOfPhase() / ptc.getTransformerWinding().getRatedU();
        } else if (ptc.stepVoltageIncrementIsSet() && ptc.getStepVoltageIncrement() != 0) {
            du = ptc.getStepVoltageIncrement() / 100f;
        } else {
            LOGGER.warn("Phase tap changer '{}' of power transformer '{}'" +
                    " do not have a valid value for voltageStepIncrementOutOfPhase or " +
                    "stepVoltageIncrement attribute, default to 1",
                    ptc.getId(), transfo.getId());
            du = 1f / 100;
        }

        float theta;
        if (ptc.windingConnectionAngleIsSet()) {
            theta = (float) Math.toRadians(ptc.getWindingConnectionAngle());
        } else {
            theta = (float) Math.PI / 2;
            LOGGER.warn("Phase tap changer '{}' of power transformer '{}'" +
                    " do not have windingConnectionAngle attribute, default to 90",
                    ptc.getId(), transfo.getId());
        }

        float xStepMin = 0;
        float xStepMax = 0;
        if (ptc.xStepMinIsSet() && ptc.xStepMaxIsSet()) {
            xStepMin = ptc.xStepMin;
            xStepMax = ptc.xStepMax;
        }
        boolean xStepRangeIsInconsistent = false;
        if (xStepMin < 0 || xStepMax <= 0 || xStepMin > xStepMax) {
            xStepRangeIsInconsistent = true;
            LOGGER.info("xStepMin and xStepMax are inconsistents for transformer {}", transfo.getId());
        }

        List<Float> alphaList = new ArrayList<>();
        List<Float> rhoList = new ArrayList<>();
        switch (ptc.getPhaseTapChangerType()) {
            case asymmetrical: {
                for (int step = lowStep; step <= highStep; step++) {
                    int n = step - neutralStep;
                    double dx = (n * du - du0) * Math.cos(theta);
                    double dy = (n * du - du0) * Math.sin(theta);
                    float alpha = (float) Math.atan2(dy, 1 + dx);
                    float rho = (float) (1 / Math.hypot(dy, 1 + dx));
                    alphaList.add(alpha);
                    rhoList.add(rho);
                }
            }
            break;

            case symmetrical:
                if (ptc.stepPhaseShiftIncrementIsSet() && ptc.stepPhaseShiftIncrement != 0) {
                    for (int step = lowStep; step <= highStep; step++) {
                        int n = step - neutralStep;
                        float alpha = n * (float) Math.toRadians((config.isInvertVoltageStepIncrementOutOfPhase() ? -1 : 1) * ptc.stepPhaseShiftIncrement);
                        float rho = 1f;
                        alphaList.add(alpha);
                        rhoList.add(rho);
                    }
                } else {
                    for (int step = lowStep; step <= highStep; step++) {
                        int n = step - neutralStep;
                        double dy = (n * du / 2 - du0) * Math.sin(theta);
                        float alpha = (float) (2 * Math.asin(dy));
                        float rho = 1f;
                        alphaList.add(alpha);
                        rhoList.add(rho);
                    }
                }
                break;

            default:
                throw new AssertionError("Unexpected PhaseTapChangerKind value: " + ptc.getPhaseTapChangerType());
        }

        double alphaMax = alphaList.stream().mapToDouble(Float::doubleValue).max().getAsDouble();

        for (int i = 0; i < alphaList.size(); i++) {
            double alpha = (double) alphaList.get(i);
            double rho = (double) rhoList.get(i);
            double x;
            if (xStepRangeIsInconsistent || alphaMax == 0) {
                x = transfo.getX();
            } else {
                switch (ptc.getPhaseTapChangerType()) {
                    case asymmetrical:
                        x = getStepXforAsymmetrical(xStepMin, xStepMax, alpha, alphaMax, theta);
                        break;

                    case symmetrical:
                        x = getStepXforSymmetrical(xStepMin, xStepMax, alpha, alphaMax);
                        break;

                    default:
                        throw new AssertionError("Unexpected PhaseTapChangerKind value: " + ptc.getPhaseTapChangerType());
                }
            }
            ptca.beginStep()
                    .setAlpha(Math.toDegrees(alpha))
                    .setRho(rho)
                    .setR(0)
                    .setX((x - transfo.getX()) / transfo.getX() * 100)
                    .setG(0)
                    .setB(0)
                .endStep();
        }

        if (ptc.regulatingControlIsSet()) {
            cim1.model.RegulatingControl rc = ptc.getRegulatingControl();
            switch (rc.getMode()) {
                case currentFlow:
                    Terminal regulationTerminal;
                    if (rc.getTerminal() == t1) {
                        regulationTerminal = transfo.getTerminal1();
                    } else if (rc.getTerminal() == t2) {
                        regulationTerminal = transfo.getTerminal2();
                    } else {
                        regulationTerminal = getTerminalMapping(rc.getTerminal().getTopologicalNode());
                    }
                    ptca.setRegulationMode(PhaseTapChanger.RegulationMode.CURRENT_LIMITER)
                            .setRegulationValue(rc.getTargetValue())
                            .setRegulating(true)
                            .setRegulationTerminal(regulationTerminal);
                    break;

                case fixed:
                    break;

                default:
                    LOGGER.warn("Phase tap changer '{}' of power transformer '{}'" +
                                " has an unsupported regulating mode: {}",
                            ptc.getId(), transfo.getId(), rc.getMode());
            }
        }
        ptca.add();
    }

    private void createRatioTapChanger(cim1.model.RatioTapChanger rtc, Supplier<RatioTapChangerAdder> transfo,
                                       boolean rtcSide1, Map<cim1.model.Terminal, Terminal> terminals) {
        int lowStep = rtc.getLowStep();
        int highStep = rtc.getHighStep();
        int neutralStep = rtc.getNeutralStep();
        if (neutralStep < lowStep || neutralStep > highStep) {
            throw new CIM1Exception("Malformed ratio tap changer: neutral step ("
                    + neutralStep + ") isn't between low (" + lowStep + ") and high ("
                    + highStep + ")");
        }
        int position = (int) rtc.svTapStep.getContinuousPosition();

        RatioTapChangerAdder rtca = transfo.get()
                .setLowTapPosition(lowStep)
                .setTapPosition(position);

        if (LOGGER.isDebugEnabled() && !rtcSide1) {
            LOGGER.debug("RTC at side 2 deviation {}", rtc.getId());
        }
        for (int step = lowStep; step <= highStep; step++) {
            int n = step - neutralStep;
            float du = rtc.getStepVoltageIncrement() / 100;
            double rho = rtcSide1 ? 1 / (1 + n * du) : (1 + n * du);

            // Impedance/admittance deviation is required when ratio tap changer is defined at side 2
            double dz = 0;
            double dy = 0;
            if (!rtcSide1) {
                double rho2 = rho * rho;
                dz = (rho2 - 1) * 100;
                dy = (1 / rho2 - 1) * 100;
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("RTC at side 2 deviation: %4d  %12.8f  %12.8f  %12.8f", step, n * du, dz, dy));
                }
            }

            rtca.beginStep()
                    .setRho(rho)
                    .setR(dz)
                    .setX(dz)
                    .setG(dy)
                    .setB(dy)
                .endStep();
        }

        if (rtc.regulatingControlIsSet()) {
            cim1.model.RegulatingControl rc = rtc.getRegulatingControl();
            switch (rc.getMode()) {
                case voltage:
                    boolean regulating = true;
                    float targetV = rc.getTargetValue();
                    if (targetV <= 0) {
                        LOGGER.warn("Ratio tap changer '{}' of power transformer '{}'" +
                                        " has a bad target voltage {}, switch off regulation",
                                rtc.getId(), transfo.toString(), targetV);
                        regulating = false;
                        targetV = Float.NaN;
                    }
                    Terminal regulationTerminal = null;
                    for (Map.Entry<cim1.model.Terminal, Terminal> e : terminals.entrySet()) {
                        if (rc.getTerminal() == e.getKey()) {
                            regulationTerminal = e.getValue();
                        }
                    }
                    if (regulationTerminal == null) {
                        regulationTerminal = getTerminalMapping(rc.getTerminal().getTopologicalNode());
                    }
                    rtca.setLoadTapChangingCapabilities(true)
                            .setRegulating(regulating)
                            .setTargetV(targetV)
                            .setRegulationTerminal(regulationTerminal);
                    break;

                case fixed:
                    rtca.setLoadTapChangingCapabilities(false);
                    break;

                default:
                    rtca.setLoadTapChangingCapabilities(false);
                    LOGGER.warn("Ratio tap changer '{}' of power transformer '{}'" +
                                    " has an unsupported regulating mode: {}",
                            rtc.getId(), transfo.toString(), rc.getMode());
            }
        } else {
            rtca.setLoadTapChangingCapabilities(false);
        }
        rtca.add();
    }

    class RatioTapChangerToCreate {
        cim1.model.RatioTapChanger rtc;
        RatioTapChangerHolder transfo;
        boolean rtcSide1;
        Map<cim1.model.Terminal, Terminal> terminals;
        RatioTapChangerToCreate(cim1.model.RatioTapChanger rtc, RatioTapChangerHolder transfo, boolean rtcSide1, Map<cim1.model.Terminal, Terminal> terminals) {
            this.rtc = rtc;
            this.rtcSide1 = rtcSide1;
            this.transfo = transfo;
            this.terminals = terminals;
        }
    }

    private boolean isPrimary(cim1.model.TransformerWinding tw) {
        return tw.getWindingType().equals(cim1.model.WindingType.primary);
    }

    private void create2WTransfos(cim1.model.PowerTransformer pt,
                                  cim1.model.TransformerWinding tw1p,
                                  cim1.model.TransformerWinding tw2p,
                                  Network network,
                                  Set<String> noOperationalLimitInOperationalLimitSet,
                                  List<RatioTapChangerToCreate> ratioTapChangerToCreateList) {
        cim1.model.TransformerWinding tw1 = tw1p;
        cim1.model.TransformerWinding tw2 = tw2p;
        if (!isPrimary(tw1)) {
            cim1.model.TransformerWinding tw = tw1;
            tw1 = tw2;
            tw2 = tw;
        }
        cim1.model.Terminal t1 = tw1.getTerminals().get(0);
        cim1.model.Terminal t2 = tw2.getTerminals().get(0);
        cim1.model.TopologicalNode tn1 = t1.getTopologicalNode();
        cim1.model.TopologicalNode tn2 = t2.getTopologicalNode();
        cim1.model.VoltageLevel vl1 = (cim1.model.VoltageLevel) tn1.getConnectivityNodeContainer();
        cim1.model.VoltageLevel vl2 = (cim1.model.VoltageLevel) tn2.getConnectivityNodeContainer();

        // check that there is only one ratio tap changer
        TwoWindingsTransformer.Side rtcSide = null;
        cim1.model.RatioTapChanger rtc = null;
        cim1.model.PhaseTapChanger ptc = null;
        if (tw1.ratioTapChangerIsSet()) {
            if (tw2.ratioTapChangerIsSet()) {
                throw new CIM1Exception("Unsupported modelling: transformer with two ratio tap changer"
                        + pt.getId());
            }
            rtc = tw1.getRatioTapChanger();
            rtcSide = Branch.Side.ONE;
        } else {
            if (tw2.ratioTapChangerIsSet()) {
                rtc = tw2.getRatioTapChanger();
                rtcSide = Branch.Side.TWO;
            }
        }

        // check that there is only one phase tap changer
        TwoWindingsTransformer.Side ptcSide = null;
        if (tw1.phaseTapChangerIsSet()) {
            if (tw2.phaseTapChangerIsSet()) {
                throw new CIM1Exception("Unsupported modelling: transformer with two phase tap changer"
                        + pt.getId());
            }
            ptc = tw1.getPhaseTapChanger();
            ptcSide = Branch.Side.ONE;
        } else {
            if (tw2.phaseTapChangerIsSet()) {
                ptc = tw2.getPhaseTapChanger();
                ptcSide = Branch.Side.TWO;
            }
        }

        if (rtcSide != null && ptcSide != null && rtcSide != ptcSide) {
            throw new CIM1Exception("Unsupported modelling: transformer with ratio and tap changer not on the same winding "
                    + pt.getId());
        }

        if (isXNode(tn1)) {
            throw new CIM1Exception("Not supported: transformer '" + pt.getId() + "' connected to  XNODE "
                    + tn1.getId() + "(" + findUcteXnodeCode(tn1) + ")");
        } else if (isXNode(tn2)) {
            throw new CIM1Exception("Not supported: transformer '" + pt.getId() + "' connected to  XNODE "
                    + tn2.getId() + "(" + findUcteXnodeCode(tn2) + ")");
        } else {
            // IIDM 2 windings transformer modelling: impedances are specfied at
            // side 2, in this case at the secondary voltage side.
            float rho0 = tw2.getRatedU() / tw1.getRatedU();
            float rho0Square = rho0 * rho0;
            float r0 = tw1.getR() * rho0Square + tw2.getR();
            float x0 = tw1.getX() * rho0Square + tw2.getX();
            float g0 = tw1.getG() / rho0Square + tw2.getG();
            float b0 = tw1.getB() / rho0Square + tw2.getB();

            VoltageLevel voltageLevel1 = network.getVoltageLevel(namingStrategy.getId(vl1));
            Substation substation = voltageLevel1.getSubstation();
            TwoWindingsTransformer transfo = substation.newTwoWindingsTransformer()
                    .setId(namingStrategy.getId(pt))
                    .setName(namingStrategy.getName(pt))
                    .setEnsureIdUnicity(false)
                    .setR(r0)
                    .setX(x0)
                    .setG(g0)
                    .setB(b0)
                    .setRatedU1(tw1.getRatedU())
                    .setBus1(t1.isConnected() ? namingStrategy.getId(tn1) : null)
                    .setConnectableBus1(namingStrategy.getId(tn1))
                    .setVoltageLevel1(namingStrategy.getId(vl1))
                    .setRatedU2(tw2.getRatedU())
                    .setBus2(t2.isConnected() ? namingStrategy.getId(tn2) : null)
                    .setConnectableBus2(namingStrategy.getId(tn2))
                    .setVoltageLevel2(namingStrategy.getId(vl2))
                 .add();
            addTerminalMapping(tn1, transfo.getTerminal1());
            addTerminalMapping(tn2, transfo.getTerminal2());

            if (rtc != null) {
                ratioTapChangerToCreateList.add(new RatioTapChangerToCreate(rtc, transfo, rtcSide == Branch.Side.ONE,
                        ImmutableMap.of(t1, transfo.getTerminal1(),
                                        t2, transfo.getTerminal2())));
            }
            if (ptc != null) {
                createPhaseTapChanger(ptc, t1, t2, transfo);
            }

            createCurrentLimits(t1, transfo::newCurrentLimits1, noOperationalLimitInOperationalLimitSet);
            createCurrentLimits(t2, transfo::newCurrentLimits2, noOperationalLimitInOperationalLimitSet);

            cim1.model.SvPowerFlow svpf1 = t1.getSvPowerFlow();
            cim1.model.SvPowerFlow svpf2 = t2.getSvPowerFlow();
            if (svpf1 != null) {
                transfo.getTerminal1().setP(svpf1.getP()).setQ(svpf1.getQ());
            }
            if (svpf2 != null) {
                transfo.getTerminal2().setP(svpf2.getP()).setQ(svpf2.getQ());
            }
        }
    }

    private void create3WTransfos(cim1.model.PowerTransformer pt,
                                  cim1.model.TransformerWinding tw1,
                                  cim1.model.TransformerWinding tw2,
                                  cim1.model.TransformerWinding tw3,
                                  Network network,
                                  Set<String> noOperationalLimitInOperationalLimitSet,
                                  List<RatioTapChangerToCreate> ratioTapChangerToCreateList) {
        // side 1 is the high voltage, side 2 is the medium voltage, side 3
        // is the low voltage
        if (tw1.getRatedU() < tw2.getRatedU()) {
            throw new IllegalStateException("ratedU1 < ratedU2");
        }
        if (tw2.getRatedU() < tw3.getRatedU()) {
            throw new IllegalStateException("ratedU2 < ratedU3");
        }
        cim1.model.Terminal t1 = tw1.getTerminals().get(0);
        cim1.model.Terminal t2 = tw2.getTerminals().get(0);
        cim1.model.Terminal t3 = tw3.getTerminals().get(0);
        cim1.model.TopologicalNode tn1 = t1.getTopologicalNode();
        cim1.model.TopologicalNode tn2 = t2.getTopologicalNode();
        cim1.model.TopologicalNode tn3 = t3.getTopologicalNode();
        cim1.model.VoltageLevel vl1 = (cim1.model.VoltageLevel) tn1.getConnectivityNodeContainer();
        cim1.model.VoltageLevel vl2 = (cim1.model.VoltageLevel) tn2.getConnectivityNodeContainer();
        cim1.model.VoltageLevel vl3 = (cim1.model.VoltageLevel) tn3.getConnectivityNodeContainer();

        if (isXNode(tn1) || isXNode(tn2) || isXNode(tn3)) {
            throw new CIM1Exception("XNODE connected to a 3 windings transformer not supported");
        }

        if (tw1.ratioTapChangerIsSet()) {
            LOGGER.warn("Power transformer " + pt.getId() + ": ratio tap changer on primary winding is not supported");
        }
        if (tw1.phaseTapChangerIsSet() || tw2.phaseTapChangerIsSet() || tw3.phaseTapChangerIsSet()) {
            throw new CIM1Exception("Power transformer " + pt.getId() + ": phase tap changers on three windings transformer are not allowed");
        }
        cim1.model.RatioTapChanger rtc2 = tw2.getRatioTapChanger();
        cim1.model.RatioTapChanger rtc3 = tw3.getRatioTapChanger();

        VoltageLevel voltageLevel1 = network.getVoltageLevel(namingStrategy.getId(vl1));
        Substation substation = voltageLevel1.getSubstation();
        float ratedU1 = tw1.getRatedU();
        float ratedU2 = tw2.getRatedU();
        float ratedU3 = tw3.getRatedU();
        float rho2Square = (float) Math.pow(tw2.getRatedU() / tw1.getRatedU(), 2);
        float rho3Square = (float) Math.pow(tw3.getRatedU() / tw1.getRatedU(), 2);
        float r1 = tw1.getR();
        float x1 = tw1.getX();
        float g1 = tw1.getG() + tw2.getG() * rho2Square + tw3.getG() * rho3Square;
        float b1 = tw1.getB() + tw2.getB() * rho2Square + tw3.getB() * rho3Square;
        float r2 = tw2.getR() / rho2Square;
        float x2 = tw2.getX() / rho2Square;
        float r3 = tw3.getR() / rho3Square;
        float x3 = tw3.getX() / rho3Square;
        ThreeWindingsTransformer transfo = substation.newThreeWindingsTransformer()
                .setId(namingStrategy.getId(pt))
                .setName(namingStrategy.getName(pt))
                .setEnsureIdUnicity(false)
                .newLeg1()
                    .setR(r1)
                    .setX(x1)
                    .setG(g1)
                    .setB(b1)
                    .setRatedU(ratedU1)
                    .setVoltageLevel(namingStrategy.getId(vl1))
                    .setBus(t1.isConnected() ? namingStrategy.getId(tn1) : null)
                    .setConnectableBus(namingStrategy.getId(tn1))
                .add()
                .newLeg2()
                    .setR(r2)
                    .setX(x2)
                    .setRatedU(ratedU2)
                    .setVoltageLevel(namingStrategy.getId(vl2))
                    .setBus(t2.isConnected() ? namingStrategy.getId(tn2) : null)
                    .setConnectableBus(namingStrategy.getId(tn2))
                .add()
                .newLeg3()
                    .setR(r3)
                    .setX(x3)
                    .setRatedU(ratedU3)
                    .setVoltageLevel(namingStrategy.getId(vl3))
                    .setBus(t3.isConnected() ? namingStrategy.getId(tn3) : null)
                    .setConnectableBus(namingStrategy.getId(tn3))
                .add()
            .add();
        addTerminalMapping(tn1, transfo.getLeg1().getTerminal());
        addTerminalMapping(tn2, transfo.getLeg2().getTerminal());
        addTerminalMapping(tn3, transfo.getLeg3().getTerminal());

        if (rtc2 != null) {
            ratioTapChangerToCreateList.add(new RatioTapChangerToCreate(rtc2, transfo.getLeg2(), false,
                    ImmutableMap.of(t2, transfo.getLeg2().getTerminal()))); // TODO true/false?
        }
        if (rtc3 != null) {
            ratioTapChangerToCreateList.add(new RatioTapChangerToCreate(rtc3, transfo.getLeg3(), false,
                    ImmutableMap.of(t3, transfo.getLeg3().getTerminal()))); // TODO true/false?
        }

        createCurrentLimits(t1, transfo.getLeg1()::newCurrentLimits, noOperationalLimitInOperationalLimitSet);
        createCurrentLimits(t2, transfo.getLeg2()::newCurrentLimits, noOperationalLimitInOperationalLimitSet);
        createCurrentLimits(t3, transfo.getLeg3()::newCurrentLimits, noOperationalLimitInOperationalLimitSet);

        cim1.model.SvPowerFlow svpf1 = t1.getSvPowerFlow();
        if (svpf1 != null) {
            transfo.getLeg1().getTerminal().setP(svpf1.getP()).setQ(svpf1.getQ());
        }
        cim1.model.SvPowerFlow svpf2 = t2.getSvPowerFlow();
        if (svpf2 != null) {
            transfo.getLeg2().getTerminal().setP(svpf2.getP()).setQ(svpf2.getQ());
        }
        cim1.model.SvPowerFlow svpf3 = t3.getSvPowerFlow();
        if (svpf3 != null) {
            transfo.getLeg3().getTerminal().setP(svpf3.getP()).setQ(svpf3.getQ());
        }
    }

    private void createTransfos(Network network, Set<String> noOperationalLimitInOperationalLimitSet, List<RatioTapChangerToCreate> ratioTapChangerToCreateList) {
        for (cim1.model.PowerTransformer pt : cimModel.getId_PowerTransformer().values()) {
            LOGGER.trace("Create power transformer {}", namingStrategy.getId(pt));

            List<cim1.model.TransformerWinding> windings = pt.getContains_TransformerWindings();
            if (windings.size() == 2) {
                cim1.model.TransformerWinding tw1 = windings.get(0);
                cim1.model.TransformerWinding tw2 = windings.get(1);

                create2WTransfos(pt, tw1, tw2, network, noOperationalLimitInOperationalLimitSet, ratioTapChangerToCreateList);

            } else if (windings.size() == 3) {
                List<cim1.model.TransformerWinding> sortedWindings = new ArrayList<>(3);
                sortedWindings.add(windings.get(0));
                sortedWindings.add(windings.get(1));
                sortedWindings.add(windings.get(2));
                Collections.sort(sortedWindings, new Comparator<cim1.model.TransformerWinding>() {
                    @Override
                    public int compare(cim1.model.TransformerWinding tw1, cim1.model.TransformerWinding tw2) {
                        return (int) (tw2.getRatedU() - tw1.getRatedU());
                    }
                });
                cim1.model.TransformerWinding tw1 = sortedWindings.get(0);
                cim1.model.TransformerWinding tw2 = sortedWindings.get(1);
                cim1.model.TransformerWinding tw3 = sortedWindings.get(2);

                create3WTransfos(pt, tw1, tw2, tw3, network, noOperationalLimitInOperationalLimitSet, ratioTapChangerToCreateList);

            } else {
                throw new CIM1Exception("Inconsistent power transformer found ("
                        + pt.getId() + "): only 2 or 3 windings are supported");
            }
        }
    }

    private void createShunt(VoltageLevel voltageLevel, cim1.model.ShuntCompensator sc) {
        LOGGER.trace("Create shunt compensator {}", namingStrategy.getId(sc));

        cim1.model.Terminal t = sc.getTerminals().get(0);
        cim1.model.TopologicalNode tn = t.getTopologicalNode();

        int sectionCount = (int) sc.getSvShuntCompensatorSections().getContinuousSections();
        sectionCount = Math.abs(sectionCount); // RTE Convergence CIM export bug (SVC)
        float bPerSection = sc.getBPerSection();
        if (bPerSection == 0) {
            bPerSection = Float.MIN_VALUE;
            LOGGER.warn("Fix {} susceptance per section: 0 -> {}", sc.getId(), bPerSection);
        }

        ShuntCompensator shunt = voltageLevel.newShuntCompensator()
                .setId(namingStrategy.getId(sc))
                .setName(namingStrategy.getName(sc))
                .setEnsureIdUnicity(false)
                .setBus(t.isConnected() ? namingStrategy.getId(tn) : null)
                .setConnectableBus(namingStrategy.getId(tn))
                .setCurrentSectionCount(sectionCount)
                .setbPerSection(bPerSection)
                .setMaximumSectionCount(Math.max(sc.getMaximumSections(), sectionCount))
            .add();
        addTerminalMapping(tn, shunt.getTerminal());

        cim1.model.SvPowerFlow svfp = t.getSvPowerFlow();
        if (svfp != null) {
            shunt.getTerminal().setQ(svfp.getQ());
        }
    }

    private void createLoad(VoltageLevel voltageLevel, cim1.model.EnergyConsumer ec) {
        cim1.model.Terminal t = ec.getTerminals().get(0);
        cim1.model.TopologicalNode tn = t.getTopologicalNode();

        // We take P and Q from SvPowerFlow as nominal P and nominal Q,
        // we don't deal with LoadResponseCharacteristic
        float p = 0;
        float q = 0;
        cim1.model.SvPowerFlow svpf = t.getSvPowerFlow();
        if (svpf != null) {
            p = svpf.getP();
            q = svpf.getQ();
        } else {
            LOGGER.warn("No active and reactive power value for load {}", ec.getId());
        }

        LoadType loadType = ec.getId().contains("fict") ? LoadType.FICTITIOUS : LoadType.UNDEFINED;

        LOGGER.trace("Create load {}", namingStrategy.getId(ec));

        Load load = voltageLevel.newLoad()
                .setId(namingStrategy.getId(ec))
                .setName(namingStrategy.getName(ec))
                .setEnsureIdUnicity(false)
                .setBus(t.isConnected() ? namingStrategy.getId(tn) : null)
                .setConnectableBus(namingStrategy.getId(tn))
                .setP0(p)
                .setQ0(q)
                .setLoadType(loadType)
            .add();
        addTerminalMapping(tn, load.getTerminal());
        if (svpf != null) {
            load.getTerminal().setP(p).setQ(q);
        }
    }

    private static EnergySource getEnergySource(cim1.model.GeneratingUnit gu) {
        EnergySource es = EnergySource.OTHER;
        if (gu instanceof cim1.model.HydroGeneratingUnit) {
            es = EnergySource.HYDRO;
        } else if (gu instanceof cim1.model.NuclearGeneratingUnit) {
            es = EnergySource.NUCLEAR;
        } else if (gu instanceof cim1.model.ThermalGeneratingUnit) {
            es = EnergySource.THERMAL;
        } else if (gu instanceof cim1.model.WindGeneratingUnit) {
            es = EnergySource.WIND;
        }
        return es;
    }

    private void createReactiveCapabilityCurve(Generator generator, cim1.model.SynchronousMachine sm, List<String> synchronousMachinesWithReactiveRangeForMinus9999MW) {
        if (sm.initialReactiveCapabilityCurveIsSet()) {
            Map<Float, cim1.model.CurveData> cdMap = new HashMap<>();
            for (cim1.model.CurveData cd : sm.initialReactiveCapabilityCurve.getCurveScheduleDatas()) {
                if (cdMap.containsKey(cd.getXvalue())) {
                    LOGGER.warn("Duplicated data for x value {} of {} reactive capability curve",
                            cd.getXvalue(), generator.getId());
                    continue;
                }
                if (cd.getXvalue() == -9999f) { // CVG bug
                    synchronousMachinesWithReactiveRangeForMinus9999MW.add(sm.getId());
                } else {
                    cdMap.put(cd.getXvalue(), cd);
                }
            }
            if (!cdMap.isEmpty()) {
                if (cdMap.size() == 1) {
                    // there is just one value of minQ and maxQ
                    cim1.model.CurveData cd = cdMap.values().iterator().next();
                    generator.newMinMaxReactiveLimits()
                            .setMinQ(cd.getY1value())
                            .setMaxQ(cd.getY2value())
                        .add();
                } else {
                    ReactiveCapabilityCurveAdder rcca = generator.newReactiveCapabilityCurve();
                    for (cim1.model.CurveData cd : cdMap.values()) {
                        rcca.beginPoint()
                                .setP(cd.getXvalue())
                                .setMinQ(cd.getY1value())
                                .setMaxQ(cd.getY2value())
                            .endPoint();
                    }
                    rcca.add();
                }
            }
        } else {
            // 4 points diagram with minQ and maxQ
            generator.newMinMaxReactiveLimits()
                    .setMinQ(sm.getMinQ())
                    .setMaxQ(sm.getMaxQ())
                .add();
        }
    }

    private void createGenerator(VoltageLevel voltageLevel, cim1.model.SynchronousMachine sm,
                                 List<String> synchronousMachinesWithoutRegulatingControl,
                                 List<String> synchronousMachinesRegulatingVoltageWithZeroTargetVoltage,
                                 List<String> synchronousMachinesWithReactiveRangeForMinus9999MW) {
        cim1.model.GeneratingUnit gu = sm.getMemberOf_GeneratingUnit();
        cim1.model.Terminal t = sm.getTerminals().get(0);
        cim1.model.TopologicalNode tn = t.getTopologicalNode();

        LOGGER.trace("Create generator {}", namingStrategy.getId(sm));

        float p = 0;
        float q = 0;
        cim1.model.SvPowerFlow svpf = t.getSvPowerFlow();
        if (svpf != null) {
            p = svpf.getP();
            q = svpf.getQ();
        } else {
            LOGGER.warn("No SvPowerFlow for synchronous machine {}", sm.getId());
        }

        EnergySource es = getEnergySource(gu);

        float minP = gu.getMinOperatingP();
        float maxP = gu.getMaxOperatingP();
//        if (minP == maxP) {
//            maxP = minP + EPSILON;
//            if (LOGGER.isWarnEnabled()) {
//                LOGGER.warn("minP = {} maxP = {} for GeneratingUnit, setting maxP to {}",
//                        minP, gu.getId(), maxP);
//            }
//        }
        boolean voltageRegulatorOn = false;
        float targetP = -p;
        float targetQ = -q;
        double targetV = Double.NaN;
        Terminal regulatingTerminal = null;
        cim1.model.RegulatingControl rc = sm.getRegulatingControl();
        if (rc != null) {
            if (rc.getMode() == cim1.model.RegulatingControlModeKind.voltage) {
                voltageRegulatorOn = true;
                targetV = rc.getTargetValue();
                if (targetV == 0) {
                    targetV = voltageLevel.getNominalV();
                    synchronousMachinesRegulatingVoltageWithZeroTargetVoltage.add(namingStrategy.getId(sm));
                }
            } else if (rc.getMode() == cim1.model.RegulatingControlModeKind.reactivePower) {
                targetQ = rc.getTargetValue();
            } else {
                LOGGER.warn("Incorrect regulating control mode {} for synchronous machine {}",
                        rc.getMode(), sm.getId());
            }
            if (rc.getTerminal() != t) {
                regulatingTerminal = getTerminalMapping(rc.getTerminal().getTopologicalNode());
            }
        } else {
            synchronousMachinesWithoutRegulatingControl.add(sm.getId());
        }
        Generator generator = voltageLevel.newGenerator()
                .setId(namingStrategy.getId(sm))
                .setName(namingStrategy.getName(gu))
                .setEnsureIdUnicity(false)
                .setBus(t.isConnected() ? namingStrategy.getId(tn) : null)
                .setConnectableBus(namingStrategy.getId(tn))
                .setEnergySource(es)
                .setMinP(minP)
                .setMaxP(maxP)
                .setVoltageRegulatorOn(voltageRegulatorOn)
                .setRegulatingTerminal(regulatingTerminal)
                .setTargetP(targetP)
                .setTargetQ(targetQ)
                .setTargetV(targetV)
                .setRatedS(sm.getRatedS() > 0 ? sm.getRatedS() : Float.NaN)
            .add();
        addTerminalMapping(tn, generator.getTerminal());

        if (svpf != null) {
            generator.getTerminal().setP(p).setQ(q);
        }

        createReactiveCapabilityCurve(generator, sm, synchronousMachinesWithReactiveRangeForMinus9999MW);
    }

    private void createSwitch(VoltageLevel vl, cim1.model.Switch sw) {
        cim1.model.Terminal t1 = sw.getTerminals().get(0);
        cim1.model.Terminal t2 = sw.getTerminals().get(1);
        cim1.model.TopologicalNode tn1 = t1.getTopologicalNode();
        cim1.model.TopologicalNode tn2 = t2.getTopologicalNode();
        cim1.model.VoltageLevel vl1 = (cim1.model.VoltageLevel) tn1.getConnectivityNodeContainer();
        cim1.model.VoltageLevel vl2 = (cim1.model.VoltageLevel) tn2.getConnectivityNodeContainer();
        if (!vl1.getId().equals(vl.getId()) || !vl2.getId().equals(vl.getId())) {
            lowImpedanceLines.add(sw);
        } else {
            vl.getBusBreakerView().newSwitch()
                    .setId(namingStrategy.getId(sw))
                    .setName(namingStrategy.getName(sw))
                    .setEnsureIdUnicity(false)
                    .setBus1(namingStrategy.getId(tn1))
                    .setBus2(namingStrategy.getId(tn2))
                    .setOpen(!t1.isConnected() || !t2.isConnected())
                .add();
        }
    }

    private void createLowImpedanceLines(Network n) {
        for (cim1.model.Switch sw : lowImpedanceLines) {
            cim1.model.VoltageLevel vl = (cim1.model.VoltageLevel) sw.getMemberOf_EquipmentContainer();
            cim1.model.Terminal t1 = sw.getTerminals().get(0);
            cim1.model.Terminal t2 = sw.getTerminals().get(1);
            cim1.model.TopologicalNode tn1 = t1.getTopologicalNode();
            cim1.model.TopologicalNode tn2 = t2.getTopologicalNode();
            cim1.model.VoltageLevel vl1 = (cim1.model.VoltageLevel) tn1.getConnectivityNodeContainer();
            cim1.model.VoltageLevel vl2 = (cim1.model.VoltageLevel) tn2.getConnectivityNodeContainer();
            LOGGER.warn("Switch '{}' is connected to a terminal not in the same voltage level '{}' (side 1: '{}', side 2: '{}') => create a low impedance line",
                    sw.getId(), vl.getId(), vl1.getId(), vl2.getId());
            Line line = n.newLine()
                    .setId(namingStrategy.getId(sw))
                    .setName(namingStrategy.getName(sw))
                    .setEnsureIdUnicity(false)
                    .setVoltageLevel1(namingStrategy.getId(vl1))
                    .setVoltageLevel2(namingStrategy.getId(vl2))
                    .setBus1(t1.isConnected() ? namingStrategy.getId(tn1) : null)
                    .setConnectableBus1(namingStrategy.getId(tn1))
                    .setBus2(t2.isConnected() ? namingStrategy.getId(tn2) : null)
                    .setConnectableBus2(namingStrategy.getId(tn2))
                    .setR(0.05)
                    .setX(0.05)
                    .setG1(0)
                    .setB1(0)
                    .setG2(0)
                    .setB2(0)
                    .add();
            addTerminalMapping(tn1, line.getTerminal1());
            addTerminalMapping(tn2, line.getTerminal2());
        }
    }

    public static Country getCountryFromSubregionName(String name) {
        Objects.requireNonNull(name);
        Country country = null;
        switch (name) {
            case "NO1":
            case "NO2":
            case "NO3":
            case "NO4":
            case "NO5":
                country = Country.NO;
                break;
            case "SE1":
            case "SE2":
            case "SE3":
            case "SE4":
                country = Country.SE;
                break;
            case "FI1":
                country = Country.FI;
                break;
            case "DK1":
            case "DK2":
                country = Country.DK;
                break;
            case "EE1":
                country = Country.EE;
                break;
            case "LV1":
                country = Country.LV;
                break;
            case "LT1":
                country = Country.LT;
                break;
        }
        return country;
    }

    Network convert() {
        LOGGER.trace("Converting CIM model to IIDM model");

        for (cim1.model.IEC61970CIMVersion v : cimModel.getId_IEC61970CIMVersion().values()) {
            if (!CIM_ENTSOE_PROFILE_1ST_EDITION_VERSION.equals(v.version)) {
                throw new CIM1Exception("CIM version " + v.version
                        + " is incorrect, the official version of CIM ENTOSO-E profile 1st edition is "
                        + CIM_ENTSOE_PROFILE_1ST_EDITION_VERSION);
            }
        }

        EntsoeFileName entsoeFileName = EntsoeFileName.parse(fileName);

        Network network = NetworkFactory.create(fileName, FORMAT);
        network.setCaseDate(entsoeFileName.getDate());
        network.setForecastDistance(entsoeFileName.getForecastDistance());

        // Ends of transformers need to be in the same substation in the IIDM model, so check that a mapping is
        // not needed
        UndirectedGraph<String, Object> graph = new Pseudograph<>(Object.class);
        for (cim1.model.Substation s : cimModel.getId_Substation().values()) {
            graph.addVertex(namingStrategy.getId(s));
        }
        for (cim1.model.PowerTransformer pt : cimModel.getId_PowerTransformer().values()) {
            List<String> substationsIds = new ArrayList<>();
            for (cim1.model.TransformerWinding tw : pt.getContains_TransformerWindings()) {
                cim1.model.Terminal t = tw.terminals.get(0);
                cim1.model.TopologicalNode tn = t.getTopologicalNode();
                if (!isXNode(tn)) {
                    cim1.model.VoltageLevel vl = (cim1.model.VoltageLevel) tn.getConnectivityNodeContainer();
                    cim1.model.Substation s = vl.getMemberOf_Substation();
                    substationsIds.add(namingStrategy.getId(s));
                }
            }
            if (substationsIds.size() > 1) {
                for (int i = 1; i < substationsIds.size(); i++) {
                    graph.addEdge(substationsIds.get(0), substationsIds.get(i));
                }
            }
        }
        Map<String, String> substationIdMapping = new HashMap<>();
        new ConnectivityInspector<>(graph).connectedSets().stream().filter(substationIds -> substationIds.size() > 1).forEach(substationIds -> {
            String selectedSubstationId = substationIds.stream()
                    .filter(substationId -> !config.getSubstationIdExcludedFromMapping().stream()
                                                                                        .anyMatch(pattern -> substationId.matches(pattern)))
                    .sorted()
                    .findFirst()
                    .orElse(substationIds.iterator().next());
            for (String substationId : substationIds) {
                if (!substationId.equals(selectedSubstationId)) {
                    substationIdMapping.put(substationId, selectedSubstationId);
                }
            }
        });

        if (!substationIdMapping.isEmpty()) {
            LOGGER.warn("Substation id mapping needed for {} substations: {}", substationIdMapping.size(), substationIdMapping);
        }

        for (cim1.model.TopologicalNode tn : cimModel.getId_TopologicalNode().values()) {
            if (isXNode(tn)) {
                List<cim1.model.ConductingEquipment> branches = new ArrayList<>(2);
                List<cim1.model.EnergyConsumer> loads = new ArrayList<>(1);
                List<cim1.model.ConductingEquipment> injections = new ArrayList<>(1);
                List<String> xNodeTopo = new ArrayList<>();
                for (cim1.model.Terminal t : tn.getTerminal()) {
                    xNodeTopo.add(namingStrategy.getId(t.getConductingEquipment()));
                    if (t.getConductingEquipment() instanceof cim1.model.ACLineSegment) {
                        branches.add(t.getConductingEquipment());
                    } else if (t.getConductingEquipment() instanceof cim1.model.TransformerWinding) {
                        cim1.model.TransformerWinding tw = (cim1.model.TransformerWinding) t.getConductingEquipment();
                        if (tw.getMemberOf_PowerTransformer().getContains_TransformerWindings().size() != 2) {
                            throw new CIM1Exception("XNODE connected to a 3 windings transformer not supported");
                        }
                        branches.add(tw);
                    } else if (t.getConductingEquipment() instanceof cim1.model.EnergyConsumer
                            || t.getConductingEquipment() instanceof cim1.model.ShuntCompensator
                            || t.getConductingEquipment() instanceof cim1.model.SynchronousMachine) {
                        injections.add(t.getConductingEquipment());
                        if (t.getConductingEquipment() instanceof cim1.model.EnergyConsumer) {
                            loads.add((cim1.model.EnergyConsumer) t.getConductingEquipment());
                        }
                    } else {
                        throw new CIM1Exception(t.getConductingEquipment().getClass()
                                + " equipments cannot be connected to a merged XNODE ("
                                + tn.getId() + ")");
                    }
                }
                if (!branches.isEmpty() || !injections.isEmpty()) {
                    if (branches.size() == 2) {
                        if (branches.get(0) instanceof cim1.model.TransformerWinding
                                || branches.get(1) instanceof cim1.model.TransformerWinding) {
                            throw new CIM1Exception("Merged XNODE with transformers not supported");
                        }
                        LOGGER.trace("Found merged XNODE {} ({})", tn.getId(), findUcteXnodeCode(tn));
                        mergedXNodes.put(tn, branches);
                        if (!injections.isEmpty()) {
                            for (cim1.model.ConductingEquipment inj : injections) {
                                cim1.model.SvPowerFlow f = inj.getTerminals().get(0).getSvPowerFlow();
                                if (f != null) {
                                    if (f.getP() != 0 || f.getQ() != 0) {
                                        LOGGER.warn("A non null injection (p={}, q={}) should not be connected to a merged XNODE {} ({})",
                                                f.getP(), f.getQ(), tn.getId(), findUcteXnodeCode(tn));
                                    }
                                }
                            }
                        }
                    } else if (branches.size() == 1) {
                        if (loads.isEmpty()) {
                            LOGGER.trace("Found boundary XNODE {} ({})", tn.getId(), findUcteXnodeCode(tn));
                            boundaryXNodes.put(tn, null);
                            if (!injections.isEmpty()) {
                                LOGGER.warn("Strange boundary XNODE topology {}", xNodeTopo);
                            }
                        } else if (loads.size() == 1) {
                            LOGGER.trace("Found boundary XNODE {} ({})", tn.getId(), findUcteXnodeCode(tn));
                            boundaryXNodes.put(tn, loads.get(0));
                            if (injections.size() > 1) {
                                LOGGER.warn("Strange boundary XNODE topology {}", xNodeTopo);
                            }
                        } else {
                            throw new CIM1Exception("Unsupported XNODE " + tn.getId() + " ("
                                + findUcteXnodeCode(tn) + ") topology: " + xNodeTopo);
                        }
                    } else {
                        throw new CIM1Exception("Unsupported XNODE " + tn.getId() + " ("
                                + findUcteXnodeCode(tn) + ") topology: " + xNodeTopo);
                    }
                }
            }
        }

        List<String> synchronousMachinesWithoutRegulatingControl = new ArrayList<>();
        List<String> synchronousMachinesRegulatingVoltageWithZeroTargetVoltage = new ArrayList<>();
        List<String> synchronousMachinesWithReactiveRangeForMinus9999MW = new ArrayList<>();
        Set<String> noOperationalLimitInOperationalLimitSet = new LinkedHashSet<>();
        List<String> substationsNotAssociatedToValidCountry = new ArrayList<>();

        Multimap<VoltageLevel, cim1.model.SynchronousMachine> synchronousMachinesToAdd = HashMultimap.create();

        // Substations
        for (cim1.model.Substation s : cimModel.getId_Substation().values()) {
            cim1.model.SubGeographicalRegion sgr = s.getRegion();
            cim1.model.GeographicalRegion gr = sgr.getRegion();

            // Country
            Country country = null;
            if (gr.getName() != null) {
                if (gr.getName().equals("D1")
                        || gr.getName().equals("D2")
                        || gr.getName().equals("D4")
                        || gr.getName().equals("D7")
                        || gr.getName().equals("D8")) {
                    country = Country.DE;
                } else {
                    try {
                        country = Country.valueOf(gr.getName());
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            if ((country == null) && (sgr.getName() != null)) {
                country = getCountryFromSubregionName(sgr.getName());
            }
            if (country == null) {
                substationsNotAssociatedToValidCountry.add(s.getId());
                country = config.getDefaultCountry();
            }

            // Region
            String regionId = namingStrategy.getId(sgr);

            // Substation
            String newSubstationId = substationIdMapping.get(namingStrategy.getId(s));
            String substationId;
            String substationName;
            if (newSubstationId == null) {
                substationId = namingStrategy.getId(s);
                substationName = s.getName();
            } else {
                cim1.model.Substation newS = cimModel.getId_Substation().get(namingStrategy.getCimId(newSubstationId));
                substationId = namingStrategy.getId(newS);
                substationName = newS.getName();
            }
            Substation substation = network.getSubstation(substationId);
            if (substation == null) {
                LOGGER.trace("Create substation {}", substationId);
                substation = network.newSubstation()
                        .setId(substationId)
                        .setName(substationName)
                        .setEnsureIdUnicity(false)
                        .setCountry(country)
                        .setGeographicalTags(regionId)
                     .add();
            }

            // Voltage levels
            if (s.getContains_VoltageLevels() != null) {
                for (cim1.model.VoltageLevel vl : s.getContains_VoltageLevels()) {
                    String voltageLevelId = namingStrategy.getId(vl);
                    VoltageLevel voltageLevel = network.getVoltageLevel(voltageLevelId);
                    if (voltageLevel == null) {
                        float[] limits = getVoltageLimits(vl, noOperationalLimitInOperationalLimitSet);
                        LOGGER.trace("Create voltage level {}", voltageLevelId);
                        voltageLevel = substation.newVoltageLevel()
                                .setId(voltageLevelId)
                                .setName(vl.getName())
                                .setEnsureIdUnicity(false)
                                .setNominalV(vl.getBaseVoltage().getNominalVoltage())
                                .setTopologyKind(TopologyKind.BUS_BREAKER)
                                .setLowVoltageLimit(limits[0])
                                .setHighVoltageLimit(limits[1])
                            .add();
                    }

                    // Nodes
                    if (vl.topologicalNode == null) {
                        LOGGER.warn("No topological node for voltage level {} in substation {}",
                                vl.getId(), vl.idMemberOf_Substation);
                    } else {
                        for (cim1.model.TopologicalNode tn : vl.getTopologicalNode()) {
                            LOGGER.trace("Create bus {}", namingStrategy.getId(tn));

                            // Create the bus corresponding to the topological node
                            Bus bus = voltageLevel.getBusBreakerView() .newBus()
                                    .setId(namingStrategy.getId(tn))
                                .add();

                            cim1.model.SvVoltage svv = tn.getSvVoltage();
                            if (svv != null) {
                                svVoltages.put(bus, svv);
                            } else {
                                LOGGER.warn("No SvVoltage for bus {}", tn.getId());
                            }
                        }
                        if (vl.getContains_Equipments() != null) {
                            for (cim1.model.Equipment eq : vl.getContains_Equipments()) {
                                if (eq instanceof cim1.model.EnergyConsumer) {
                                    createLoad(voltageLevel, (cim1.model.EnergyConsumer) eq);
                                } else if (eq instanceof cim1.model.ShuntCompensator) {
                                    createShunt(voltageLevel, (cim1.model.ShuntCompensator) eq);
                                } else if (eq instanceof cim1.model.SynchronousMachine) {
                                    synchronousMachinesToAdd.put(voltageLevel, (cim1.model.SynchronousMachine) eq);
                                } else if (eq instanceof cim1.model.Switch) {
                                    createSwitch(voltageLevel, (cim1.model.Switch) eq);
                                }
                            }
                        }
                    }
                }
            } else {
                LOGGER.warn("Substation {} doesn't contain any voltage level", s.getName());
            }
        }

        if (!substationsNotAssociatedToValidCountry.isEmpty()) {
            LOGGER.warn("Substations not associated to a valid country and so on associated to default country {}: {}",
                    config.getDefaultCountry(), substationsNotAssociatedToValidCountry);
        }

        createLines(network, noOperationalLimitInOperationalLimitSet);
        createLowImpedanceLines(network);
        List<RatioTapChangerToCreate> ratioTapChangerToCreateList = new ArrayList<>();
        createTransfos(network, noOperationalLimitInOperationalLimitSet, ratioTapChangerToCreateList);

        for (Map.Entry<VoltageLevel, Collection<cim1.model.SynchronousMachine>> entry : synchronousMachinesToAdd.asMap().entrySet()) {
            VoltageLevel vl = entry.getKey();
            for (cim1.model.SynchronousMachine sm : entry.getValue()) {
                createGenerator(vl, sm,
                        synchronousMachinesWithoutRegulatingControl,
                        synchronousMachinesRegulatingVoltageWithZeroTargetVoltage,
                        synchronousMachinesWithReactiveRangeForMinus9999MW);
            }
        }

        if (!synchronousMachinesWithoutRegulatingControl.isEmpty()) {
            LOGGER.warn("Synchronous machines without regulating control: {}", synchronousMachinesWithoutRegulatingControl);
        }

        if (!synchronousMachinesRegulatingVoltageWithZeroTargetVoltage.isEmpty()) {
            LOGGER.warn("Synchronous machines with voltage regulator on and a voltage setpoint to zero, fixed to nominal voltage: {}",
                    synchronousMachinesRegulatingVoltageWithZeroTargetVoltage);
        }

        if (!synchronousMachinesWithReactiveRangeForMinus9999MW.isEmpty()) {
            LOGGER.warn("CVG bug: synchronous machines with a reactive limit associated to -9999 MW: {}", synchronousMachinesWithReactiveRangeForMinus9999MW);
        }

        if (!noOperationalLimitInOperationalLimitSet.isEmpty()) {
            LOGGER.warn("No OperationalLimit in OperationalLimitSet {}", noOperationalLimitInOperationalLimitSet);
        }

        for (RatioTapChangerToCreate ratioTapChangerToCreate : ratioTapChangerToCreateList) {
            createRatioTapChanger(ratioTapChangerToCreate.rtc, ratioTapChangerToCreate.transfo::newRatioTapChanger,
                    ratioTapChangerToCreate.rtcSide1, ratioTapChangerToCreate.terminals);
        }

        for (Map.Entry<Bus, cim1.model.SvVoltage> entry : svVoltages.entrySet()) {
            Bus bus = entry.getKey();
            cim1.model.SvVoltage svv = entry.getValue();
            bus.setAngle(svv.getAngle());
            if (svv.getV() > 0) {
                bus.setV(svv.getV());
            }
        }

        LOGGER.trace("Network built");

        return network;
    }
}
