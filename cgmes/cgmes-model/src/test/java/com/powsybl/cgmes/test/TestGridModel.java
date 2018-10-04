package com.powsybl.cgmes.test;

/*
 * #%L
 * CGMES data model
 * %%
 * Copyright (C) 2017 - 2018 RTE (http://rte-france.com)
 * %%
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * #L%
 */

import java.nio.file.Path;

import com.powsybl.cgmes.CgmesModel;
import com.powsybl.commons.datasource.CompressionFormat;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TestGridModel {

    public TestGridModel(
            Path path,
            String basename,
            CgmesModel expected,
            boolean containsBays,
            boolean solved) {
        this(path, basename, null, expected, containsBays, solved);
    }

    public TestGridModel(
            Path path,
            String basename,
            CompressionFormat compressionExtension,
            CgmesModel expected,
            boolean containsBays,
            boolean solved) {
        this.id = path.getName(path.getNameCount() - 1).toString();
        this.path = path;
        this.basename = basename;
        this.compressionExtension = compressionExtension;
        this.expected = expected;
        this.containsBays = containsBays;
        this.solved = solved;
    }

    public String id() {
        return id;
    }

    public Path path() {
        return path;
    }

    public String basename() {
        return basename;
    }

    public CompressionFormat getCompressionExtension() {
        return compressionExtension;
    }

    public boolean containsBays() {
        return containsBays;
    }

    public boolean solved() {
        return solved;
    }

    public CgmesModel expected() {
        return expected;
    }

    private final String            id;
    private final Path              path;
    private final String            basename;
    private final CompressionFormat compressionExtension;
    private final CgmesModel        expected;
    private final boolean           containsBays;
    private final boolean           solved;
}
