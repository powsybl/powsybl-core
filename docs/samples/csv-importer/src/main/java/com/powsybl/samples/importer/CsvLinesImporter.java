/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.samples.importer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;

@AutoService(Importer.class)
public class CsvLinesImporter implements Importer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvLinesImporter.class);

    private static final String EXTENSION = "csv";

    @Override
    public String getFormat() {
        return "CSV";
    }

    @Override
    public String getComment() {
        return "CSV importer";
    }

    @Override
    public boolean exists(ReadOnlyDataSource datasource) {
        try {
            return datasource.exists(null, EXTENSION);
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            return false;
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource data, Properties props) {
        Network network = NetworkFactory.create("Network_2Lines_Example", EXTENSION);
        LOGGER.debug("Start import from file {}", data.getBaseName());
        long startTime = System.currentTimeMillis();
        try {
            CsvReader reader = new CsvReader(data.newInputStream(null, EXTENSION), Charset.defaultCharset());
            reader.readHeaders();
            while (reader.readRecord()) {
                String id = reader.get("LineId");
                LOGGER.info("import lineID {}", id);
                Substation s1 = getSubstation(reader.get("SubStationId1"), network, Country.FR);
                Substation s2 = getSubstation(reader.get("SubStationId2"), network, Country.FR);
                VoltageLevel vlhv1 = getVoltageLevel(reader.get("VoltageLevelId1"), network, s1, 220, TopologyKind.BUS_BREAKER);
                VoltageLevel vlhv2 = getVoltageLevel(reader.get("VoltageLevelId2"), network, s2, 220, TopologyKind.BUS_BREAKER);
                Bus nhv1 = getBus(vlhv1, reader.get("BusId1"));
                Bus nhv2 = getBus(vlhv2, reader.get("BusId2"));
                network.newLine()
                       .setId(id)
                       .setVoltageLevel1(vlhv1.getId())
                       .setVoltageLevel2(vlhv2.getId())
                       .setBus1(nhv1.getId())
                       .setConnectableBus1(nhv1.getId())
                       .setBus2(nhv2.getId())
                       .setConnectableBus2(nhv2.getId())
                       .setR(Double.valueOf(reader.get("R")))
                       .setX(Double.valueOf(reader.get("X")))
                       .setG1(Double.valueOf(reader.get("G1")))
                       .setB1(Double.valueOf(reader.get("B1")))
                       .setG2(Double.valueOf(reader.get("G2")))
                       .setB2(Double.valueOf(reader.get("B2")))
                       .add();
            }
            LOGGER.debug("{} import done in {} ms", EXTENSION, System.currentTimeMillis() - startTime);
            return network;

        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            return null;
        }
    }

    private Substation getSubstation(String id, Network network, Country country) {
        return (network.getSubstation(id) == null) ? network.newSubstation().setId(id).setCountry(country).add() : network.getSubstation(id);
    }

    private Bus getBus(VoltageLevel vlhv, String id) {
        return (vlhv.getBusBreakerView().getBus(id) == null) ? vlhv.getBusBreakerView().newBus().setId(id).add() : vlhv.getBusBreakerView().getBus(id);
    }

    private VoltageLevel getVoltageLevel(String id, Network network, Substation s, double nominalVoltage, TopologyKind topologyKind) {
        return (network.getVoltageLevel(id) == null) ? s.newVoltageLevel().setId(id).setNominalV(nominalVoltage).setTopologyKind(topologyKind).add() : network.getVoltageLevel(id);
    }

}
