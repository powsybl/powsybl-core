/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface IdentifiableAdder<T extends Identifiable<? super T>, A extends IdentifiableAdder> {

    A setId(String id);

    A setEnsureIdUnicity(boolean ensureIdUnicity);

    A setName(String name);

    default A setFictitious(boolean fictitious) {
        throw new UnsupportedOperationException();
    }

    T add();
}
