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
import com.powsybl.iidm.network.HvdcLine.ConvertersMode;
import com.powsybl.iidm.network.StaticVarCompensator.RegulationMode;

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
import java.util.stream.Collectors;

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
    private final Map<String, Bus> buses;

    public AmplNetworkReader(DataSource dataSource, Network network, StringToIntMapper<AmplSubset> mapper) {
        this.dataSource = dataSource;
        this.network = network;
        this.mapper = mapper;
        this.buses = network.getBusView().getBusStream().collect(Collectors.toMap(Identifiable::getId, Function.identity()));
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

        read("_generators", 8, this::readGenerator);

        return this;
    }

    private Void readGenerator(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int busNum = Integer.parseInt(tokens[1]);
        boolean vregul = Boolean.parseBoolean(tokens[2]);
        double targetV = readDouble(tokens[3]);
        double targetP = readDouble(tokens[4]);
        double targetQ = readDouble(tokens[5]);
        double p = readDouble(tokens[6]);
        double q = readDouble(tokens[7]);
        String id = mapper.getId(AmplSubset.GENERATOR, num);
        Generator g = network.getGenerator(id);
        if (g == null) {
            throw new AmplException("Invalid generator id '" + id + "'");
        }

        g.setVoltageRegulatorOn(vregul);

        g.setTargetP(targetP);
        g.setTargetQ(targetQ);

        Terminal t = g.getTerminal();
        t.setP(p).setQ(q);

        double vb = t.getVoltageLevel().getNominalV();
        g.setTargetV(targetV * vb);

        busConnection(t, busNum);

        return null;
    }

    public AmplNetworkReader readLoads() throws IOException {
        read("_loads", 6, this::readLoad);

        return this;
    }

    private Void readLoad(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int busNum = Integer.parseInt(tokens[1]);
        double p = readDouble(tokens[2]);
        double q = readDouble(tokens[3]);
        double p0 = readDouble(tokens[4]);
        double q0 = readDouble(tokens[5]);
        String id = mapper.getId(AmplSubset.LOAD, num);
        Load l = network.getLoad(id);
        if (l != null) {
            l.setP0(p0).setQ0(q0);
            l.getTerminal().setP(p).setQ(q);
            busConnection(l.getTerminal(), busNum);
        } else {
            DanglingLine dl = network.getDanglingLine(id);
            if (dl != null) {
                dl.setP0(p0).setQ0(q0);
                dl.getTerminal().setP(p).setQ(q);
                busConnection(dl.getTerminal(), busNum);
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
            ThreeWindingsTransformer twt = network.getThreeWindingsTransformer(id.substring(0, id.indexOf(AmplConstants.LEG2_SUFFIX)));
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
        read("_shunts", 5, this::readShunt);

        return this;
    }

    private Void readShunt(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int busNum = Integer.parseInt(tokens[1]);

        double q = readDouble(tokens[3]);
        int sections = Integer.parseInt(tokens[4]);

        String id = mapper.getId(AmplSubset.SHUNT, num);
        ShuntCompensator sc = network.getShuntCompensator(id);
        if (sc == null) {
            throw new AmplException("Invalid shunt compensator id '" + id + "'");
        }

        sc.setCurrentSectionCount(Math.max(0, Math.min(sc.getMaximumSectionCount(), sections)));
        Terminal t = sc.getTerminal();
        t.setQ(q);

        busConnection(t, busNum);

        return null;
    }

    public AmplNetworkReader readBuses() throws IOException {
        read("_buses", 3, this::readBus);

        return this;
    }

    private Void readBus(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        double v = readDouble(tokens[1]);
        double theta = readDouble(tokens[2]);

        String id = mapper.getId(AmplSubset.BUS, num);
        Bus bus = buses.get(id);

        if (bus != null) {
            bus.setV(v * bus.getVoltageLevel().getNominalV());
            bus.setAngle(Math.toDegrees(theta));
        } else {
            throw new AmplException("Invalid bus id '" + id + "'");
        }

        return null;
    }

    public AmplNetworkReader readBranches() throws IOException {
        read("_branches", 7, this::readBranch);

        return this;
    }

    private Void readBranch(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int busNum = Integer.parseInt(tokens[1]);
        int busNum2 = Integer.parseInt(tokens[2]);
        double p1 = readDouble(tokens[3]);
        double p2 = readDouble(tokens[4]);
        double q1 = readDouble(tokens[5]);
        double q2 = readDouble(tokens[6]);

        String id = mapper.getId(AmplSubset.BRANCH, num);

        Branch br = network.getBranch(id);
        if (br != null) {
            br.getTerminal1().setP(p1).setQ(q1);
            br.getTerminal2().setP(p2).setQ(q2);
            busConnection(br.getTerminal1(), busNum);
            busConnection(br.getTerminal2(), busNum2);
            return null;
        }

        if (readThreeWindingsTransformerBranch(id, p1, q1, busNum)) {
            return null;
        }

        DanglingLine dl = network.getDanglingLine(id);
        if (dl != null) {
            dl.getTerminal().setP(p1).setQ(q1);
            busConnection(dl.getTerminal(), busNum);
        } else {
            throw new AmplException("Invalid branch id '" + id + "'");
        }

        return null;
    }

    private boolean readThreeWindingsTransformerBranch(String id, double p, double q, int busNum) {
        if (id.endsWith(AmplConstants.LEG1_SUFFIX)) {
            ThreeWindingsTransformer tht = network.getThreeWindingsTransformer(id.substring(0, id.indexOf(AmplConstants.LEG1_SUFFIX)));
            if (tht != null) {
                tht.getLeg1().getTerminal().setP(p).setQ(q);
                busConnection(tht.getLeg1().getTerminal(), busNum);
            } else {
                throw new AmplException("Invalid branch (leg1) id '" + id + "'");
            }
        } else if (id.endsWith(AmplConstants.LEG2_SUFFIX)) {
            ThreeWindingsTransformer tht = network.getThreeWindingsTransformer(id.substring(0, id.indexOf(AmplConstants.LEG2_SUFFIX)));
            if (tht != null) {
                tht.getLeg2().getTerminal().setP(p).setQ(q);
                busConnection(tht.getLeg1().getTerminal(), busNum);
            } else {
                throw new AmplException("Invalid branch (leg2) id '" + id + "'");
            }
        } else if (id.endsWith(AmplConstants.LEG3_SUFFIX)) {
            ThreeWindingsTransformer tht = network.getThreeWindingsTransformer(id.substring(0, id.indexOf(AmplConstants.LEG3_SUFFIX)));
            if (tht != null) {
                tht.getLeg3().getTerminal().setP(p).setQ(q);
                busConnection(tht.getLeg1().getTerminal(), busNum);
            } else {
                throw new AmplException("Invalid branch (leg3) id '" + id + "'");
            }
        } else {
            return false;
        }
        return true;
    }

    public AmplNetworkReader readHvdcLines() throws IOException {
        read("_hvdc", 3, this::readHvdcLine);

        return this;
    }

    private Void readHvdcLine(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        String converterMode = tokens[1].replace("\"", "");
        double targetP = readDouble(tokens[2]);

        String id = mapper.getId(AmplSubset.HVDC_LINE, num);

        HvdcLine hl = network.getHvdcLine(id);

        if (hl == null) {
            throw new AmplException("Invalid HvdcLine id '" + id + "'");
        }
        hl.setConvertersMode(ConvertersMode.valueOf(converterMode));
        hl.setActivePowerSetpoint(targetP);

        return null;
    }


    public AmplNetworkReader readStaticVarcompensator() throws IOException {
        read("_static_var_compensators", 5, this::readSvc);

        return this;
    }

    private Void readSvc(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int busNum = Integer.parseInt(tokens[1]);
        boolean vregul = Boolean.parseBoolean(tokens[2]);
        double targetV = readDouble(tokens[3]);
        double q = readDouble(tokens[4]);

        String id = mapper.getId(AmplSubset.STATIC_VAR_COMPENSATOR, num);
        StaticVarCompensator svc = network.getStaticVarCompensator(id);
        if (svc == null) {
            throw new AmplException("Invalid StaticVarCompensator id '" + id + "'");
        }

        if (vregul) {
            svc.setRegulationMode(RegulationMode.VOLTAGE);
        } else {
            if (q == 0) {
                svc.setRegulationMode(RegulationMode.OFF);
            } else {
                svc.setReactivePowerSetPoint(-q);
                svc.setRegulationMode(RegulationMode.REACTIVE_POWER);
            }
        }


        Terminal t = svc.getTerminal();
        t.setQ(q);
        double nominalV = t.getVoltageLevel().getNominalV();
        svc.setVoltageSetPoint(targetV * nominalV);

        busConnection(t, busNum);

        return null;
    }

    public AmplNetworkReader readLccConverterStations() throws IOException {
        read("_lcc_converter_stations", 4, this::readLcc);

        return this;
    }

    private Void readLcc(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int busNum = Integer.parseInt(tokens[1]);
        double p = readDouble(tokens[2]);
        double q = readDouble(tokens[3]);

        String id = mapper.getId(AmplSubset.LCC_CONVERTER_STATION, num);
        LccConverterStation lcc = network.getLccConverterStation(id);
        lcc.getTerminal().setP(p).setQ(q);
        busConnection(lcc.getTerminal(), busNum);

        return null;
    }

    public AmplNetworkReader readVscConverterStations() throws IOException {
        read("_vsc_converter_stations", 7, this::readVsc);

        return this;
    }

    private Void readVsc(String[] tokens) {
        int num = Integer.parseInt(tokens[0]);
        int busNum = Integer.parseInt(tokens[1]);
        boolean vregul = Boolean.parseBoolean(tokens[2]);
        double targetV = readDouble(tokens[3]);
        double targetQ = readDouble(tokens[4]);
        double p = readDouble(tokens[5]);
        double q = readDouble(tokens[6]);

        String id = mapper.getId(AmplSubset.VSC_CONVERTER_STATION, num);
        VscConverterStation vsc = network.getVscConverterStation(id);
        Terminal t = vsc.getTerminal();
        t.setP(p).setQ(q);


        vsc.setReactivePowerSetpoint(targetQ);
        vsc.setVoltageRegulatorOn(vregul);

        double vb = t.getVoltageLevel().getNominalV();
        vsc.setVoltageSetpoint(targetV * vb);

        busConnection(t, busNum);

        return null;
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

    private void busConnection(Terminal t, int busNum) {
        if (busNum == -1) {
            t.disconnect();
        } else {
            String busId = mapper.getId(AmplSubset.BUS, busNum);
            Bus connectable = AmplUtil.getConnectableBus(t);
            if (connectable != null && connectable.getId().equals(busId)) {
                t.connect();
            }
        }
    }

    private static List<String> parseExceptIfBetweenQuotes(String str) {
        List<String> tokens = new ArrayList<>();
        Matcher m = PATTERN.matcher(str);
        while (m.find()) {
            String tok = m.group(1).replace("'", "");
            tokens.add(tok);
        }
        return tokens;
    }

    private float readFloat(String f) {
        float res = Float.parseFloat(f);
        return res != AmplConstants.INVALID_FLOAT_VALUE ? res : Float.NaN;
    }

    private double readDouble(String d) {
        return Float.parseFloat(d) != AmplConstants.INVALID_FLOAT_VALUE ? Double.parseDouble(d) : Double.NaN;
    }

}
