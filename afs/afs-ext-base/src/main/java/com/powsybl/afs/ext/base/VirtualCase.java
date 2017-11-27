/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.powsybl.afs.FileIcon;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.xml.NetworkXml;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VirtualCase extends ProjectFile implements ProjectCase {

    public static final String PSEUDO_CLASS = "virtualCase";

    private static final FileIcon VIRTUAL_CASE_ICON = new FileIcon("virtualCase", VirtualCase.class.getResourceAsStream("/icons/virtualCase16x16.png"));

    private static final String NETWORK_CACHE_KEY = "network";

    private static final String SCRIPT_OUTPUT = "scriptOutput";

    static final String CASE_DEPENDENCY_NAME = "case";
    static final String SCRIPT_DEPENDENCY_NAME = "script";

    public VirtualCase(ProjectFileCreationContext context) {
        super(context, VIRTUAL_CASE_ICON);
    }

    public ProjectCase getCase() {
        return (ProjectCase) fileSystem.findProjectFile(storage.getDependencyInfo(info.getId(), CASE_DEPENDENCY_NAME));
    }

    public ModificationScript getScript() {
        return (ModificationScript) fileSystem.findProjectFile(storage.getDependencyInfo(info.getId(), SCRIPT_DEPENDENCY_NAME));
    }

    public Writer getScriptOutputWriter() {
        return storage.writeStringAttribute(info.getId(), SCRIPT_OUTPUT);
    }

    public Reader getScriptOutputReader() {
        return storage.readStringAttribute(info.getId(), SCRIPT_OUTPUT);
    }

    public Network loadFromCache() {
        try (InputStream is = storage.readFromCache(info.getId(), NETWORK_CACHE_KEY)) {
            if (is != null) {
                return NetworkXml.read(is);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return null;
    }

    public void saveToCache(Network network) {
        try (OutputStream os = storage.writeToCache(info.getId(), NETWORK_CACHE_KEY)) {
            NetworkXml.write(network, os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.flush();
    }

    private void runGroovyScript(Network network, Reader reader, Writer out) {
        // put network in the binding so that it is accessible from the script
        Binding binding = new Binding();
        binding.setProperty("network", network);
        binding.setProperty("out", out);

        CompilerConfiguration conf = new CompilerConfiguration();
        GroovyShell shell = new GroovyShell(binding, conf);
        shell.evaluate(reader);
    }

    @Override
    public Network loadNetwork() {
        // load network from the cache
        Network network = loadFromCache();

        // if no network cached, recreate it
        if (network == null) {
            // load network
            network = getCase().loadNetwork();

            // load script
            ModificationScript script = getScript();

            try (Reader reader = new StringReader(script.read());
                 Writer out = getScriptOutputWriter()) {
                switch (script.getScriptType()) {
                    case GROOVY:
                        runGroovyScript(network, reader, out);
                        break;

                    default:
                        throw new AssertionError();
                }

            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            // store network in the cache
            saveToCache(network);
        }

        return network;
    }

    @Override
    public void onDependencyChanged() {
        storage.setStringAttribute(info.getId(), SCRIPT_OUTPUT, null);
        storage.invalidateCache(info.getId(), NETWORK_CACHE_KEY);
        storage.flush();
    }
}
