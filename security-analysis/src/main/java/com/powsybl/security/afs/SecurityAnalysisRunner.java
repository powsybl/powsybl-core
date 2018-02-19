/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.powsybl.afs.DependencyCache;
import com.powsybl.afs.FileIcon;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.ext.base.ProjectCase;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import com.powsybl.security.json.SecurityAnalysisResultSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisRunner extends ProjectFile {

    public static final String PSEUDO_CLASS = "securityAnalysisConfig";
    static final int VERSION = 0;
    static final String RESULT_JSON_NAME = "resultJson";
    static final String CASE_DEPENDENCY_NAME = "case";

    private static final FileIcon LOCK_ICON = new FileIcon("lock", SecurityAnalysisRunner.class.getResourceAsStream("/icons/lock16x16.png"));

    private final DependencyCache<ProjectCase> caseDependency = new DependencyCache<>(this, CASE_DEPENDENCY_NAME, ProjectCase.class);

    public SecurityAnalysisRunner(ProjectFileCreationContext context) {
        super(context, VERSION, LOCK_ICON);
    }

    public Optional<ProjectCase> getCase() {
        return caseDependency.getFirst();
    }

    public void run() {
        findService(SecurityAnalysisRunningService.class).run(this);
    }

    public SecurityAnalysisResult readResult() {
        try (InputStream is = storage.readBinaryData(info.getId(), RESULT_JSON_NAME).orElse(null)) {
            if (is != null) {
                return SecurityAnalysisResultDeserializer.read(is);
            }
            return null;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void writeResult(SecurityAnalysisResult result) {
        Objects.requireNonNull(result);
        try (Writer writer = new OutputStreamWriter(storage.writeBinaryData(info.getId(), SecurityAnalysisRunner.RESULT_JSON_NAME), StandardCharsets.UTF_8)) {
            SecurityAnalysisResultSerializer.write(result, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.flush();
    }
}
