/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.rulesdb.fs;

import eu.itesla_project.modules.rules.SecurityRuleSerializerLoader;
import eu.itesla_project.modules.rules.SecurityRuleSerializerServiceLoader;

import java.io.IOException;
import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalFileSystemRulesDbClient extends FileSystemRulesDbClient {

    private static class RuleLocalFS implements RulesFS {

        private final Path dir;

        public RuleLocalFS(Path dir) {
            this.dir = dir;
        }

        @Override
        public Path getRoot() {
            return dir;
        }

        @Override
        public void close() {
        }
    }

    private final Path dir;

    public LocalFileSystemRulesDbClient(Path dir, SecurityRuleSerializerLoader loader) {
        super(loader);
        this.dir = dir;
    }

    public LocalFileSystemRulesDbClient(Path dir) {
        this(dir, new SecurityRuleSerializerServiceLoader());
    }

    protected RulesFS createRulesFS() throws IOException {
        return new RuleLocalFS(dir);
    }
    
}
