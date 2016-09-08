package eu.itesla_project.histodb.server.rest;

import be.pepite.dataserver.api.ColumnDescriptor;
import be.pepite.dataserver.api.Dataset;
import eu.itesla_project.histodb.server.ITeslaDatasource;
import org.restlet.data.Form;
import org.restlet.resource.Get;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/10/12
 * Time: 16:19
 * To change this template use File | Settings | File Templates.
 */
public class ITeslaTopologies
    extends ITeslaDataResource
{

    @Get("json")
    public Object getRepresentation() {

        Form queryForm = getRequest().getOriginalRef().getQueryAsForm();

        String stationId = (String)getRequest().getAttributes().get("stationId");
        String topoId = (String)getRequest().getAttributes().get("topoId");
        // try to get topoId from parameters -- needed to work around proxy limitations on URL path decoding
        if (topoId == null) topoId = queryForm.getFirstValue("topoId");
        boolean showHeaders = Boolean.parseBoolean(queryForm.getFirstValue("headers"));

        ColumnDescriptor[] topoColumns;
        if (stationId != null) {
            topoColumns = new ColumnDescriptor[] {new ColumnDescriptor(stationId+"_TOPOHASH")};
        } else {
            // force the query to take only topologies hashes
            queryForm.set("equip", "stations");
            queryForm.set("attr", "T");
            topoColumns = this.parseColumns(queryForm);
            // remove datetime,forecast,horizon columns
            topoColumns = Arrays.copyOfRange(topoColumns,3,topoColumns.length);
        }

        if (topoId == null) {
            // retrieve stats for all topologies
            String startStr = queryForm.getFirstValue("start", "0");
            String countStr = queryForm.getFirstValue("count", "50");

            Map query = parseQuery(queryForm);

            Dataset topologies = ds.getData(
                    query,
                    Integer.parseInt(startStr),
                    Integer.parseInt(countStr),
                    topoColumns);

            Map<String,Map<String,Float>> stationsFreq = new TreeMap();
            Iterator<Collection> topoIt = topologies.getRowIterator();
            while(topoIt.hasNext()) {
                int i=0;
                for (String topoHash: (Collection<String>)topoIt.next()) {
                    String colName = topologies.getColumnNames()[i];
                    String station = colName.substring(0, colName.length() - 9);
                    if (topoHash != null) {
                        Map<String,Float> frequencies = stationsFreq.get(station);
                        if (frequencies == null) stationsFreq.put(station, frequencies = new TreeMap<String, Float>());

                        float count = frequencies.containsKey(topoHash)?frequencies.get(topoHash):0;
                        frequencies.put(topoHash, count+1);
                    } else {
                        //TODO there's no topology for this station in this record
                    }
                    i++;
                }
            }

            for (Map<String,Float> frequencies: stationsFreq.values())
                for (String topoHash: frequencies.keySet())
                    frequencies.put(topoHash, ((float)frequencies.get(topoHash)) / topologies.getRowCount());

            return stationsFreq;

        } else {
            //retrieve a single topology
            return ((ITeslaDatasource)ds).getMetadata().getToposPerSubstation().get(stationId).get(topoId);
        }


    }

}
