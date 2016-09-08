/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import be.pepite.dataserver.api.ColumnDescriptor;
import be.pepite.dataserver.api.ColumnMetadata;
import be.pepite.dataserver.datastores.BasicMetadata;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 19/06/13
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */
@JsonIgnoreProperties({"nameCache", "log"})
public class ITeslaMetadata
    extends BasicMetadata
{
    private Logger log = LoggerFactory.getLogger(ITeslaMetadata.class);

    Map<String, Map<String, JSONArray>> toposPerSubstation = new HashMap();
    Set<String> regions = new HashSet();
    Set<String> countries = new HashSet();

    public ITeslaMetadata() {

    }

    public ITeslaMetadata(ColumnMetadata[] colMd, String timeColumn, long recordCount, Map<String, Map<String, JSONArray>> toposPerSubstation, Set<String> regions, Set<String> countries) {
        super(colMd, timeColumn, recordCount);
        this.toposPerSubstation = toposPerSubstation;
        this.regions = regions;
        this.countries = countries;
    }

    public void addTopology(String substationId, String topoHash, JSONArray topo) {
        Map<String, JSONArray> topos = toposPerSubstation.get(substationId);

        if (topos == null) toposPerSubstation.put(substationId, topos = new HashMap());

        topos.put(topoHash, topo);
    }

    public void addTopologies(Map<String, Map<String, JSONArray>> toposPerSubstation) {
        for (String substationId: toposPerSubstation.keySet()) {
            Map<String, JSONArray> topos = toposPerSubstation.get(substationId);
            if (topos == null) toposPerSubstation.put(substationId, topos = new HashMap());

            topos.putAll(toposPerSubstation.get(substationId));
        }
    }

    public Map<String, Map<String, JSONArray>> getToposPerSubstation() {
        return toposPerSubstation;
    }

    public Set<String> getRegions() {
        return regions;
    }

    public Set<String> getCountries() {
        return countries;
    }

    public ITeslaMetadata subset(ColumnDescriptor[] colNames) {

        return new ITeslaMetadata(
                getColumnMetadatas(colNames),
                getTimeColumn(),
                getRecordCount(),
                toposPerSubstation,
                regions,
                countries
        );
    }
}
