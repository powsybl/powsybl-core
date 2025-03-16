/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.util.Locale;

/**
 * Empty template provider used when deserializing a report node
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class EmptyMessageTemplateProvider implements MessageTemplateProvider {

    @Override
    public String getTemplate(String key, Locale locale) {
        return MessageTemplateProvider.getMissingKeyMessage(key, locale);
    }
}
