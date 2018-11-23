/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.complex.ComplexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.DanglingLine;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformer.LegBase;
import com.powsybl.iidm.network.VscConverterStation;
import com.powsybl.iidm.network.util.BranchData;
import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class BusesValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusesValidation.class);

    private BusesValidation() {
    }

    public static boolean checkBuses(Network network, ValidationConfig config, Path file) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkBuses(network, config, writer);
        }
    }

    public static boolean checkBuses(Network network, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);
        try (ValidationWriter busesWriter = ValidationUtils.createValidationWriter(network.getId(), config, writer, ValidationType.BUSES)) {
            return checkBuses(network, config, busesWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkBuses(Network network, ValidationConfig config, ValidationWriter busesWriter) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(config);
        Objects.requireNonNull(busesWriter);
        LOGGER.info("Checking buses of network {}", network.getId());
        return network.getBusView()
                      .getBusStream()
                      .sorted(Comparator.comparing(Bus::getId))
                      .map(bus -> checkBuses(bus, config, busesWriter))
                      .reduce(Boolean::logicalAnd)
                      .orElse(true);
    }

    public static boolean checkBuses(Bus bus, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(bus);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter bussWriter = ValidationUtils.createValidationWriter(bus.getId(), config, writer, ValidationType.BUSES)) {
            return checkBuses(bus, config, bussWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void p(String eqt, Identifiable<?> id, Terminal t) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(String.format("    %-5s  %10.4f  %10.4f  %s",
                    eqt,
                    t.getP(),
                    t.getQ(),
                    id.getId()));
        }
    }

    private static void p(String cat, double p, double q) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn(String.format("    %-5s  %10.4f  %10.4f", cat, p, q));
        }
    }

    private static Complex calcStarBusVoltage(ThreeWindingsTransformer tx) {
        Bus bus1 = tx.getLeg1().getTerminal().getBusView().getBus();
        Bus bus2 = tx.getLeg2().getTerminal().getBusView().getBus();
        Bus bus3 = tx.getLeg3().getTerminal().getBusView().getBus();
        Complex v1 = ComplexUtils.polar2Complex(bus1.getV(), Math.toRadians(bus1.getAngle()));
        Complex v2 = ComplexUtils.polar2Complex(bus2.getV(), Math.toRadians(bus2.getAngle()));
        Complex v3 = ComplexUtils.polar2Complex(bus3.getV(), Math.toRadians(bus3.getAngle()));
        Complex ytr1 = new Complex(tx.getLeg1().getR(), tx.getLeg1().getX()).reciprocal();
        Complex ytr2 = new Complex(tx.getLeg2().getR(), tx.getLeg2().getX()).reciprocal();
        Complex ytr3 = new Complex(tx.getLeg3().getR(), tx.getLeg3().getX()).reciprocal();
        Complex ysh01 = new Complex(0, 0);
        Complex ysh02 = new Complex(0, 0);
        Complex ysh03 = new Complex(0, 0);
        double ratedU0 = tx.getLeg1().getRatedU();
        Complex a01 = new Complex(1, 0);
        Complex a1 = new Complex(tx.getLeg1().getRatedU() / ratedU0, 0);
        Complex a02 = new Complex(1, 0);
        Complex a2 = new Complex(tx.getLeg2().getRatedU() / ratedU0, 0);
        Complex a03 = new Complex(1, 0);
        Complex a3 = new Complex(tx.getLeg3().getRatedU() / ratedU0, 0);

        // At star bus sum of currents from each end must be zero
        Complex y01 = ytr1.negate().divide(a01.conjugate().multiply(a1));
        Complex y02 = ytr2.negate().divide(a02.conjugate().multiply(a2));
        Complex y03 = ytr3.negate().divide(a03.conjugate().multiply(a3));
        Complex y0101 = ytr1.add(ysh01).divide(a01.conjugate().multiply(a01));
        Complex y0202 = ytr2.add(ysh02).divide(a02.conjugate().multiply(a02));
        Complex y0303 = ytr3.add(ysh03).divide(a03.conjugate().multiply(a03));
        return y01.multiply(v1).add(y02.multiply(v2)).add(y03.multiply(v3)).negate()
                .divide(y0101.add(y0202).add(y0303));
    }

    private static LegBase<?> getLeg(ThreeWindingsTransformer tx, Terminal t, Bus bus) {
        tx.getSide(t);
        switch (tx.getSide(t)) {
            case ONE:
                return tx.getLeg1();
            case TWO:
                return tx.getLeg2();
            case THREE:
                return tx.getLeg3();
            default:
                throw new PowsyblException("no leg for bus " + bus.getId() + " in transformer " + tx.getId());
        }
    }

    private static Complex calcFlow(ThreeWindingsTransformer tx, Bus bus) {
        Terminal t = getThreeWindingTransformerTerminal(tx, bus);
        Complex v0 = calcStarBusVoltage(tx);
        LOGGER.info("Transformer {}", tx.getId());
        LOGGER.info("    Voltage star bus {} {}", v0.abs(), Math.toDegrees(v0.getArgument()));

        LegBase<?> leg = getLeg(tx, t, bus);
        String id = tx.getId() + "." + bus.getId();
        double r = leg.getR();
        double x = leg.getX();
        double gk = 0;
        double bk = 0;
        double g0 = 0;
        double b0 = 0;
        // In IIDM only the Leg1 has admittance to ground
        // And it is modeled at end corresponding to star bus
        // All (gk, bk) are zero in the IIDM model
        if (leg == tx.getLeg1()) {
            g0 = tx.getLeg1().getG();
            b0 = tx.getLeg1().getB();
        }
        double ratedU0 = tx.getLeg1().getRatedU();
        double rhok = ratedU0 / leg.getRatedU();
        double alphak = 0;
        double rho0 = 1;
        double alpha0 = 0;
        boolean buskMainComponent = true;
        boolean bus0MainComponent = true;
        boolean buskConnected = true;
        boolean bus0Connected = true;
        boolean applyReactanceCorrection = false;
        double epsilonX = 0;
        double expectedFlowPk = Double.NaN;
        double expectedFlowQk = Double.NaN;
        double expectedFlowP0 = Double.NaN;
        double expectedFlowQ0 = Double.NaN;
        BranchData branch = new BranchData(id,
                r, x,
                rhok, rho0,
                bus.getV(), v0.abs(),
                Math.toRadians(bus.getAngle()), v0.getArgument(),
                alphak, alpha0,
                gk, g0, bk, b0,
                expectedFlowPk, expectedFlowQk,
                expectedFlowP0, expectedFlowQ0,
                buskConnected, bus0Connected,
                buskMainComponent, bus0MainComponent,
                epsilonX, applyReactanceCorrection);

        Complex flow = new Complex(branch.getComputedP1(), branch.getComputedQ1());
        LOGGER.info("    Flow at bus {} : {} {}", bus.getVoltageLevel().getNominalV(), flow.getReal(), flow.getImaginary());
        // Store the result at the terminal
        t.setP(branch.getComputedP1());
        t.setQ(branch.getComputedQ1());
        return flow;
    }

    public static boolean checkBuses(Bus bus, ValidationConfig config, ValidationWriter busesWriter) {
        Objects.requireNonNull(bus);
        Objects.requireNonNull(config);
        Objects.requireNonNull(busesWriter);
        double loadP = bus.getLoadStream().map(Load::getTerminal).mapToDouble(Terminal::getP).sum();
        double loadQ = bus.getLoadStream().map(Load::getTerminal).mapToDouble(Terminal::getQ).sum();
        double genP = bus.getGeneratorStream().map(Generator::getTerminal).mapToDouble(Terminal::getP).sum();
        double genQ = bus.getGeneratorStream().map(Generator::getTerminal).mapToDouble(Terminal::getQ).sum();
        double shuntP = bus.getShuntCompensatorStream().map(ShuntCompensator::getTerminal).mapToDouble(Terminal::getP).map(p -> Double.isNaN(p) ? 0 : p).sum();
        double shuntQ = bus.getShuntCompensatorStream().map(ShuntCompensator::getTerminal).mapToDouble(Terminal::getQ).sum();
        double svcP = bus.getStaticVarCompensatorStream().map(StaticVarCompensator::getTerminal).mapToDouble(Terminal::getP).sum();
        double svcQ = bus.getStaticVarCompensatorStream().map(StaticVarCompensator::getTerminal).mapToDouble(Terminal::getQ).sum();
        double vscCSP = bus.getVscConverterStationStream().map(VscConverterStation::getTerminal).mapToDouble(Terminal::getP).sum();
        double vscCSQ = bus.getVscConverterStationStream().map(VscConverterStation::getTerminal).mapToDouble(Terminal::getQ).sum();
        double lineP = bus.getLineStream().map(line -> getBranchTerminal(line, bus)).mapToDouble(Terminal::getP).sum();
        double lineQ = bus.getLineStream().map(line -> getBranchTerminal(line, bus)).mapToDouble(Terminal::getQ).sum();
        double danglingLineP = bus.getDanglingLineStream().map(DanglingLine::getTerminal).mapToDouble(Terminal::getP).sum();
        double danglingLineQ = bus.getDanglingLineStream().map(DanglingLine::getTerminal).mapToDouble(Terminal::getQ).sum();
        double twtP = bus.getTwoWindingTransformerStream().map(twt -> getBranchTerminal(twt, bus)).mapToDouble(Terminal::getP).sum();
        double twtQ = bus.getTwoWindingTransformerStream().map(twt -> getBranchTerminal(twt, bus)).mapToDouble(Terminal::getQ).sum();

        bus.getThreeWindingTransformerStream().forEach(tx -> calcFlow(tx, bus));
        double tltP = bus.getThreeWindingTransformerStream().map(tlt -> getThreeWindingTransformerTerminal(tlt, bus)).mapToDouble(Terminal::getP).sum();
        double tltQ = bus.getThreeWindingTransformerStream().map(tlt -> getThreeWindingTransformerTerminal(tlt, bus)).mapToDouble(Terminal::getQ).sum();

        boolean mainComponent = bus.isInMainConnectedComponent();
        boolean r = checkBuses(bus.getId(), loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ,
                               danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, config, busesWriter);
        String ref = "";
        if (bus.getVoltageLevel() != null) {
            ref = bus.getVoltageLevel().getName() + ref;
            if (bus.getVoltageLevel().getSubstation() != null) {
                ref = bus.getVoltageLevel().getSubstation().getName() + " " + ref;
            }
        }
        boolean debug = true; //ref.contains("BUS");
        if (!r || debug) {
            LOGGER.warn("Check {}. Details of {} bus {}, (V,A) = ({}, {})",
                    r ? "succeeded" : "failed",
                    ref,
                    bus.getId(),
                    bus.getV(),
                    bus.getAngle());
            bus.getLoadStream().forEach(l -> p("load", l, l.getTerminal()));
            bus.getGeneratorStream().forEach(g -> p("gen", g, g.getTerminal()));
            bus.getLineStream().forEach(l -> p("line", l, getBranchTerminal(l, bus)));
            bus.getTwoWindingTransformerStream().forEach(t -> p("2wtx", t, getBranchTerminal(t, bus)));
            bus.getDanglingLineStream().forEach(d -> p("dngl", d, d.getTerminal()));
            bus.getShuntCompensatorStream().forEach(s -> p("shunt", s, s.getTerminal()));
            bus.getThreeWindingTransformerStream().forEach(t -> p("3wtx", t, getThreeWindingTransformerTerminal(t, bus)));

            double incomingP = genP + shuntP + svcP + vscCSP + lineP + danglingLineP + twtP + tltP;
            double incomingQ = genQ + shuntQ + svcQ + vscCSQ + lineQ + danglingLineQ + twtQ + tltQ;
            double sump = Math.abs(incomingP + loadP);
            double sumq = Math.abs(incomingQ + loadQ);
            p("SUM", sump, sumq);
        }
        return r;
    }

    private static Terminal getBranchTerminal(Branch branch, Bus bus) {
        if (branch.getTerminal1().isConnected() && bus.getId().equals(branch.getTerminal1().getBusView().getBus().getId())) {
            return branch.getTerminal1();
        } else {
            return branch.getTerminal2();
        }
    }

    private static Terminal getThreeWindingTransformerTerminal(ThreeWindingsTransformer tlt, Bus bus) {
        if (tlt.getLeg1().getTerminal().isConnected() && bus.getId().equals(tlt.getLeg1().getTerminal().getBusView().getBus().getId())) {
            return tlt.getLeg1().getTerminal();
        } else if (tlt.getLeg2().getTerminal().isConnected() && bus.getId().equals(tlt.getLeg2().getTerminal().getBusView().getBus().getId())) {
            return tlt.getLeg2().getTerminal();
        } else {
            return tlt.getLeg3().getTerminal();
        }
    }

    public static boolean checkBuses(String id, double loadP, double loadQ, double genP, double genQ, double shuntP, double shuntQ,
                                     double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ, double danglingLineP, double danglingLineQ,
                                     double twtP, double twtQ, double tltP, double tltQ, boolean mainComponent, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter busesWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.BUSES)) {
            return checkBuses(id, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ,
                              danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, config, busesWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkBuses(String id, double loadP, double loadQ, double genP, double genQ, double shuntP, double shuntQ,
                                     double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ, double danglingLineP, double danglingLineQ,
                                     double twtP, double twtQ, double tltP, double tltQ, boolean mainComponent, ValidationConfig config, ValidationWriter busesWriter) {
        Objects.requireNonNull(id);
        boolean validated = true;

        double incomingP = genP + shuntP + svcP + vscCSP + lineP + danglingLineP + twtP + tltP;
        double incomingQ = genQ + shuntQ + svcQ + vscCSQ + lineQ + danglingLineQ + twtQ + tltQ;
        if (ValidationUtils.isMainComponent(config, mainComponent)) {
            if (ValidationUtils.areNaN(config, incomingP, loadP) || Math.abs(incomingP + loadP) > config.getThreshold()) {
                LOGGER.warn("{} {}: {} P {}   {} {}", ValidationType.BUSES, ValidationUtils.VALIDATION_ERROR, id, Math.abs(incomingP + loadP), incomingP, loadP);
                validated = false;
            }
            if (ValidationUtils.areNaN(config, incomingQ, loadQ) || Math.abs(incomingQ + loadQ) > config.getThreshold()) {
                LOGGER.warn("{} {}: {} Q {}   {} {}", ValidationType.BUSES, ValidationUtils.VALIDATION_ERROR, id, Math.abs(incomingQ + loadQ), incomingQ, loadQ);
                validated = false;
            }
        }
        try {
            busesWriter.write(id, incomingP, incomingQ, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                              lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }
}
