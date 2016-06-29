/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.export.ampl;

import eu.itesla_project.commons.util.StringToIntMapper;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.network.DanglingLine;
import eu.itesla_project.iidm.network.Generator;
import eu.itesla_project.iidm.network.Load;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.network.PhaseTapChanger;
import eu.itesla_project.iidm.network.RatioTapChanger;
import eu.itesla_project.iidm.network.ShuntCompensator;
import eu.itesla_project.iidm.network.Terminal;
import eu.itesla_project.iidm.network.ThreeWindingsTransformer;
import eu.itesla_project.iidm.network.TwoWindingsTransformer;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AmplNetworkReader implements AmplConstants {

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

    public AmplNetworkReader readGenerators() throws IOException {
        // Bug fix, to avoid generators out of main cc to have a different target voltage while connected to same bus (Eurostag check)
        // In that case it will not be part of result file, so not overwritten. So first reset all target voltages to nominal voltage
        for (Generator g : network.getGenerators()) {
            g.setTargetV(g.getTerminal().getVoltageLevel().getNominalV());
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream("_generators", "txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimedLine = line.trim();
                // skip comments
                if (trimedLine.startsWith("#")) {
                    continue;
                }
                String[] tokens = trimedLine.split("( )+");
                if (tokens.length != 4) {
                    throw new RuntimeException("Wrong number of columns "
                            + tokens.length + ", expected 4");
                }
                int num = Integer.valueOf(tokens[0]);
                float v = Float.valueOf(tokens[1]);
                float p = Float.valueOf(tokens[2]);
                float q = Float.valueOf(tokens[3]);
                String id = mapper.getId(AmplSubset.GENERATOR, num);
                Generator g = network.getGenerator(id);
                if (g == null) {
                    throw new RuntimeException("Invalid generator id '" + id + "'");
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
            }
        }
        return this;
    }

    public AmplNetworkReader readLoads() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream("_loads", "txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimedLine = line.trim();
                // skip comments
                if (trimedLine.startsWith("#")) {
                    continue;
                }
                String[] tokens = trimedLine.split("( )+");
                if (tokens.length != 3) {
                    throw new RuntimeException("Wrong number of columns "
                            + tokens.length + ", expected 3");
                }
                int num = Integer.valueOf(tokens[0]);
                float pAfterCurtailment = Float.valueOf(tokens[1]);
                float qAfterCurtailment = Float.valueOf(tokens[2]);
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
                        throw new RuntimeException("Invalid load id '" + id + "'");
                    }
                }
            }
        }
        return this;
    }

    public AmplNetworkReader readRatioTapChangers() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream("_rtc", "txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimedLine = line.trim();
                // skip comments
                if (trimedLine.startsWith("#")) {
                    continue;
                }
                String[] tokens = trimedLine.split("( )+");
                if (tokens.length != 2) {
                    throw new RuntimeException("Wrong number of columns "
                            + tokens.length + ", expected 2");
                }
                int num = Integer.valueOf(tokens[0]);
                int tap = Integer.valueOf(tokens[1]);
                String id = mapper.getId(AmplSubset.RATIO_TAP_CHANGER, num);
                if (id.endsWith("_leg2") || id.endsWith("_leg3")) {
                    ThreeWindingsTransformer twt = network.getThreeWindingsTransformer(id.substring(0, id.length()-3));
                    if (twt == null) {
                        throw new RuntimeException("Invalid three windings transformer id '" + id + "'");
                    }
                    if (id.endsWith("_leg2")) {
                        RatioTapChanger rtc2 = twt.getLeg2().getRatioTapChanger();
                        rtc2.setTapPosition(rtc2.getLowTapPosition() + tap - 1);
                    } else if (id.endsWith("_leg3")) {
                        RatioTapChanger rtc3 = twt.getLeg3().getRatioTapChanger();
                        rtc3.setTapPosition(rtc3.getLowTapPosition() + tap - 1);
                    } else {
                        throw new InternalError();
                    }
                } else {
                    TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id);
                    if (twt == null) {
                        throw new RuntimeException("Invalid two windings transformer id '" + id + "'");
                    }
                    RatioTapChanger rtc = twt.getRatioTapChanger();
                    rtc.setTapPosition(rtc.getLowTapPosition() + tap - 1);
                }
            }
        }
        return this;
    }

    public AmplNetworkReader readPhaseTapChangers() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream("_ptc", "txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimedLine = line.trim();
                // skip comments
                if (trimedLine.startsWith("#")) {
                    continue;
                }
                String[] tokens = trimedLine.split("( )+");
                if (tokens.length != 2) {
                    throw new RuntimeException("Wrong number of columns "
                            + tokens.length + ", expected 2");
                }
                int num = Integer.valueOf(tokens[0]);
                int tap = Integer.valueOf(tokens[1]);
                String id = mapper.getId(AmplSubset.PHASE_TAP_CHANGER, num);
                TwoWindingsTransformer twt = network.getTwoWindingsTransformer(id);
                if (twt == null) {
                    throw new RuntimeException("Invalid two windings transformer id '" + id + "'");
                }
                PhaseTapChanger ptc = twt.getPhaseTapChanger();
                ptc.setTapPosition(ptc.getLowTapPosition() + tap - 1);
            }
        }
        return this;
    }

    public AmplNetworkReader readShunts() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(dataSource.newInputStream("_shunts", "txt"), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimedLine = line.trim();
                // skip comments
                if (trimedLine.startsWith("#")) {
                    continue;
                }
                String[] tokens = trimedLine.split("( )+");
                if (tokens.length != 2) {
                    throw new RuntimeException("Wrong number of columns "
                            + tokens.length + ", expected 2");
                }
                int num = Integer.valueOf(tokens[0]);
                int sections = Integer.valueOf(tokens[1]);
                String id = mapper.getId(AmplSubset.SHUNT, num);
                ShuntCompensator sc = network.getShunt(id);
                if (sc == null) {
                    throw new RuntimeException("Invalid shunt compensator id '" + id + "'");
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
            }
        }
        return this;
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
