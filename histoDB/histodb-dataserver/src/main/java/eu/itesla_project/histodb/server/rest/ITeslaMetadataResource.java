package eu.itesla_project.histodb.server.rest;

import org.restlet.resource.Get;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/06/13
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
public class ITeslaMetadataResource
    extends ITeslaDataResource
{

    @Get("json")
    public Object getRepresentation() {
        return getMetadata();
    }

}
