/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.import_;

import eu.itesla_project.computation.script.GroovyExtension;
import eu.itesla_project.computation.script.GroovyScriptAbstractTest;
import eu.itesla_project.iidm.datasource.ReadOnlyDataSource;
import eu.itesla_project.iidm.network.Network;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.nio.file.ShrinkWrapFileSystems;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

import java.io.Reader;
import java.io.StringReader;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ImportGroovyExtensionTest extends GroovyScriptAbstractTest {

    private FileSystem fileSystem;

    private Network fooNetwork;

    private Importer fooImporter;

    @Before
    public void setUp() throws Exception {
        JavaArchive archive = ShrinkWrap.create(JavaArchive.class);
        fileSystem = ShrinkWrapFileSystems.newFileSystem(archive);

        // create /n.foo
        Files.newBufferedWriter(fileSystem.getPath("/n.foo")).close();

        // create foo network
        fooNetwork = Mockito.mock(Network.class);
        Mockito.when(fooNetwork.getId())
                .thenReturn("test");

        // create importer for *.foo network
        fooImporter = Mockito.mock(Importer.class);
        Mockito.when(fooImporter.exists(Mockito.any(ReadOnlyDataSource.class)))
                .thenReturn(true);
        Mockito.when(fooImporter.import_(Mockito.any(ReadOnlyDataSource.class), Mockito.any()))
                .thenReturn(fooNetwork);
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Override
    protected Reader getCodeReader() {
        return new StringReader("n = loadNetwork('/n.foo')\n" +
                                "print n.id");
    }

    @Override
    protected String getExpectedOutput() {
        return "test";
    }

    @Override
    protected List<GroovyExtension> getExtensions() {
        return Collections.singletonList(new ImportGroovyExtension(fileSystem, new ImportersLoaderTest(fooImporter)));
    }
}
