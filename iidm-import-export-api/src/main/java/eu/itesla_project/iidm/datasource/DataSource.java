/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface DataSource {

    OutputStream newOutputStream(String suffix, String ext, boolean append) throws IOException;

    String getBaseName();

    boolean exists(String suffix, String ext) throws IOException;

    boolean exists(String fileName) throws IOException;

    InputStream newInputStream(String suffix, String ext) throws IOException;

    InputStream newInputStream(String fileName) throws IOException;

}
