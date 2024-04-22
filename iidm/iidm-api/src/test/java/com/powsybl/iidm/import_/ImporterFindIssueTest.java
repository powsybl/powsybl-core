/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.DataSourceUtil;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class ImporterFindIssueTest {

    static class FooImporter implements Importer {

        @Override
        public String getFormat() {
            return "foo";
        }

        @Override
        public String getComment() {
            return "";
        }

        @Override
        public boolean exists(ReadOnlyDataSource dataSource) {
            try {
                return dataSource.exists(null, "foo");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, ReportNode reportNode) {
            throw new UnsupportedOperationException();
        }
    }

    static class BarImporter implements Importer {

        @Override
        public String getFormat() {
            return "bar";
        }

        @Override
        public String getComment() {
            return "";
        }

        @Override
        public boolean exists(ReadOnlyDataSource dataSource) {
            try {
                return dataSource.exists(null, "bar");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        @Override
        public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, ReportNode reportNode) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    void test() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path workingDir = fileSystem.getPath("/work");
            Path fooFile = workingDir.resolve("test.foo");
            Path barFile = workingDir.resolve("test.bar");
            Files.createFile(fooFile);
            Files.createFile(barFile);

            var dataSource = DataSourceUtil.createDataSource(barFile.getParent(), barFile.getFileName().toString());
            ComputationManager computationManager = Mockito.mock(ComputationManager.class);
            var importer = Importer.find(dataSource, new ImportersLoaderList(new FooImporter(), new BarImporter()), computationManager, new ImportConfig());
            assertNotNull(importer);
            assertInstanceOf(BarImporter.class, importer);
        }
    }
}
