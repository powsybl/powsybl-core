/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.google.common.io.Files;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.datastore.DataEntry;
import com.powsybl.commons.datastore.DataPack;
import com.powsybl.commons.datastore.DataResolver;
import com.powsybl.commons.datastore.NonUniqueResultException;
import com.powsybl.commons.datastore.ReadOnlyDataStore;
import com.powsybl.iidm.ConversionParameters;
import com.powsybl.iidm.parameters.Parameter;
import com.powsybl.iidm.parameters.ParameterDefaultValueConfig;
import com.powsybl.iidm.parameters.ParameterType;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class XmlDataResolver implements DataResolver {

    private static final String[] EXTENSIONS = {"xiidm", "iidm", "xml", "iidm.xml"};

    private static final String DATA_FORMAT_ID = "XIIDM";

    public XmlDataResolver() {
        this(PlatformConfig.defaultConfig());
    }

    public XmlDataResolver(PlatformConfig platformConfig) {
        defaultValueConfig = new ParameterDefaultValueConfig(platformConfig);
    }

    public static final String THROW_EXCEPTION_IF_EXTENSION_NOT_FOUND = "iidm.import.xml.throw-exception-if-extension-not-found";

    public static final String IMPORT_MODE = "iidm.import.xml.import-mode";

    public static final String EXTENSIONS_LIST = "iidm.import.xml.extensions";

    private static final Parameter EXTENSIONS_LIST_PARAMETER
            = new Parameter(EXTENSIONS_LIST, ParameterType.STRING_LIST, "The list of extension files ", null);

    private final ParameterDefaultValueConfig defaultValueConfig;

    @Override
    public Optional<DataPack> resolve(ReadOnlyDataStore store, @Nullable String mainFileName, @Nullable Properties parameters) throws IOException, NonUniqueResultException {
        Objects.requireNonNull(store);
        HashSet<String> extensions = ConversionParameters.readStringListParameter(DATA_FORMAT_ID, parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig) != null ? new HashSet<>(ConversionParameters.readStringListParameter(DATA_FORMAT_ID, parameters, EXTENSIONS_LIST_PARAMETER, defaultValueConfig)) : null;

        DataPack dp = null;
        if (mainFileName != null) {
            if (store.exists(mainFileName) && checkFileExtension(mainFileName)) {
                dp = buildDataPack(store, mainFileName, extensions);
            }
        } else {
            List<String> candidates = store.getEntryNames().stream().filter(XmlDataResolver::checkFileExtension).collect(Collectors.toList());
            if (candidates.size() > 1) {
                throw new NonUniqueResultException();
            } else if (candidates.size() == 1) {
                String entryName = candidates.get(0);
                dp = buildDataPack(store, entryName, extensions);
            }
        }
        return Optional.ofNullable(dp);
    }

    @Override
    public boolean validate(DataPack pack, Properties properties) {
        Optional<DataEntry> main = pack.getMainEntry();
        return main.isPresent() && checkFileExtension(main.get().getName());
    }

    public static boolean checkFileExtension(String filename) {
        return Arrays.asList(EXTENSIONS).contains(Files.getFileExtension(filename));
    }

    private DataPack buildDataPack(ReadOnlyDataStore store, String mainFileName, HashSet<String> extensions) throws IOException {
        DataPack dp = new DataPack(store, DATA_FORMAT_ID);
        DataEntry entry = new DataEntry(mainFileName, DataPack.MAIN_ENTRY_TAG);
        dp.addEntry(entry);
        return dp;
    }

}
