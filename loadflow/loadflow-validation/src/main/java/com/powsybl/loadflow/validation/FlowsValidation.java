/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.loadflow.validation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TieLine;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerPhaseAngleClock;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.loadflow.validation.io.ValidationWriter;

import static com.powsybl.loadflow.validation.ValidationUtils.*;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class FlowsValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlowsValidation.class);

    public static final FlowsValidation INSTANCE = new FlowsValidation();

    private FlowsValidation() {
    }

    public boolean checkFlows(BranchData branch, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(branch.getId());
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = createValidationWriter(branch.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(branch, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Rules for valid results: <br/>
     * Rule 1: checks disconnected terminal: P and Q must be NaN or ~0 <br/>
     * Rule 2: checks connected terminal: |P - Pcalc| <= ε and |Q - Qcalc| <= ε, where <br/>
     *     - pCalc = ComputedP1 calculated in BranchData <br/>
     *     - QCalc = ComputedQ1 calculated in BranchData
     */
    public boolean checkFlows(BranchData branch, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(branch);
        Objects.requireNonNull(branch.getId());
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);
        boolean validated = true;

        if (!branch.isConnected1()) {
            validated &= checkDisconnectedTerminal(branch.getId(), "1", branch.getP1(), branch.getComputedP1(), branch.getQ1(), branch.getComputedQ1(), config.getThreshold());
        }
        if (!branch.isConnected2()) {
            validated &= checkDisconnectedTerminal(branch.getId(), "2", branch.getP2(), branch.getComputedP2(), branch.getQ2(), branch.getComputedQ2(), config.getThreshold());
        }
        if (branch.isConnected1() && isMainComponent(config, branch.isMainComponent1())) {
            validated &= checkConnectedTerminal(branch.getId(), "1", branch.getP1(), branch.getComputedP1(), branch.getQ1(), branch.getComputedQ1(), config);
        }
        if (branch.isConnected2() && isMainComponent(config, branch.isMainComponent2())) {
            validated &= checkConnectedTerminal(branch.getId(), "2", branch.getP2(), branch.getComputedP2(), branch.getQ2(), branch.getComputedQ2(), config);
        }
        try {
            flowsWriter.write(branch.getId(),
                    branch.getP1(), branch.getComputedP1(), branch.getQ1(), branch.getComputedQ1(),
                    branch.getP2(), branch.getComputedP2(), branch.getQ2(), branch.getComputedQ2(),
                    branch.getR(), branch.getX(),
                    branch.getG1(), branch.getG2(), branch.getB1(), branch.getB2(),
                    branch.getRho1(), branch.getRho2(), branch.getAlpha1(), branch.getAlpha2(),
                    branch.getU1(), branch.getU2(), branch.getTheta1(), branch.getTheta2(),
                    branch.getZ(), branch.getY(), branch.getKsi(), branch.getPhaseAngleClock(),
                    branch.isConnected1(), branch.isConnected2(),
                    branch.isMainComponent1(), branch.isMainComponent2(),
                    validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }

    private static boolean checkDisconnectedTerminal(String id, String terminalNumber, double p, double pCalc, double q, double qCalc, double threshold) {
        boolean validated = true;
        if (!Double.isNaN(p) && Math.abs(p) > threshold) {
            LOGGER.warn("{} {}: {} disconnected P{} {} {}", ValidationType.FLOWS, VALIDATION_ERROR, id, terminalNumber, p, pCalc);
            validated = false;
        }
        if (!Double.isNaN(q) && Math.abs(q) > threshold) {
            LOGGER.warn("{} {}: {} disconnected Q{} {} {}", ValidationType.FLOWS, VALIDATION_ERROR, id, terminalNumber, q, qCalc);
            validated = false;
        }
        return validated;
    }

    private static boolean checkConnectedTerminal(String id, String terminalNumber, double p, double pCalc, double q, double qCalc, ValidationConfig config) {
        boolean validated = true;
        if (areNaN(config, pCalc) || isOutsideTolerance(p, pCalc, config.getThreshold())) {
            LOGGER.warn("{} {}: {} P{} {} {}", ValidationType.FLOWS, VALIDATION_ERROR, id, terminalNumber, p, pCalc);
            validated = false;
        }
        if (areNaN(config, qCalc) || isOutsideTolerance(q, qCalc, config.getThreshold())) {
            LOGGER.warn("{} {}: {} Q{} {} {}", ValidationType.FLOWS, VALIDATION_ERROR, id, terminalNumber, q, qCalc);
            validated = false;
        }
        return validated;
    }

    public boolean checkFlows(Line l, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(l);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = createValidationWriter(l.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(l, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkFlows(Line l, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(l);
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);

        BranchData branch = new BranchData(l, config.getEpsilonX(), config.applyReactanceCorrection());
        return checkFlows(branch, config, flowsWriter);
    }

    public boolean checkFlows(TwoWindingsTransformer twt, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = createValidationWriter(twt.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(twt, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkFlows(TwoWindingsTransformer twt, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(twt);
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);
        int phaseAngleClock = 0;
        TwoWindingsTransformerPhaseAngleClock phaseAngleClockExtension = twt.getExtension(TwoWindingsTransformerPhaseAngleClock.class);
        if (phaseAngleClockExtension != null) {
            phaseAngleClock = phaseAngleClockExtension.getPhaseAngleClock();
        }

        BranchData branch = new BranchData(twt, phaseAngleClock, config.getEpsilonX(), config.applyReactanceCorrection(), config.getLoadFlowParameters().isTwtSplitShuntAdmittance());
        return checkFlows(branch, config, flowsWriter);
    }

    public boolean checkFlows(TieLine tl, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(tl);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = createValidationWriter(tl.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(tl, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkFlows(TieLine tl, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(tl);
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);

        BranchData branch = new BranchData(tl, config.getEpsilonX(), config.applyReactanceCorrection());
        return checkFlows(branch, config, flowsWriter);
    }

    public boolean checkFlows(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter flowsWriter = createValidationWriter(network.getId(), config, writer, ValidationType.FLOWS)) {
            return checkFlows(network, config, flowsWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkFlows(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkFlows(network, config, writer);
        }
    }

    public boolean checkFlows(Network network, ValidationConfig config, ValidationWriter flowsWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(flowsWriter);
        LOGGER.info("Checking flows of network {}", network.getId());

        boolean linesValidated = network.getLineStream()
                .sorted(Comparator.comparing(Line::getId))
                .map(l -> checkFlows(l, config, flowsWriter))
                .reduce(Boolean::logicalAnd).orElse(true);

        boolean transformersValidated = network.getTwoWindingsTransformerStream()
                .sorted(Comparator.comparing(TwoWindingsTransformer::getId))
                .map(t -> checkFlows(t, config, flowsWriter))
                .reduce(Boolean::logicalAnd).orElse(true);

        boolean tieLinesValidated = network.getTieLineStream()
            .sorted(Comparator.comparing(TieLine::getId))
            .map(tl -> checkFlows(tl, config, flowsWriter))
            .reduce(Boolean::logicalAnd).orElse(true);

        return linesValidated && transformersValidated && tieLinesValidated;
    }

}
