/*
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

/**
 * @author Olivier Perrin {@literal <olivier.perrin at rte-france.com>}
 */
public abstract class AbstractMessageTemplateProvider implements MessageTemplateProvider {

    private boolean strictMode;

    AbstractMessageTemplateProvider(boolean strictMode) {
        this.strictMode = strictMode;
    }

    @Override
    public void setStrictMode(boolean strictMode) {
        this.strictMode = strictMode;
    }

    @Override
    public boolean isStrictMode() {
        return this.strictMode;
    }
}
