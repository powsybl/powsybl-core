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
public interface MessageTemplateProvider {

    MessageTemplateProvider EMPTY_STRICT = new EmptyMessageTemplateProvider(true);
    MessageTemplateProvider EMPTY_LOOSE = new EmptyMessageTemplateProvider(false);

    ResourceBundle.Control NO_FALLBACK_CONTROL = ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_DEFAULT);

    void setStrictMode(boolean strictMode);

    boolean isStrictMode();

    String getTemplate(String key, Locale locale);

    static String getMissingKeyMessage(String key, Locale locale) {
        return getMissingKeyMessage(key, locale, true);
    }

    static String getMissingKeyMessage(String key, Locale locale, boolean strictMode) {
        if (strictMode) {
            String pattern = ResourceBundle.getBundle(PowsyblCoreReportResourceBundle.BASE_NAME, locale, NO_FALLBACK_CONTROL)
                    .getString("core.commons.missingKey");
            return new MessageFormat(pattern, locale).format(new Object[]{key});
        }
        return null;
    }
}
