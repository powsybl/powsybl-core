/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.json;

import com.powsybl.afs.storage.AppFileSystemStorage;
import com.powsybl.afs.storage.NodeId;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.math.timeseries.json.TimeSeriesJsonModule;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppStorageJsonModule extends TimeSeriesJsonModule {

    public AppStorageJsonModule(AppFileSystemStorage storage) {
        Objects.requireNonNull(storage);

        addSerializer(NodeId.class, new NodeIdJsonSerializer());
        addSerializer(NodeInfo.class, new NodeInfoJsonSerializer());

        addDeserializer(NodeId.class, new NodeIdJsonDeserializer(storage));
        addDeserializer(NodeInfo.class, new NodeInfoJsonDeserializer(storage));
    }
}
