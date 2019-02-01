package com.powsybl.ucte.converter;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.BusbarSection;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.ucte.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

//        Stream<Line> lineStream = network.getLineStream();
//        lineStream.forEach(line -> LOGGER.info(line.getId()));

        int i = 0;
        for(Line line : lines)
        {
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


            Iterable<BusbarSection> busbarSections = network.getBusbarSections();
            Network.BusBreakerView busBreakerView = network.getBusBreakerView();
            Iterable<Bus> buses = busBreakerView.getBuses();

            for(Bus bus: buses)
            {
                LOGGER.info(" Voltage level = " + String.valueOf(bus.getVoltageLevel()));
                LOGGER.info(" Bus id = " + bus.getId());
                LOGGER.info(" Bus name = " + bus.getName());

            }


            String[] idInfo = line.getId().split(" ");
            UcteNodeCode code1 = new UcteNodeCode(UcteCountryCode.ES, "AAAAA", UcteVoltageLevelCode.VL_380, '1');

            UcteNodeCode code2 = new UcteNodeCode(UcteCountryCode.XX, "BBBBB", UcteVoltageLevelCode.VL_220, '1');

            UcteElementId lineId = new UcteElementId(code1, code2, idInfo[2].charAt(0));
            UcteLine ucteLine = new UcteLine(lineId, UcteElementStatus.REAL_ELEMENT_IN_OPERATION,
                    1.0f, 0.1f, 1e-6f, 1250, null);
            ucteNetwork.addLine(ucteLine);

        }

    }
}
