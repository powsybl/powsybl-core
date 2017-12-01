/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.Importer;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.NetworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalNetworkService implements NetworkService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalNetworkService.class);

    private final class ModifiedNetwork {

        private final Network network;

        private final ScriptError scriptError;

        private final String scriptOutput;

        private ModifiedNetwork(Network network, ScriptError scriptError, String scriptOutput) {
            this.network = Objects.requireNonNull(network);
            this.scriptError = scriptError;
            this.scriptOutput = Objects.requireNonNull(scriptOutput);
        }

        private Network getNetwork() {
            return network;
        }

        private ScriptError getScriptError() {
            return scriptError;
        }

        private String getScriptOutput() {
            return scriptOutput;
        }
    }

    private final Cache<NodeId, ModifiedNetwork> cache;

    public LocalNetworkService() {
        cache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .removalListener(notification -> LOGGER.info("Network associated to project file {} removed ({})", notification.getKey(), notification.getCause()))
                .build();
    }

    private ModifiedNetwork loadNetwork(ImportedCase importedCase) {
        try {
            return cache.get(importedCase.getId(), () -> {
                Importer importer = importedCase.getImporter();
                ReadOnlyDataSource dataSource = importedCase.getDataSource();
                Properties parameters = importedCase.getParameters();
                Network network = importer.importData(dataSource, parameters);
                return new ModifiedNetwork(network, null, "");
            });

        } catch (ExecutionException e) {
            throw new UncheckedExecutionException(e);
        }
    }

    private ModifiedNetwork applyScript(Network network, String previousScriptOutput, ModificationScript script) {
        ScriptError scriptError;
        String scriptOutput;
        try (StringWriter scriptOutputWriter = new StringWriter()) {
            scriptError = ScriptUtils.runScript(network, script.getScriptType(), script.readScript(), scriptOutputWriter);
            scriptOutputWriter.flush();
            scriptOutput = scriptOutputWriter.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        if (scriptError == null) {
            return new ModifiedNetwork(network, null, previousScriptOutput + scriptOutput);
        } else {
            // return an empty network
            return new ModifiedNetwork(NetworkFactory.create("error", ""), scriptError, scriptOutput);
        }
    }

    private ModifiedNetwork loadNetwork(VirtualCase virtualCase) {
        try {
            return cache.get(virtualCase.getId(), () -> {
                ProjectCase baseCase = virtualCase.getCase();

                // getNetwork network
                Network network = baseCase.getNetwork();

                if (baseCase.getScriptError() != null) {
                    return new ModifiedNetwork(network, baseCase.getScriptError(), baseCase.getScriptOutput());
                }

                // getNetwork script
                ModificationScript script = virtualCase.getScript();

                // apply script
                return applyScript(network, baseCase.getScriptOutput(), script);
            });
        } catch (ExecutionException e) {
            throw new UncheckedExecutionException(e);
        }
    }

    private <T extends ProjectFile & ProjectCase> ModifiedNetwork loadNetwork(T projectCase) {
        Objects.requireNonNull(projectCase);
        if (projectCase instanceof ImportedCase) {
            return loadNetwork((ImportedCase) projectCase);
        } else if (projectCase instanceof VirtualCase) {
            return loadNetwork((VirtualCase) projectCase);
        } else {
            throw new AssertionError("ProjectCase implementation " + projectCase.getClass().getName() + " not supported");
        }
    }

    @Override
    public <T extends ProjectFile & ProjectCase> Network getNetwork(T projectCase) {
        return loadNetwork(projectCase).getNetwork();
    }

    @Override
    public <T extends ProjectFile & ProjectCase> ScriptError getScriptError(T projectCase) {
        return loadNetwork(projectCase).getScriptError();
    }

    @Override
    public <T extends ProjectFile & ProjectCase> String getScriptOutput(T projectCase) {
        return loadNetwork(projectCase).getScriptOutput();
    }

    @Override
    public <T extends ProjectFile & ProjectCase> void invalidateCache(T projectCase) {
        Objects.requireNonNull(projectCase);
        cache.invalidate(projectCase.getId());
    }
}

