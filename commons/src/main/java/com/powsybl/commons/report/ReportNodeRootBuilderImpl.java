/**
 *  Copyright (c) 2024, RTE (http://www.rte-france.com)
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *  SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import com.google.common.base.Suppliers;
import org.jspecify.annotations.NonNull;

import java.util.Locale;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * A builder to create a root {@link ReportNode} object.
 *
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class ReportNodeRootBuilderImpl extends AbstractReportNodeAdderOrBuilder<ReportNodeBuilder> implements ReportNodeBuilder {

    private static final Supplier<String[]> BUNDLE_BASE_NAMES_SUPPLIER = Suppliers.memoize(ReportNodeRootBuilderImpl::loadBundleBaseNames);
    private String defaultTimestampPattern;
    private Locale locale;

    @Override
    public ReportNodeBuilder withDefaultTimestampPattern(String timestampPattern) {
        this.defaultTimestampPattern = timestampPattern;
        return this;
    }

    @Override
    public ReportNodeBuilder withLocale(Locale locale) {
        this.locale = locale;
        return this;
    }

    @Override
    public ReportNode build() {
        TreeContext treeContext = new TreeContextImpl(locale, defaultTimestampPattern);
        if (withTimestamp) {
            addTimeStampValue(treeContext);
        }
        updateTreeDictionary(treeContext);
        return ReportNodeImpl.createRootReportNode(key, values, treeContext, messageTemplateProvider);
    }

    @Override
    public ReportNodeBuilder self() {
        return this;
    }

    @Override
    public String @NonNull [] getBundleBaseNames() {
        return BUNDLE_BASE_NAMES_SUPPLIER.get();
    }

    private static String @NonNull [] loadBundleBaseNames() {
        return ServiceLoader.load(ReportResourceBundle.class).stream()
                .map(ServiceLoader.Provider::get)
                .map(ReportResourceBundle::getBaseName)
                .toArray(String[]::new);
    }
}
