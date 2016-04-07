/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.modules.histo;

import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Identifiable;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import eu.itesla_project.modules.histo.cache.HistoDbCache;
import org.joda.time.Interval;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface HistoDbClient extends AutoCloseable {

    HistoDbCache getCache();

    List<HistoDbAttributeId> listAttributes() throws IOException;

    int queryCount(Interval interval, HistoDbHorizon horizon) throws IOException;

    InputStream queryCsv(HistoQueryType queryType, Set<Country> countries, Set<HistoDbEquip> equips, Set<HistoDbAttr> attrs, Interval interval, HistoDbHorizon horizon, boolean zipped, boolean async) throws IOException, InterruptedException;

    InputStream queryCsv(HistoQueryType queryType, Set<HistoDbAttributeId> attrIds, Interval interval, HistoDbHorizon horizon, boolean zipped, boolean async) throws IOException, InterruptedException;

    HistoDbStats queryStats(Set<Country> countries, Set<HistoDbEquip> equips, Set<HistoDbAttr> attrs, Interval interval, HistoDbHorizon horizon, boolean async) throws IOException, InterruptedException;

    HistoDbStats queryStats(Set<HistoDbAttributeId> attrIds, Interval interval, HistoDbHorizon horizon, boolean async) throws IOException, InterruptedException;

    List<String> listDbs();

    String getDbName();

    void setDbName(String dbName);

    void clearDb(String dbName);

    void clearDb();
}
