/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.cgmes.conversion.update;

import java.util.Objects;

/**
 * @author Elena Kaltakova <kaltakovae at aia.es>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public class TripleStoreChange {

    public TripleStoreChange(String queryName, String subject, TripleStoreChangeParams params) {
        this.queryName = Objects.requireNonNull(queryName);
        this.subject = Objects.requireNonNull(subject);
        this.params = Objects.requireNonNull(params);
    }

    public String queryName() {
        return queryName;
    }

    public String subject() {
        return subject;
    }

    public TripleStoreChangeParams params() {
        return params;
    }

    private final String queryName;
    private final String subject;
    private final TripleStoreChangeParams params;
}
