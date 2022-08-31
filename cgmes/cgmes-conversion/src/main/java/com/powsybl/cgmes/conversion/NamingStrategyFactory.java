/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.datasource.ReadOnlyDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Miora Vedelago <miora.ralambotiana at rte-france.com>
 */
public final class NamingStrategyFactory {

    public static final String IDENTITY = "identity";
    public static final String CGMES = "cgmes"; // This simple naming strategy will fix only ids for IIDM identifiables
    public static final String CGMES_FIX_ALL_INVALID_IDS = "cgmes-fix-all-invalid-ids";

    public static NamingStrategy create(String impl, ReadOnlyDataSource ds, String mappingFileName, Path defaultPath) {
        Objects.requireNonNull(ds);
        Objects.requireNonNull(mappingFileName);
        try {
            if (ds.exists(mappingFileName)) {
                return readFromDataSource(impl, ds, mappingFileName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (defaultPath != null) {
            return create(impl, defaultPath);
        }
        return new NamingStrategy.Identity();
    }

    public static NamingStrategy create(String impl, ReadOnlyDataSource ds, String mappingFileName) {
        Objects.requireNonNull(ds);
        Objects.requireNonNull(mappingFileName);
        try {
            if (ds.exists(mappingFileName)) {
                return readFromDataSource(impl, ds, mappingFileName);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new NamingStrategy.Identity();
    }

    public static NamingStrategy create(String impl, Path path) {
        Objects.requireNonNull(path);
        try (InputStream is = Files.newInputStream(path)) {
            return createWithMapping(impl).readFrom(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static NamingStrategy readFromDataSource(String impl, ReadOnlyDataSource ds, String mappingFileName) throws IOException {
        try (InputStream is = ds.newInputStream(mappingFileName)) {
            return createWithMapping(impl).readFrom(is);
        }
    }

    public static AbstractCgmesAliasNamingStrategy createWithMapping(String impl) {
        Objects.requireNonNull(impl);
        switch (impl) {
            case IDENTITY:
                throw new PowsyblException("Identity naming strategy not expected when using an ID mapping file");
            case CGMES:
                return new SimpleCgmesAliasNamingStrategy();
            case CGMES_FIX_ALL_INVALID_IDS:
                return new FixedCgmesAliasNamingStrategy();
            default:
                throw new PowsyblException("Unknown naming strategy: " + impl);
        }
    }

    public static NamingStrategy create(String impl) {
        Objects.requireNonNull(impl);
        switch (impl) {
            case IDENTITY:
                return new NamingStrategy.Identity();
            case CGMES:
                return new SimpleCgmesAliasNamingStrategy();
            case CGMES_FIX_ALL_INVALID_IDS:
                return new FixedCgmesAliasNamingStrategy();
            default:
                throw new PowsyblException("Unknown naming strategy: " + impl);
        }
    }

    private NamingStrategyFactory() {
    }
}
