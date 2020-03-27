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
    public DataResolver getDataResolver(Properties parameters) {
        return new DataResolver() {

            @Override
            public Optional<DataPackage> resolve(ReadOnlyDataStore store, String mainFileName)
                    throws IOException, NonUniqueResultException {
                DataPackage dp = null;
                if (store.exists(mainFileName)) {
                    dp = new DataPackage(store, getId());
                    DataEntry d = new DataEntry(mainFileName);
                    d.addTag(DataPackage.MAIN_ENTRY_TAG);
                    dp.addEntry(d);
                }
                return Optional.ofNullable(dp);
            }

            @Override
            public boolean validate(DataPackage pack) {
                return pack.getMainEntry().isPresent();
            }
        };
    }

}
