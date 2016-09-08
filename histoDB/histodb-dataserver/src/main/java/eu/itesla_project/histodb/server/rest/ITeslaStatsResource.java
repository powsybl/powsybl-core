package eu.itesla_project.histodb.server.rest;

import be.pepite.dataserver.rest.resource.CsvRepresentation;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.Get;

/**
 * Created by IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/10/12
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public class ITeslaStatsResource
    extends ITeslaDataResource
{

    @Get("csv")
    public Object getRepresentation() {

        if (ds == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        if (!ds.getStatus().isInitialized()) {
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            return "Initializing...";
        }

        Form queryForm = getRequest().getOriginalRef().getQueryAsForm();
        char delimiter = queryForm.getFirstValue("delimiter", ",").charAt(0);

        return new CsvRepresentation(getStats(), true, delimiter);
    }

}
