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

    public static NamingStrategy create(ReadOnlyDataSource ds, String mappingFileName, Path defaultPath) {
        Objects.requireNonNull(ds);
        Objects.requireNonNull(mappingFileName);
        try {
            if (ds.exists(mappingFileName)) {
                try (InputStream is = ds.newInputStream(mappingFileName)) {
                    return new CgmesAliasNamingStrategy(is);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (defaultPath != null) {
            return create(defaultPath);
        }
        return new NamingStrategy.Identity();
    }

    public static NamingStrategy create(ReadOnlyDataSource ds, String mappingFileName) {
        Objects.requireNonNull(ds);
        Objects.requireNonNull(mappingFileName);
        try {
            if (ds.exists(mappingFileName)) {
                try (InputStream is = ds.newInputStream(mappingFileName)) {
                    return new CgmesAliasNamingStrategy(is);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return new NamingStrategy.Identity();
    }

    public static NamingStrategy create(Path path) {
        Objects.requireNonNull(path);
        try (InputStream is = Files.newInputStream(path)) {
            return new CgmesAliasNamingStrategy(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static NamingStrategy create(String impl) {
        Objects.requireNonNull(impl);
        if ("identity".equals(impl)) {
            return new NamingStrategy.Identity();
        } else if ("cgmes".equals(impl)) {
            return new CgmesAliasNamingStrategy();
        }
        throw new PowsyblException("Unknown naming strategy: " + impl);
    }

    private NamingStrategyFactory() {
    }
}
