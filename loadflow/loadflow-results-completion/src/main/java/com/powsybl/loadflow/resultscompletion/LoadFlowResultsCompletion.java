/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.resultscompletion;

import java.util.Objects;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.iidm.network.util.TwtData;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.resultscompletion.z0flows.Z0FlowsCompletion;
import com.powsybl.loadflow.resultscompletion.z0flows.Z0LineChecker;
import com.powsybl.loadflow.validation.CandidateComputation;

/**
 *
 * @author Massimo Ferraro {@literal <massimo.ferraro@techrain.eu>}
 */
@AutoService(CandidateComputation.class)
public class LoadFlowResultsCompletion implements CandidateComputation {

    public static final String NAME = "loadflowResultsCompletion";
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadFlowResultsCompletion.class);

    private final LoadFlowResultsCompletionParameters parameters;
    private final LoadFlowParameters lfParameters;

    public LoadFlowResultsCompletion(LoadFlowResultsCompletionParameters parameters, LoadFlowParameters lfParameters) {
        this.parameters = Objects.requireNonNull(parameters);
        this.lfParameters = Objects.requireNonNull(lfParameters);
        // A line is considered Z0 (null impedance) if and only if
        // it is connected at both ends and the voltage at end buses are the same
        this.z0checker = (Line l) -> {
            if (!l.getTerminal1().isConnected() || !l.getTerminal2().isConnected()) {
                return false;
            }
            Bus b1 = l.getTerminal1().getBusView().getBus();
            Bus b2 = l.getTerminal2().getBusView().getBus();
            Objects.requireNonNull(b1);
            Objects.requireNonNull(b2);
            double threshold = parameters.getZ0ThresholdDiffVoltageAngle();
            boolean r = Math.abs(b1.getV() - b2.getV()) < threshold
                    && Math.abs(b1.getAngle() - b2.getAngle()) < threshold;
            if (r) {
                LOGGER.debug("Line Z0 {} ({}) dV = {}, dA = {}", l.getNameOrId(), l.getId(), Math.abs(b1.getV() - b2.getV()), Math.abs(b1.getAngle() - b2.getAngle()));
            }
            return r;
        };
    }

    public LoadFlowResultsCompletion() {
        this(LoadFlowResultsCompletionParameters.load(), LoadFlowParameters.load());
    }

    public Z0LineChecker z0checker() {
        return z0checker;
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

        network.getLineStream()
            // Do not try to compute flows on loops
            .filter(l -> l.getTerminal1().getBusView().getBus() != l.getTerminal2().getBusView().getBus())
            .forEach(line -> {
                BranchData lineData = new BranchData(line,
                                                     parameters.getEpsilonX(),
                                                     parameters.isApplyReactanceCorrection());
                completeTerminalData(line.getTerminal(TwoSides.ONE), TwoSides.ONE, lineData);
                completeTerminalData(line.getTerminal(TwoSides.TWO), TwoSides.TWO, lineData);
            });

        network.getTwoWindingsTransformerStream().forEach(twt -> {
            int phaseAngleClock = 0;
            TwoWindingsTransformerPhaseAngleClock phaseAngleClockExtension = twt.getExtension(TwoWindingsTransformerPhaseAngleClock.class);
            if (phaseAngleClockExtension != null) {
                phaseAngleClock = phaseAngleClockExtension.getPhaseAngleClock();
            }

            BranchData twtData = new BranchData(twt,
                                                phaseAngleClock,
                                                parameters.getEpsilonX(),
                                                parameters.isApplyReactanceCorrection(),
                                                lfParameters.isTwtSplitShuntAdmittance());
            completeTerminalData(twt.getTerminal(TwoSides.ONE), TwoSides.ONE, twtData);
            completeTerminalData(twt.getTerminal(TwoSides.TWO), TwoSides.TWO, twtData);
        });

        network.getShuntCompensatorStream().forEach(sh -> {
            Terminal terminal = sh.getTerminal();
            if (terminal.isConnected()
                    && Double.isNaN(terminal.getQ())
                    && terminal.getBusView().getBus() != null
                    && terminal.getBusView().getBus().isInMainConnectedComponent()) {
                double v = terminal.getBusView().getBus().getV();
                double q = -sh.getB() * v * v;
                LOGGER.debug("Shunt {}, setting q = {}", sh, q);
                terminal.setQ(q);
            }
        });

        network.getThreeWindingsTransformerStream().forEach(twt -> {
            int phaseAngleClock2 = 0;
            int phaseAngleClock3 = 0;
            ThreeWindingsTransformerPhaseAngleClock phaseAngleClockExtension = twt.getExtension(ThreeWindingsTransformerPhaseAngleClock.class);
            if (phaseAngleClockExtension != null) {
                phaseAngleClock2 = phaseAngleClockExtension.getPhaseAngleClockLeg2();
                phaseAngleClock3 = phaseAngleClockExtension.getPhaseAngleClockLeg3();
            }

            TwtData twtData = new TwtData(twt,
                                          phaseAngleClock2,
                                          phaseAngleClock3,
                                          parameters.getEpsilonX(),
                                          parameters.isApplyReactanceCorrection(),
                                          lfParameters.isTwtSplitShuntAdmittance());
            completeTerminalData(twt.getLeg1().getTerminal(), ThreeSides.ONE, twtData);
            completeTerminalData(twt.getLeg2().getTerminal(), ThreeSides.TWO, twtData);
            completeTerminalData(twt.getLeg3().getTerminal(), ThreeSides.THREE, twtData);
        });

        Z0FlowsCompletion z0FlowsCompletion = new Z0FlowsCompletion(network, z0checker);
        z0FlowsCompletion.complete();
    }

    private void completeTerminalData(Terminal terminal, Load load) {
        if (terminal.isConnected() && terminal.getBusView().getBus() != null && terminal.getBusView().getBus().isInMainConnectedComponent()) {
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

    private void completeTerminalData(Terminal terminal, Generator generator) {
        if (terminal.isConnected() && terminal.getBusView().getBus() != null && terminal.getBusView().getBus().isInMainConnectedComponent()) {
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

    private void completeTerminalData(Terminal terminal, TwoSides side, BranchData branchData) {
        if (terminal.isConnected() && terminal.getBusView().getBus() != null && terminal.getBusView().getBus().isInMainConnectedComponent()) {
            if (Double.isNaN(terminal.getP())) {
                LOGGER.debug("Branch {}, Side {}: setting p = {}", branchData.getId(), side, branchData.getComputedP(side));
                terminal.setP(branchData.getComputedP(side));
            }
            if (Double.isNaN(terminal.getQ())) {
                LOGGER.debug("Branch {}, Side {}: setting q = {}", branchData.getId(), side, branchData.getComputedQ(side));
                terminal.setQ(branchData.getComputedQ(side));
            }
        }
    }

    private void completeTerminalData(Terminal terminal, ThreeSides side, TwtData twtData) {
        if (terminal.isConnected() && terminal.getBusView().getBus() != null && terminal.getBusView().getBus().isInMainConnectedComponent()) {
            if (Double.isNaN(terminal.getP())) {
                LOGGER.debug("Twt {}, Side {}: setting p = {}", twtData.getId(), side, twtData.getComputedP(side));
                terminal.setP(twtData.getComputedP(side));
            }
            if (Double.isNaN(terminal.getQ())) {
                LOGGER.debug("Twt {}, Side {}: setting q = {}", twtData.getId(), side, twtData.getComputedQ(side));
                terminal.setQ(twtData.getComputedQ(side));
            }
        }
    }

    private final Z0LineChecker z0checker;
}
