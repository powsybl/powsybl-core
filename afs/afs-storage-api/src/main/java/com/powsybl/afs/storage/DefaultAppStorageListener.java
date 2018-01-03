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
public class DefaultAppStorageListener implements AppStorageListener {

    @Override
    public void nodeCreated(String id) {
        // empty default implementation
    }

    @Override
    public void nodeRemoved(String id) {
        // empty default implementation
    }

    @Override
    public void nodeDataUpdated(String id, String attributeName) {
        // empty default implementation
    }

    @Override
    public void dependencyAdded(String id, String dependencyName) {
        // empty default implementation
    }

    @Override
    public void timeSeriesCreated(String id, String timeSeriesName) {
        // empty default implementation
    }

    @Override
    public void timeSeriesDataUpdated(String id, String timeSeriesName) {
        // empty default implementation
    }

    @Override
    public void timeSeriesRemoved(String id) {
        // empty default implementation
    }
}
