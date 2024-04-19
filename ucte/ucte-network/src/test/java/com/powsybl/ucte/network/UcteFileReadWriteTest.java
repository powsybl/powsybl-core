/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.ucte.network;

import com.powsybl.commons.test.AbstractSerDeTest;
import com.powsybl.commons.report.ReportNode;
import com.powsybl.ucte.network.io.UcteReader;
import com.powsybl.ucte.network.io.UcteWriter;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Christian Biasuzzi {@literal <christian.biasuzzi@techrain.it>}
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
class UcteFileReadWriteTest extends AbstractSerDeTest {

    private static final String REFERENCE = "/20170322_1844_SN3_FR2.uct";

    private static UcteNetwork create() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(UcteFileReadWriteTest.class.getResourceAsStream(REFERENCE)))) {
            return new UcteReader().read(br, ReportNode.NO_OP);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static void write(UcteNetwork network, Path file) {
        try (BufferedWriter bw = Files.newBufferedWriter(file)) {
            new UcteWriter(network).write(bw);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static UcteNetwork read(Path file) {
        try (BufferedReader br = Files.newBufferedReader(file)) {
            return new UcteReader().read(br, ReportNode.NO_OP);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void roundTripTest() throws IOException {
        roundTripTest(create(), UcteFileReadWriteTest::write, UcteFileReadWriteTest::read, REFERENCE);
    }

}
