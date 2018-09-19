/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.samples.exporter;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.io.table.Column;
import com.powsybl.commons.io.table.CsvTableFormatterFactory;
import com.powsybl.commons.io.table.TableFormatter;
import com.powsybl.commons.io.table.TableFormatterConfig;
import com.powsybl.iidm.export.Exporter;
import com.powsybl.iidm.network.Bus;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;

@AutoService(Exporter.class)
public class CsvLinesExporter implements Exporter {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsvLinesExporter.class);

    private static final String EXTENSION = "csv";
    private static final char CSV_SEPARATOR = ',';

    @Override
    public String getFormat() {
        return "CSV";
    }

    @Override
    public String getComment() {
        return "CSV exporter";
    }

    @Override
    public void export(Network network, Properties parameters, DataSource dataSource) {
        Objects.requireNonNull(network);
        Objects.requireNonNull(dataSource);
        try {
            long startTime = System.currentTimeMillis();
            OutputStream outputStream = dataSource.newOutputStream(null, EXTENSION, false);
            Writer writer = new OutputStreamWriter(outputStream);
            CsvTableFormatterFactory csvTableFormatterFactory = new CsvTableFormatterFactory();
            TableFormatterConfig tfc = new TableFormatterConfig(Locale.getDefault(), CSV_SEPARATOR, "",	true, false);

            try (TableFormatter formatter = csvTableFormatterFactory.create(writer, "", tfc,
                    new Column("LineId"),
                    new Column("SubstationId1"),
                    new Column("SubstationId2"),
                    new Column("VoltageLevelId1"),
                    new Column("VoltageLevelId2"),
                    new Column("BusId1"),
                    new Column("BusId2"),
                    new Column("R"),
                    new Column("X"),
                    new Column("G1"),
                    new Column("B1"),
                    new Column("G2"),
                    new Column("B2"))) {

                for (Line line : network.getLines()) {
                    String id = line.getId();
                    Bus bus1 = line.getTerminal1().getBusBreakerView().getBus();
                    String bus1Id = (bus1 != null) ? bus1.getId() : "";
                    VoltageLevel vhl1 = (bus1 != null) ? bus1.getVoltageLevel() : null ;
                    String vhl1Id = (vhl1 != null) ? vhl1.getId() : "";
                    String substationId1 = (vhl1 != null) ? vhl1.getSubstation().getId() : "";
                    Bus bus2 = line.getTerminal2().getBusBreakerView().getBus();
                    String bus2Id = (bus2 != null) ? bus2.getId() : "";
                    VoltageLevel vhl2 = (bus2 != null) ? bus2.getVoltageLevel() : null;					
                    String vhl2Id = (vhl2 != null) ? vhl2.getId() : "";
                    String substationId2 = (vhl2 != null) ? vhl2.getSubstation().getId() : "";
                    double r = line.getR();
                    double x = line.getX();
                    double b1 = line.getB1();
                    double b2 = line.getB2();
                    double g1 = line.getG1();
                    double g2 = line.getG2();
                    LOGGER.debug("export lineID {} ", id);
                    formatter.writeCell(id)
                             .writeCell(substationId1)
                             .writeCell(substationId2)
                             .writeCell(vhl1Id)
                             .writeCell(vhl2Id)
                             .writeCell(bus1Id)
                             .writeCell(bus2Id)
                             .writeCell(r)
                             .writeCell(x)
                             .writeCell(g1)
                             .writeCell(b1)
                             .writeCell(g2)
                             .writeCell(b2);
                }
                LOGGER.info("CSV export done in {} ms", System.currentTimeMillis() - startTime);
            }
        } catch (IOException e) {
            LOGGER.error(e.toString(), e);
            throw new UncheckedIOException(e);
        }

    }

}
