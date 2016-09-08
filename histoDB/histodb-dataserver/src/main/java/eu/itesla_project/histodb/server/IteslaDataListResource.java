/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.server;

import be.pepite.dataserver.api.DataSource;
import be.pepite.dataserver.rest.resource.DataListResource;
import eu.itesla_project.modules.histo.HistoDbAttributeId;
import eu.itesla_project.modules.histo.IIDM2DB;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Post;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/10/12
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public class IteslaDataListResource
    extends DataListResource
{

    @Post("form:html")
    public Object postCimDirectory(Form queryForm) throws IOException {

        if (queryForm == null) queryForm = getRequest().getOriginalRef().getQueryAsForm();

        if (dataId == null || storeId == null) {
            throw new IllegalArgumentException("Missing datasource or store id");
        } else {

            ITeslaDatastore store = (ITeslaDatastore)getApplication().getDataService().getDataStore(storeId);
            if (store == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return "Datastore does not exist: "+storeId;
            }

            ITeslaDatasource ds = store.getDataSource(dataId);
            if (ds == null) {
                ds = store.createDataSource(dataId);
            }

            CimHistoImporter importer = new CimHistoImporter(ds);
            importer.addCim(new File(queryForm.getFirstValue("dir")));

            ds.persistState();
        }

        return null;
    }

    @Override
    protected void insertRecord(DataSource ds, String[] headers, String[] csvValues) {

        if (((ITeslaDatasource)ds).getLatestNetwork() != null) {
            // if a reference network is available, use it as template

            // assume the networkReference contains only one set of Values
            LinkedHashMap<String, Object> valueMap = new LinkedHashMap<String, Object>();
            for (Map.Entry<HistoDbAttributeId, Object> entry : IIDM2DB.extractCimValues(((ITeslaDatasource) ds).getLatestNetwork(), new IIDM2DB.Config(null, true)).getSingleValueMap().entrySet()) {
                valueMap.put(entry.getKey().toString(), entry.getValue());
            }
            
            Object[] newValues = parseCsvRecord(ds, headers, csvValues);

            for (int i=0;i<headers.length;i++) valueMap.put(headers[i], newValues[i]);

            ds.putData(valueMap.keySet().toArray(new String[]{}), valueMap.values().toArray());
        } else
            super.insertRecord(ds, headers, csvValues);
    }
}
