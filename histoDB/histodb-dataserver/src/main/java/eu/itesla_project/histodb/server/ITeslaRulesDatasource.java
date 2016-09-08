/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import be.pepite.dataserver.api.ColumnDescriptor;
import be.pepite.dataserver.datastores.mongodb.MongoDataSource;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/06/13
 * Time: 16:18
 * To change this template use File | Settings | File Templates.
 */
public class ITeslaRulesDatasource
    extends MongoDataSource
{

    public ITeslaRulesDatasource(DBCollection db) {
        super(db);
    }

    @Override
    public void ensureIndexes() {
        mongoDb.ensureIndex("contingencyId");
        mongoDb.ensureIndex("indexType");
        mongoDb.ensureIndex("algoType");
        mongoDb.ensureIndex("workflowId");
    }


    @Override
    protected DBCursor getDataCursor(Map<String, ?> ranges, int start, int count, ColumnDescriptor[] columnDescriptors, Map<String, INDEXTYPE> indexTypes) {
        DBCursor cursor = super.getDataCursor(ranges, start, count, columnDescriptors, indexTypes);

        return cursor.sort(new BasicDBObject("datetime", 1));
    }

    public Collection<String> getIndexTypes() {
        return mongoDb.distinct("indexType");
    }

}
