/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
@FunctionalInterface
public interface MessageTemplateProvider {

    MessageTemplateProvider EMPTY = new EmptyMessageTemplateProvider();

    String getTemplate(String key, Locale locale);

    static String getMissingKeyMessage(String key, Locale locale) {
        String pattern = ResourceBundle.getBundle(PowsyblCoreReportResourceBundles.BASE_NAME, locale)
                .getString("core.commons.missingKey");
        return new MessageFormat(pattern, locale).format(key);
    }
}
