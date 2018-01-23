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
import java.util.HashSet;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class StringSetSerializer implements Serializer<Set<String>>, Serializable {

    public static final StringSetSerializer INSTANCE = new StringSetSerializer();

    private StringSetSerializer() {
    }

    @Override
    public void serialize(DataOutput2 out, Set<String> set) throws IOException {
        out.writeInt(set.size());
        for (String str : set) {
            Serializer.STRING.serialize(out, str);
        }
    }

    @Override
    public Set<String> deserialize(DataInput2 input, int available) throws IOException {
        int size = input.readInt();
        Set<String> set = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            set.add(Serializer.STRING.deserialize(input, available));
        }
        return set;
    }
}

