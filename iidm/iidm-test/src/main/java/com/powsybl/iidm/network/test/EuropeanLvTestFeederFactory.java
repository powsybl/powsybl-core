/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.test;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.Coordinate;
import com.powsybl.iidm.network.extensions.GeneratorFortescueAdder;
import com.powsybl.iidm.network.extensions.SlackTerminalAdder;
import com.powsybl.iidm.network.extensions.SubstationPositionAdder;
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
import java.util.List;
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
        String busName;

        @Parsed
        double x;

        @Parsed
        double y;
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

    private static void createBuses(Network network) {
        for (BusCoord busCoord : parseCsv("/europeanLvTestFeeder/Buscoords.csv", BusCoord.class)) {
            Substation s = network.newSubstation()
                    .setId("Substation" + busCoord.busName)
                    .add();
            s.newExtension(SubstationPositionAdder.class)
                    .withCoordinate(new Coordinate(busCoord.y, busCoord.x))
                    .add();
            VoltageLevel vl = s.newVoltageLevel()
                    .setId("VoltageLevel" + busCoord.busName)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .setNominalV(1)
                    .add();
            vl.getBusBreakerView().newBus()
                    .setId("Bus" + busCoord.busName)
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
        return network;
    }
}
