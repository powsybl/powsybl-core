/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.dataframe.network.adders;

import com.powsybl.commons.PowsyblException;
import com.powsybl.dataframe.SeriesMetadata;
import com.powsybl.dataframe.update.StringSeries;
import com.powsybl.dataframe.update.UpdatingDataframe;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * @author Etienne Lesot <etienne.lesot at rte-france.com>
 */
public class AliasDataframeAdder extends AbstractSimpleAdder {

    private static final List<SeriesMetadata> METADATA = List.of(
        SeriesMetadata.stringIndex("id"),
        SeriesMetadata.strings("alias"),
        SeriesMetadata.strings("alias_type")
    );

    @Override
    public List<List<SeriesMetadata>> getMetadata() {
        return Collections.singletonList(METADATA);
    }

    private static class AliasSeries {
        private final StringSeries ids;
        private final StringSeries aliases;
        private final StringSeries aliasTypes;

        AliasSeries(UpdatingDataframe dataframe) {
            this.ids = dataframe.getStrings("id");
            this.aliases = dataframe.getStrings("alias");
            this.aliasTypes = dataframe.getStrings("alias_type");
        }

        void create(Network network, int row) {
            Identifiable identifiable = network.getIdentifiable(ids.get(row));
            if (identifiable == null) {
                throw new PowsyblException("identifiable " + ids.get(row) + " does not exist");
            }

            String alias = aliases.get(row);
            String type = null;
            if (aliasTypes != null) {
                type = aliasTypes.get(row);
            }
            if (StringUtils.isBlank(type)) {
                identifiable.addAlias(alias);
            } else {
                identifiable.addAlias(alias, type);
            }
        }

        void delete(Network network, int row) {
            Identifiable identifiable = network.getIdentifiable(ids.get(row));
            if (identifiable != null) {
                if (aliases != null) {
                    identifiable.removeAlias(aliases.get(row));
                } else {
                    throw new PowsyblException("missing alias id to remove");
                }
            } else {
                throw new PowsyblException("identifiable " + ids.get(row) + " not found");
            }
        }
    }

    @Override
    public void addElements(Network network, UpdatingDataframe dataframe) {
        AliasSeries series = new AliasSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.create(network, row);
        }
    }

    public static void deleteElements(Network network, UpdatingDataframe dataframe) {
        AliasSeries series = new AliasSeries(dataframe);
        for (int row = 0; row < dataframe.getRowCount(); row++) {
            series.delete(network, row);
        }
    }
}
