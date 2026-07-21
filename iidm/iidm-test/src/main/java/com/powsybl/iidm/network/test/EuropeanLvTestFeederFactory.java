/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.test;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;
import com.powsybl.iidm.network.extensions.LineFortescueAdder;
import com.powsybl.iidm.network.extensions.LoadAsymmetricalAdder;
import com.powsybl.iidm.network.extensions.LoadConnectionType;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.extensions.TwoWindingsTransformerFortescueAdder;
import com.powsybl.iidm.network.extensions.WindingConnectionType;
import de.siegmar.fastcsv.reader.CommentStrategy;
import de.siegmar.fastcsv.reader.CsvReader;
import de.siegmar.fastcsv.reader.FieldModifiers;
import de.siegmar.fastcsv.reader.NamedCsvRecord;
import de.siegmar.fastcsv.reader.NamedCsvRecordHandler;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * European Low Voltage Test Feeder.
 * <p><a href="https://cmte.ieee.org/pes-testfeeders/resources/">PES test feeders</a></p>
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public final class EuropeanLvTestFeederFactory {

    private static final Map<Class<?>, Mapper<?>> MAPPERS = Map.of(
        BusCoord.class, (Mapper<BusCoord>) EuropeanLvTestFeederFactory::mapBusCoord,
        Line.class, (Mapper<Line>) EuropeanLvTestFeederFactory::mapLine,
        LineCode.class, (Mapper<LineCode>) EuropeanLvTestFeederFactory::mapLineCode,
        Load.class, (Mapper<Load>) EuropeanLvTestFeederFactory::mapLoad,
        Transformer.class, (Mapper<Transformer>) EuropeanLvTestFeederFactory::mapTransformer
    );

    private EuropeanLvTestFeederFactory() {
    }

    private static void createSource(Transformer transformer, Network network) {
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
                .setId(getSubstationId(transformer.bus2)) // so that transformer has both sides in same substation
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

    private static BusCoord mapBusCoord(NamedCsvRecord rec) {
        return new BusCoord(
            Integer.parseInt(rec.getField("Busname")),
            Double.parseDouble(rec.getField("x")),
            Double.parseDouble(rec.getField("y"))
        );
    }

    private static Line mapLine(NamedCsvRecord rec) {
        return new Line(
            rec.getField("Name"),
            Integer.parseInt(rec.getField("Bus1")),
            Integer.parseInt(rec.getField("Bus2")),
            rec.getField("Phases"),
            Double.parseDouble(rec.getField("Length")),
            rec.getField("Units"),
            rec.getField("LineCode")
        );
    }

    private static LineCode mapLineCode(final NamedCsvRecord rec) {
        return new LineCode(
            rec.getField("Name"),
            Integer.parseInt(rec.getField("nphases")),
            Double.parseDouble(rec.getField("R1")),
            Double.parseDouble(rec.getField("X1")),
            Double.parseDouble(rec.getField("R0")),
            Double.parseDouble(rec.getField("X0")),
            Double.parseDouble(rec.getField("C1")),
            Double.parseDouble(rec.getField("C0")),
            rec.getField("Units")
        );
    }

    private static Load mapLoad(final NamedCsvRecord rec) {
        return new Load(
            rec.getField("Name"),
            Integer.parseInt(rec.getField("numPhases")),
            Integer.parseInt(rec.getField("Bus")),
            rec.getField("phases").charAt(0),
            Double.parseDouble(rec.getField("kV")),
            Integer.parseInt(rec.getField("Model")),
            rec.getField("Connection"),
            Double.parseDouble(rec.getField("kW")),
            Double.parseDouble(rec.getField("PF")),
            rec.getField("Yearly")
        );
    }

    private static Transformer mapTransformer(final NamedCsvRecord rec) {
        return new Transformer(
            rec.getField("Name"),
            Integer.parseInt(rec.getField("phases")),
            rec.getField("bus1"),
            Integer.parseInt(rec.getField("bus2")),
            Double.parseDouble(rec.getField("kV_pri")),
            Double.parseDouble(rec.getField("kV_sec")),
            Double.parseDouble(rec.getField("MVA")),
            rec.getField("Conn_pri"),
            rec.getField("Conn_sec"),
            Double.parseDouble(rec.getField("%XHL")),
            Double.parseDouble(rec.getField("% resistance"))
        );
    }

    // Casting is safe here
    @SuppressWarnings("unchecked")
    private static <T> List<T> parseCsv(String resourceName, Class<T> clazz) {
        try (Reader inputReader = new InputStreamReader(Objects.requireNonNull(EuropeanLvTestFeederFactory.class.getResourceAsStream(resourceName)), StandardCharsets.UTF_8);
             CsvReader<NamedCsvRecord> csvReader = CsvReader.builder()
                 .commentStrategy(CommentStrategy.SKIP)
                 .build(NamedCsvRecordHandler.of(c -> c.fieldModifier(FieldModifiers.TRIM)), inputReader)) {
            Mapper<T> mapper = (Mapper<T>) MAPPERS.get(clazz);
            if (mapper == null) {
                throw new IllegalArgumentException("Unsupported class: " + clazz);
            }

            return csvReader.stream().map(mapper::apply).toList();
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

    private static String getSubstationId(int busName) {
        return "Substation-" + busName;
    }

    private static void createBuses(Network network) {
        for (BusCoord busCoord : parseCsv("/europeanLvTestFeeder/Buscoords.csv", BusCoord.class)) {
            String substationId = getSubstationId(busCoord.busName);
            Substation s = network.getSubstation(substationId);
            if (s == null) {
                s = network.newSubstation()
                        .setId(substationId)
                        .add();
            }
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

    private static LoadConnectionType getConnectionType(Load load) {
        if (load.connection.equals("wye")) {
            return LoadConnectionType.Y;
        }
        throw new PowsyblException("Unknown load connection: " + load.connection);
    }

    private static void createLoads(Network network) {
        for (Load load : parseCsv("/europeanLvTestFeeder/Loads.csv", Load.class)) {
            var vl = network.getVoltageLevel(getVoltageLevelId(load.bus));
            double p0 = load.kW / 1000;
            double q0 = p0 * load.pf;
            var l = vl.newLoad()
                    .setId("Load-" + load.bus)
                    .setBus(getBusId(load.bus))
                    .setP0(p0)
                    .setQ0(q0)
                    .add();
            double deltaPa = 0;
            double deltaQa = 0;
            double deltaPb = 0;
            double deltaQb = 0;
            double deltaPc = 0;
            double deltaQc = 0;
            switch (load.phases) {
                case 'A':
                    deltaPb = -p0;
                    deltaQb = -q0;
                    deltaPc = -p0;
                    deltaQc = -q0;
                    break;
                case 'B':
                    deltaPa = -p0;
                    deltaQa = -q0;
                    deltaPc = -p0;
                    deltaQc = -q0;
                    break;
                case 'C':
                    deltaPa = -p0;
                    deltaQa = -q0;
                    deltaPb = -p0;
                    deltaQb = -q0;
                    break;
                default:
                    throw new PowsyblException("Unknown phase: " + load.phases);
            }
            l.newExtension(LoadAsymmetricalAdder.class)
                    .withConnectionType(getConnectionType(load))
                    .withDeltaPa(deltaPa)
                    .withDeltaQa(deltaQa)
                    .withDeltaPb(deltaPb)
                    .withDeltaQb(deltaQb)
                    .withDeltaPc(deltaPc)
                    .withDeltaQc(deltaQc)
                    .add();
        }
    }

    private static WindingConnectionType getConnectionType(String conn) {
        switch (conn) {
            case "Delta":
                return WindingConnectionType.DELTA;
            case "Wye":
                return WindingConnectionType.Y;
            default:
                throw new PowsyblException("Connection type not supported: " + conn);
        }
    }

    private static void createTransformer(Transformer transformer, Network network) {
        String busId1 = transformer.bus1; // source
        String busId2 = getBusId(transformer.bus2);
        Bus bus1 = network.getBusBreakerView().getBus(busId1);
        Bus bus2 = network.getBusBreakerView().getBus(busId2);
        Substation s = bus1.getVoltageLevel().getSubstation().orElseThrow();
        double sb = 1; // 1 mva
        double zb = transformer.kvPri * transformer.kvPri / sb;
        double r = transformer.resistance / 100 / transformer.mva * zb;
        double x = transformer.xhl / 100 / transformer.mva * zb;
        var twt = s.newTwoWindingsTransformer()
                .setId("Transformer-" + transformer.bus1 + "-" + transformer.bus2)
                .setBus1(busId1)
                .setVoltageLevel1(bus1.getVoltageLevel().getId())
                .setBus2(busId2)
                .setVoltageLevel2(bus2.getVoltageLevel().getId())
                .setRatedU1(transformer.kvPri)
                .setRatedU2(transformer.kvSec)
                .setRatedS(transformer.mva)
                .setR(r)
                .setX(x)
                .add();
        twt.newExtension(TwoWindingsTransformerFortescueAdder.class)
                .withConnectionType1(getConnectionType(transformer.connPri))
                .withConnectionType2(getConnectionType(transformer.connSec))
                .add();
    }

    public static Network create() {
        return create(NetworkFactory.findDefault());
    }

    public static Network create(NetworkFactory networkFactory) {
        Network network = networkFactory.createNetwork("EuropeanLvTestFeeder", "csv");
        network.setCaseDate(ZonedDateTime.parse("2023-04-11T23:59:00.000+01:00"));
        Transformer transformer = parseCsv("/europeanLvTestFeeder/Transformer.csv", Transformer.class).get(0);
        createSource(transformer, network);
        createBuses(network);
        createLines(network);
        createLoads(network);
        createTransformer(transformer, network);
        return network;
    }

    private interface Mapper<T> {
        T apply(NamedCsvRecord rec);
    }

    public record BusCoord(int busName, double x, double y) {
    }

    public record Line(String name, int bus1, int bus2, String phases, double length, String units, String code) {
    }

    public record LineCode(String name, int nphases,
                           double r1, double x1, double r0, double x0, double c1, double c0, String units) {
    }

    public record Load(String name, int numPhases, int bus, char phases, double kV, int model,
                       String connection, double kW, double pf, String yearly) {
    }

    public record Transformer(String name, int phases, String bus1, int bus2,
                              double kvPri, double kvSec, double mva,
                              String connPri, String connSec, double xhl, double resistance) {
    }
}
