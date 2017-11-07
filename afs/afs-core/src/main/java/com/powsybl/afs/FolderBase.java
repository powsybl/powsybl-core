/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.List;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface FolderBase<N extends AbstractNodeBase<F>, F extends FolderBase<N, F>> {

    List<N> getChildren();

    N getChild(String name, String... more);

    <T extends N> T getChild(Class<T> clazz, String name, String... more);

    F getFolder(String name, String... more);

    F createFolder(String name);
}
