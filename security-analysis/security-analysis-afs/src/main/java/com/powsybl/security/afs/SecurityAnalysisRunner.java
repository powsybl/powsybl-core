/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.powsybl.afs.AfsException;
import com.powsybl.afs.DependencyCache;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.ext.base.ProjectCase;
import com.powsybl.afs.storage.AppStorage;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.afs.ContingencyStore;
import com.powsybl.security.SecurityAnalysisParameters;
import com.powsybl.security.SecurityAnalysisResult;
import com.powsybl.security.json.JsonSecurityAnalysisParameters;
import com.powsybl.security.json.SecurityAnalysisResultDeserializer;
import com.powsybl.security.json.SecurityAnalysisResultSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SecurityAnalysisRunner extends ProjectFile {

    static final String PSEUDO_CLASS = "securityAnalysisRunner";

    static final int VERSION = 0;
    static final String CASE_DEPENDENCY_NAME = "case";
    static final String CONTINGENCY_STORE_DEPENDENCY_NAME = "contingencyListProvider"; // keep old key for backward compat

    private static final String PARAMETERS_JSON_NAME = "parametersJson";
    private static final String RESULT_JSON_NAME = "resultJson";

    private final DependencyCache<ProjectFile> caseDependency = new DependencyCache<>(this, CASE_DEPENDENCY_NAME, ProjectFile.class);

    private final DependencyCache<ContingencyStore> contingencyStoreDependency = new DependencyCache<>(this, CONTINGENCY_STORE_DEPENDENCY_NAME, ContingencyStore.class);

    public SecurityAnalysisRunner(ProjectFileCreationContext context) {
        super(context, VERSION);
    }

    public Optional<ProjectFile> getCase() {
        return caseDependency.getFirst();
    }

    public void setCase(ProjectFile aCase) {
        Objects.requireNonNull(aCase);
        if (!(aCase instanceof ProjectCase)) {
            throw new AfsException("Bad project case " + aCase.getClass());
        }
        setDependencies(CASE_DEPENDENCY_NAME, Collections.singletonList(aCase));
        caseDependency.invalidate();
    }

    public Optional<ContingencyStore> getContingencyStore() {
        return contingencyStoreDependency.getFirst();
    }

    public void setContingencyStore(ProjectFile contingencyStore) {
        if (contingencyStore == null) {
            removeDependencies(CONTINGENCY_STORE_DEPENDENCY_NAME);
        } else {
            if (!(contingencyStore instanceof ContingenciesProvider)) {
                throw new AfsException("Bad contingency store " + contingencyStore.getClass());
            }
            setDependencies(CONTINGENCY_STORE_DEPENDENCY_NAME, Collections.singletonList(contingencyStore));
        }
        contingencyStoreDependency.invalidate();
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

    public boolean hasResult() {
        return storage.dataExists(info.getId(), RESULT_JSON_NAME);
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
