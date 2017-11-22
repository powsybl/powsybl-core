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
    public void nodeCreated(NodeId id) {
        // empty default implementation
    }

    @Override
    public void nodeRemoved(NodeId id) {
        // empty default implementation
    }

    @Override
    public void attributeUpdated(NodeId id, String attributeName) {
        // empty default implementation
    }

    @Override
    public void dependencyAdded(NodeId id, String dependencyName) {
        // empty default implementation
    }

    @Override
    public void timeSeriesCreated(NodeId id, String timeSeriesName) {
        // empty default implementation
    }

    @Override
    public void timeSeriesDataUpdated(NodeId id, String timeSeriesName) {
        // empty default implementation
    }

    @Override
    public void timeSeriesRemoved(NodeId id) {
        // empty default implementation
    }
}
