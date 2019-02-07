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
                LOGGER.info(" Low voltage limit = " + voltageLevel.getLowVoltageLimit());
                LOGGER.info(" High voltage limit = " + voltageLevel.getHighVoltageLimit());

                int loadCount = voltageLevel.getLoadCount();
                double p0 = 0;
                double q0 = 0;

                if(loadCount==1)
                {
                    Iterable<Load> loads = voltageLevel.getLoads();
                    for(Load load : loads)
                    {
                        LOGGER.info("-------------LOAD--------------");
                        LOGGER.info("P0 = " +load.getP0());
                        LOGGER.info("Q0 = " +load.getQ0());
                        p0 = load.getP0();
                        q0 = load.getQ0();

                    }
                }

                Iterable<Bus> buses = busBreakerView.getBuses();
                for(Bus bus : buses)
                {
                    LOGGER.info("---------------BUS------------------");
                    LOGGER.info(" Bus id = " + bus.getId());
                    LOGGER.info(" Bus name = " + bus.getName());
                    LOGGER.info(" V = " + bus.getV());
                    LOGGER.info(" P = " + bus.getP());
                    LOGGER.info(" Q = " + bus.getQ());
                    LOGGER.info(" Angle = " + bus.getAngle());

                    UcteNodeCode ucteNodeCode = new UcteNodeCode(
                            UcteCountryCode.valueOf(country),
                            "TTTTT",
                            iidmVoltageToUcteVoltageLevelCode(voltageLevel.getNominalV()),
                            '1'
                    );

                    UcteNode ucteNode = new UcteNode(
                            ucteNodeCode,
                            "",
                            UcteNodeStatus.REAL,
                            UcteNodeTypeCode.PQ,
                            0f,
                            (float)p0,
                            (float)q0,
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
                            UctePowerPlantType.N
                    ); //FIXME
                    ucteNetwork.addNode(ucteNode);

                    LOGGER.info("-----------GENERATORS--------");
                    Iterable<Generator> generators = bus.getGenerators();
                    for(Generator generator : generators)
                    {
                        LOGGER.info("-----------GENERATOR--------");
                        LOGGER.info(generator.getId());
                        LOGGER.info(generator.getName());
                        LOGGER.info(generator.getEnergySource().toString());


                    }
                }
            }
        }

        Iterable<Line> lines = network.getLines();

        LOGGER.info("-----------LINES------------");
        for(Line line : lines)
        {
            LOGGER.info("-----------LINE------------");
            LOGGER.info("ID = " + line.getId()); //Node code 1 + node code 2 + Order code  1-8 10-17 19
            LOGGER.info("R = " + String.valueOf(line.getR())); //Resistance position UTCE 23-28
            LOGGER.info("X = " +String.valueOf(line.getX())); //Reactance position UTCE 30-35
            LOGGER.info("Name = " + line.getName());
            LOGGER.info("CurrentLimits1 = " + String.valueOf(line.getCurrentLimits1().getPermanentLimit())); //Current limit I (A) 46-51
            LOGGER.info("CurrentLimits2 = " + String.valueOf(line.getCurrentLimits2().getPermanentLimit()));
            LOGGER.info("B1 = " + String.valueOf(line.getB1()));
            LOGGER.info("B2 = " + String.valueOf(line.getB2()));
            LOGGER.info("G1 = " + String.valueOf(line.getG1()));
            LOGGER.info("G2 = " + String.valueOf(line.getG2()));
//            LOGGER.info(" Line terminal 1 country = " + line.getTerminal1().getVoltageLevel().getSubstation().getCountry());
//            LOGGER.info(" Line terminal 2 country = " + line.getTerminal2().getVoltageLevel().getSubstation().getCountry());

            Terminal terminal1 = line.getTerminal1();
            Terminal terminal2 = line.getTerminal2();

            UcteNodeCode ucteTerminal1NodeCode = new UcteNodeCode(
                    UcteCountryCode.valueOf(terminal1.getVoltageLevel().getSubstation().getCountry().toString()),
                    "TTTTT",
                    iidmVoltageToUcteVoltageLevelCode(terminal1.getVoltageLevel().getNominalV()),
                    '1'
            );
            UcteNodeCode ucteTerminal2NodeCode = new UcteNodeCode(
                    UcteCountryCode.valueOf(terminal2.getVoltageLevel().getSubstation().getCountry().toString()),
                    "TTTTT",
                    iidmVoltageToUcteVoltageLevelCode(terminal2.getVoltageLevel().getNominalV()),
                    '1'
            );

            UcteElementId lineId = new UcteElementId(ucteTerminal1NodeCode, ucteTerminal2NodeCode, '1');
            UcteLine ucteLine = new UcteLine(lineId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
                    (float)line.getR(), (float)line.getX(), (float)line.getB1(), (int)line.getCurrentLimits1().getPermanentLimit(), null);

            ucteNetwork.addLine(ucteLine);
        }
        write(ucteNetwork, FileSystems.getDefault().getPath("","test.uct")); // FIXME: it's just to test


    }

    private UcteVoltageLevelCode iidmVoltageToUcteVoltageLevelCode(double nominalV)
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

    private void write(UcteNetwork network, Path file) {
        try (BufferedWriter bw = Files.newBufferedWriter(file)) {
            new UcteWriter(network).write(bw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
