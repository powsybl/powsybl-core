/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.resultscompletion;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.math3.complex.Complex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.powsybl.commons.PowsyblException;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Branch.Side;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.HvdcConverterStation;
import com.powsybl.iidm.network.HvdcLine;
import com.powsybl.iidm.network.LccConverterStation;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.Leg;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.DanglingLineData;
import com.powsybl.iidm.network.util.LegData;
import com.powsybl.iidm.network.util.LinkData;
import com.powsybl.iidm.network.util.TwtData;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.z0flows.Z0Tools;
import com.powsybl.loadflow.resultscompletion.z0flows.Z0Vertex;
import com.powsybl.loadflow.resultscompletion.z0flows.ControlTerminals;
import com.powsybl.loadflow.resultscompletion.z0flows.ControlTerminals.ControlType;
import com.powsybl.loadflow.resultscompletion.z0flows.Z0Checker;
import com.powsybl.loadflow.resultscompletion.z0flows.Z0FlowsCompletion;
import com.powsybl.loadflow.validation.CandidateComputation;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
@AutoService(CandidateComputation.class)
public class LoadFlowResultsCompletion implements CandidateComputation {

    public LoadFlowResultsCompletion(LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters) {
        this(parameters, lfParameters, 1.0);
    }

    public LoadFlowResultsCompletion(LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters, double distributeTolerance) {
        this.parameters = Objects.requireNonNull(parameters);
        this.lfParameters = Objects.requireNonNull(lfParameters);
        this.distributeTolerance = distributeTolerance;
        this.z0checker = new Z0Checker(parameters, lfParameters);
    }

    public LoadFlowResultsCompletion() {
        this(LoadFlowResultsCompletionParameters.load(), LoadFlowParameters.load());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void run(Network network, ComputationManager computationManager) {
        Objects.requireNonNull(network);
        LOGGER.info("Running {} on network {}, variant {}", getName(), network.getId(), network.getVariantManager().getWorkingVariantId());
        LOGGER.info("LoadFlowResultsCompletionParameters={}", parameters);
        LOGGER.info("LoadFlowParameters={}", lfParameters);

        network.getLoadStream().forEach(load -> completeTerminalData(load.getTerminal(), load));
        network.getGeneratorStream().forEach(generator -> completeTerminalData(generator.getTerminal(), generator));
        network.getBatteryStream().forEach(battery -> completeTerminalData(battery.getTerminal(), battery));
        network.getStaticVarCompensatorStream().forEach(svc -> completeTerminalData(svc.getTerminal(), svc));
        network.getShuntCompensatorStream().forEach(LoadFlowResultsCompletion::completeShunt);

        network.getLineStream().forEach(this::completeLine);
        network.getTwoWindingsTransformerStream().forEach(this::completeTwoWindingsTransformer);
        network.getThreeWindingsTransformerStream().forEach(this::completeThreeWindingsTransformer);
        network.getDanglingLineStream().forEach(this::completeDanglingLine);
        network.getHvdcLineStream().forEach(this::completeHvdcLine);

        network.getBusView().getBusStream().filter(bus1 -> !z0checker.z0GraphContains(bus1))
            .forEach(bus -> distributeBalanceBetweenContinuousControls(bus, distributeTolerance));

        // Complete z0 data
        Z0FlowsCompletion z0FlowsCompletion = new Z0FlowsCompletion(z0checker, distributeTolerance);
        z0FlowsCompletion.complete();
    }

    private static void distributeBalanceBetweenContinuousControls(Bus bus, double distributeTolerance) {
        Z0Vertex vertex = new Z0Vertex(bus);
        vertex.calculateBalanceForBus();

        if (Math.abs(vertex.getBalanceP()) >= distributeTolerance) {
            new ControlTerminals(Collections.singletonList(vertex), ControlType.ACTIVE).distribute(vertex.getBalanceP());
        }
        if (Math.abs(vertex.getBalanceQ()) >= distributeTolerance) {
            new ControlTerminals(Collections.singletonList(vertex), ControlType.REACTIVE).distribute(vertex.getBalanceQ());
        }
    }

    private static void completeShunt(ShuntCompensator sh) {
        Terminal terminal = sh.getTerminal();
        if (terminal.isConnected() && Double.isNaN(terminal.getQ()) && terminal.getBusView().getBus() != null) {

            double v = terminal.getBusView().getBus().getV();
            double p = sh.getG() != 0.0 ? sh.getG() * v * v : Double.NaN;
            double q = -sh.getB() * v * v;
            LOGGER.debug("Shunt {}, setting p = {} q = {}", sh, p, q);
            completeTerminalData(terminal, new Complex(p, q));
        }
    }

    private void completeLine(Line line) {

        boolean loopedLine = line.getTerminal1().isConnected() && line.getTerminal2().isConnected()
            && line.getTerminal1().getBusView().getBus().equals(line.getTerminal2().getBusView().getBus());

        // Zero flow in loops and in zero impedance lines connected only on one side
        if (loopedLine || Z0Checker.isZ0Antenna(line)) {
            completeTerminalDataToZero(line.getTerminal1());
            completeTerminalDataToZero(line.getTerminal2());
        } else if (z0checker.isZ0(line)) {
            z0checker.addToZ0Graph(line);
        } else {
            BranchData lineData = new BranchData(line, parameters.getEpsilonX(), parameters.isApplyReactanceCorrection());
            completeTerminalData(line.getTerminal(Side.ONE), Side.ONE, lineData);
            completeTerminalData(line.getTerminal(Side.TWO), Side.TWO, lineData);
        }
    }

    private void completeTwoWindingsTransformer(TwoWindingsTransformer t2wt) {

        boolean loopedTransformer = t2wt.getTerminal1().isConnected() && t2wt.getTerminal2().isConnected()
            && t2wt.getTerminal1().getBusView().getBus().equals(t2wt.getTerminal2().getBusView().getBus());

        if (loopedTransformer || Z0Checker.isZ0Antenna(t2wt)) {
            completeTerminalDataToZero(t2wt.getTerminal1());
            completeTerminalDataToZero(t2wt.getTerminal2());
        } else if (z0checker.isZ0(t2wt)) {
            z0checker.addToZ0Graph(t2wt);
        } else {
            completeT2wtData(t2wt);
        }
    }

    private void completeT2wtData(TwoWindingsTransformer t2wt) {

        int phaseAngleClock = 0;
        TwoWindingsTransformerPhaseAngleClock phaseAngleClockExtension = t2wt.getExtension(TwoWindingsTransformerPhaseAngleClock.class);
        if (phaseAngleClockExtension != null) {
            phaseAngleClock = phaseAngleClockExtension.getPhaseAngleClock();
        }

        BranchData twtData = new BranchData(t2wt, phaseAngleClock, parameters.getEpsilonX(),
            parameters.isApplyReactanceCorrection(), lfParameters.isTwtSplitShuntAdmittance());

        completeTerminalData(t2wt.getTerminal(Side.ONE), Side.ONE, twtData);
        completeTerminalData(t2wt.getTerminal(Side.TWO), Side.TWO, twtData);
    }

    private void completeThreeWindingsTransformer(ThreeWindingsTransformer t3wt) {
        List<Leg> z0Legs = z0checker.getZ0Legs(t3wt);
        List<Leg> z0AntennaLegs = z0checker.getZ0AntennaLegs(t3wt);
        if (z0Legs.isEmpty() && z0AntennaLegs.isEmpty()) {
            completeAllT3wtData(t3wt);
        } else {

            z0AntennaLegs.forEach(leg -> completeTerminalDataToZero(leg.getTerminal()));
            if (!z0Legs.isEmpty()) {
                z0checker.addToZ0Graph(t3wt, z0Legs);
            }

            List<Leg> legsWithImpedance = t3wt.getLegs().stream()
                .filter(leg -> !z0Legs.contains(leg) && !z0AntennaLegs.contains(leg))
                .collect(Collectors.toList());

            if (!legsWithImpedance.isEmpty()) {
                if (z0Legs.isEmpty()) {
                    completePartialT3wtData(legsWithImpedance, t3wt);
                } else {
                    completeT3wtData(legsWithImpedance, z0Legs, t3wt);
                }
            }
        }
    }

    private void completeAllT3wtData(ThreeWindingsTransformer t3wt) {

        int phaseAngleClock2 = 0;
        int phaseAngleClock3 = 0;
        ThreeWindingsTransformerPhaseAngleClock phaseAngleClockExtension = t3wt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
        if (phaseAngleClockExtension != null) {
            phaseAngleClock2 = phaseAngleClockExtension.getPhaseAngleClockLeg2();
            phaseAngleClock3 = phaseAngleClockExtension.getPhaseAngleClockLeg3();
        }

        TwtData twtData = new TwtData(t3wt, phaseAngleClock2, phaseAngleClock3, parameters.getEpsilonX(),
            parameters.isApplyReactanceCorrection(), lfParameters.isTwtSplitShuntAdmittance());

        completeTerminalData(t3wt.getLeg1().getTerminal(), ThreeWindingsTransformer.Side.ONE, twtData);
        completeTerminalData(t3wt.getLeg2().getTerminal(), ThreeWindingsTransformer.Side.TWO, twtData);
        completeTerminalData(t3wt.getLeg3().getTerminal(), ThreeWindingsTransformer.Side.THREE, twtData);
    }

    // Two legs maximum, zero injection at the star bus as zero impedance legs are antennas
    private void completePartialT3wtData(List<Leg> legsWithImpedance, ThreeWindingsTransformer t3wt) {
        if (legsWithImpedance.size() == 1) { // must be connected
            Complex flow = new LegData(legsWithImpedance.get(0), t3wt.getRatedU0(),
                Z0Tools.getPhaseAngleClock(t3wt, legsWithImpedance.get(0)), parameters.getEpsilonX(),
                parameters.isApplyReactanceCorrection(), lfParameters.isTwtSplitShuntAdmittance())
                    .flowWhenIsAntennaAtTheStarBus();
            completeTerminalData(legsWithImpedance.get(0).getTerminal(), flow);
        } else if (legsWithImpedance.size() == 2) { // one of them can be disconnected
            LegData legData1 = new LegData(legsWithImpedance.get(0), t3wt.getRatedU0(),
                Z0Tools.getPhaseAngleClock(t3wt, legsWithImpedance.get(0)), parameters.getEpsilonX(),
                parameters.isApplyReactanceCorrection(), lfParameters.isTwtSplitShuntAdmittance());
            LegData legData2 = new LegData(legsWithImpedance.get(1), t3wt.getRatedU0(),
                Z0Tools.getPhaseAngleClock(t3wt, legsWithImpedance.get(1)), parameters.getEpsilonX(),
                parameters.isApplyReactanceCorrection(), lfParameters.isTwtSplitShuntAdmittance());

            LinkData.Flow flow = legData1.flowWhenIsChainAtTheStarBus(legData2);
            completeTerminalData(legsWithImpedance.get(0).getTerminal(), flow.getFromTo());
            completeTerminalData(legsWithImpedance.get(1).getTerminal(), flow.getToFrom());
        } else { // never should happen
            throw new PowsyblException("Unexpected number of legs in ThreeWindingTransformer " + t3wt.getId());
        }
    }

    private void completeT3wtData(List<Leg> legsWithImpedance, List<Leg> z0Legs, ThreeWindingsTransformer t3wt) {
        Complex vstar = Z0Tools.getVstarFromZ0Leg(t3wt, z0Legs, parameters, lfParameters);
        if (Z0Tools.isValidVoltage(vstar)) {
            for (Leg leg : legsWithImpedance) {
                Complex flow = Z0Tools.getLegFlow(t3wt, leg, vstar,  parameters, lfParameters);
                completeTerminalData(leg.getTerminal(), flow);
            }
        }
    }

    private void completeDanglingLine(DanglingLine dl) {
        if (Z0Checker.isZ0(dl)) {
            z0checker.addToZ0Graph(dl);
        } else {
            DanglingLineData danglingLineData = new DanglingLineData(dl, true);
            completeTerminalData(dl.getTerminal(), danglingLineData);
        }
    }

    private void completeHvdcLine(HvdcLine hvdcLine) {
        completeHvdcConverterStation(hvdcLine.getConverterStation1(), hvdcLine);
        completeHvdcConverterStation(hvdcLine.getConverterStation2(), hvdcLine);
    }

    private static void completeHvdcConverterStation(HvdcConverterStation hvdcConverterStation, HvdcLine hvdcLine) {
        if (hvdcConverterStation instanceof LccConverterStation) {
            completeLccConverterStation((LccConverterStation) hvdcConverterStation, hvdcLine);
        } else if (hvdcConverterStation instanceof VscConverterStation) {
            completeVscConverterStation((VscConverterStation) hvdcConverterStation, hvdcLine);
        } else {
            throw new PowsyblException("Unexpected HvdcConverterStation" + hvdcConverterStation.getId());
        }
    }

    private static void completeLccConverterStation(LccConverterStation lccConverterStation, HvdcLine hvdcLine) {
        if (lccConverterStation.getTerminal().isConnected()) {
            double powerFactor = lccConverterStation.getPowerFactor();
            double p = getPAC(hvdcLine.getActivePowerSetpoint(), rectifierLossFactor(hvdcLine),
                inverterLossFactor(hvdcLine), hvdcLine.getR(), hvdcLine.getNominalV(),
                isRectifier(lccConverterStation, hvdcLine));
            double q = powerFactor == 0.0 ? 0.0 : Math.abs(p) * Math.sqrt((1 - powerFactor * powerFactor) / (powerFactor * powerFactor));
            completeTerminalData(lccConverterStation.getTerminal(), new Complex(p, q));
        }
    }

    private static void completeVscConverterStation(VscConverterStation vscConverterStation, HvdcLine hvdcLine) {
        if (vscConverterStation.getTerminal().isConnected()) {
            double p = getPAC(hvdcLine.getActivePowerSetpoint(), rectifierLossFactor(hvdcLine),
                inverterLossFactor(hvdcLine), hvdcLine.getR(), hvdcLine.getNominalV(),
                isRectifier(vscConverterStation, hvdcLine));
            double q = vscConverterStation.getReactivePowerSetpoint();
            completeTerminalData(vscConverterStation.getTerminal(), new Complex(p, q));
        }
    }

    private static double getPAC(double activePowerSetpoint, double rectifierLossFactor, double inverterLossFactor,
        double hvdcLineR, double hvdcLineNominalV, boolean isRectifier) {
        if (isRectifier) {
            return activePowerSetpoint;
        } else {
            double rectifierPDc = activePowerSetpoint * (1 - rectifierLossFactor / 100); // rectifierPDc positive.
            double hvdcLosses = hvdcLineR * rectifierPDc * rectifierPDc / (hvdcLineNominalV * hvdcLineNominalV);
            double inverterPDc = rectifierPDc - hvdcLosses;
            return -inverterPDc * (1 - inverterLossFactor / 100); // always negative.
        }
    }

    private static boolean isRectifier(HvdcConverterStation hvdcConverterStation, HvdcLine hvdcLine) {
        return hvdcLine.getConvertersMode() == HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER
            ? hvdcConverterStation.equals(hvdcLine.getConverterStation1())
            : !hvdcConverterStation.equals(hvdcLine.getConverterStation1());
    }

    private static double rectifierLossFactor(HvdcLine hvdcLine) {
        return hvdcLine.getConvertersMode() == HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER
            ? hvdcLine.getConverterStation1().getLossFactor()
            : hvdcLine.getConverterStation2().getLossFactor();
    }

    private static double inverterLossFactor(HvdcLine hvdcLine) {
        return hvdcLine.getConvertersMode() == HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER
            ? hvdcLine.getConverterStation2().getLossFactor()
            : hvdcLine.getConverterStation1().getLossFactor();
    }

    private static void completeTerminalData(Terminal terminal, Load load) {
        if (terminal.isConnected()) {
            if (Double.isNaN(terminal.getP())) {
                LOGGER.debug("Load {}, setting p = {}", load.getId(), load.getP0());
                terminal.setP(load.getP0());
            }
            if (Double.isNaN(terminal.getQ())) {
                LOGGER.debug("Load {}, setting q = {}", load.getId(), load.getQ0());
                terminal.setQ(load.getQ0());
            }
        }
    }

    private static void completeTerminalData(Terminal terminal, Generator generator) {
        if (terminal.isConnected()) {
            if (Double.isNaN(terminal.getP())) {
                LOGGER.debug("Generator {}, setting p = {}", generator.getId(), -generator.getTargetP());
                terminal.setP(-generator.getTargetP());
            }
            if (Double.isNaN(terminal.getQ())) {
                LOGGER.debug("Generator {}, setting q = {}", generator.getId(), -generator.getTargetQ());
                terminal.setQ(-generator.getTargetQ());
            }
        }
    }

    private static void completeTerminalData(Terminal terminal, Battery battery) {
        if (terminal.isConnected()) {
            if (Double.isNaN(terminal.getP())) {
                LOGGER.debug("Battery {}, setting p = {}", battery.getId(), -battery.getTargetP());
                terminal.setP(-battery.getTargetP());
            }
            if (Double.isNaN(terminal.getQ())) {
                LOGGER.debug("Battery {}, setting q = {}", battery.getId(), -battery.getTargetQ());
                terminal.setQ(-battery.getTargetQ());
            }
        }
    }

    private static void completeTerminalData(Terminal terminal, StaticVarCompensator svc) {
        if (terminal.isConnected()) {
            terminal.setP(0.0);
            if (Double.isNaN(terminal.getQ())) {
                LOGGER.debug("StaticVarCompensator {}, setting q = {}", svc.getId(), svc.getReactivePowerSetpoint());
                terminal.setQ(svc.getReactivePowerSetpoint());
            }
        }
    }

    private static void completeTerminalData(Terminal terminal, Side side, BranchData branchData) {
        if (Double.isNaN(terminal.getP())) {
            LOGGER.debug("Branch {}, Side {}: setting p = {}", branchData.getId(), side, branchData.getComputedP(side));
            terminal.setP(branchData.getComputedP(side));
        }
        if (Double.isNaN(terminal.getQ())) {
            LOGGER.debug("Branch {}, Side {}: setting q = {}", branchData.getId(), side, branchData.getComputedQ(side));
            terminal.setQ(branchData.getComputedQ(side));
        }
    }

    private static void completeTerminalData(Terminal terminal, ThreeWindingsTransformer.Side side, TwtData twtData) {
        if (Double.isNaN(terminal.getP())) {
            LOGGER.debug("Twt {}, Side {}: setting p = {}", twtData.getId(), side, twtData.getComputedP(side));
            terminal.setP(twtData.getComputedP(side));
        }
        if (Double.isNaN(terminal.getQ())) {
            LOGGER.debug("Twt {}, Side {}: setting q = {}", twtData.getId(), side, twtData.getComputedQ(side));
            terminal.setQ(twtData.getComputedQ(side));
        }
    }

    private static void completeTerminalData(Terminal terminal, DanglingLineData danglingLineData) {
        if (Double.isNaN(terminal.getP()) && !Double.isNaN(danglingLineData.getNetworkFlowP())) {
            LOGGER.debug("DanglingLine {}: setting p = {}", danglingLineData.getId(), danglingLineData.getNetworkFlowP());
            terminal.setP(danglingLineData.getNetworkFlowP());
        }
        if (Double.isNaN(terminal.getQ()) && !Double.isNaN(danglingLineData.getNetworkFlowQ())) {
            LOGGER.debug("DanglingLine {}: setting q = {}", danglingLineData.getId(), danglingLineData.getNetworkFlowQ());
            terminal.setQ(danglingLineData.getNetworkFlowQ());
        }
    }

    private static void completeTerminalData(Terminal terminal, Complex data) {
        if (Double.isNaN(terminal.getP()) && !Double.isNaN(data.getReal())) {
            terminal.setP(data.getReal());
        }
        if (Double.isNaN(terminal.getQ()) && !Double.isNaN(data.getImaginary())) {
            terminal.setQ(data.getImaginary());
        }
    }

    private static void completeTerminalDataToZero(Terminal terminal) {
        if (Double.isNaN(terminal.getP())) {
            terminal.setP(0.0);
        }
        if (Double.isNaN(terminal.getQ())) {
            terminal.setQ(0.0);
        }
    }

    public static final String NAME = "loadflowResultsCompletion";

    private final LoadFlowResultsCompletionParameters parameters;
    private final LoadFlowParameters lfParameters;
    private final double distributeTolerance;
    private final Z0Checker z0checker;

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowResultsCompletion.class);
}
