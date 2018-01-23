/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.List;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface FolderBase<N extends AbstractNodeBase<F>, F extends FolderBase<N, F>> {

    List<N> getChildren();

    Optional<N> getChild(String name, String... more);

    <T extends N> Optional<T> getChild(Class<T> clazz, String name, String... more);

    Optional<F> getFolder(String name, String... more);

    F createFolder(String name);
}
