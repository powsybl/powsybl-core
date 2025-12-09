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

    /**
     * Define if the mode should be strict or not. When {@link MessageTemplateProvider#getTemplate(String, Locale)}
     * is called with an unknown key, an exception is thrown if the {@link MessageTemplateProvider} is in strict mode.
     * In non-strict mode, the method will return {@code null} and the key will be used as a template when the message will be generated.
     * @param strictMode boolean
     */
    void setStrictMode(boolean strictMode);

    /**
     * Is the {@link MessageTemplateProvider} set in strict mode?
     * @return {@code true} if it is set in strict mode, {@code false} otherwise
     * @see MessageTemplateProvider#setStrictMode(boolean)
     */
    boolean isStrictMode();

    /**
     * <p>
     *     Returns the template to use to generate a message when the key is the given one, for the specified {@link Locale}.
     * </p>
     * <p>
     *     Note that this method can return {@code null} when it is called with an unknown key and
     *     the {@link MessageTemplateProvider} is not set in strict mode ({@code setStrictMode(false)}). In that case, the key
     *     will be used as a template when the message will be generated.
     * </p>
     * <p>
     *     When the strict mode is set ({@code setStrictMode(true)}), calling this method with an unknown key will throw an exception.
     * </p>
     * @param key the key corresponding to the template that should be returned
     * @param locale the Locale of the template that should be returned
     * @return the template corresponding to the given key and locale, or null if no template was defined for this key and {@code strictMode} is false.
     */
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
