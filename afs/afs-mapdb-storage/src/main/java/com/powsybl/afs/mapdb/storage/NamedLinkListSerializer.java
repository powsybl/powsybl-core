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
import java.util.ArrayList;
import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class NamedLinkListSerializer implements Serializer<List<NamedLink>>, Serializable {

    public static final NamedLinkListSerializer INSTANCE = new NamedLinkListSerializer();

    private NamedLinkListSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, List<NamedLink> namedLinks) throws IOException {
        out.writeInt(namedLinks.size());
        for (int i = 0; i < namedLinks.size(); i++) {
            NamedLinkSerializer.INSTANCE.serialize(out, namedLinks.get(i));
        }
    }

    @Override
    public List<NamedLink> deserialize(DataInput2 input, int available) throws IOException {
        int size = input.readInt();
        List<NamedLink> namedLinks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            namedLinks.add(NamedLinkSerializer.INSTANCE.deserialize(input, available));
        }
        return namedLinks;
    }
}
