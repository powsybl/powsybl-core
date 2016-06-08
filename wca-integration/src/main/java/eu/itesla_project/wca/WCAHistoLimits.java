/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.wca;

import com.google.common.collect.Range;
import eu.itesla_project.commons.util.StringToIntMapper;
import eu.itesla_project.iidm.datasource.DataSource;
import eu.itesla_project.iidm.export.ampl.AmplConstants;
import eu.itesla_project.iidm.export.ampl.AmplSubset;
import eu.itesla_project.iidm.export.ampl.util.Column;
import eu.itesla_project.iidm.export.ampl.util.TableFormatter;
import eu.itesla_project.iidm.network.*;
import eu.itesla_project.modules.histo.*;
import org.joda.time.Interval;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class WCAHistoLimits implements AmplConstants, WCAConstants {

    private final Interval histoInterval;

    private final Map<String, Range<Float>> loadLimits = new HashMap<>();
    private final Map<String, Range<Float>> danglingLineLimits = new HashMap<>();
    private final Map<String, Range<Float>> generatorLimits = new HashMap<>();

    public WCAHistoLimits(Interval histoInterval) {
        this.histoInterval = Objects.requireNonNull(histoInterval);
    }

    private static Range<Float> range(String id, HistoDbAttr attr, HistoDbStats stats) {
        HistoDbAttributeId pAttrId = new HistoDbNetworkAttributeId(id, HistoDbAttr.P);
        float p_min = stats.getValue(HistoDbStatsType.MIN, pAttrId, INVALID_FLOAT_VALUE);
        float p_max = stats.getValue(HistoDbStatsType.MAX, pAttrId, INVALID_FLOAT_VALUE);
        return Range.closed(p_min, p_max);
    }

    public void load(Network network, HistoDbClient histoDbClient) throws IOException, InterruptedException {
        Set<HistoDbAttributeId> attributeIds = new LinkedHashSet<>();
        for (Load l : network.getLoads()) {
            if (l.getLoadType() != LoadType.FICTITIOUS) {
                attributeIds.add(new HistoDbNetworkAttributeId(l.getId(), HistoDbAttr.P));
            }
        }
        for (DanglingLine dl : network.getDanglingLines()) {
            attributeIds.add(new HistoDbNetworkAttributeId(dl.getId(), HistoDbAttr.P0));
        }
        for (Generator g : network.getGenerators()) {
            attributeIds.add(new HistoDbNetworkAttributeId(g.getId(), HistoDbAttr.P));
        }

        HistoDbStats stats = histoDbClient.queryStats(attributeIds, histoInterval, HistoDbHorizon.SN, true);

        for (Load l : network.getLoads()) {
            String id = l.getId();
            loadLimits.put(id, range(id, HistoDbAttr.P, stats));
        }

        for (DanglingLine dl : network.getDanglingLines()) {
            String id = dl.getId();
            danglingLineLimits.put(id, range(id, HistoDbAttr.P0, stats));
        }

        for (Generator g : network.getGenerators()) {
            String id = g.getId();
            generatorLimits.put(id, range(id, HistoDbAttr.P, stats));
        }
    }

    public void write(DataSource dataSource, StringToIntMapper<AmplSubset> mapper) throws IOException {

        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream(HISTO_LOADS_FILE_SUFFIX, TXT_EXT, false), StandardCharsets.UTF_8)) {
            TableFormatter formatter = new TableFormatter(LOCALE, writer,
                    "loads historical data " + histoInterval,
                    INVALID_FLOAT_VALUE,
                    new Column("num"),
                    new Column("min p (MW)"),
                    new Column("max p (MW)"),
                    new Column("id"));
            formatter.writeHeader();

            for (Map.Entry<String, Range<Float>> e : loadLimits.entrySet()) {
                String id = e.getKey();
                Range<Float> range = e.getValue();
                int num = mapper.getInt(AmplSubset.LOAD, id);
                formatter.writeCell(num)
                        .writeCell(range.lowerEndpoint())
                        .writeCell(range.upperEndpoint())
                        .writeCell(id)
                        .newRow();
            }
            for (Map.Entry<String, Range<Float>> e : danglingLineLimits.entrySet()) {
                String id = e.getKey();
                Range<Float> range = e.getValue();
                int num = mapper.getInt(AmplSubset.LOAD, id);
                formatter.writeCell(num)
                        .writeCell(range.lowerEndpoint())
                        .writeCell(range.upperEndpoint())
                        .writeCell(id + "_load")
                        .newRow();
            }
        }

        try (Writer writer = new OutputStreamWriter(dataSource.newOutputStream(HISTO_GENERATORS_FILE_SUFFIX, TXT_EXT, false), StandardCharsets.UTF_8)) {
            TableFormatter formatter = new TableFormatter(LOCALE, writer,
                    "generators historical data " + histoInterval,
                    INVALID_FLOAT_VALUE,
                    new Column("num"),
                    new Column("min p (MW)"),
                    new Column("max p (MW)"),
                    new Column("id"));
            formatter.writeHeader();

            for (Map.Entry<String, Range<Float>> e : generatorLimits.entrySet()) {
                String id = e.getKey();
                Range<Float> range = e.getValue();
                int num = mapper.getInt(AmplSubset.GENERATOR, id);
                formatter.writeCell(num)
                        .writeCell(range.lowerEndpoint())
                        .writeCell(range.upperEndpoint())
                        .writeCell(id)
                        .newRow();
            }
        }
    }

}
