/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.*;
import com.univocity.parsers.annotations.Parsed;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class EuropeanLvTestFeederFactory {

    private EuropeanLvTestFeederFactory() {
    }

    private static void createSource(Network network) {
        INIConfiguration iniConfiguration = new INIConfiguration();
        try (Reader reader = new InputStreamReader(Objects.requireNonNull(EuropeanLvTestFeederFactory.class.getResourceAsStream("/europeanLvTestFeeder/Source.csv")), StandardCharsets.UTF_8)) {
            iniConfiguration.read(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ConfigurationException e) {
            throw new PowsyblException(e);
        }
        SubnodeConfiguration source = iniConfiguration.getSection("Source");
        double voltage = Integer.parseInt(source.getString("Voltage").replace(" kV", ""));
        double pu = Double.parseDouble(source.getString("pu"));
        double isc3 = Integer.parseInt(source.getString("ISC3").replace(" A", ""));
        double isc1 = Integer.parseInt(source.getString("ISC1").replace(" A", ""));
        Substation sourceSubstation = network.newSubstation()
                .setId("SourceSubstation")
                .add();
        VoltageLevel sourceVoltageLevel = sourceSubstation.newVoltageLevel()
                .setId("SourceVoltageLevel")
                .setNominalV(voltage)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        sourceVoltageLevel.getBusBreakerView().newBus()
                .setId("SourceBus")
                .add();
        Generator sourceGenerator = sourceVoltageLevel.newGenerator()
                .setId("SourceGenerator")
                .setBus("SourceBus")
                .setMinP(0)
                .setMaxP(0)
                .setTargetP(0)
                .setVoltageRegulatorOn(true)
                .setTargetV(voltage * pu)
                .add();
        sourceVoltageLevel.newExtension(SlackTerminalAdder.class)
                .withTerminal(sourceGenerator.getTerminal())
                .add();
        double zn = pu * voltage / (Math.sqrt(3) * isc1);
        double zz = pu * voltage / (Math.sqrt(3) * isc3);
        double rn = Math.sqrt(zn * zn / 101);
        double xn = 10 * rn;
        double rz = Math.sqrt(zz * zz / 101);
        double xz = 10 * rz;
        sourceGenerator.newExtension(GeneratorFortescueAdder.class)
                .withRn(rn)
                .withXn(xn)
                .withRz(rz)
                .withXz(xz)
                .add();
    }

    public static class BusCoord {
        @Parsed(field = "Busname")
        int busName;

        @Parsed
        double x;

        @Parsed
        double y;
    }

    public static class Line {
        @Parsed(field = "Name")
        String name;

        @Parsed(field = "Bus1")
        int bus1;

        @Parsed(field = "Bus2")
        int bus2;

        @Parsed(field = "Phases")
        String phases;

        @Parsed(field = "Length")
        double length;

        @Parsed(field = "Units")
        String units;

        @Parsed(field = "LineCode")
        String code;
    }

    public static class LineCode {
        @Parsed(field = "Name")
        String name;

        @Parsed
        int nphases;

        @Parsed(field = "R1")
        double r1;

        @Parsed(field = "X1")
        double x1;

        @Parsed(field = "R0")
        double r0;

        @Parsed(field = "X0")
        double x0;

        @Parsed(field = "C1")
        double c1;

        @Parsed(field = "C0")
        double c0;

        @Parsed(field = "Units")
        String units;
    }

    public static class Load {
        @Parsed(field = "Name")
        String name;

        @Parsed
        int numPhases;

        @Parsed(field = "Bus")
        int bus;

        @Parsed
        char phases;

        @Parsed
        double kV;

        @Parsed(field = "Model")
        int model;

        @Parsed(field = "Connection")
        String connection;

        @Parsed
        double kW;

        @Parsed(field = "PF")
        double pf;

        @Parsed(field = "Yearly")
        String yearly;
    }

    private static <T> List<T> parseCsv(String resourceName, Class<T> clazz) {
        try (Reader inputReader = new InputStreamReader(Objects.requireNonNull(EuropeanLvTestFeederFactory.class.getResourceAsStream(resourceName)), StandardCharsets.UTF_8)) {
            BeanListProcessor<T> rowProcessor = new BeanListProcessor<>(clazz);
            CsvParserSettings settings = new CsvParserSettings();
            settings.setHeaderExtractionEnabled(true);
            settings.setProcessor(rowProcessor);
            CsvParser parser = new CsvParser(settings);
            parser.parse(inputReader);
            return rowProcessor.getBeans();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String getBusId(int busName) {
        return "Bus-" + busName;
    }

    private static String getVoltageLevelId(int busName) {
        return "VoltageLevel-" + busName;
    }

    private static void createBuses(Network network) {
        for (BusCoord busCoord : parseCsv("/europeanLvTestFeeder/Buscoords.csv", BusCoord.class)) {
            Substation s = network.newSubstation()
                    .setId("Substation-" + busCoord.busName)
                    .add();
            s.newExtension(SubstationPositionAdder.class)
                    .withCoordinate(new Coordinate(busCoord.y, busCoord.x)) // FIXME how to unproject ?
                    .add();
            VoltageLevel vl = s.newVoltageLevel()
                    .setId(getVoltageLevelId(busCoord.busName))
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .setNominalV(1)
                    .add();
            vl.getBusBreakerView().newBus()
                    .setId(getBusId(busCoord.busName))
                    .add();
        }
    }

    private static void createLines(Network network) {
        Map<String, LineCode> lineCodes = new HashMap<>();
        for (LineCode lineCode : parseCsv("/europeanLvTestFeeder/LineCodes.csv", LineCode.class)) {
            lineCodes.put(lineCode.name, lineCode);
        }
        for (Line line : parseCsv("/europeanLvTestFeeder/Lines.csv", Line.class)) {
            LineCode lineCode = lineCodes.get(line.code);
            var l = network.newLine()
                    .setId("Line-" + line.bus1 + "-" + line.bus2)
                    .setVoltageLevel1(getVoltageLevelId(line.bus1))
                    .setBus1(getBusId(line.bus1))
                    .setVoltageLevel2(getVoltageLevelId(line.bus2))
                    .setBus2(getBusId(line.bus2))
                    .setR(lineCode.r1 * line.length)
                    .setX(lineCode.x1 * line.length)
                    .add();
            l.newExtension(LineFortescueAdder.class)
                    .withRz(lineCode.r0 * line.length)
                    .withXz(lineCode.x0 * line.length)
                    .add();
        }
    }

    private static void createLoads(Network network) {
        for (Load load : parseCsv("/europeanLvTestFeeder/Loads.csv", Load.class)) {
            var vl = network.getVoltageLevel(getVoltageLevelId(load.bus));
            vl.newLoad()
                    .setId("Load-" + load.bus)
                    .setBus(getBusId(load.bus))
                    .setP0(load.kW / 1000)
                    .setQ0(load.kW  / 1000 * load.pf)
                    .add();
        }
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork("EuropeanLvTestFeeder", "csv");
        createSource(network);
        createBuses(network);
        createLines(network);
        createLoads(network);
        return network;
    }
}
