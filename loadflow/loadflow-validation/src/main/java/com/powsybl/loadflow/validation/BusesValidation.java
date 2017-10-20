/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.iidm.network.Branch;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Load;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.ShuntCompensator;
import com.powsybl.iidm.network.StaticVarCompensator;
import com.powsybl.iidm.network.Terminal;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.VscConverterStation;
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

    public static boolean checkBuses(Bus bus, ValidationConfig config, ValidationWriter busesWriter) {
        Objects.requireNonNull(bus);
        Objects.requireNonNull(config);
        Objects.requireNonNull(busesWriter);
        double loadP = bus.getLoadStream().map(Load::getTerminal).mapToDouble(Terminal::getP).sum();
        double loadQ = bus.getLoadStream().map(Load::getTerminal).mapToDouble(Terminal::getQ).sum();
        double genP = bus.getGeneratorStream().map(Generator::getTerminal).mapToDouble(Terminal::getP).sum();
        double genQ = bus.getGeneratorStream().map(Generator::getTerminal).mapToDouble(Terminal::getQ).sum();
        double shuntP = bus.getShuntStream().map(ShuntCompensator::getTerminal).mapToDouble(Terminal::getP).sum();
        double shuntQ = bus.getShuntStream().map(ShuntCompensator::getTerminal).mapToDouble(Terminal::getQ).sum();
        double svcP = bus.getStaticVarCompensatorStream().map(StaticVarCompensator::getTerminal).mapToDouble(Terminal::getP).sum();
        double svcQ = bus.getStaticVarCompensatorStream().map(StaticVarCompensator::getTerminal).mapToDouble(Terminal::getQ).sum();
        double vscCSP = bus.getVscConverterStationStream().map(VscConverterStation::getTerminal).mapToDouble(Terminal::getP).sum();
        double vscCSQ = bus.getVscConverterStationStream().map(VscConverterStation::getTerminal).mapToDouble(Terminal::getQ).sum();
        double lineP = bus.getLineStream().map(line -> getBranchTerminal(line, bus)).mapToDouble(Terminal::getP).sum();
        double lineQ = bus.getLineStream().map(line -> getBranchTerminal(line, bus)).mapToDouble(Terminal::getQ).sum();
        double twtP = bus.getTwoWindingTransformerStream().map(twt -> getBranchTerminal(twt, bus)).mapToDouble(Terminal::getP).sum();
        double twtQ = bus.getTwoWindingTransformerStream().map(twt -> getBranchTerminal(twt, bus)).mapToDouble(Terminal::getQ).sum();
        double tltP = bus.getThreeWindingTransformerStream().map(tlt -> getThreeWindingTransformerTerminal(tlt, bus)).mapToDouble(Terminal::getP).sum();
        double tltQ = bus.getThreeWindingTransformerStream().map(tlt -> getThreeWindingTransformerTerminal(tlt, bus)).mapToDouble(Terminal::getQ).sum();
        return checkBuses(bus.getId(), loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, twtP, twtQ, tltP, tltQ, config, busesWriter);
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
                                     double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                                     double twtP, double twtQ, double tltP, double tltQ, ValidationConfig config, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(config);
        Objects.requireNonNull(writer);

        try (ValidationWriter busesWriter = ValidationUtils.createValidationWriter(id, config, writer, ValidationType.BUSES)) {
            return checkBuses(id, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, twtP, twtQ, tltP, tltQ, config, busesWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static boolean checkBuses(String id, double loadP, double loadQ, double genP, double genQ, double shuntP, double shuntQ,
                                     double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                                     double twtP, double twtQ, double tltP, double tltQ, ValidationConfig config, ValidationWriter busesWriter) {
        Objects.requireNonNull(id);
        boolean validated = true;
        try {
            Double incomingP = genP + shuntP + svcP + vscCSP + lineP + twtP + tltP;
            Double incomingQ = genQ + shuntQ + svcQ + vscCSQ + lineQ + twtQ + tltQ;
            if (((Double.isNaN(incomingP) || Double.isNaN(loadP)) && !config.areOkMissingValues()) || Math.abs(incomingP + loadP) > config.getThreshold()) {
                LOGGER.warn("{} {}: {} P {} {}", ValidationType.BUSES, ValidationUtils.VALIDATION_ERROR, id, incomingP, loadP);
                validated = false;
            }
            if (((Double.isNaN(incomingQ) || Double.isNaN(loadQ)) && !config.areOkMissingValues()) || Math.abs(incomingQ + loadQ) > config.getThreshold()) {
                LOGGER.warn("{} {}: {} Q {} {}", ValidationType.BUSES, ValidationUtils.VALIDATION_ERROR, id, incomingQ, loadQ);
                validated = false;
            }
            busesWriter.write(id, incomingP, incomingQ, loadP, loadQ, genP, genQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, twtP, twtQ, tltP, tltQ, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }
}
