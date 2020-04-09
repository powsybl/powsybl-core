/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class DummyDataFormat implements DataFormat {

    @Override
    public String getId() {
        return "DUMMY";
    }

    @Override
    public String getDescription() {
        return "Dummy data format";
    }

    @Override
    public DataResolver getDataResolver() {
        return new DataResolver() {

            @Override
            public Optional<DataPack> resolve(ReadOnlyDataStore store, String mainFileName, Properties parameters)
                    throws IOException, NonUniqueResultException {
                DataPack dp = null;
                if (store.exists(mainFileName)) {
                    dp = new DataPack(store, getId());
                    DataEntry d = new DataEntry(mainFileName, DataPack.MAIN_ENTRY_TAG);
                    dp.addEntry(d);
                }
                return Optional.ofNullable(dp);
            }

            @Override
            public boolean validate(DataPack pack) {
                return pack.getMainEntry().isPresent();
            }
        };
    }

}
