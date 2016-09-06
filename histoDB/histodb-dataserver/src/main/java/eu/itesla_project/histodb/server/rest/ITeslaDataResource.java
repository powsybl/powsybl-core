package eu.itesla_project.histodb.server.rest;

import be.pepite.dataserver.api.*;
import be.pepite.dataserver.datastores.mongodb.MongoDataSource;
import be.pepite.dataserver.datastores.mongodb.MongoDataset;
import eu.itesla_project.histodb.server.ITeslaDatasource;
import eu.itesla_project.histodb.server.attributes.CurrentPowerRatio;
import eu.itesla_project.histodb.server.attributes.StrictlyNegative;
import eu.itesla_project.histodb.server.attributes.StrictlyPositive;
import be.pepite.dataserver.rest.resource.CsvRepresentation;
import be.pepite.dataserver.rest.resource.DataResource;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Get;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: pduchesne
 * Date: 4/06/13
 * Time: 12:19
 * To change this template use File | Settings | File Templates.
 */
public class ITeslaDataResource
    extends DataResource
{

    Logger log = LoggerFactory.getLogger(ITeslaDataResource.class);

    @Get("csv|json")
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

        ITeslaDatasource teslaDs = (ITeslaDatasource)ds;

        String fieldName = (String)getRequest().getAttributes().get("field");
        if (fieldName != null) {
            if ("regions".equals(fieldName))
                return teslaDs.getMetadata().getRegions();
            else if ("count".equals(fieldName)) {
                DBCursor cursor = getData().getCursor();
                cursor.sort(null);
                return ((Number)cursor.explain().get("n")).intValue();
            }
            else if ("forecastsDiff".equals(fieldName))
                return getForecastsAndSnapshots();
            else if ("stations".equals(fieldName))
                return teslaDs.getMetadata().getToposPerSubstation().keySet();
            else if ("explain".equals(fieldName))
                return new JsonRepresentation(((MongoDataset)getData()).explain().toString());
            else if ("countries".equals(fieldName))
                return teslaDs.getMetadata().getCountries();
            else if ("topos".equals(fieldName)) {
                //warn this is potentially a large result - must implement a finer query mechanism (per substation id)
                String stationId = queryForm.getFirstValue("topoId", true);
                if (stationId == null) {
                    return teslaDs.getMetadata().getToposPerSubstation();
                }
                else return teslaDs.getMetadata().getToposPerSubstation().get(stationId).keySet();
            }

            else return null;
        } else
            return super.getRepresentation();
    }

    @Override
    public MongoDataset getData() {
        MongoDataset dataset = (MongoDataset)super.getData();

        String indexing = getRequest().getOriginalRef().getQueryAsForm().getFirstValue("indexing", null);
        if (indexing != null) {
            List<Map.Entry<String, MongoDataSource.INDEXTYPE>> sortedIndexFields =
                    new ArrayList(dataset.getIndexTypes().entrySet());
            Collections.sort(sortedIndexFields,
                    new Comparator<Map.Entry<String, MongoDataSource.INDEXTYPE>>() {
                        @Override
                        public int compare(Map.Entry<String, MongoDataSource.INDEXTYPE> o1, Map.Entry<String, MongoDataSource.INDEXTYPE> o2) {
                            return Integer.compare(o1.getValue().getOrder(), o2.getValue().getOrder());
                        }
                    }
            );
            DBObject indexObject = new BasicDBObject();
            for (Map.Entry<String, MongoDataSource.INDEXTYPE> indexEntry: sortedIndexFields) {
                indexObject.put(indexEntry.getKey(), 1);
            }
            if ("auto".equals(indexing)) {
                // nothing to do
            } else if ("optimal".equals(indexing)) {
                List<DBObject> indexInfo = dataset.getCursor().getCollection().getIndexInfo();
                boolean exists = false;
                for (DBObject indexObj: indexInfo) {
                    DBObject indexDescr = (DBObject)indexObj.get("key");
                    if (indexDescr.equals(indexObject)) exists = true;
                }
                if (exists)
                    dataset.getCursor().hint(indexObject);
            } else if ("force".equals(indexing)) {
                dataset.getCursor().getCollection().ensureIndex(indexObject);
                dataset.getCursor().hint(indexObject);
            } else {
                // assume the indexing value is a named index
                dataset.getCursor().hint(indexing);
            }
        }


        return dataset;
    }

    private CsvRepresentation getForecastsAndSnapshots() {
        String forecastStr = getRequest().getOriginalRef().getQueryAsForm().getFirstValue("forecast");
        String horizon = getRequest().getOriginalRef().getQueryAsForm().getFirstValue("horizon");
        if ((forecastStr == null || Double.parseDouble(forecastStr) == 0) && (horizon == null || "SN".equals(horizon)))
            throw new IllegalArgumentException("ForecastsDiff operation must be used with either a positive 'forecast' value or a non-snapshot 'horizon'");

        final MongoDataset forecasts = (MongoDataset)getData();

        Form queryForm = getRequest().getOriginalRef().getQueryAsForm();
        queryForm.set("forecast", "0");
        queryForm.removeFirst("horizon");  //TODO use one or the other. Better to use horizon?
        MongoDataset snapshots = (MongoDataset)ds.getData(
                parseQuery(queryForm),
                0,
                -1,
                parseColumns(queryForm));

        Set<ColumnDescriptor> mergedList = new TreeSet<ColumnDescriptor>();
        mergedList.addAll(Arrays.asList(forecasts.getQueriedFields()));
        mergedList.addAll(Arrays.asList(snapshots.getQueriedFields()));
        ColumnDescriptor[] mergedFields = mergedList.toArray(new ColumnDescriptor[] {});

        Dataset result = new MergeDataset(forecasts, snapshots, mergedFields);

        String headersStr = queryForm.getFirstValue("headers");
        String nanValue = queryForm.getFirstValue("nanValue");
        char delimiter = queryForm.getFirstValue("delimiter", ",").charAt(0);

        return new CsvRepresentation(result, Boolean.parseBoolean(headersStr), delimiter, nanValue);


    }

    @Override
    protected Map<String, Object> parseQuery(Form queryForm) {
        Map<String, Object> query = super.parseQuery(queryForm);

        String timeStr = queryForm.getFirstValue("time");
        String daytimeStr = queryForm.getFirstValue("daytime");
        String forecastStr = queryForm.getFirstValue("forecast");
        String horizonStr = queryForm.getFirstValue("horizon");

        DateTimeFormatter dateParser = ISODateTimeFormat.dateTimeParser();
        DateTimeFormatter hourParser = ISODateTimeFormat.timeParser();

        if (timeStr != null) {
            Object filterValue;
            if (timeStr.startsWith("[")) {
                // interval
                if (!timeStr.endsWith("]")) throw new IllegalArgumentException("time filter must be of the form [ISODate,ISODate] ");
                String[] dates = timeStr.substring(1,timeStr.length()-1).split(",");
                filterValue = new Date[] {dateParser.parseDateTime(dates[0]).toDate(), dateParser.parseDateTime(dates[1]).toDate()};
            } else {
                // single value for equality
                filterValue = dateParser.parseDateTime(timeStr);
            }
            query.put("datetime", filterValue);
        }

        if (daytimeStr != null) {
            Object filterValue;
            if (daytimeStr.startsWith("[")) {
                // interval
                if (!daytimeStr.endsWith("]")) throw new IllegalArgumentException("daytime filter must be of the form [ISOTime,ISOTime] ");
                String[] times = daytimeStr.substring(1,daytimeStr.length()-1).split(",");
                filterValue = new Long[] {hourParser.parseMillis(times[0]), hourParser.parseMillis(times[1])};
            } else {
                // single value for equality
                filterValue = hourParser.parseMillis(daytimeStr);
            }
            query.put("daytime", filterValue);
        }

        if (forecastStr != null) {
            query.put("forecastTime", Integer.parseInt(forecastStr));
        }

        if (horizonStr != null) {
            query.put("horizon", horizonStr);
        }

        return query;
    }

    @Override
    protected ColumnDescriptor[] parseColumns(Form queryForm) {
        String useReferenceCIM = queryForm.getFirstValue("useReferenceCIM", true, "true");

        ColumnDescriptor[] attributes;

        if (Boolean.parseBoolean(useReferenceCIM)) {
            // rely on referenceCIM to choose attributes

            String powerTypeStr = queryForm.getFirstValue("powerType");
            String eqtypeStr = queryForm.getFirstValue("equip");
            String measuretypeStr = queryForm.getFirstValue("attr");
            String regionStr = queryForm.getFirstValue("region");
            String countryStr = queryForm.getFirstValue("country");
            String equipIdsStr = queryForm.getFirstValue("ids");

            Collection<String> powerTypes = null;
            if (powerTypeStr != null) powerTypes = Arrays.asList(powerTypeStr.split(","));

            Collection<String> types = null;
            if (eqtypeStr != null) types = Arrays.asList(eqtypeStr.split(","));

            Collection<String> measureTypes = null;
            if (measuretypeStr != null) measureTypes = Arrays.asList(measuretypeStr.split(","));

            Collection<String> regions = null;
            if (regionStr != null) regions = Arrays.asList(regionStr.split(","));

            Collection<String> countries = null;
            if (countryStr != null) countries = Arrays.asList(countryStr.split(","));

            Collection<String> equipIds = null;
            if (equipIdsStr != null) equipIds = Arrays.asList(equipIdsStr.split(","));

            //String[] equipIds = ((ITeslaDatasource)ds).findEquipments(types, regions, countries);

            attributes = ((ITeslaDatasource)ds).findAttributes(types, powerTypes, measureTypes, regions, countries, equipIds);
        } else {
            // ignore referenceCIM --> take all attributes found in DB
            attributes = ColumnDescriptor.getDescriptorsForNames(ds.getMetadata().getColumnNames());
            List<ColumnDescriptor> tempList = new ArrayList(Arrays.asList(attributes));

            // add functional attributes on every potential equipment (guesswork)
            for (ColumnDescriptor cd: attributes) {
                // in the absence of a referenceCIM, using heuristics on attribute names to guess generators or lines
                if (cd.getName().endsWith("_SM_P") || cd.getName().endsWith("_SM_Q")) {
                    tempList.add(new StrictlyPositive(cd.getName()));
                    tempList.add(new StrictlyNegative(cd.getName()));
                } else if (cd.getName().contains("__TO__") && cd.getName().endsWith("_I")) {
                    tempList.add(new CurrentPowerRatio(cd.getName().substring(0, cd.getName().length()-2)));
                }
            }

            attributes = tempList.toArray(new ColumnDescriptor[] {});

        }

        return subsetColumns(attributes, queryForm);
    }

    @Override
    protected ColumnDescriptor[] subsetColumns(ColumnDescriptor[] columns, Form queryForm) {
        ColumnDescriptor[] cols = super.subsetColumns(columns, queryForm);

        for (int i=0;i<cols.length;i++) {
            ColumnDescriptor cd = cols[i];
            if (cd.getName().endsWith("_PP") && !(cd instanceof FunctionalColumn)) {
                cols[i] = new StrictlyPositive(cd.getName().substring(0, cd.getName().length()-1));
            } else if (cd.getName().endsWith("_PN") && !(cd instanceof FunctionalColumn)) {
                cols[i] = new StrictlyNegative(cd.getName().substring(0, cd.getName().length()-1));
            } else if (cd.getName().endsWith("_IP") && !(cd instanceof FunctionalColumn)) {
                cols[i] = new CurrentPowerRatio(cd.getName().substring(0, cd.getName().length()-3));
            }
        }

        return cols;
    }

}
