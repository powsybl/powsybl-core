/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.serde.anonymizer.Anonymizer;
import com.powsybl.iidm.serde.anonymizer.FakeAnonymizer;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public abstract class AbstractSerDeContext<T> {

    private final Anonymizer anonymizer;

    protected AbstractSerDeContext(Anonymizer anonymizer) {
        this.anonymizer = anonymizer != null ? anonymizer : new FakeAnonymizer();
    }

    public Anonymizer getAnonymizer() {
        return anonymizer;
    }

    public abstract T getOptions();
}
