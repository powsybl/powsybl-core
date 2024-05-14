/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.powsybl.ampl.converter.AmplConstants.DEFAULT_VARIANT_INDEX;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class AmplNetworkReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplNetworkReader.class);

    private static final Pattern PATTERN = Pattern.compile("([^'\\\"]\\S*|'.+?'|\\\".+?\\\")\\s*");

    private final ReadOnlyDataSource dataSource;

    private final Network network;
    private final int variantIndex;

    private final StringToIntMapper<AmplSubset> mapper;
    private final Map<String, Bus> buses;

    private final AmplNetworkUpdater networkUpdater;

    private final OutputFileFormat format;

    public AmplNetworkReader(ReadOnlyDataSource dataSource, Network network, int variantIndex,
                             StringToIntMapper<AmplSubset> mapper, AmplNetworkUpdaterFactory networkUpdater,
                             OutputFileFormat format) {
        this.dataSource = dataSource;
        this.network = network;
        this.mapper = mapper;
        this.networkUpdater = networkUpdater.create(mapper, network);
        this.buses = network.getBusView()
                            .getBusStream()
                            .collect(Collectors.toMap(Identifiable::getId, Function.identity()));
        this.variantIndex = variantIndex;
        this.format = format;
    }

    public AmplNetworkReader(ReadOnlyDataSource dataSource, Network network, int variantIndex,
                             StringToIntMapper<AmplSubset> mapper, AmplNetworkUpdaterFactory networkUpdater) {
        this(dataSource, network, variantIndex, mapper, networkUpdater, OutputFileFormat.getDefault());
    }

    public AmplNetworkReader(ReadOnlyDataSource dataSource, Network network, int variantIndex,
            StringToIntMapper<AmplSubset> mapper) {
        this(dataSource, network, variantIndex, mapper, new DefaultAmplNetworkUpdaterFactory());
    }

    public AmplNetworkReader(ReadOnlyDataSource dataSource, Network network, StringToIntMapper<AmplSubset> mapper) {
        this(dataSource, network, DEFAULT_VARIANT_INDEX, mapper);
    }

    private static AmplException createWrongNumberOfColumnException(int expected, int actual) {
        return new AmplException("Wrong number of columns " + actual + ", expected " + expected);
    }

    protected static List<String> parseExceptIfBetweenQuotes(String str) {
        List<String> tokens = new ArrayList<>();
        Matcher m = PATTERN.matcher(str);
        while (m.find()) {
            String tok = m.group(1).replace("'", "");
            tokens.add(tok);
        }
        return tokens;
    }

    private void read(String suffix, int expectedTokenCount, Function<String[], Void> handler) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                dataSource.newInputStream(suffix, format.getFileExtension()), format.getFileEncoding()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimedLine = line.trim();

                // skip comments
                if (trimedLine.startsWith("#")) {
                    continue;
                }

                String[] tokens = trimedLine.split(format.getTokenSeparator());
                if (tokens.length != expectedTokenCount) {
                    throw createWrongNumberOfColumnException(expectedTokenCount, tokens.length);
                }

                //check if it is the right network
                if (variantIndex == Integer.parseInt(tokens[0])) {
                    handler.apply(tokens);
                }
            }
        }
    }

    public AmplNetworkReader readGenerators() throws IOException {
        // Bug fix, to avoid generators out of main cc to have a different target voltage while connected to same bus (Eurostag check)
        // In that case it will not be part of result file, so not overwritten. So first reset all target voltages to nominal voltage
        for (Generator g : network.getGenerators()) {
            g.setTargetV(g.getTerminal().getVoltageLevel().getNominalV());
        }

        read("_generators", 9, this::readGenerator);

        return this;
    }

    private Void readGenerator(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int busNum = Integer.parseInt(tokens[2]);
        boolean vregul = Boolean.parseBoolean(tokens[3]);
        double targetV = readDouble(tokens[4]);
        double targetP = readDouble(tokens[5]);
        double targetQ = readDouble(tokens[6]);
        double p = readDouble(tokens[7]);
        double q = readDouble(tokens[8]);
        String id = mapper.getId(AmplSubset.GENERATOR, num);
        Generator g = network.getGenerator(id);
        if (g == null) {
            throw new AmplException("Invalid generator id '" + id + "'");
        }

        networkUpdater.updateNetworkGenerators(g, busNum, vregul, targetV, targetP, targetQ, p, q);

        return null;
    }

    public AmplNetworkReader readBatteries() throws IOException {
        read("_batteries", 7, this::readBattery);

        return this;
    }

    private Void readBattery(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int busNum = Integer.parseInt(tokens[2]);
        double targetP = readDouble(tokens[3]);
        double targetQ = readDouble(tokens[4]);
        double p = readDouble(tokens[5]);
        double q = readDouble(tokens[6]);

        String id = mapper.getId(AmplSubset.BATTERY, num);
        Battery b = network.getBattery(id);
        if (b == null) {
            throw new AmplException("Invalid battery id '" + id + "'");
        }
        networkUpdater.updateNetworkBattery(b, busNum, targetP, targetQ, p, q);

        return null;
    }

    public AmplNetworkReader readLoads() throws IOException {
        read("_loads", 7, this::readLoad);

        return this;
    }

    private Void readLoad(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int busNum = Integer.parseInt(tokens[2]);
        double p = readDouble(tokens[3]);
        double q = readDouble(tokens[4]);
        double p0 = readDouble(tokens[5]);
        double q0 = readDouble(tokens[6]);
        String id = mapper.getId(AmplSubset.LOAD, num);
        Load l = network.getLoad(id);
        networkUpdater.updateNetworkLoad(l, network, id, busNum, p, q, p0, q0);

        return null;
    }

    public AmplNetworkReader readRatioTapChangers() throws IOException {
        read("_rtc", 3, this::readRatioTapChanger);

        return this;
    }

    private Void readRatioTapChanger(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int tap = Integer.parseInt(tokens[2]);
        String id = mapper.getId(AmplSubset.RATIO_TAP_CHANGER, num);
        networkUpdater.updateNetworkRatioTapChanger(network, id, tap);

        return null;
    }

    public AmplNetworkReader readPhaseTapChangers() throws IOException {
        read("_ptc", 3, this::readPhaseTapChanger);

        return this;
    }

    private Void readPhaseTapChanger(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int tap = Integer.parseInt(tokens[2]);
        String id = mapper.getId(AmplSubset.PHASE_TAP_CHANGER, num);
        networkUpdater.updateNetworkPhaseTapChanger(network, id, tap);

        return null;
    }

    public AmplNetworkReader readShunts() throws IOException {
        read("_shunts", 6, this::readShunt);

        return this;
    }

    private Void readShunt(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int busNum = Integer.parseInt(tokens[2]);
        double b = readDouble(tokens[3]);
        double q = readDouble(tokens[4]);
        int sections = Integer.parseInt(tokens[5]);

        String id = mapper.getId(AmplSubset.SHUNT, num);
        ShuntCompensator sc = network.getShuntCompensator(id);
        if (sc == null) {
            throw new AmplException("Invalid shunt compensator id '" + id + "'");
        }

        networkUpdater.updateNetworkShunt(sc, busNum, q, b, sections);

        return null;
    }

    public AmplNetworkReader readBuses() throws IOException {
        read("_buses", 4, this::readBus);

        return this;
    }

    private Void readBus(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        double v = readDouble(tokens[2]);
        double theta = readDouble(tokens[3]);

        String id = mapper.getId(AmplSubset.BUS, num);
        Bus bus = buses.get(id);
        if (bus == null) {
            throw new AmplException("Invalid bus id '" + id + "'");
        }

        networkUpdater.updateNetworkBus(bus, v, theta);

        return null;
    }

    public AmplNetworkReader readBranches() throws IOException {
        read("_branches", 8, this::readBranch);

        return this;
    }

    private Void readBranch(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int busNum = Integer.parseInt(tokens[2]);
        int busNum2 = Integer.parseInt(tokens[3]);
        double p1 = readDouble(tokens[4]);
        double p2 = readDouble(tokens[5]);
        double q1 = readDouble(tokens[6]);
        double q2 = readDouble(tokens[7]);

        String id = mapper.getId(AmplSubset.BRANCH, num);

        Branch br = network.getBranch(id);
        networkUpdater.updateNetworkBranch(br, network, id, busNum, busNum2, p1, p2, q1, q2);

        return null;
    }

    public AmplNetworkReader readHvdcLines() throws IOException {
        read("_hvdc", 4, this::readHvdcLine);

        return this;
    }

    private Void readHvdcLine(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        String converterMode = tokens[2].replace("\"", "");
        double targetP = readDouble(tokens[3]);

        String id = mapper.getId(AmplSubset.HVDC_LINE, num);
        HvdcLine hl = network.getHvdcLine(id);
        if (hl == null) {
            throw new AmplException("Invalid HvdcLine id '" + id + "'");
        }
        networkUpdater.updateNetworkHvdcLine(hl, converterMode, targetP);

        return null;
    }

    public AmplNetworkReader readStaticVarcompensator() throws IOException {
        read("_static_var_compensators", 6, this::readSvc);

        return this;
    }

    private Void readSvc(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int busNum = Integer.parseInt(tokens[2]);
        boolean vregul = Boolean.parseBoolean(tokens[3]);
        double targetV = readDouble(tokens[4]);
        double q = readDouble(tokens[5]);

        String id = mapper.getId(AmplSubset.STATIC_VAR_COMPENSATOR, num);
        StaticVarCompensator svc = network.getStaticVarCompensator(id);
        if (svc == null) {
            throw new AmplException("Invalid StaticVarCompensator id '" + id + "'");
        }

        networkUpdater.updateNetworkSvc(svc, busNum, vregul, targetV, q);

        return null;
    }

    public AmplNetworkReader readLccConverterStations() throws IOException {
        read("_lcc_converter_stations", 5, this::readLcc);

        return this;
    }

    private Void readLcc(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int busNum = Integer.parseInt(tokens[2]);
        double p = readDouble(tokens[3]);
        double q = readDouble(tokens[4]);

        String id = mapper.getId(AmplSubset.LCC_CONVERTER_STATION, num);
        LccConverterStation lcc = network.getLccConverterStation(id);
        if (lcc == null) {
            throw new AmplException("Invalid bus id '" + id + "'");
        }

        networkUpdater.updateNetworkLcc(lcc, busNum, p, q);

        return null;
    }

    public AmplNetworkReader readVscConverterStations() throws IOException {
        read("_vsc_converter_stations", 8, this::readVsc);

        return this;
    }

    private Void readVsc(String[] tokens) {
        int num = Integer.parseInt(tokens[1]);
        int busNum = Integer.parseInt(tokens[2]);
        boolean vregul = Boolean.parseBoolean(tokens[3]);
        double targetV = readDouble(tokens[4]);
        double targetQ = readDouble(tokens[5]);
        double p = readDouble(tokens[6]);
        double q = readDouble(tokens[7]);

        String id = mapper.getId(AmplSubset.VSC_CONVERTER_STATION, num);
        VscConverterStation vsc = network.getVscConverterStation(id);
        networkUpdater.updateNetworkVsc(vsc, busNum, vregul, targetV, targetQ, p, q);

        return null;
    }

    public AmplNetworkReader readMetrics(Map<String, String> metrics) throws IOException {
        Objects.requireNonNull(metrics);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(dataSource.newInputStream("_indic", "txt"), format.getFileEncoding()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimedLine = line.trim();
                // skip comments
                if (trimedLine.startsWith("#")) {
                    continue;
                }
                List<String> tokens = parseExceptIfBetweenQuotes(trimedLine);
                if (tokens.size() > 2) {
                    LOGGER.error("Wrong number of columns {} , expected 0, 1 or 2: '{}'", tokens.size(), trimedLine);
                }
                if (!tokens.isEmpty()) { // allow empty lines
                    String name = tokens.get(0);
                    if (name.length() > 0) {
                        String value = "";
                        if (tokens.size() == 2) {
                            value = tokens.get(1);
                        }
                        metrics.put(name, value);
                    }
                }
            }
        }
        return this;
    }

    private double readDouble(String d) {
        return Float.parseFloat(d) != AmplConstants.INVALID_FLOAT_VALUE ? Double.parseDouble(d) : Double.NaN;
    }

}
