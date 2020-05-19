/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.google.common.io.Files;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public abstract class AbstractDataResolver implements DataResolver {

    @Override
    public Optional<DataPack> resolve(ReadOnlyDataStore store, String mainFileName, Properties parameters)
            throws IOException, NonUniqueResultException {
        Objects.requireNonNull(store);

        DataPack dp = null;
        if (mainFileName != null) {
            if (store.exists(mainFileName) && checkFileExtension(mainFileName)) {
                dp = buildDataPack(store, mainFileName);
            }
        } else {
            List<String> candidates = store.getEntryNames().stream().filter(a -> checkFileExtension(a)).collect(Collectors.toList());
            if (candidates.size() > 1) {
                throw new NonUniqueResultException();
            } else if (candidates.size() == 1) {
                String entryName = candidates.get(0);
                dp = buildDataPack(store, entryName);
            }
        }
        return Optional.ofNullable(dp);
    }

    @Override
    public boolean validate(DataPack pack, Properties properties) {
        Optional<DataEntry> main = pack.getMainEntry();
        return pack.getDataFormatId().equals(getDataFormatId()) && main.isPresent() && checkFileExtension(main.get().getName());
    }

    public boolean checkFileExtension(String filename) {
        return getExtensions().contains(Files.getFileExtension(filename));
    }

    private DataPack buildDataPack(ReadOnlyDataStore store, String mainFileName) {
        DataPack dp = new DataPack(store, getDataFormatId());
        DataEntry entry = new DataEntry(mainFileName, DataPack.MAIN_ENTRY_TAG);
        dp.addEntry(entry);
        return dp;
    }

    public abstract String getDataFormatId();

    public abstract List<String> getExtensions();

}
