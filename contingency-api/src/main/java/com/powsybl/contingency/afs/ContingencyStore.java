/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency.afs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.afs.ProjectFile;
import com.powsybl.afs.ProjectFileCreationContext;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.json.ContingencyJsonModule;
import com.powsybl.iidm.network.Network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ContingencyStore extends ProjectFile implements ContingenciesProvider {

    public static final String PSEUDO_CLASS = "contingencyStore";
    static final int VERSION = 0;
    static final String CONTINGENCY_LIST_JSON_NAME = "contingencyListJson";

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new ContingencyJsonModule());

    protected ContingencyStore(ProjectFileCreationContext context) {
        super(context, VERSION);
    }

    public void write(Contingency... contingencies) {
        write(Arrays.asList(contingencies));
    }

    public void write(List<Contingency> contingencies) {
        Objects.requireNonNull(contingencies);
        try (OutputStream os = storage.writeBinaryData(info.getId(), CONTINGENCY_LIST_JSON_NAME)) {
            objectMapper.writeValue(os, contingencies);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        storage.flush();
    }

    public List<Contingency> read() {
        try (InputStream is = storage.readBinaryData(info.getId(), CONTINGENCY_LIST_JSON_NAME).orElse(null)) {
            if (is != null) {
                return objectMapper.readValue(is, new TypeReference<List<Contingency>>() {
                });
            }
            return Collections.emptyList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Contingency> getContingencies(Network network) {
        return Contingency.checkValidity(read(), network);
    }
}
