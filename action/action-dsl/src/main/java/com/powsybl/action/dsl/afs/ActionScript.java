/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.action.dsl.afs;

import com.google.common.io.CharStreams;
import com.powsybl.action.dsl.ActionDb;
import com.powsybl.action.dsl.ActionDslLoader;
import com.powsybl.afs.FileIcon;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.ext.base.ScriptListener;
import com.powsybl.afs.ext.base.ScriptType;
import com.powsybl.afs.ext.base.StorableScript;
import com.powsybl.afs.storage.events.NodeDataUpdated;
import com.powsybl.afs.storage.events.NodeEvent;
import com.powsybl.afs.storage.events.NodeEventType;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ActionScript extends ProjectFile implements StorableScript, ContingenciesProvider {

    public static final String PSEUDO_CLASS = "actionScript";
    public static final int VERSION = 0;

    static final String SCRIPT_CONTENT = "scriptContent";

    private static final FileIcon IAL_ICON = new FileIcon("script", ActionScript.class.getResourceAsStream("/icons/ial16x16.png"));

    private final List<ScriptListener> listeners = new ArrayList<>();

    public ActionScript(ProjectFileCreationContext context) {
        super(context, VERSION, IAL_ICON);
        storage.addListener(this, eventList -> {
            for (NodeEvent event : eventList.getEvents()) {
                if (event.getType() == NodeEventType.NODE_DATA_UPDATED) {
                    NodeDataUpdated dataUpdated = (NodeDataUpdated) event;
                    if (dataUpdated.getId().equals(info.getId()) && SCRIPT_CONTENT.equals(dataUpdated.getDataName())) {
                        for (ScriptListener listener : listeners) {
                            listener.scriptUpdated();
                        }
                    }
                }
            }
        });
    }

    public ScriptType getScriptType() {
        return ScriptType.GROOVY;
    }

    @Override
    public String readScript() {
        try {
            return CharStreams.toString(new InputStreamReader(storage.readBinaryData(info.getId(), SCRIPT_CONTENT).orElseThrow(AssertionError::new), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void writeScript(String content) {
        try (Reader reader = new StringReader(content);
             Writer writer = new OutputStreamWriter(storage.writeBinaryData(info.getId(), SCRIPT_CONTENT), StandardCharsets.UTF_8)) {
            CharStreams.copy(reader, writer);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.updateModificationTime(info.getId());
        storage.flush();
        notifyDependencyListeners();
    }

    @Override
    public void addListener(ScriptListener listener) {
        Objects.requireNonNull(listener);
        listeners.add(listener);
    }

    @Override
    public void removeListener(ScriptListener listener) {
        Objects.requireNonNull(listener);
        listeners.remove(listener);
    }

    public ActionDb load(Network network) {
        Objects.requireNonNull(network);
        return new ActionDslLoader(readScript()).load(network);
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return new ArrayList<>(load(network).getContingencies());
    }
}
