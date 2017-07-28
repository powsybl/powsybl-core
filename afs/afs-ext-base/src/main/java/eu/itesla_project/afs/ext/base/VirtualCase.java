/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.ext.base;

import eu.itesla_project.afs.core.AppFileSystem;
import eu.itesla_project.afs.core.FileIcon;
import eu.itesla_project.afs.storage.AppFileSystemStorage;
import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.iidm.network.Network;
import eu.itesla_project.iidm.xml.NetworkXml;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.*;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class VirtualCase extends ProjectCase {

    public static final String PSEUDO_CLASS = "virtualCase";

    private static final FileIcon VIRTUAL_CASE_ICON = new FileIcon("virtualCase", VirtualCase.class.getResourceAsStream("/icons/virtualCase16x16.png"));

    private static final String NETWORK_CACHE_KEY = "network";

    private static final String SCRIPT_OUTPUT = "scriptOutput";

    static final String CASE_DEPENDENCY_NAME = "case";
    static final String SCRIPT_DEPENDENCY_NAME = "script";

    public VirtualCase(NodeId id, AppFileSystemStorage storage, NodeId projectId, AppFileSystem fileSystem) {
        super(id, storage, projectId, fileSystem);
    }

    @Override
    public FileIcon getIcon() {
        return VIRTUAL_CASE_ICON;
    }

    public ProjectCase getCase() {
        return (ProjectCase) findProjectFile(storage.getDependency(id, CASE_DEPENDENCY_NAME));
    }

    public ModificationScript getScript() {
        return (ModificationScript) findProjectFile(storage.getDependency(id, SCRIPT_DEPENDENCY_NAME));
    }

    public Writer getScriptOutputWriter() {
        return storage.writeStringAttribute(id, SCRIPT_OUTPUT);
    }

    public Reader getScriptOutputReader() {
        return storage.readStringAttribute(id, SCRIPT_OUTPUT);
    }

    public Network loadFromCache() {
        InputStream is = storage.readFromCache(id, NETWORK_CACHE_KEY);
        if (is != null) {
            try {
                return NetworkXml.read(is);
            } finally {
                try {
                    is.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }
        return null;
    }

    public void saveToCache(Network network) {
        try (OutputStream os = storage.writeToCache(id, NETWORK_CACHE_KEY)) {
            NetworkXml.write(network, os);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.commit();
    }

    private void runGroovyScript(Network network, Reader reader, Writer out) throws IOException {
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
        storage.setStringAttribute(id, SCRIPT_OUTPUT, null);
        storage.invalidateCache(id, NETWORK_CACHE_KEY);
        storage.commit();
    }
}
