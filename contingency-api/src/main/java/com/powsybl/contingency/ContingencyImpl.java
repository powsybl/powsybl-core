/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.contingency;

import java.util.List;

/**
 * @deprecated Use {@link Contingency} instead.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@Deprecated
public class ContingencyImpl extends Contingency {

    public ContingencyImpl(String id, ContingencyElement elements) {
        super(id, elements);
    }

    public ContingencyImpl(String id, List<ContingencyElement> elements) {
        super(id, elements);
    }
}
