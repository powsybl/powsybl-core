/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion;

import com.powsybl.commons.datasource.DataSource;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.network.Identifiable;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public interface NamingStrategy {

    String getGeographicalTag(String geo);

    String getIidmId(String type, String id);

    String getCgmesId(Identifiable<?> identifiable, String type, String id);

    String getName(String type, String name);

    void readIdMapping(Identifiable<?> identifiable, String type);

    void writeIdMapping(Path path);

    void writeIdMapping(String mappingFileName, DataSource ds);

    final class Identity implements NamingStrategy {

        @Override
        public String getGeographicalTag(String geo) {
            return geo;
        }

        @Override
        public String getIidmId(String type, String id) {
            return id;
        }

        @Override
        public String getCgmesId(Identifiable<?> identifiable, String type, String id) {
            return id;
        }

        @Override
        public String getName(String type, String name) {
            return name;
        }

        @Override
        public void readIdMapping(Identifiable<?> identifiable, String type) {
            // do nothing
        }

        @Override
        public void writeIdMapping(Path path) {
            // do nothing
        }

        @Override
        public void writeIdMapping(String mappingFileName, DataSource ds) {
            // do nothing
        }
    }

    static NamingStrategy create(ReadOnlyDataSource ds, String mappingFileName) {
        try {
            if (ds.exists(mappingFileName)) {
                try (InputStream is = ds.newInputStream(mappingFileName)) {
                    return new CgmesAliasNamingStrategy(is);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new Identity();
    }
}
