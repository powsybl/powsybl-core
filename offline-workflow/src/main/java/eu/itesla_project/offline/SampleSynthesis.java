/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.offline;

import java.io.Serializable;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class SampleSynthesis implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;

    private OfflineTaskEvent lastTaskEvent;

    public SampleSynthesis(int id, OfflineTaskEvent lastTaskEvent) {
        this.id = id;
        this.lastTaskEvent = lastTaskEvent;
    }

    public int getId() {
        return id;
    }

    public OfflineTaskEvent getLastTaskEvent() {
        return lastTaskEvent;
    }

    public void setLastTaskEvent(OfflineTaskEvent lastTaskEvent) {
        this.lastTaskEvent = lastTaskEvent;
    }

}
