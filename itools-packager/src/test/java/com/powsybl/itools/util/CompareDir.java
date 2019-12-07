/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools.util;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */
public class CompareDir {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareDir.class);

    protected CompareDir() {
    }

    public static boolean compareTwoVirtualDirectories(Path resources, Path unzipPath, String packageNameNotNull) throws IOException {
        boolean bool = false;

        Path resourcesPath = resources;
        Path powsyblPath = resources.resolve(packageNameNotNull);

        Path uncompressedPath = unzipPath;
        Path uncompressedPowsyblPath = unzipPath.resolve(packageNameNotNull);

        //LOGGER.info("Compare two virtual directories between '{}' and '{}'", powsyblPath, uncompressedPowsyblPath);

        if (!Files.isDirectory(powsyblPath) || !Files.isDirectory(uncompressedPowsyblPath)) {
            throw new IOException("Can't compare two directories ! ");
        }

        List<String> powsyblList = ListFileTree.listFileDir(resourcesPath, powsyblPath);
        List<String> uncompressedList = ListFileTree.listFileDir(uncompressedPath, uncompressedPowsyblPath);

        if (powsyblList != null && uncompressedList != null) {
            if (powsyblList.equals(uncompressedList)) {
                for (String file : powsyblList) {
                    Path path = resources.resolve(file);
                    if (!Files.isDirectory(path)) {
                        String uncompressedMd5;
                        Path path1 = unzipPath.resolve(file);
                        try (InputStream is = Files.newInputStream(path1)) {
                            uncompressedMd5 = DigestUtils.md5Hex(is);
                        }

                        String powsyblMd5;
                        Path path2 = resources.resolve(file);
                        try (InputStream is2 = Files.newInputStream(path2)) {
                            powsyblMd5 = DigestUtils.md5Hex(is2);
                        }

                        assertEquals("The binary files are different ! ", powsyblMd5, uncompressedMd5);
                        bool = true;
                    }
                }
            } else { // Differences case
                if (powsyblList.size() == uncompressedList.size()) {
                    Assert.assertTrue("Failure : The binary files in directory are different ! ", false);
                } else if ((powsyblList.size() == 1) || (uncompressedList.size() == 1)) {
                    Assert.assertTrue("Failure : One directory is empty ! ", false);
                } else {
                    Assert.assertTrue("Failure : The number of files in directory are different " + "(" + powsyblList.size() +
                                      " vs " + uncompressedList.size() + ") ! ", false);
                }
            }
        } else {
            Assert.assertTrue("Failure : Two directories are empty ! ", false);
        }
        return bool;
    }
}
