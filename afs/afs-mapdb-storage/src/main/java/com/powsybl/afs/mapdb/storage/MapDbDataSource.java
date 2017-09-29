/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.mapdb.storage;

import eu.itesla_project.afs.storage.NodeId;
import eu.itesla_project.commons.datasource.DefaultDataSourceObserver;
import eu.itesla_project.commons.datasource.DataSource;
import eu.itesla_project.commons.datasource.ObservableOutputStream;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class MapDbDataSource implements DataSource {

    static class Key implements Serializable {

        private static final long serialVersionUID = -667026329394633704L;

        private final NodeId nodeId;

        private final String attributeName;

        private final String suffix;

        private final String ext;

        Key(NodeId nodeId, String attributeName, String suffix, String ext) {
            this.nodeId = Objects.requireNonNull(nodeId);
            this.attributeName = Objects.requireNonNull(attributeName);
            this.suffix = suffix;
            this.ext = ext;
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeId, attributeName, suffix, ext);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Key) {
                Key other = (Key) obj;
                return attributeName.equals(other.attributeName)
                        && Objects.equals(suffix, other.suffix)
                        && Objects.equals(ext, other.ext);
            }
            return false;
        }
    }

    private final NodeId nodeId;

    private final String attributeName;

    private final ConcurrentMap<Key, byte[]> data;

    private final ConcurrentMap<String, byte[]> data2;

    public MapDbDataSource(NodeId nodeId, String attributeName, ConcurrentMap<Key, byte[]> data, ConcurrentMap<String, byte[]> data2) {
        this.nodeId = Objects.requireNonNull(nodeId);
        this.attributeName = Objects.requireNonNull(attributeName);
        this.data = Objects.requireNonNull(data);
        this.data2 = Objects.requireNonNull(data2);
    }

    @Override
    public String getBaseName() {
        return "";
    }

    @Override
    public OutputStream newOutputStream(final String suffix, final String ext, boolean append) throws IOException {
        final Key key = new Key(nodeId, attributeName, suffix, ext);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (append) {
            byte[] ba = data.get(new Key(nodeId, attributeName, suffix, ext));
            if (ba != null) {
                os.write(ba, 0, ba.length);
            }
        }
        return new ObservableOutputStream(os, key.toString(), new DefaultDataSourceObserver() {
            @Override
            public void closed(String streamName) {
                data.put(key, os.toByteArray());
            }
        });
    }

    @Override
    public OutputStream newOutputStream(String fileName, boolean append) throws IOException {
        Objects.requireNonNull(fileName);
        final ByteArrayOutputStream os = new ByteArrayOutputStream();
        if (append) {
            byte[] ba = data2.get(fileName);
            if (ba != null) {
                os.write(ba, 0, ba.length);
            }
        }
        return new ObservableOutputStream(os, fileName, new DefaultDataSourceObserver() {
            @Override
            public void closed(String streamName) {
                data2.put(fileName, os.toByteArray());
            }
        });
    }

    @Override
    public boolean exists(String suffix, String ext) {
        return data.containsKey(new Key(nodeId, attributeName, suffix, ext));
    }

    @Override
    public boolean exists(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        return data2.containsKey(fileName);
    }

    @Override
    public InputStream newInputStream(String suffix, String ext) throws IOException {
        byte[] ba = data.get(new Key(nodeId, attributeName, suffix, ext));
        if (ba == null) {
            throw new IOException("*" + (suffix != null ? suffix : "") + "." + (ext != null ? ext : "") + " does not exist");
        }
        return new ByteArrayInputStream(ba);
    }

    @Override
    public InputStream newInputStream(String fileName) throws IOException {
        Objects.requireNonNull(fileName);
        byte[] ba = data2.get(fileName);
        if (ba == null) {
            throw new IOException(fileName + " does not exist");
        }
        return new ByteArrayInputStream(ba);
    }
}
