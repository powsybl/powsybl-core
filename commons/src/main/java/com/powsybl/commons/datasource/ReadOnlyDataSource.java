/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface ReadOnlyDataSource {

    String getBaseName();

    /**
     * Check if a file exists in the datasource. The file name will be constructed as:
     * {@code <basename><suffix>.<ext>}</p>
     * @param suffix Suffix to add to the basename of the datasource
     * @param ext Extension of the file (for example: .iidm, .xml, .txt, etc.)
     * @return true if the file exists, else false
     */
    boolean exists(String suffix, String ext) throws IOException;

    /**
     * Check if a file exists in the datasource.
     * @param fileName Name of the file (excluding the compression extension)
     * @return true if the file exists, else false
     */
    boolean exists(String fileName) throws IOException;

    InputStream newInputStream(String suffix, String ext) throws IOException;

    InputStream newInputStream(String fileName) throws IOException;

    /**
     * Returns a set of Strings corresponding to the name of the different files in the datasource.
     * @param regex regex used to identify files in the datasource
     * @return a set of filenames
     * @throws IOException exception thrown during file opening
     */
    Set<String> listNames(String regex) throws IOException;
}
