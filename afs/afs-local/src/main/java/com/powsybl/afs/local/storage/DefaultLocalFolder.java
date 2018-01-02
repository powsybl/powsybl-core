/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultLocalFolder implements LocalFolder {

    private final Path dir;

    private final Path rootDir;

    private final String fileSystemName;

    public DefaultLocalFolder(Path dir, Path rootDir, String fileSystemName) {
        this.dir = Objects.requireNonNull(dir);
        this.rootDir = Objects.requireNonNull(rootDir);
        this.fileSystemName = Objects.requireNonNull(fileSystemName);
    }

    @Override
    public List<Path> getChildPaths() {
        List<Path> childPaths = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path childPath : stream) {
                childPaths.add(childPath);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return childPaths;
    }

    @Override
    public Optional<Path> getChildPath(String name) {
        return Optional.ofNullable(dir.resolve(name));
    }

    @Override
    public String getName() {
        return dir.equals(rootDir) ? fileSystemName : dir.getFileName().toString();
    }

    @Override
    public Optional<Path> getParentPath() {
        return dir.equals(rootDir) ? Optional.empty() : Optional.ofNullable(dir.getParent());
    }
}
