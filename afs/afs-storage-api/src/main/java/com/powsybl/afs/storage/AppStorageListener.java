/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface AppStorageListener {

    void nodeCreated(String id);

    void nodeRemoved(String id);

    void nodeDataUpdated(String id, String dataName);

    void dependencyAdded(String id, String dependencyName);

    void dependencyRemoved(String id, String dependencyName);

    void timeSeriesCreated(String id, String timeSeriesName);

    void timeSeriesDataUpdated(String id, String timeSeriesName);

    void timeSeriesRemoved(String id);
}
