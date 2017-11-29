/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ampl.converter;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.util.StringToIntMapper;
import com.powsybl.iidm.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplNetworkReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(AmplNetworkReader.class);

    private static final Pattern PATTERN = Pattern.compile("([^']\\S*|'.+?')\\s*");

    private final DataSource dataSource;

    private final Network network;

    private final StringToIntMapper<AmplSubset> mapper;

    public AmplNetworkReader(DataSource dataSource, Network network, StringToIntMapper<AmplSubset> mapper) {
        this.dataSource = dataSource;
        this.network = network;
        this.mapper = mapper;
    }

    private static AmplException createWrongNumberOfColumnException(int expected, int actual) {
        return new AmplException("Wrong number of columns " + actual + ", expected " + expected);
    }

    private void read(String suffix, int expectedTokenCount, Function<String[], Void> handler) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream(suffix, "txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimedLine = line.trim();

                // skip comments
                if (trimedLine.startsWith("#")) {
                    continue;
                }

                String[] tokens = trimedLine.split("( )+");
                if (tokens.length != expectedTokenCount) {
                    throw createWrongNumberOfColumnException(expectedTokenCount, tokens.length);
                }

                handler.apply(tokens);
            }
        }
    }

    public AmplNetworkReader readGenerators() throws IOException {
        // Bug fix, to avoid generators out of main cc to have a different target voltage while connected to same bus (Eurostag check)
        // In that case it will not be part of result file, so not overwritten. So first reset all target voltages to nominal voltage
        for (Generator g : network.getGenerators()) {
            g.setTargetV(g.getTerminal().getVoltageLevel().getNominalV());
        }

        read("_generators", 4, this::readGenerator);

        return this;
    }

    private Void readGenerator(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        float v = Float.parseFloat(tokens[1]);
        float p = Float.parseFloat(tokens[2]);
        float q = Float.parseFloat(tokens[3]);
        String id = mapper.getId(AmplSubset.GENERATOR, num);
        Generator g = network.getGenerator(id);
        if (g == null) {
            throw new AmplException("Invalid generator id '" + id + "'");
        }
        Terminal t = g.getTerminal();
        float vb = t.getVoltageLevel().getNominalV();
        g.setTargetV(v * vb);
        g.setTargetP(p);
        g.setTargetQ(q);
        g.getTerminal().setP(-p).setQ(-q);
        if (t.isConnected()) {
            if (p == 0 && q == 0) {
                t.disconnect();
            }
        } else {
            if (p != 0 || q != 0) {
                t.connect();
            }
        }

        return null;
    }

    public AmplNetworkReader readLoads() throws IOException {
        read("_loads", 3, this::readLoad);

        return this;
    }

    private Void readLoad(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        float pAfterCurtailment = Float.parseFloat(tokens[1]);
        float qAfterCurtailment = Float.parseFloat(tokens[2]);
        String id = mapper.getId(AmplSubset.LOAD, num);
        Load l = network.getLoad(id);
        if (l != null) {
            l.setP0(pAfterCurtailment).setQ0(qAfterCurtailment);
            l.getTerminal().setP(pAfterCurtailment).setQ(qAfterCurtailment);
        } else {
            DanglingLine dl = network.getDanglingLine(id);
            if (dl != null) {
                dl.setP0(pAfterCurtailment).setQ0(qAfterCurtailment);
                dl.getTerminal().setP(pAfterCurtailment).setQ(qAfterCurtailment);
            } else {
                throw new AmplException("Invalid load id '" + id + "'");
            }
        }

        return null;
    }

    public AmplNetworkReader readRatioTapChangers() throws IOException {
        read("_rtc", 2, this::readRatioTapChanger);

        return this;
    }

    private Void readRatioTapChanger(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int tap = Integer.parseInt(tokens[1]);
        String id = mapper.getId(AmplSubset.RATIO_TAP_CHANGER, num);
        if (id.endsWith(AmplConstants.LEG2_SUFFIX) || id.endsWith(AmplConstants.LEG3_SUFFIX)) {
            ThreeWindingsTransformer twt = network.getThreeWindingsTransformer(id.substring(0, id.length() - 3));
            if (twt == null) {
                throw new AmplException("Invalid three windings transformer id '" + id + "'");
            }
            if (id.endsWith(AmplConstants.LEG2_SUFFIX)) {
                RatioTapChanger rtc2 = twt.getLeg2().getRatioTapChanger();
                rtc2.setTapPosition(rtc2.getLowTapPosition() + tap - 1);
            } else if (id.endsWith(AmplConstants.LEG3_SUFFIX)) {
                RatioTapChanger rtc3 = twt.getLeg3().getRatioTapChanger();
                rtc3.setTapPosition(rtc3.getLowTapPosition() + tap - 1);
            } else {
                throw new AssertionError();
            }
        } else {
            TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id);
            if (twt == null) {
                throw new AmplException("Invalid two windings transformer id '" + id + "'");
            }
            RatioTapChanger rtc = twt.getRatioTapChanger();
            rtc.setTapPosition(rtc.getLowTapPosition() + tap - 1);
        }

        return null;
    }

    public AmplNetworkReader readPhaseTapChangers() throws IOException {
        read("_ptc", 2, this::readPhaseTapChanger);

        return this;
    }

    private Void readPhaseTapChanger(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int tap = Integer.parseInt(tokens[1]);
        String id = mapper.getId(AmplSubset.PHASE_TAP_CHANGER, num);
        TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id);
        if (twt == null) {
            throw new AmplException("Invalid two windings transformer id '" + id + "'");
        }
        PhaseTapChanger ptc = twt.getPhaseTapChanger();
        ptc.setTapPosition(ptc.getLowTapPosition() + tap - 1);

        return null;
    }

    public AmplNetworkReader readShunts() throws IOException {
        read("_shunts", 2, this::readShunt);

        return this;
    }

    private Void readShunt(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int sections = Integer.parseInt(tokens[1]);
        String id = mapper.getId(AmplSubset.SHUNT, num);
        ShuntCompensator sc = network.getShunt(id);
        if (sc == null) {
            throw new AmplException("Invalid shunt compensator id '" + id + "'");
        }
        sc.setCurrentSectionCount(sc.getbPerSection() > 0 ? sections : sc.getMaximumSectionCount() - sections);
        Terminal t = sc.getTerminal();
        if (t.isConnected()) {
            if (sc.getCurrentSectionCount() == 0) {
                t.disconnect();
            }
        } else {
            if (sc.getCurrentSectionCount() > 0) {
                t.connect();
            }
        }

        return null;
    }

    private static List<String> parseExceptIfBetweenQuotes(String str) {
        List<String> tokens = new ArrayList<>();
        Matcher m = PATTERN.matcher(str);
        while (m.find()) {
            tokens.add(m.group(1).replace("'", ""));
        }
        return tokens;
    }

    public AmplNetworkReader readMetrics(Map<String, String> metrics) throws IOException {
        Objects.requireNonNull(metrics);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream("_indic", "txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimedLine = line.trim();
                // skip comments
                if (trimedLine.startsWith("#")) {
                    continue;
                }
                List<String> tokens = parseExceptIfBetweenQuotes(trimedLine);
                if (tokens.size() != 1 && tokens.size() != 2) {
                    LOGGER.error("Wrong number of columns {} , expected 1 or 2: '{}'", tokens.size(), trimedLine);
                }
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
        return this;
    }
}
