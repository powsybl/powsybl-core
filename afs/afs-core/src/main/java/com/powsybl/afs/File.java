/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

/**
 *
 * A file in an {@link AppFileSystem} object. New types of files may be added through
 * the extension mechanism (see {@link FileExtension}).
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class File extends Node {

    public File(FileCreationContext context, int codeVersion) {
        super(context, codeVersion, false);
    }
}
