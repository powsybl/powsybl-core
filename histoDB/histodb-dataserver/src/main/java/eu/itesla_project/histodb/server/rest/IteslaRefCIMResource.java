package eu.itesla_project.histodb.server.rest;

import eu.itesla_project.histodb.server.CimHistoImporter;
import eu.itesla_project.histodb.server.ITeslaDatasource;
import be.pepite.dataserver.rest.resource.AbstractDataserverResource;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;

import java.io.File;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 26/02/14
 * Time: 09:42
 * To change this template use File | Settings | File Templates.
 */
public class IteslaRefCIMResource
    extends AbstractDataserverResource
{
    @Get
    public String getReferenceCIM() {
        return ((ITeslaDatasource)datasource).getLatestCim();
    }

    @Put
    @Post
    public void setReferenceCIM(String referenceCimPath) throws IOException {
        if (datasource == null) {
            //create datasource if needed
            datasource = getApplication().getDataService().getDataStore(storeId).createDataSource(dataId);
        }

        ((ITeslaDatasource)datasource).setLatestCim(referenceCimPath);
        ((ITeslaDatasource)datasource).setLatestNetwork(CimHistoImporter.readCim(new File(referenceCimPath)));
    }

}
