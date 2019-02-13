/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.ucte.converter;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.*;
import com.powsybl.ucte.network.*;
import com.powsybl.ucte.network.io.UcteWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Properties;

/**
 * @author Abdelsalem Hedhili <abdelsalem.hedhili at rte-france.com>
 */
@AutoService(Exporter.class)
public class UcteExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteExporter.class);

    HashMap<String, UcteNodeCode> iidmIdToUcteId = new HashMap<>();

    @Override
    public String getFormat() {
        return "XIIDM";
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {

        UcteNetwork ucteNetwork = createUcteNetwork(network);
        write(ucteNetwork, FileSystems.getDefault().getPath("", "test.uct")); // FIXME: it's just to test

    }

    private void createTwoWindingTransformers(UcteNetwork ucteNetwork, Bus bus) {
        LOGGER.info("-----------TWO WINDING TRANSFORMERS--------");
        Iterable<TwoWindingsTransformer> twoWindingsTransformers = bus.getTwoWindingsTransformers();
        for (TwoWindingsTransformer twoWindingsTransformer : twoWindingsTransformers) {
            LOGGER.info("-----------TWO WINDING TRANSFORMER--------");
            LOGGER.info(" Id = {}", twoWindingsTransformer.getId());
            LOGGER.info(" Name = {}", twoWindingsTransformer.getName());
            LOGGER.info(" X = {}", twoWindingsTransformer.getX());
            LOGGER.info(" R = {}", String.valueOf(twoWindingsTransformer.getR()));
            LOGGER.info(" B = {}", String.valueOf(twoWindingsTransformer.getB()));
            LOGGER.info(" G = {}", String.valueOf(twoWindingsTransformer.getG()));
            LOGGER.info(" RatedU1 = {}", String.valueOf(twoWindingsTransformer.getRatedU1()));
            LOGGER.info(" RatedU2 = {}", String.valueOf(twoWindingsTransformer.getRatedU2()));
            createTwoWindingTransformer(ucteNetwork, twoWindingsTransformer);

        }
    }

    private void createTwoWindingTransformer(UcteNetwork ucteNetwork, TwoWindingsTransformer twoWindingsTransformer) {
        Terminal terminal1 = twoWindingsTransformer.getTerminal1();
        Terminal terminal2 = twoWindingsTransformer.getTerminal2();

        UcteNodeCode ucteNodeCode = new UcteNodeCode(
                UcteCountryCode.valueOf(terminal1.getVoltageLevel().getSubstation().getCountry().toString()),
                terminal1.getVoltageLevel().getSubstation().getName(),
                iidmVoltageToUcteVoltageLevelCode(terminal1.getVoltageLevel().getNominalV()),
                '1');
        UcteNodeCode ucteNodeCode2 = new UcteNodeCode(
                UcteCountryCode.valueOf(terminal2.getVoltageLevel().getSubstation().getCountry().toString()),
                terminal2.getVoltageLevel().getSubstation().getName(),
                iidmVoltageToUcteVoltageLevelCode(terminal2.getVoltageLevel().getNominalV()),
                '1');

        UcteElementId ucteElementId = new UcteElementId(ucteNodeCode, ucteNodeCode2, '1');

        if (isNotAlreadyCreated(ucteNetwork, ucteElementId)) {
            UcteTransformer ucteTransformer = new UcteTransformer(
                    ucteElementId,
                    UcteElementStatus.fromCode(1),
                    (float) twoWindingsTransformer.getR(),
                    (float) twoWindingsTransformer.getX(),
                    (float) twoWindingsTransformer.getB(),
                    (int) twoWindingsTransformer.getCurrentLimits2().getPermanentLimit(),
                    twoWindingsTransformer.getName(),
                    (float) twoWindingsTransformer.getRatedU2(),
                    (float) twoWindingsTransformer.getRatedU1(),
                    100,
                    (float) twoWindingsTransformer.getG()); //TODO Find a representation for the nominal power

            ucteNetwork.addTransformer(ucteTransformer);
        }
    }

    private void createLines(UcteNetwork ucteNetwork, Network network) {
        LOGGER.info("-----------LINES------------");
        Iterable<Line> lines = network.getLines();
        for (Line line : lines) {
            LOGGER.info("-----------LINE------------");
            LOGGER.info("ID = {}", line.getId()); //Node code 1 + node code 2 + Order code  1-8 10-17 19
            LOGGER.info("R = {}", String.valueOf(line.getR())); //Resistance position UTCE 23-28
            LOGGER.info("X = {}", String.valueOf(line.getX())); //Reactance position UTCE 30-35
            LOGGER.info("Name = {}", line.getName());
            LOGGER.info("CurrentLimits1 = {}", String.valueOf(line.getCurrentLimits1().getPermanentLimit())); //Current limit I (A) 46-51
            LOGGER.info("CurrentLimits2 = {}", String.valueOf(line.getCurrentLimits2().getPermanentLimit()));
            LOGGER.info("B1 = {}", String.valueOf(line.getB1()));
            LOGGER.info("B2 = {}", String.valueOf(line.getB2()));
            LOGGER.info("G1 = {}", String.valueOf(line.getG1()));
            LOGGER.info("G2 = {}", String.valueOf(line.getG2()));
            createLine(ucteNetwork, line);
        }
    }

    private UcteNetwork createUcteNetwork(Network network) {

        UcteNetwork ucteNetwork = new UcteNetworkImpl();
        Iterable<Substation> substations = network.getSubstations();
        for (Substation substation : substations) {
            LOGGER.info("---------------SUBSTATION------------------");
            LOGGER.info(" Geographical tags = {}", substation.getGeographicalTags().toString());
            LOGGER.info(" Substation country = {}", substation.getCountry());
            LOGGER.info(" Substation Id = {}", substation.getId());
            LOGGER.info(" Substation name = {}", substation.getName());


            Iterable<VoltageLevel> voltageLevels = substation.getVoltageLevels();
            for (VoltageLevel voltageLevel : voltageLevels) {
                LOGGER.info("---------------VOLTAGE LEVEL------------------");
                VoltageLevel.BusBreakerView busBreakerView = voltageLevel.getBusBreakerView();
                LOGGER.info(" ID = {}", voltageLevel.getId());
                LOGGER.info(" NominalV = {}", voltageLevel.getNominalV());
                LOGGER.info(" Low voltage limit = {}", voltageLevel.getLowVoltageLimit());
                LOGGER.info(" High voltage limit = {}", voltageLevel.getHighVoltageLimit());

                createBuses(ucteNetwork, voltageLevel);

            }
        }
        createLines(ucteNetwork, network);
        return ucteNetwork;
    }

    private void createLine(UcteNetwork ucteNetwork, Line line) {
        Terminal terminal1 = line.getTerminal1();
        Terminal terminal2 = line.getTerminal2();

        UcteNodeCode ucteTerminal1NodeCode = createUcteNodeCode(
                terminal1.getBusBreakerView().getBus().getId(),
                terminal1.getVoltageLevel(),
                terminal1.getVoltageLevel().getSubstation().getCountry().toString());

        UcteNodeCode ucteTerminal2NodeCode = createUcteNodeCode(
                terminal2.getBusBreakerView().getBus().getId(),
                terminal2.getVoltageLevel(),
                terminal2.getVoltageLevel().getSubstation().getCountry().toString());

        UcteElementId lineId = new UcteElementId(ucteTerminal1NodeCode, ucteTerminal2NodeCode, '1');
        UcteLine ucteLine = new UcteLine(lineId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
                (float) line.getR(), (float) line.getX(), (float) line.getB1() + (float) line.getB2(), (int) line.getCurrentLimits1().getPermanentLimit(), null);

        ucteNetwork.addLine(ucteLine);
    }

    private void createBuses(UcteNetwork ucteNetwork, VoltageLevel voltageLevel) {
        VoltageLevel.BusBreakerView busBreakerView = voltageLevel.getBusBreakerView();
        Iterable<Bus> buses = busBreakerView.getBuses();
        for (Bus bus : buses) {
            LOGGER.info("---------------BUS------------------");
            LOGGER.info(" Bus id = {}", bus.getId());
            LOGGER.info(" Bus name = {}", bus.getName());
            LOGGER.info(" V = {}", bus.getV());
            LOGGER.info(" P = {}", bus.getP());
            LOGGER.info(" Q = {}", bus.getQ());
            LOGGER.info(" Angle = {}", bus.getAngle());
            createBus(ucteNetwork, bus);

            LOGGER.info("-----------GENERATORS--------");
            Iterable<Generator> generators = bus.getGenerators();
            for (Generator generator : generators) {
                LOGGER.info("-----------GENERATOR--------");
                LOGGER.info(" Id = {}", generator.getId());
                LOGGER.info(" Name = {}", generator.getName());
                LOGGER.info(" Energy source = {}", generator.getEnergySource().toString());
                LOGGER.info(" RatedS = {}", String.valueOf(generator.getRatedS()));
                LOGGER.info(" MaxP = {}", String.valueOf(generator.getMaxP()));
                LOGGER.info(" MinP = {}", String.valueOf(generator.getMinP()));
                LOGGER.info(" TargetP = {}", String.valueOf(generator.getTargetP()));
                LOGGER.info(" TargetV = {}", String.valueOf(generator.getTargetV()));
                LOGGER.info(" TargetQ = {}", String.valueOf(generator.getTargetQ()));
                LOGGER.info(" ReactiveLimitsOrdinal = {}", String.valueOf(generator.getReactiveLimits().getKind().ordinal()));

            }


            createTwoWindingTransformers(ucteNetwork, bus);
        }
    }

    private void createBus(UcteNetwork ucteNetwork, Bus bus) {
        VoltageLevel voltageLevel = bus.getVoltageLevel();
        long loadCount = voltageLevel.getLoadStream().count();
        long generatorCount = bus.getGeneratorStream().count();
        String country = voltageLevel.getSubstation().getCountry().toString();

        double p0 = 0;
        double q0 = 0;
        double activePowerGeneration = 0;
        double reactivePowerGeneration = 0;
        double voltageReference = 0;
        double minimumPermissibleActivePowerGeneration = 0;
        double maximumPermissibleActivePowerGeneration = 0;
        double minimumPermissibleReactivePowerGeneration = 0;
        double maximumPermissibleReactivePowerGeneration = 0;
        UctePowerPlantType uctePowerPlantType = null;

        if (loadCount == 1) {
            Load load = (Load) voltageLevel.getLoadStream().toArray()[0];
            p0 = load.getP0();
            q0 = load.getQ0();
            LOGGER.info("-------------LOAD--------------");
            LOGGER.info("P0 = {}", p0);
            LOGGER.info("Q0 = {}", q0);
        }

        if (generatorCount == 1) { //the node is a generator
            Generator generator = (Generator) bus.getGeneratorStream().toArray()[0];
            activePowerGeneration = -generator.getTargetP();
            reactivePowerGeneration = -generator.getTargetQ();
            voltageReference = generator.getTargetV();
            minimumPermissibleActivePowerGeneration = -generator.getMinP();
            maximumPermissibleActivePowerGeneration = -generator.getMaxP();
            minimumPermissibleReactivePowerGeneration = -generator.getReactiveLimits().getMinQ(activePowerGeneration);
            maximumPermissibleReactivePowerGeneration = -generator.getReactiveLimits().getMaxQ(activePowerGeneration);
            uctePowerPlantType = energySourceToUctePowerPlantType(generator.getEnergySource());

        }

        UcteNodeCode ucteNodeCode = createUcteNodeCode(bus.getId(), voltageLevel, country);

        UcteNode ucteNode = new UcteNode(
                ucteNodeCode,
                voltageLevel.getSubstation().getName(),
                UcteNodeStatus.REAL,
                UcteNodeTypeCode.PQ,
                (float) voltageReference,
                (float) p0,
                (float) q0,
                (float) activePowerGeneration,
                (float) reactivePowerGeneration,
                (float) minimumPermissibleActivePowerGeneration,
                (float) maximumPermissibleActivePowerGeneration,
                (float) minimumPermissibleReactivePowerGeneration,
                (float) maximumPermissibleReactivePowerGeneration,
                0f,
                0f,
                0f,
                0f,
                null
        ); //TODO :
        ucteNode.setPowerPlantType(uctePowerPlantType);
        ucteNetwork.addNode(ucteNode);
    }

    boolean isNotAlreadyCreated(UcteNetwork ucteNetwork, UcteElementId ucteElementId) {
        return ucteNetwork.getTransformer(ucteElementId) == null;
    }

    UcteVoltageLevelCode iidmVoltageToUcteVoltageLevelCode(double nominalV) {
        if (nominalV == 27) {
            return UcteVoltageLevelCode.VL_27;
        }
        if (nominalV == 70) {
            return UcteVoltageLevelCode.VL_70;
        }
        if (nominalV == 110) {
            return UcteVoltageLevelCode.VL_110;
        }
        if (nominalV == 120) {
            return UcteVoltageLevelCode.VL_120;
        }
        if (nominalV == 150) {
            return UcteVoltageLevelCode.VL_150;
        }
        if (nominalV == 220) {
            return UcteVoltageLevelCode.VL_220;
        }
        if (nominalV == 330) {
            return UcteVoltageLevelCode.VL_330;
        }
        if (nominalV == 380) {
            return UcteVoltageLevelCode.VL_380;
        }
        if (nominalV == 500) {
            return UcteVoltageLevelCode.VL_500;
        }
        if (nominalV == 750) {
            return UcteVoltageLevelCode.VL_750;
        }
        return null;
    }

    UctePowerPlantType energySourceToUctePowerPlantType(EnergySource energySource) {
        if (EnergySource.HYDRO == energySource) {
            return UctePowerPlantType.H;
        } else if (EnergySource.NUCLEAR == energySource) {
            return UctePowerPlantType.N;
        } else if (EnergySource.THERMAL == energySource) {
            return UctePowerPlantType.C;
        } else if (EnergySource.WIND == energySource) {
            return UctePowerPlantType.W;
        } else {
            return UctePowerPlantType.F;
        }
    }

    UcteNodeCode createUcteNodeCode(String id, VoltageLevel voltageLevel, String country) {
        UcteNodeCode ucteNodeCode;
        if (isUcteNodeId(id)) { // the ID is already an UCTE id
            ucteNodeCode = new UcteNodeCode(
                    UcteCountryCode.valueOf(country),
                    id.substring(1, 6),
                    iidmVoltageToUcteVoltageLevelCode(voltageLevel.getNominalV()),
                    id.charAt(7)
            );
        } else {
            ucteNodeCode = new UcteNodeCode(
                    UcteCountryCode.valueOf(country),
                    voltageLevel.getSubstation().getName(),
                    iidmVoltageToUcteVoltageLevelCode(voltageLevel.getNominalV()),
                    '1'
            );
            iidmIdToUcteId.put(id, ucteNodeCode);
        }
        return ucteNodeCode;
    }

    boolean isUcteId(String id) {
        return id != null &&
                id.length() >= 17 &&
                isUcteNodeId(id.substring(0, 8)) &&
                isUcteNodeId(id.substring(9, 17)) &&
                id.charAt(8) == ' ' &&
                id.charAt(17) == ' ';
    }

    boolean isUcteNodeId(String id) {
        return id != null &&
                id.length() == 8 &&
                isUcteCountryCode(id.charAt(0)) &&
                isVoltageLevel(id.charAt(6));
    }

    boolean isUcteCountryCode(char character) {
        try {
            UcteCountryCode.fromUcteCode(character);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    boolean isVoltageLevel(char character) {
        return (int) character >= 48 && (int) character <= 57;
    }

    void write(UcteNetwork network, Path file) {
        try (BufferedWriter bw = Files.newBufferedWriter(file)) {
            new UcteWriter(network).write(bw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String generate(int length) {  //FIXME: delete this when you know how to get geographical name
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        String pass = "";
        for (int x = 0; x < length; x++) {
            int i = (int) Math.floor(Math.random() * (chars.length() - 1));
            pass += chars.charAt(i);
        }
        return pass;
    }
}
