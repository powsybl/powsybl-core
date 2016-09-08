package eu.itesla_project.histodb.server.rest;

import be.pepite.dataserver.datastores.mongodb.MongoDataSource;
import be.pepite.dataserver.rest.resource.AbstractDataserverResource;
import org.json.JSONArray;
import org.restlet.resource.Get;

/**
 * Created by IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/10/12
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public class ITeslaRulesIndexTypes
    extends AbstractDataserverResource
{

    @Get("json")
    public Object getRepresentation() throws Exception{
        return new JSONArray(((MongoDataSource)datasource).getDistinctValues("indexType"));
    }
}
