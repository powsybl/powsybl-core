/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.events;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum NodeEventType {
    NODE_CREATED,
    NODE_REMOVED,
    NODE_DESCRIPTION_UPDATED,
    NODE_NAME_UPDATED,
    NODE_DATA_UPDATED,
    NODE_DATA_REMOVED,
    PARENT_CHANGED,
    DEPENDENCY_ADDED,
    DEPENDENCY_REMOVED,
    BACKWARD_DEPENDENCY_ADDED,
    BACKWARD_DEPENDENCY_REMOVED,
    TIME_SERIES_CREATED,
    TIME_SERIES_DATA_UPDATED,
    TIME_SERIES_CLEARED
}
