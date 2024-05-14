/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.extensions;

import com.powsybl.commons.io.TreeDataFormat;
import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.ImportOptions;
import com.powsybl.iidm.serde.NetworkSerDe;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public abstract class AbstractCgmesExtensionTest extends AbstractSerDeTest {

    protected void allFormatsRoundTripTest(Network network, String xmlRefFile) throws IOException {
        roundTripXmlTest(network, this::jsonWriteAndRead, NetworkSerDe::write, NetworkSerDe::validateAndRead, xmlRefFile);
    }

    private Network jsonWriteAndRead(Network network, Path path) {
        var anonymizer = NetworkSerDe.write(network, new ExportOptions().setFormat(TreeDataFormat.JSON), path);
        try (InputStream is = Files.newInputStream(path)) {
            return NetworkSerDe.read(is, new ImportOptions().setFormat(TreeDataFormat.JSON), anonymizer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
