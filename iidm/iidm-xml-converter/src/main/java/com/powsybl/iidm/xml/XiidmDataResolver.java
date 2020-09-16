/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import com.powsybl.commons.datastore.AbstractDataResolver;
import com.powsybl.commons.datastore.DataEntry;
import com.powsybl.commons.datastore.DataFormat;
import com.powsybl.commons.datastore.DataPack;
import com.powsybl.commons.datastore.NonUniqueResultException;
import com.powsybl.commons.datastore.ReadOnlyDataStore;
import com.powsybl.commons.util.Filenames;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class XiidmDataResolver extends AbstractDataResolver {

    static final String SUFFIX_MAPPING = "_mapping";

    static final String MAPPING_ENTRY = "MAPPING_ENTRY";

    @Override
    public DataFormat getDataFormat() {
        return XiidmDataFormat.INSTANCE;
    }

    @Override
    public Optional<DataPack> resolve(ReadOnlyDataStore store, String mainFileName, Properties parameters)
            throws IOException, NonUniqueResultException {
        Optional<DataPack> pack = super.resolve(store, mainFileName, parameters);
        if (pack.isPresent()) {
            Optional<DataEntry> main = pack.get().getMainEntry();
            if (main.isPresent()) {
                String mapping = Filenames.getBasename(main.get().getName()) + SUFFIX_MAPPING + ".csv";
                Optional<String> mappingFile = store.getEntryNames().stream().filter(mapping::equals).findFirst();
                if (mappingFile.isPresent()) {
                    pack.get().addEntry(new DataEntry(mappingFile.get(), MAPPING_ENTRY));
                }
            }
        }
        return pack;
    }

}
