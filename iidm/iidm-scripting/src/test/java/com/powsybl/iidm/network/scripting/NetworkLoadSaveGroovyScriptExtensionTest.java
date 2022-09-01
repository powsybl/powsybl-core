/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.ExportersLoaderList;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.xml.NetworkXml;
import com.powsybl.iidm.xml.XMLExporter;
import com.powsybl.iidm.xml.XMLImporter;
import com.powsybl.scripting.AbstractGroovyScriptTest;
import com.powsybl.scripting.groovy.GroovyScriptExtension;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NetworkLoadSaveGroovyScriptExtensionTest extends AbstractGroovyScriptTest {

    private FileSystem fileSystem;

    @Before
    public void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Network network = EurostagTutorialExample1Factory.create();
        NetworkXml.write(network, fileSystem.getPath("/work/n.xiidm"));
    }

    @After
    public void tearDown() throws Exception {
        fileSystem.close();
    }

    @Override
    protected String getCode() {
        return "n = loadNetwork('/work/n.xiidm')\n"
                + "print(n.id)\n"
                + "saveNetwork('XIIDM', n, '/work/n2.xiidm')";
    }

    @Override
    protected String getExpectedOutput() {
        return "sim1";
    }

    @Override
    protected List<GroovyScriptExtension> getExtensions() {
        return Collections.singletonList(new NetworkLoadSaveGroovyScriptExtension(new ImportConfig(),
                                                                                  new ImportersLoaderList(new XMLImporter()),
                                                                                  new ExportersLoaderList(new XMLExporter()),
                                                                                  fileSystem));
    }

    @Test
    public void test() {
        doTest();
        assertTrue(Files.exists(fileSystem.getPath("/work/n2.xiidm")));
    }
}
