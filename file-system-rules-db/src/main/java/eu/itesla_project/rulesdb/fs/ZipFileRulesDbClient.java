/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.rulesdb.fs;

import com.google.common.collect.ImmutableMap;
import eu.itesla_project.modules.rules.SecurityRuleSerializerLoader;
import eu.itesla_project.modules.rules.SecurityRuleSerializerServiceLoader;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Map;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ZipFileRulesDbClient extends FileSystemRulesDbClient {

    private static class RuleZipFS implements RulesFS {

        private static final Map<String, ?> ZIP_FS_ENV = ImmutableMap.of("create", "true");

        private FileSystem zipFs;

        public RuleZipFS(Path zipFile) throws IOException {
            this.zipFs = FileSystems.newFileSystem(URI.create("jar:file:" + zipFile.toAbsolutePath().toString()), ZIP_FS_ENV);;
        }

        @Override
        public Path getRoot() {
            return zipFs.getPath("/");
        }

        @Override
        public void close() throws IOException {
            zipFs.close();
        }
    }

    private final Path zipFile;

    public ZipFileRulesDbClient(Path zipFile, SecurityRuleSerializerLoader loader) {
        super(loader);
        this.zipFile = zipFile;
    }

    public ZipFileRulesDbClient(Path zipFile) {
        this(zipFile, new SecurityRuleSerializerServiceLoader());
    }

    protected RulesFS createRulesFS() throws IOException {
        return new RuleZipFS(zipFile);
    }

}
