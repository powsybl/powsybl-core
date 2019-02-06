package com.powsybl.ucte.converter;

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
import java.util.Properties;

public class UcteExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(UcteExporter.class);

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

        UcteNetwork ucteNetwork = new UcteNetworkImpl();
        Iterable<Line> lines = network.getLines();
        String[] idInfo = null;

        Iterable<Substation> substations = network.getSubstations();

        for(Substation substation : substations)
        {
            LOGGER.info("---------------SUBSTATION------------------");
            String country = substation.getCountry().toString();
            LOGGER.info(" Geographical tags = " + substation.getGeographicalTags().toString());
            LOGGER.info(" Substation country = " + country);

            Iterable<VoltageLevel> voltageLevels = substation.getVoltageLevels();
            for(VoltageLevel voltageLevel : voltageLevels)
            {
                LOGGER.info("---------------VOLTAGE LEVEL------------------");
                VoltageLevel.BusBreakerView busBreakerView = voltageLevel.getBusBreakerView();
                LOGGER.info(" ID = " + voltageLevel.getId());
                LOGGER.info(" NominalV = " + voltageLevel.getNominalV());


                Iterable<Bus> buses = busBreakerView.getBuses();
                for(Bus bus : buses)
                {
                    LOGGER.info("---------------BUS------------------");
                    LOGGER.info(" Bus id = " + bus.getId());
                    LOGGER.info(" Bus name = " + bus.getName());
                    LOGGER.info(" V = " + bus.getV());
                    UcteNodeCode ucteNodeCode = new UcteNodeCode(UcteCountryCode.valueOf(country),
                            "TTTTT",
                            iidmVoltageToUcteVoltageLevelCode(voltageLevel.getNominalV()),
                            '1');

                    UcteNode ucteNode = new UcteNode(ucteNodeCode,
                            "",
                            UcteNodeStatus.REAL,
                            UcteNodeTypeCode.PQ,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            0f,
                            UctePowerPlantType.C
                    ); //FIXME
                    ucteNetwork.addNode(ucteNode);
                }
            }

            write(ucteNetwork, FileSystems.getDefault().getPath("","test.uct")); // Fixme: it's just to test

        }

//        Stream<Line> lineStream = network.getLineStream();
//        lineStream.forEach(line -> LOGGER.info(line.getId()));

//        int i = 0;
//        for(Line line : lines)
//        {
//            LOGGER.info("ID = " + line.getId()); //Node code 1 + node code 2 + Order code  1-8 10-17 19
//            LOGGER.info("R = " + String.valueOf(line.getR())); //Resistance position UTCE 23-28
//            LOGGER.info("X = " +String.valueOf(line.getX())); //Reactance position UTCE 30-35
//            LOGGER.info("Name = " + line.getName());
//            LOGGER.info("CurrentLimits1 = " + String.valueOf(line.getCurrentLimits1().getPermanentLimit())); //Current limit I (A) 46-51
//            LOGGER.info("CurrentLimits2 = " + String.valueOf(line.getCurrentLimits2().getPermanentLimit()));
//            LOGGER.info("B1 = " + String.valueOf(line.getB1()));
//            LOGGER.info("B2 = " + String.valueOf(line.getB2()));
//            LOGGER.info("G1 = " + String.valueOf(line.getG1()));
//            LOGGER.info("G2 = " + String.valueOf(line.getG2()));
//
//            idInfo = line.getId().split(" ");
//
//
//        }

        Iterable<BusbarSection> busbarSections = network.getBusbarSections();
        Network.BusBreakerView busBreakerView = network.getBusBreakerView();
        Iterable<Bus> buses = busBreakerView.getBuses();

//        for(Bus bus: buses)
//        {
//            LOGGER.info(" Voltage level = " + String.valueOf(bus.getVoltageLevel()));
//            LOGGER.info(" Bus id = " + bus.getId());
//            LOGGER.info(" Bus name = " + bus.getName());
//            LOGGER.info(" V = " + bus.getV());
//            LOGGER.info(" NominalV = " + bus.getVoltageLevel().getNominalV());
//        }


        UcteNodeCode code1 = new UcteNodeCode(UcteCountryCode.ES, "TTTTT", UcteVoltageLevelCode.VL_220, '1');

        UcteNodeCode code2 = new UcteNodeCode(UcteCountryCode.BE, "TTTTT", UcteVoltageLevelCode.VL_380, '1');

//        UcteNode node1 = new UcteNode(UcteNodeCode code, String geographicalName, UcteNodeStatus status, UcteNodeTypeCode typeCode,
//        float voltageReference, float activeLoad, float reactiveLoad, float activePowerGeneration,
//        float reactivePowerGeneration, float minimumPermissibleActivePowerGeneration,
//        float maximumPermissibleActivePowerGeneration, float minimumPermissibleReactivePowerGeneration,
//        float maximumPermissibleReactivePowerGeneration, float staticOfPrimaryControl,
//        float nominalPowerPrimaryControl, float threePhaseShortCircuitPower, float xrRatio,
//        UctePowerPlantType powerPlantType);

//        UcteElementId lineId = new UcteElementId(code1, code2, idInfo[2].charAt(0));
//        UcteLine ucteLine = new UcteLine(lineId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
//                1.0f, 0.1f, 1e-6f, 1250, null);
//        ucteNetwork.addLine(ucteLine);
//
//        LOGGER.info(ucteNetwork.getLines().toString());

    }

    public UcteVoltageLevelCode iidmVoltageToUcteVoltageLevelCode(double nominalV)
    {
        if(nominalV == 27) {
            return UcteVoltageLevelCode.VL_27;
        }
        if(nominalV == 70) {
            return UcteVoltageLevelCode.VL_70;
        }
        if(nominalV == 110) {
             return UcteVoltageLevelCode.VL_110;
        }
        if(nominalV == 120) {
            return UcteVoltageLevelCode.VL_120;
        }
        if(nominalV == 150) {
            return UcteVoltageLevelCode.VL_150;
        }
        if(nominalV == 220) {
            return UcteVoltageLevelCode.VL_220;
        }
        if(nominalV == 330) {
            return UcteVoltageLevelCode.VL_330;
        }
        if(nominalV == 380) {
            return UcteVoltageLevelCode.VL_380;
        }
        if(nominalV == 500) {
            return UcteVoltageLevelCode.VL_500;
        }
        if(nominalV == 750) {
            return UcteVoltageLevelCode.VL_750;
        }
        return null;
    }

    public void write(UcteNetwork network, Path file) {
        try (BufferedWriter bw = Files.newBufferedWriter(file)) {
            new UcteWriter(network).write(bw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
