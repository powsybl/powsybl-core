/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

/**
 *
 * Represents a project file object of an unknown type (for instance when trying to read a file of type unknown to your instance of AFS).
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class UnknownProjectFile extends ProjectFile {

    UnknownProjectFile(ProjectFileCreationContext context) {
        super(context, context.getInfo().getVersion());
    }
}
