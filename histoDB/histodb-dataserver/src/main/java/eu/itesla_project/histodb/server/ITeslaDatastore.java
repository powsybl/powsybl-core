/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import be.pepite.dataserver.api.DataSource;
import be.pepite.dataserver.datastores.mongodb.MongoDataStore;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;

import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/06/13
 * Time: 16:16
 * To change this template use File | Settings | File Templates.
 */
public class ITeslaDatastore
    extends MongoDataStore
{

    private Map<String, DatasourceJob> harvestJobs;

    public ITeslaDatastore(String ip, int port, List<String> dbNames) throws UnknownHostException {
        super(ip, port, dbNames);
    }

    public ITeslaDatastore(Mongo mongoClient, List<String> dbNames) {
        super(mongoClient, dbNames);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    protected void initFromDb(Mongo mongoClient, List<String> dbNames) {
        super.initFromDb(mongoClient, dbNames);

        //TODO run jobs
    }

    @Override
    public ITeslaDatasource getDataSource(String id) {
        return (ITeslaDatasource)super.getDataSource(id);
    }

    @Override
    public ITeslaDatasource createDataSource(String id) {
        return (ITeslaDatasource)super.createDataSource(id);
    }

    @Override
    protected DataSource createSourceInternal(DBCollection coll) {
        return new ITeslaDatasource(coll);
    }

    public void setHarvestJobs(Map<String, DatasourceJob> harvestJobs) {
        this.harvestJobs = harvestJobs;
    }

    public Map<String, DatasourceJob> getHarvestJobs() {
        return harvestJobs;
    }
}
