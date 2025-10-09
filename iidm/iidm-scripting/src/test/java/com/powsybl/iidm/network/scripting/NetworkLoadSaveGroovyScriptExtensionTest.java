/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.scripting;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import com.powsybl.iidm.network.ExportersLoaderList;
import com.powsybl.iidm.network.ImportConfig;
import com.powsybl.iidm.network.ImportersLoaderList;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import com.powsybl.iidm.serde.NetworkSerDe;
import com.powsybl.iidm.serde.XMLExporter;
import com.powsybl.iidm.serde.XMLImporter;
import com.powsybl.scripting.groovy.GroovyScriptExtension;
import com.powsybl.scripting.test.AbstractGroovyScriptTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
class NetworkLoadSaveGroovyScriptExtensionTest extends AbstractGroovyScriptTest {

    private FileSystem fileSystem;

    @BeforeEach
    void setUp() {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        Network network = EurostagTutorialExample1Factory.create();
        NetworkSerDe.write(network, fileSystem.getPath("/work/n.xiidm"));
    }

    @AfterEach
    void tearDown() throws Exception {
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
    void test() {
        doTest();
        assertTrue(Files.exists(fileSystem.getPath("/work/n2.xiidm")));
    }
}
