/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.powsybl.commons.datastore.DataEntry;
import com.powsybl.commons.datastore.DataFormat;
import com.powsybl.commons.datastore.DataPack;
import com.powsybl.commons.datastore.DataResolver;
import com.powsybl.commons.datastore.NonUniqueResultException;
import com.powsybl.commons.datastore.ReadOnlyDataStore;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class TstDataFormat implements DataFormat {

    private final String format;
    private static final List<String> EXTENSIONS = ImmutableList.of("tst");

    public TstDataFormat(String format) {
        this.format = format;
    }

    @Override
    public String getId() {
        return format;
    }

    @Override
    public String getDescription() {
        return "Dummy data format";
    }

    @Override
    public DataResolver newDataResolver() {
        return new DataResolver() {

            @Override
            public Optional<DataPack> resolve(ReadOnlyDataStore store, String mainFileName, Properties parameters)
                    throws IOException, NonUniqueResultException {
                DataPack dp = null;
                if (checkFileExtension(mainFileName) && store.exists(mainFileName)) {
                    dp = new DataPack(store, getId());
                    DataEntry d = new DataEntry(mainFileName, DataPack.MAIN_ENTRY_TAG);
                    dp.addEntry(d);
                }
                return Optional.ofNullable(dp);
            }

            @Override
            public boolean validate(DataPack pack, Properties parameters) {
                return pack.getMainEntry().isPresent();
            }
        };
    }

    private static boolean checkFileExtension(String filename) {
        return EXTENSIONS.contains(Files.getFileExtension(filename));
    }

    @Override
    public List<String> getExtensions() {
        return EXTENSIONS;
    }

}
