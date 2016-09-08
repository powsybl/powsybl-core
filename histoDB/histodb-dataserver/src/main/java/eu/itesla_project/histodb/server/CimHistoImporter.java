/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import be.pepite.dataserver.api.ColumnDescriptor;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.computation.local.LocalComputationManager;
import eu.itesla_project.iidm.datasource.GenericReadOnlyDataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.HistoDbMetaAttributeType;
import eu.itesla_project.modules.histo.IIDM2DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.RuntimeException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 21/05/13
 * Time: 12:16
 * To change this template use File | Settings | File Templates.
 */
public class CimHistoImporter {

    static Logger log = LoggerFactory.getLogger(CimHistoImporter.class);

    private ITeslaDatasource datasource;

    private static final ComputationManager computationManager;

    private static final Importer importerCim;
    private static final Importer importerXml;
    private static final List<Importer> importers;

    static {
        try {
            importers=new ArrayList<>();
            computationManager = new LocalComputationManager();
            importerCim = Importers.getImporter("CIM1", computationManager);
            if (importerCim!=null) {
                importers.add(importerCim);
            } else {
                log.warn("CIM importer implementation not found");
            }
            importerXml = Importers.getImporter("XML", computationManager);
            if (importerXml!=null) {
                importers.add(importerXml);
            } else {
                log.warn("iidm-xml importer implementation not found");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CimHistoImporter(ITeslaDatasource datasource) {
        this.datasource = datasource;
    }

    public void addCim(File cims) throws IOException {
        try {
            for (Importer importer : importers) {
                log.info("processing import - importer format = " + importer.getFormat());
                Importers.importAll(cims.toPath(), importer, true, n -> {
                Map<String, Object> query = new HashMap<String, Object>();
                query.put(HistoDbMetaAttributeType.cimName.name(), n.getId());

                //WARN is this still relevant with potential multi-horizon forecasts ?
                if (datasource.getData(query, 0, 1, ColumnDescriptor.getDescriptorsForNames("_id")).getRowIterator().hasNext()) {
                    log.warn("network already in DB: " + n.getId());
                    return;
                }

                try {
                    IIDM2DB.CimValuesMap valueMaps = IIDM2DB.extractCimValues(n, new IIDM2DB.Config(n.getId(), true));
                    datasource.getMetadata().addTopologies(valueMaps.getToposPerSubstation());

                    for (Map.Entry<IIDM2DB.HorizonKey, LinkedHashMap<HistoDbAttributeId, Object>> valueMapEntry : valueMaps.entrySet()) {
                        LinkedHashMap<HistoDbAttributeId, Object> valueMap = valueMapEntry.getValue();
                        query = new HashMap<String, Object>();
                        query.put(HistoDbMetaAttributeType.datetime.name(), n.getCaseDate().toDate());
                        query.put(HistoDbMetaAttributeType.forecastTime.name(), valueMapEntry.getKey().forecastDistance);
                        query.put(HistoDbMetaAttributeType.horizon.name(), valueMapEntry.getKey().horizon.toString());
                        List<String> colNames = new ArrayList<String>(valueMap.size());
                        for (HistoDbAttributeId attrId : valueMap.keySet()) {
                            colNames.add(attrId.toString());
                        }
                        datasource.updateData(query, colNames.toArray(new String[]{}), valueMap.values().toArray());
                    }

                    // only use snapshots as structural network, not forecasts
                    // forecasts sometimes have different lines/topologies that will mess up metadata computation
                    if (!valueMaps.hasForecastValues()) datasource.setLatestNetwork(n);

                    log.info("Inserted network: " + n.getId() + ", format: " + importer.getFormat());
                } catch (Exception e) {
                    log.error(e.toString(), e);
                }
                });
            }
        } catch(Exception e){
            log.warn("Failed to insert network from " + cims.getAbsolutePath(), e);
        }
    }

    public static Network readCim(File cim) throws IOException {
        Path file = cim.toPath();
        if (!Files.isRegularFile(file)) {
            throw new RuntimeException("Not a regular file");
        }
        Path dir = file.getParent();
        String baseName = Importers.getBaseName(file);
        for (Importer importer: importers) {
            try {
              Network n=importer.import_(new GenericReadOnlyDataSource(dir, baseName), null);
              if (n!=null) {
                  log.info("read network: " + n.getId());
                  return n;
              }
            } catch (Exception e) {
                log.warn("could not read format " + importer.getFormat());
            }
        }
        log.warn("network NULL, file " + cim.getAbsolutePath());
        return null;
    }
}
