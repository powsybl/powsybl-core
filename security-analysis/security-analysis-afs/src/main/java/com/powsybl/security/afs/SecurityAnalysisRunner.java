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
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.json.JsonSecurityAnalysisParameters;
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

    static final String PSEUDO_CLASS = "securityAnalysisRunner";

    static final int VERSION = 0;
    static final String CASE_DEPENDENCY_NAME = "case";
    static final String CONTINGENCY_PROVIDER_DEPENDENCY_NAME = "contingencyListProvider";

    private static final String PARAMETERS_JSON_NAME = "parametersJson";
    private static final String RESULT_JSON_NAME = "resultJson";

    private static final FileIcon LOCK_ICON = new FileIcon("lock", SecurityAnalysisRunner.class.getResourceAsStream("/icons/lock16x16.png"));

    private final DependencyCache<ProjectCase> caseDependency = new DependencyCache<>(this, CASE_DEPENDENCY_NAME, ProjectCase.class);

    private final DependencyCache<ContingenciesProvider> contingencyListProviderDependency = new DependencyCache<>(this, CONTINGENCY_PROVIDER_DEPENDENCY_NAME, ContingenciesProvider.class);

    public SecurityAnalysisRunner(ProjectFileCreationContext context) {
        super(context, VERSION, LOCK_ICON);
    }

    public Optional<ProjectCase> getCase() {
        return caseDependency.getFirst();
    }

    public Optional<ContingenciesProvider> getContingencyListProvider() {
        return contingencyListProviderDependency.getFirst();
    }

    public void run() {
        findService(SecurityAnalysisRunningService.class).run(this);
    }

    public SecurityAnalysisParameters readParameters() {
        try (InputStream is = storage.readBinaryData(info.getId(), PARAMETERS_JSON_NAME)
                .orElseThrow(AssertionError::new)) {
            return JsonSecurityAnalysisParameters.read(is);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void writeParameters(AppStorage storage, NodeInfo info, SecurityAnalysisParameters parameters) {
        Objects.requireNonNull(parameters);
        try (OutputStream os = storage.writeBinaryData(info.getId(), PARAMETERS_JSON_NAME)) {
            JsonSecurityAnalysisParameters.write(parameters, os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.flush();
    }

    public void writeParameters(SecurityAnalysisParameters parameters) {
        writeParameters(storage, info, parameters);
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
        try (Writer writer = new OutputStreamWriter(storage.writeBinaryData(info.getId(), RESULT_JSON_NAME), StandardCharsets.UTF_8)) {
            SecurityAnalysisResultSerializer.write(result, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.flush();
    }
}
