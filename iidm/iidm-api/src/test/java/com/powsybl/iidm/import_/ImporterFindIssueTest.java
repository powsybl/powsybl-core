/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.import_;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.commons.reporter.Reporter;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImporterFindIssueTest {

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
        public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, Reporter reporter) {
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
        public Network importData(ReadOnlyDataSource dataSource, NetworkFactory networkFactory, Properties parameters, Reporter reporter) {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    public void test() throws IOException {
        try (FileSystem fileSystem = Jimfs.newFileSystem(Configuration.unix())) {
            Path workingDir = fileSystem.getPath("/work");
            Path fooFile = workingDir.resolve("test.foo");
            Path barFile = workingDir.resolve("test.bar");
            Files.createFile(fooFile);
            Files.createFile(barFile);

            var dataSource = Importers.createDataSource(barFile);
            ComputationManager computationManager = Mockito.mock(ComputationManager.class);
            var importer = Importer.find(dataSource, new ImportersLoaderList(new FooImporter(), new BarImporter()), computationManager, new ImportConfig());
            assertNotNull(importer);
            assertTrue(importer instanceof BarImporter);
        }
    }
}
