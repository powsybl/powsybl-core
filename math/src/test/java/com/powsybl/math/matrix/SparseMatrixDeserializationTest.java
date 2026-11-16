/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.math.matrix;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
class SparseMatrixDeserializationTest {
    private static FileSystem fileSystem;
    protected static Path testDir;

    @BeforeAll
    static void setUp() throws Exception {
        fileSystem = Jimfs.newFileSystem(Configuration.unix());
        testDir = fileSystem.getPath("/tmp");
        Files.createDirectories(testDir);
    }

    @AfterAll
    static void tearDown() throws Exception {
        fileSystem.close();
    }

    @Test
    void testSecureDeserialization() throws IOException {
        // Prepare exploit payload
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(new Exploit());
        }

        // Try to deserialize the false SparseMatrix object
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        assertThrows(UncheckedIOException.class, () -> SparseMatrix.read(bais), "Exploit may be present.");

        // Confirm there is no exploit: the "rce" file should not exist
        Path rceFile = testDir.resolve("rce");
        assertFalse(Files.exists(rceFile), "The exploit is present.");
    }

    static class Exploit implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;

        @Serial
        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            // Emulate a security problem: when reading the object, create a new file.
            // If this file is indeed created when deserializing the payload, then an attacker
            // may perform dangerous operations (download and install a malware, connect to an external server, ...)
            in.defaultReadObject();
            Path rceFile = testDir.resolve("rce");
            Files.writeString(rceFile, "Security problem", StandardCharsets.UTF_8);
        }
    }
}
