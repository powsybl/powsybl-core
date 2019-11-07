/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.mapdb.storage;

import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;

import java.io.IOException;
import java.io.Serializable;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class NamedLinkSerializer implements Serializer<NamedLink>, Serializable {

    public static final NamedLinkSerializer INSTANCE = new NamedLinkSerializer();

    @Override
    public void serialize(DataOutput2 out, NamedLink namedLink) throws IOException {
        out.writeInt(MapDbStorageConstants.STORAGE_VERSION);
        UuidSerializer.INSTANCE.serialize(out, namedLink.getNodeUuid());
        out.writeUTF(namedLink.getName());
    }

    @Override
    public NamedLink deserialize(DataInput2 input, int available) throws IOException {
        input.readInt(); // Storage version is retrieved here
        java.util.UUID nodeUuid = UuidSerializer.INSTANCE.deserialize(input, available);
        String name = input.readUTF();
        return new NamedLink(nodeUuid, name);
    }
}
