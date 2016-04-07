/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Identifiable;
import eu.itesla_project.modules.histo.cache.HistoDbCache;
import org.joda.time.Interval;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ForwardingHistoDbClient implements HistoDbClient {

    private final HistoDbClient delegate;

    public ForwardingHistoDbClient(HistoDbClient delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public void clearDb() {
        delegate.clearDb();
    }

    @Override
    public void clearDb(String dbName) {
        delegate.clearDb(dbName);
    }

    @Override
    public HistoDbCache getCache() {
        return delegate.getCache();
    }

    @Override
    public String getDbName() {
        return delegate.getDbName();
    }

    @Override
    public List<HistoDbAttributeId> listAttributes() throws IOException {
        return delegate.listAttributes();
    }

    @Override
    public List<String> listDbs() {
        return delegate.listDbs();
    }

    @Override
    public int queryCount(Interval interval, HistoDbHorizon horizon) throws IOException {
        return delegate.queryCount(interval, horizon);
    }

    @Override
    public InputStream queryCsv(HistoQueryType queryType, Set<HistoDbAttributeId> attrIds, Interval interval, HistoDbHorizon horizon, boolean zipped, boolean async) throws IOException, InterruptedException {
        return delegate.queryCsv(queryType, attrIds, interval, horizon, zipped, async);
    }

    @Override
    public InputStream queryCsv(HistoQueryType queryType, Set<Country> countries, Set<HistoDbEquip> equips, Set<HistoDbAttr> attrs, Interval interval, HistoDbHorizon horizon, boolean zipped, boolean async) throws IOException, InterruptedException {
        return delegate.queryCsv(queryType, countries, equips, attrs, interval, horizon, zipped, async);
    }

    @Override
    public HistoDbStats queryStats(Set<HistoDbAttributeId> attrIds, Interval interval, HistoDbHorizon horizon, boolean async) throws IOException, InterruptedException {
        return delegate.queryStats(attrIds, interval, horizon, async);
    }

    @Override
    public HistoDbStats queryStats(Set<Country> countries, Set<HistoDbEquip> equips, Set<HistoDbAttr> attrs, Interval interval, HistoDbHorizon horizon, boolean async) throws IOException, InterruptedException {
        return delegate.queryStats(countries, equips, attrs, interval, horizon, async);
    }

    @Override
    public void setDbName(String dbName) {
        delegate.setDbName(dbName);
    }

    @Override
    public void close() throws Exception {
        delegate.close();
    }
}
