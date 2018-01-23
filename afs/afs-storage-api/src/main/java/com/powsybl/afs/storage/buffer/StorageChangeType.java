/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.buffer;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public enum StorageChangeType {
    TIME_SERIES_CREATION,
    DOUBLE_TIME_SERIES_CHUNKS_ADDITION,
    STRING_TIME_SERIES_CHUNKS_ADDITION
}
