/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.google.common.io.ByteStreams;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public class DataPack {

    public static String MAIN_ENTRY_TAG = "MAIN_ENTRY";

    public static String EXTENSION_TAG = "EXTENSION";

    private final String dataFormatId;

    private final ReadOnlyDataStore source;

    private final List<DataEntry> entries;

    public DataPack(ReadOnlyDataStore source, String formatId) {
        this.source = Objects.requireNonNull(source);
        this.dataFormatId = Objects.requireNonNull(formatId);
        this.entries = new ArrayList<>();
    }

    public List<DataEntry> getEntries() {
        return entries;
    }

    public void addEntry(DataEntry entry) {
        entries.add(entry);
    }

    public Optional<DataEntry> getEntry(String entryName) {
        Objects.requireNonNull(entryName);
        return entries.stream().filter(e -> e.getName().equals(entryName)).findFirst();
    }

    public Optional<DataEntry> getMainEntry() {
        return entries.stream().filter(e -> e.getTags().contains(MAIN_ENTRY_TAG)).findFirst();
    }

    public ReadOnlyDataStore getSource() {
        return source;
    }

    public DataPack copyTo(DataStore target) throws IOException {
        Objects.requireNonNull(target);
        DataPack copy = new DataPack(target, dataFormatId);
        for (DataEntry e : entries) {
            try (InputStream in = source.newInputStream(e.getName()); OutputStream out = target.newOutputStream(e.getName(), false)) {
                ByteStreams.copy(in, out);
            }
            copy.addEntry(e);
        }

        return copy;
    }

    public String getDataFormatId() {
        return dataFormatId;
    }

}
