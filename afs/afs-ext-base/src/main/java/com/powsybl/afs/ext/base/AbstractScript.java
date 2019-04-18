/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.common.io.CharStreams;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.afs.storage.events.AppStorageListener;
import com.powsybl.afs.storage.events.NodeDataUpdated;
import com.powsybl.afs.storage.events.NodeEvent;
import com.powsybl.afs.storage.events.NodeEventType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class AbstractScript extends ProjectFile implements ReadOnlyScript {

    protected final String scriptContentName;

    private final List<ScriptListener> listeners = new ArrayList<>();

    private final AppStorageListener l = eventList -> processEvents(eventList.getEvents(), info.getId(), listeners);

    public AbstractScript(ProjectFileCreationContext context, int codeVersion, String scriptContentName) {
        super(context, codeVersion);
        this.scriptContentName = Objects.requireNonNull(scriptContentName);
        storage.addListener(l);
    }

    private void processEvents(List<NodeEvent> events, String nodeId, List<ScriptListener> listeners) {
        for (NodeEvent event : events) {
            if (event.getType() == NodeEventType.NODE_DATA_UPDATED) {
                NodeDataUpdated dataUpdated = (NodeDataUpdated) event;
                if (dataUpdated.getId().equals(nodeId) && scriptContentName.equals(dataUpdated.getDataName())) {
                    for (ScriptListener listener : listeners) {
                        listener.scriptUpdated();
                    }
                }
            }
        }
    }

    @Override
    public String readScript() {
        try {
            return CharStreams.toString(new InputStreamReader(storage.readBinaryData(info.getId(), scriptContentName).orElseThrow(AssertionError::new), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
}
