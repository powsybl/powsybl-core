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

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.io.table.TableFormatterConfig;

import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.loadflow.validation.io.ValidationWriter;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public final class BusesValidation {

    private static final Logger LOGGER = LoggerFactory.getLogger(BusesValidation.class);

    private static final Supplier<TableFormatterConfig> TABLE_FORMATTER_CONFIG = Suppliers.memoize(TableFormatterConfig::load);

    public static final BusesValidation INSTANCE = new BusesValidation();

    private BusesValidation() {
    }

    public boolean checkBuses(Network network, ValidationConfig validationConfig, TableFormatterConfig formatterConfig, Path file) throws IOException {
        Objects.requireNonNull(network);
        Objects.requireNonNull(validationConfig);
        Objects.requireNonNull(formatterConfig);
        Objects.requireNonNull(file);
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            return checkBuses(network, validationConfig, formatterConfig, writer);
        }
    }

    public boolean checkBuses(Network network, ValidationConfig config, Path file) throws IOException {
        return checkBuses(network, config, TABLE_FORMATTER_CONFIG.get(), file);
    }

    public boolean checkBuses(Network network, ValidationConfig validationConfig, TableFormatterConfig formatterConfig, Writer writer) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(validationConfig);
        Objects.requireNonNull(formatterConfig);
        Objects.requireNonNull(writer);
        try (ValidationWriter busesWriter = ValidationUtils.createValidationWriter(network.getId(), validationConfig, formatterConfig, writer, ValidationType.BUSES)) {
            return checkBuses(network, validationConfig, busesWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkBuses(Network network, ValidationConfig config, Writer writer) {
        return checkBuses(network, config, TABLE_FORMATTER_CONFIG.get(), writer);
    }

    public boolean checkBuses(Network network, ValidationConfig config, ValidationWriter busesWriter) {
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

    public boolean checkBuses(Bus bus, ValidationConfig validationConfig, TableFormatterConfig formatterConfig, Writer writer) {
        Objects.requireNonNull(bus);
        Objects.requireNonNull(validationConfig);
        Objects.requireNonNull(formatterConfig);
        Objects.requireNonNull(writer);

        try (ValidationWriter bussWriter = ValidationUtils.createValidationWriter(bus.getId(), validationConfig, formatterConfig, writer, ValidationType.BUSES)) {
            return checkBuses(bus, validationConfig, bussWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkBuses(Bus bus, ValidationConfig config, Writer writer) {
        return checkBuses(bus, config, TABLE_FORMATTER_CONFIG.get(), writer);
    }

    public boolean checkBuses(Bus bus, ValidationConfig config, ValidationWriter busesWriter) {
        Objects.requireNonNull(bus);
        Objects.requireNonNull(config);
        Objects.requireNonNull(busesWriter);
        double loadP = bus.getLoadStream().map(Load::getTerminal).mapToDouble(Terminal::getP).sum();
        double loadQ = bus.getLoadStream().map(Load::getTerminal).mapToDouble(Terminal::getQ).sum();
        double genP = bus.getGeneratorStream().map(Generator::getTerminal).mapToDouble(Terminal::getP).sum();
        double genQ = bus.getGeneratorStream().map(Generator::getTerminal).mapToDouble(Terminal::getQ).sum();
        double batP = bus.getBatteryStream().map(Battery::getTerminal).mapToDouble(Terminal::getP).sum();
        double batQ = bus.getBatteryStream().map(Battery::getTerminal).mapToDouble(Terminal::getQ).sum();
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
        double twtP = bus.getTwoWindingsTransformerStream().map(twt -> getBranchTerminal(twt, bus)).mapToDouble(Terminal::getP).sum();
        double twtQ = bus.getTwoWindingsTransformerStream().map(twt -> getBranchTerminal(twt, bus)).mapToDouble(Terminal::getQ).sum();
        double tltP = bus.getThreeWindingsTransformerStream().map(tlt -> getThreeWindingsTransformerTerminal(tlt, bus)).mapToDouble(Terminal::getP).sum();
        double tltQ = bus.getThreeWindingsTransformerStream().map(tlt -> getThreeWindingsTransformerTerminal(tlt, bus)).mapToDouble(Terminal::getQ).sum();
        boolean mainComponent = bus.isInMainConnectedComponent();
        return checkBuses(bus.getId(), loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, config, busesWriter);
    }

    private static Terminal getBranchTerminal(Branch branch, Bus bus) {
        if (branch.getTerminal1().isConnected() && bus.getId().equals(branch.getTerminal1().getBusView().getBus().getId())) {
            return branch.getTerminal1();
        } else {
            return branch.getTerminal2();
        }
    }

    private static Terminal getThreeWindingsTransformerTerminal(ThreeWindingsTransformer tlt, Bus bus) {
        if (tlt.getLeg1().getTerminal().isConnected() && bus.getId().equals(tlt.getLeg1().getTerminal().getBusView().getBus().getId())) {
            return tlt.getLeg1().getTerminal();
        } else if (tlt.getLeg2().getTerminal().isConnected() && bus.getId().equals(tlt.getLeg2().getTerminal().getBusView().getBus().getId())) {
            return tlt.getLeg2().getTerminal();
        } else {
            return tlt.getLeg3().getTerminal();
        }
    }

    public boolean checkBuses(String id, double loadP, double loadQ, double genP, double genQ, double batP, double batQ,
                                     double shuntP, double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ,
                                     double lineP, double lineQ, double danglingLineP, double danglingLineQ,
                                     double twtP, double twtQ, double tltP, double tltQ, boolean mainComponent, ValidationConfig validationConfig, TableFormatterConfig formatterConfig, Writer writer) {
        Objects.requireNonNull(id);
        Objects.requireNonNull(validationConfig);
        Objects.requireNonNull(formatterConfig);
        Objects.requireNonNull(writer);

        try (ValidationWriter busesWriter = ValidationUtils.createValidationWriter(id, validationConfig, formatterConfig, writer, ValidationType.BUSES)) {
            return checkBuses(id, loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ,
                              danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, validationConfig, busesWriter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean checkBuses(String id, double loadP, double loadQ, double genP, double genQ, double batP, double batQ, double shuntP, double shuntQ,
                                     double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ, double danglingLineP, double danglingLineQ,
                                     double twtP, double twtQ, double tltP, double tltQ, boolean mainComponent, ValidationConfig config, Writer writer) {
        return checkBuses(id, loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ, lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ,
                mainComponent, config, TABLE_FORMATTER_CONFIG.get(), writer);
    }

    public boolean checkBuses(String id, double loadP, double loadQ, double genP, double genQ, double batP, double batQ, double shuntP,
                                     double shuntQ, double svcP, double svcQ, double vscCSP, double vscCSQ, double lineP, double lineQ,
                                     double danglingLineP, double danglingLineQ, double twtP, double twtQ, double tltP, double tltQ,
                                     boolean mainComponent, ValidationConfig config, ValidationWriter busesWriter) {
        Objects.requireNonNull(id);
        boolean validated = true;

        double incomingP = genP + batP + shuntP + svcP + vscCSP + lineP + danglingLineP + twtP + tltP;
        double incomingQ = genQ + batQ + shuntQ + svcQ + vscCSQ + lineQ + danglingLineQ + twtQ + tltQ;
        if (ValidationUtils.isMainComponent(config, mainComponent)) {
            if (ValidationUtils.areNaN(config, incomingP, loadP) || Math.abs(incomingP + loadP) > config.getThreshold()) {
                LOGGER.warn("{} {}: {} P {} {}", ValidationType.BUSES, ValidationUtils.VALIDATION_ERROR, id, incomingP, loadP);
                validated = false;
            }
            if (ValidationUtils.areNaN(config, incomingQ, loadQ) || Math.abs(incomingQ + loadQ) > config.getThreshold()) {
                LOGGER.warn("{} {}: {} Q {} {}", ValidationType.BUSES, ValidationUtils.VALIDATION_ERROR, id, incomingQ, loadQ);
                validated = false;
            }
        }
        try {
            busesWriter.write(id, incomingP, incomingQ, loadP, loadQ, genP, genQ, batP, batQ, shuntP, shuntQ, svcP, svcQ, vscCSP, vscCSQ,
                              lineP, lineQ, danglingLineP, danglingLineQ, twtP, twtQ, tltP, tltQ, mainComponent, validated);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return validated;
    }
}
