/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.ddb.eurostag;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class EurostagDDB {

    private final Map<String, Path> generators = new HashMap<>();

    EurostagDDB(List<Path> ddbDirs) throws IOException {
        for (Path ddbDir : ddbDirs) {
            if (!Files.exists(ddbDir) && !Files.isDirectory(ddbDir)) {
                throw new IllegalArgumentException(ddbDir + " must exist and be a dir");
            }
            Files.walkFileTree(ddbDir, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (Files.isRegularFile(file) && file.toString().endsWith(".tg")) {
                        String fileName = file.getFileName().toString();
                        generators.put(fileName.substring(0, fileName.length()-3), file);
                    }
                    return super.visitFile(file, attrs);
                }
            });
        }
    }

    Path findGenerator(String idDdb) throws IOException {
        return generators.get(idDdb);
    }

}
