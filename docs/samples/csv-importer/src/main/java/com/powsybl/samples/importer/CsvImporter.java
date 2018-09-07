/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.samples.importer;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.csvreader.CsvReader;
import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.FileDataSource;
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
public class CsvImporter implements Importer {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CsvImporter.class);

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
        	return datasource.exists(null, EXTENSION );
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString(), e);
            return false;
        }
    }

    @Override
    public Network importData(ReadOnlyDataSource data, Properties props) {
    	Network network = NetworkFactory.create("SamplesNetwork", EXTENSION);
        LOGGER.debug("Start import from file {}",data.getBaseName());
        long startTime = System.currentTimeMillis();
        try {
            CsvReader reader = new CsvReader(data.newInputStream(null,EXTENSION), Charset.defaultCharset());
            reader.readHeaders();
            while(reader.readRecord())
            {
                String id = reader.get("ID");
                Substation s1 = network.newSubstation().setId(reader.get("S1")).setCountry(Country.FR).add();
                Substation s2 = network.newSubstation().setId(reader.get("S2")).setCountry(Country.FR).add();
                VoltageLevel vlhv1 = s1.newVoltageLevel().setId(reader.get("VL1")).setNominalV(220).setTopologyKind(TopologyKind.BUS_BREAKER).add();
                VoltageLevel vlhv2 = s2.newVoltageLevel().setId(reader.get("VL2")).setNominalV(220).setTopologyKind(TopologyKind.BUS_BREAKER).add();
                Bus nhv1 = vlhv1.getBusBreakerView().newBus().setId(reader.get("BUS1")).add();
                Bus nhv2 = vlhv2.getBusBreakerView().newBus().setId(reader.get("BUS2")).add();
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
            LOGGER.debug("{} import done in {} ms",EXTENSION, System.currentTimeMillis() - startTime);
            return network;
            
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error(e.toString(), e);
            return null;
        }
    }

    public static void main(String[] args) {
        CsvImporter imp = new CsvImporter();
        Network net = imp.importData(new FileDataSource(Paths.get("../resources"), "test"), null);
        LOGGER.info("Network Loaded: {},  is composed by {} lines num.: "  +net.getId(), net.getLineCount() );
    }
        
}
