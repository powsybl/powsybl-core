/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.histodb.client.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Table;
import com.google.common.io.ByteStreams;
import eu.itesla_project.commons.io.MathUtil;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Identifiable;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.modules.histo.*;
import eu.itesla_project.modules.histo.cache.HistoDbCache;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class HistoDbClientImpl implements HistoDbClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoDbClientImpl.class);

    private static final int ASYNC_CHECK_DELAY = 10; // seconds

    private final HistoDbConfig config;

    private final HistoDbHttpClient httpClient;

    public HistoDbClientImpl(HistoDbConfig config) {
        this(config, new HistoDbHttpClientImpl(null));
    }

    public HistoDbClientImpl(HistoDbConfig config, HistoDbCache cache) {
        this(config, new HistoDbHttpClientImpl(cache));
    }

    public HistoDbClientImpl(HistoDbConfig config, HistoDbHttpClient httpClient) {
        this.config = Objects.requireNonNull(config, "config is null");
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient is null");
    }

    @Override
    public HistoDbCache getCache() {
        return httpClient.getCache();
    }

    @Override
    public List<HistoDbAttributeId> listAttributes() throws IOException {
        List<HistoDbAttributeId> attrs = new ArrayList<>();
        try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(config, "metadata.json", ImmutableMap.of("colRange", "*")))) {
            JsonFactory jfactory = new JsonFactory();
            try (JsonParser jParser = jfactory.createParser(is)) {
                while (jParser.nextToken() != null) {
                    String fieldname = jParser.getCurrentName();
                    if ("name".equals(fieldname)) {
                        jParser.nextToken();
                        attrs.add(HistoDbAttributeIdParser.parse(jParser.getText()));
                    }
                }
            }
        }
        return attrs;
    }

    private int parseCount(InputStream is) throws IOException {
        byte[] data = ByteStreams.toByteArray(is);

        int count = -1;
        try {
            count = Integer.valueOf(new String(data, Charset.forName("UTF-8")));
        } catch (NumberFormatException e) {
            // FIXME I don't know why but histo db seems to return from time to time
            // an empty string
            LOGGER.error(e.toString() , e);
        }

        return count;
    }

    public int totalCount(Map<String, String> query) throws IOException {
        Map<String, String> queryMap = new LinkedHashMap<>(3);
        queryMap.put("headers", "true");
        queryMap.put("count", "-1");
        if (query != null) {
            queryMap.putAll(query);
        }

        try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(config, "itesla/" + HistoQueryType.data + "/count.csv", queryMap))) {
            return parseCount(is);
        }
    }

    private static String toString(Interval interval) {
        DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm'Z'");
        return "[" + interval.getStart().toDateTime(DateTimeZone.UTC).toString(fmt) + "," +
                                interval.getEnd().toDateTime(DateTimeZone.UTC).toString(fmt) + "]";
    }

    @Override
    public int queryCount(Interval interval, HistoDbHorizon horizon) throws IOException {
        Objects.requireNonNull(interval, "interval is null");
        Objects.requireNonNull(horizon, "horizon is null");

        Map<String, String> query = new LinkedHashMap<>();
        // pour debrider le nombre de lignes
        query.put("count", "-1");
        query.put("time", toString(interval));
        query.put("horizon", horizon.toString());

        try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(config, "itesla/" + HistoQueryType.data + "/count.csv", query))) {
            byte[] data = ByteStreams.toByteArray(is);
            return Integer.valueOf(new String(data, Charset.forName("UTF-8")));
        }
    }

    public int queryCount(Identifiable equipment, HistoDbAttr attr, List<? extends HistoDbFilter> filters, Interval interval) throws IOException {
        Objects.requireNonNull(equipment, "equipment is null");
        Objects.requireNonNull(attr, "attr is null");
        Objects.requireNonNull(interval, "interval is null");

        Map<String, String> query = new LinkedHashMap<>();
        query.put("headers", "true");
        query.put("cols", equipment.getId() + "_" + attr);
        // filters
        if (filters != null && filters.size() > 0) {
            StringBuilder value = new StringBuilder();
            for (Iterator<? extends HistoDbFilter> it = filters.iterator(); it.hasNext();) {
                value.append(equipment.getId()).append("_").append(attr).append(":");
                it.next().format(value);
                if (it.hasNext()) {
                    value.append(",");
                }
            }
            query.put("filters", value.toString());
        }
        // pour debrider le nombre de lignes
        query.put("count", "-1");
        query.put("time", toString(interval));
        query.put("horizon", HistoDbHorizon.SN.toString());

        try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(config, "itesla/" + HistoQueryType.data + "/count.csv", query))) {
            return parseCount(is);
        }
    }

    @Override
    public HistoDbStats queryStats(Set<Country> countries, Set<HistoDbEquip> equips,
                                   Set<HistoDbAttr> attrs, Interval interval, HistoDbHorizon horizon, boolean async) throws IOException, InterruptedException {
        try (Reader reader = new InputStreamReader(queryCsv(HistoQueryType.stats, countries, equips, attrs, interval, horizon, true, async))) {
            Table<String, String, Float> table = MathUtil.parseMatrix(reader);
            return new HistoDbStats(table);
        }
    }

    @Override
    public HistoDbStats queryStats(Set<HistoDbAttributeId> attrIds, Interval interval, HistoDbHorizon horizon, boolean async) throws IOException, InterruptedException {
        try (Reader reader = new InputStreamReader(queryCsv(HistoQueryType.stats, attrIds, interval, horizon, true, async))) {
            Table<String, String, Float> table = MathUtil.parseMatrix(reader);
            return new HistoDbStats(table);
        }
    }

    String[] getColumns(Map<String, String> query) throws IOException, InterruptedException {
        Objects.requireNonNull(query, "query is null");

        Map<String, String> queryMap = new LinkedHashMap<>();
        queryMap.put("headers", "true");
        queryMap.put("count", "1");
        if (query != null) {
            queryMap.putAll(query);
        }

        try (InputStream is =  httpClient.getHttpRequest(new HistoDbUrl(config, "itesla/" + HistoQueryType.data + ".csv", queryMap));
             CsvListReader reader = new CsvListReader(new InputStreamReader(is), CsvPreference.STANDARD_PREFERENCE)) {
            return reader.getHeader(true);
        }

    }

    Reader openDataStream(Map<String, String> query, char delimiter) throws IOException, InterruptedException {
        Map<String, String> queryMap = new LinkedHashMap<>();
        queryMap.put("headers", "true");
        queryMap.put("delimiter", Character.toString(delimiter));
        queryMap.put("count", "-1");
        queryMap.put("colRange", "*");
        if (query != null) {
            queryMap.putAll(query);
        }

        return new InputStreamReader(httpClient.getHttpRequest(new HistoDbUrl(config, "itesla/" + HistoQueryType.data + ".csv", queryMap)));
    }

    public Map<String, Map<String, Collection<Collection<String>>>> getTopologyDescriptions(Interval interval) {
        try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(config,  "itesla/data/topos.csv", Collections.emptyMap()))) {
            byte[] bytes = ByteStreams.toByteArray(is);
            JSONObject topologiesPerStation = JSONObject.fromObject(new String(bytes, Charset.forName("UTF-8")));
            return topologiesPerStation;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Collection<Collection<String>> getTopologyDescription(String id, String topoHash) {
        try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(config, "itesla/topos/" + id + "/" + topoHash + ".json", Collections.emptyMap()))) {
            byte[] bytes = ByteStreams.toByteArray(is);

            Collection<Collection<String>> result = new HashSet<>();

            JSONArray topology = JSONArray.fromObject(new String(bytes, Charset.forName("UTF-8")));
            for (int i=0;i<topology.size();i++) {
                JSONArray jsonConnectedSet = topology.getJSONArray(i);
                Collection<String> connectedSet = Arrays.asList((String[]) jsonConnectedSet.toArray(new String[]{}));

                result.add(connectedSet);
            }

            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private InputStream getHttpRequest(String path, Map<String, String> query, boolean async) throws IOException, InterruptedException {
        InputStream is;
        if (async) {
            query.put("async", "true");
            while ((is = httpClient.getHttpRequest(new HistoDbUrl(config, path, query))) == null) {
                Thread.sleep(1000 * ASYNC_CHECK_DELAY); // check every 10 seconds
            }
        } else {
            is = httpClient.getHttpRequest(new HistoDbUrl(config, path, query));
        }
        return is;
    }

    @Override
    public InputStream queryCsv(HistoQueryType queryType, Set<Country> countries, Set<HistoDbEquip> equips,
                           Set<HistoDbAttr> attrs, Interval interval, HistoDbHorizon horizon, boolean zipped, boolean async) throws IOException, InterruptedException {
        Objects.requireNonNull(queryType, "queryType is null");
        Objects.requireNonNull(equips, "equips is null");
        Objects.requireNonNull(attrs, "attr is null");
        Objects.requireNonNull(horizon, "horizon is null");
        if (equips.isEmpty()) {
            throw new IllegalArgumentException("equips is empty");
        }
        if (attrs.isEmpty()) {
            throw new IllegalArgumentException("attrs is empty");
        }
        Objects.requireNonNull(interval, "interval is null");
        if (async && !zipped) {
            throw new IllegalArgumentException("Asynchronous mode only works when zipped");
        }

        String path = "itesla/" + queryType + (zipped ? ".zip" : ".csv");
        Map<String, String> query = new LinkedHashMap<>();
        query.put("headers", "true");
        Joiner joiner = Joiner.on(",");
        query.put("equip", joiner.join(equips));
        query.put("attr", joiner.join(attrs));
        if (countries != null && countries.size() > 0) {
            query.put("country", joiner.join(countries));
        }
        // pour debrider le nombre de lignes et colonnes
        query.put("count", "-1");
        query.put("colRange", "*");
        query.put("time", toString(interval));
        query.put("horizon", horizon.toString());

        return getHttpRequest(path, query, async);
    }

    @Override
    public InputStream queryCsv(HistoQueryType queryType, Set<HistoDbAttributeId> attrIds, Interval interval, HistoDbHorizon horizon, boolean zipped, boolean async) throws IOException, InterruptedException {
        Objects.requireNonNull(queryType, "queryType is null");
        Objects.requireNonNull(attrIds, "attrIds is null");
        Objects.requireNonNull(horizon, "horizon is null");
        if (attrIds.isEmpty()) {
            throw new IllegalArgumentException("attrs is empty");
        }
        Objects.requireNonNull(interval, "interval is null");
        if (async && !zipped) {
            throw new IllegalArgumentException("Asynchronous mode only works when zipped");
        }

        String path = "itesla/" + queryType + (zipped ? ".zip" : ".csv");
        Map<String, String> query = new LinkedHashMap<>();
        query.put("headers", "true");
        // pour debrider le nombre de lignes et colonnes
        query.put("count", "-1");
        query.put("colRange", "*");
        Joiner joiner = Joiner.on(",");
        query.put("cols", joiner.join(new TreeSet<>(attrIds)));
        query.put("time", toString(interval));
        query.put("horizon", horizon.toString());

        return getHttpRequest(path, query, async);
    }

/*
    @Override
    public void createRecord(String[] headers, Object[] values) {
        StringWriter sw = new StringWriter();
        CsvListWriter writer = new CsvListWriter(sw, CsvPreference.STANDARD_PREFERENCE);

        try {
            writer.writeHeader(headers);
            writer.write(values);

            writer.close();

            StringBuilder url = new StringBuilder();
            url.append("https://").append(histoDbParameters.getHost()).append(":").append(histoDbParameters.getPort())
                    .append("/histodb/rest/").append(dbName).append("/data/autoIncrement") // WARN here one must NOT use the itesla suffix (not supporting POST of new data)
                    .append(".csv");

            postHttpRequest(url.toString(), sw.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to store network values for id "+id, e);
        }
    }
    */

    public String updateRecord(String id, String[] headers, Object[] values) {
        StringWriter sw = new StringWriter();
        CsvListWriter writer = new CsvListWriter(sw, new CsvPreference.Builder('"', ',', "\r\n").build());

        /*
        There's no need to add the _id, as it is passed in the URL

        ArrayList<String> headersList = new ArrayList<>();
        headersList.add("_id");
        headersList.addAll(Arrays.asList(headers));

        ArrayList<Object> valuesList = new ArrayList<>();
        valuesList.add(id);
        valuesList.addAll(Arrays.asList(values));

        writer.writeHeader(headersList.toArray(new String[] {}));
        writer.write(valuesList.toArray());
        */

        try {
            writer.writeHeader(headers);
            writer.write(values);

            writer.close();

            // if no id is provided, rely on server-side auto-increment mechanism
            if (id == null) id = "autoIncrement";

            try (InputStream is = httpClient.postHttpRequest(new HistoDbUrl(config,
                                                                            "data/" + id + ".csv", // WARN here one must NOT use the itesla suffix (not supporting POST of new data)
                                                                            Collections.emptyMap()),
                                                             sw.toString().getBytes("UTF-8"))) {
                return new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to store network values for id "+id, e);
        }
    }

    public void storeStartState(String simulationId, Network network, Set<Country> countryFilter) {

        Map<HistoDbAttributeId, Object> networkValues = IIDM2DB.extractCimValues(network, new IIDM2DB.Config(null, false, true, countryFilter)).getSingleValueMap();

        List<String> headers = new ArrayList<>(networkValues.size());
        for (HistoDbAttributeId attrId : networkValues.keySet()) {
            headers.add(attrId.toString());
        }

        updateRecord(
                simulationId,
                headers.toArray(new String[] {}),
                networkValues.values().toArray());

    }

    public void deleteTable() {
        deleteTable(config);
    }

    public void deleteTable(HistoDbConfig config) {
        try {
            httpClient.deleteHttpRequest(new HistoDbUrl(config, "itesla", Collections.emptyMap()));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete histo DB table", e);
        }
    }

    @Override
    public List<String> listDbs() {
        HistoDbConfig otherConfig = config.clone();
        otherConfig.setSourceName(null);
        List<String> dbs = new ArrayList<>();
        try (InputStream is = httpClient.getHttpRequest(new HistoDbUrl(otherConfig, ".json", Collections.emptyMap()))) {
            String json = new String(ByteStreams.toByteArray(is), StandardCharsets.UTF_8);
            JSONObject jsonObj = JSONObject.fromObject(json);
            for (Object name : jsonObj.names()) {
                dbs.add((String) name);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dbs;
    }

    @Override
    public String getDbName() {
        return config.getSourceName();
    }

    @Override
    public void setDbName(String dbName) {
        config.setSourceName(dbName);
    }

    @Override
    public void clearDb() {
        deleteTable();
    }

    @Override
    public void clearDb(String dbName) {
        HistoDbConfig otherConfig = config.clone();
        otherConfig.setSourceName(dbName);
        deleteTable(otherConfig);
    }

    @Override
    public void close() throws Exception {
        httpClient.close();
    }

}
