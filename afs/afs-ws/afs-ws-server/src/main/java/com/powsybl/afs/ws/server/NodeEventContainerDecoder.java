/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ws.server;

import com.powsybl.afs.storage.events.NodeEventContainer;
import com.powsybl.afs.ws.utils.JacksonDecoder;

/**
 * @author Chamseddine Benhamed <Chamseddine.Benhamed at rte-france.com>
 */
public class NodeEventContainerDecoder extends JacksonDecoder<NodeEventContainer> {

    public NodeEventContainerDecoder() {
        super(NodeEventContainer.class);
    }
}
