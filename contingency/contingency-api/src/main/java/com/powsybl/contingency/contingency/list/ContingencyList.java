/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.contingency.contingency.list;

import com.powsybl.contingency.Contingency;
import com.powsybl.contingency.ContingencyListLoader;
import com.powsybl.contingency.ContingencyListLoaderProvider;
import com.powsybl.iidm.network.Network;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Mathieu Bague {@literal <mathieu.bague@rte-france.com>}
 */
public interface ContingencyList {

    // VERSION = 1.0 : first version
    String VERSION = "1.0";

    static String getVersion() {
        return VERSION;
    }

    /**
     * Return the name of this contingency list
     */
    String getName();

    /**
     * Return the type of this contingency list
     */
    String getType();

    /**
     * Return a list of contingency for the given network.
     */
    List<Contingency> getContingencies(Network network);

    /**
     * Load a {@link ContingencyList} from a path
     *
     * @param path The file to load
     *
     * @return a contingency list
     */
    static ContingencyList load(Path path) {
        try (InputStream stream = new BufferedInputStream(Files.newInputStream(path))) {
            String filename = path.getFileName().toString();
            return load(filename, stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Load a {@link ContingencyList} from an input stream of given file name
     *
     * @param filename The input file name
     * @param stream The input stream to load
     *
     * @return a contingency list
     */
    static ContingencyList load(String filename, InputStream stream) {
        try {
            ContingencyListLoader loader = ContingencyListLoaderProvider.getLoader(FilenameUtils.getExtension(filename));
            return loader.load(filename, stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Create a contingency list from a set of contingencies
     */
    static ContingencyList of(Contingency... contingencies) {
        return new DefaultContingencyList(contingencies);
    }

    /**
     * Return only valid contingencies based on given list of contingencies and network
     */
    static List<Contingency> getValidContingencies(List<Contingency> contingencies, Network network) {
        Objects.requireNonNull(contingencies);
        Objects.requireNonNull(network);
        return contingencies.stream()
                .filter(c -> c.isValid(network))
                .collect(Collectors.toList());
    }
}
