/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.entsoe.cases;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Sets;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import eu.itesla_project.cases.CaseType;
import eu.itesla_project.commons.datasource.DataSource;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.network.Country;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.entsoe.util.EntsoeGeographicalCode;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import static org.junit.Assert.assertTrue;

/**
 *
 * @author Quinary <itesla@quinary.com>
 */
public class EntsoeAndXmlCaseRepositoryTest {

    private FileSystem fileSystem;
    private EntsoeAndXmlCaseRepository caseRepository;
    private Network xmlNetwork;

    private final class DataSourceMock implements DataSource {
        private final Path directory;
        private final String baseName;

        private DataSourceMock(Path directory, String baseName) {
            this.directory = directory;
            this.baseName = baseName;
        }

        private Path getDirectory() {
            return directory;
        }

        @Override
        public boolean exists(String fileName) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getBaseName() {
            return baseName;
        }

        @Override
        public boolean exists(String suffix, String ext) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream newInputStream(String suffix, String ext) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream newInputStream(String fileName) throws IOException {
            throw new UnsupportedOperationException();
        }
    }

    private static void createFile(Path dir, String fileName) throws IOException {
        try (Writer writer = Files.newBufferedWriter(dir.resolve(fileName))) {
            writer.write("test");
        }
    }

    @Before
    public void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Path rootDir = fileSystem.getPath("/");

        Importer cimImporter = Mockito.mock(Importer.class);
        Mockito.when(cimImporter.exists(Matchers.isA(DataSource.class)))
                .thenAnswer(invocation -> {
                    DataSourceMock dataSource = invocation.getArgumentAt(0, DataSourceMock.class);
                    Path file = dataSource.getDirectory().resolve(dataSource.getBaseName() + ".zip");
                    return Files.isRegularFile(file);
                });
        Mockito.when(cimImporter.getFormat())
                .thenReturn("CIM1");
        Network cimNetwork = Mockito.mock(Network.class);
        Mockito.when(cimImporter.importData(Matchers.isA(DataSource.class), Matchers.any()))
                .thenReturn(cimNetwork);

        Importer uctImporter = Mockito.mock(Importer.class);
        Mockito.when(uctImporter.exists(Matchers.isA(DataSource.class)))
                .thenAnswer(invocation -> {
                    DataSourceMock dataSource = invocation.getArgumentAt(0, DataSourceMock.class);
                    Path file = dataSource.getDirectory().resolve(dataSource.getBaseName() + ".uct");
                    return Files.isRegularFile(file);
                });
        Mockito.when(uctImporter.getFormat())
                .thenReturn("UCTE");
        Network uctNetwork = Mockito.mock(Network.class);
        Mockito.when(uctImporter.importData(Matchers.isA(DataSource.class), Matchers.any()))
                .thenReturn(uctNetwork);

        Importer iidmImporter = Mockito.mock(Importer.class);
        Mockito.when(iidmImporter.exists(Matchers.isA(DataSource.class)))
                .thenAnswer(invocation -> {
                    DataSourceMock dataSource = invocation.getArgumentAt(0, DataSourceMock.class);
                    Path fileXiidm = dataSource.getDirectory().resolve(dataSource.getBaseName() + ".xiidm");
                    Path fileXml = dataSource.getDirectory().resolve(dataSource.getBaseName() + ".xml");
                    return Files.isRegularFile(fileXiidm) || Files.isRegularFile(fileXml);
                });
        Mockito.when(iidmImporter.getFormat())
                .thenReturn("XIIDM");
        xmlNetwork = Mockito.mock(Network.class);
        Mockito.when(iidmImporter.importData(Matchers.isA(DataSource.class), Matchers.any()))
                .thenReturn(xmlNetwork);


        caseRepository = new EntsoeAndXmlCaseRepository(new EntsoeAndXmlCaseRepositoryConfig(rootDir, HashMultimap.create()),
                Arrays.asList(
                        new EntsoeCaseRepository.EntsoeFormat(cimImporter, "CIM"),
                        new EntsoeCaseRepository.EntsoeFormat(uctImporter, "UCT"),
                        new EntsoeCaseRepository.EntsoeFormat(iidmImporter, "IIDM")),
                DataSourceMock::new);

        Path dir5 = fileSystem.getPath("/IIDM/SN/2013/01/14");
        Files.createDirectories(dir5);
        createFile(dir5, "20130114_0015_SN1_FR0.xml");
        Path dir6 = fileSystem.getPath("/IIDM/SN/2016/01/01");
        Files.createDirectories(dir6);
        createFile(dir6, "20160101_0015_SN5_FR0.xml");
        createFile(dir6, "20160101_0045_SN5_FR0.xml");

        Path dir7 = fileSystem.getPath("/IIDM/SN/2016/02/02");
        Files.createDirectories(dir7);
        createFile(dir7, "20160202_0115_SN2_FR0.xiidm");
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    public void testLoad() throws Exception {
        // check that, when cim and ucte is forbidden for france, xml is loaded
        caseRepository.getConfig().getForbiddenFormatsByGeographicalCode().put(EntsoeGeographicalCode.FR, "CIM1");
        caseRepository.getConfig().getForbiddenFormatsByGeographicalCode().put(EntsoeGeographicalCode.FR, "UCTE");
        assertTrue(caseRepository.load(DateTime.parse("2013-01-14T00:15:00+01:00"), CaseType.SN, Country.FR).equals(Collections.singletonList(xmlNetwork)));
    }

    @Test
    public void testDataAvailable() throws Exception {
        assertTrue(caseRepository.dataAvailable(CaseType.SN, EnumSet.of(Country.FR), Interval.parse("2016-01-01T00:00:00+01:00/2016-01-14T01:00:00+01:00"))
                .equals(Sets.newHashSet(DateTime.parse("2016-01-01T00:15:00+01:00"), DateTime.parse("2016-01-01T00:45:00+01:00"))));
    }

    @Test
    public void testLoadXiidm() throws Exception {
        // check that, when cim and ucte is forbidden for france, xml is loaded
        // file suffix .xiidm
        caseRepository.getConfig().getForbiddenFormatsByGeographicalCode().put(EntsoeGeographicalCode.FR, "CIM1");
        caseRepository.getConfig().getForbiddenFormatsByGeographicalCode().put(EntsoeGeographicalCode.FR, "UCTE");
        assertTrue(caseRepository.load(DateTime.parse("2016-02-02T01:15:00+01:00"), CaseType.SN, Country.FR).equals(Collections.singletonList(xmlNetwork)));
    }

}
