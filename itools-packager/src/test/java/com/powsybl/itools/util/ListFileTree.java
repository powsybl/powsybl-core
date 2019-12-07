/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.itools.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertNotNull;

/**
 * @author François Nicot <francois.nicot@rte-france.com>
 */
public class ListFileTree {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListFileTree.class);

    protected ListFileTree() {
    }

    public static List<String> listFileDir(final Path resourcesPath, final Path fromPath) throws IOException {
        List<String> lst = new ArrayList<String>();

        assertNotNull(resourcesPath);
        assertNotNull(fromPath);

        AtomicLong totalSize = new AtomicLong(0L);
        AtomicLong totalDir = new AtomicLong(0);
        AtomicLong totalFiles = new AtomicLong(0);

        //System.out.println("\n List of '" + resourcesPath + "' directory :");

        Files.walkFileTree(fromPath, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) throws IOException {
                String fileName = resourcesPath.relativize(file).toString();
                lst.add(fileName);

                //final String nom = file.getFileName().toString();
                //final String rep = file.getParent().toString();
                //System.out.println(String.format(" -> %s (%db)", file.toAbsolutePath(), Files.readAllBytes(file).length));

                totalSize.addAndGet(Files.readAllBytes(file).length);
                totalFiles.getAndIncrement();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(final Path dir, final BasicFileAttributes attrs) throws IOException {
                String fileDir = resourcesPath.relativize(dir).toString();
                lst.add(fileDir);

                totalDir.getAndIncrement();
                return FileVisitResult.CONTINUE;
            }
        });

        //LOGGER.info("In the '{}' repertoire : ", fromPath.toAbsolutePath());
        //LOGGER.info(" - the number of subdirectories is : {}", totalDir.decrementAndGet());
        //LOGGER.info(" - the number of files is : {}", totalFiles);
        //LOGGER.info(" - the total size of this directory is : {} bytes, ie {} MB.", totalSize,
        //                                                totalSize.doubleValue() / (1024.0 * 1024.0));

        return lst;
    }
}
