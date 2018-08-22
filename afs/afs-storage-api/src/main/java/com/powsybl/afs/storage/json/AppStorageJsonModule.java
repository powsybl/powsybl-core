/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.storage.json;

import com.powsybl.afs.storage.NodeDependency;
import com.powsybl.afs.storage.NodeGenericMetadata;
import com.powsybl.afs.storage.NodeInfo;
import com.powsybl.timeseries.json.TimeSeriesJsonModule;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class AppStorageJsonModule extends TimeSeriesJsonModule {

    public AppStorageJsonModule() {
        addSerializer(NodeGenericMetadata.class, new NodeGenericMetadataJsonSerializer());
        addSerializer(NodeInfo.class, new NodeInfoJsonSerializer());
        addSerializer(NodeDependency.class, new NodeDependencySerializer());

        addDeserializer(NodeGenericMetadata.class, new NodeGenericMetadataJsonDeserializer());
        addDeserializer(NodeInfo.class, new NodeInfoJsonDeserializer());
        addDeserializer(NodeDependency.class, new NodeDependencyDeserializer());
    }
}
