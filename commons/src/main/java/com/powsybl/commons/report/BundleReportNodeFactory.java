/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author Florian Dupuy {@literal <florian.dupuy at rte-france.com>}
 */
public class BundleReportNodeFactory implements ReportNodeFactory<ReportNodeImpl> {

    private final String bundleBaseName;

    public BundleReportNodeFactory(String bundleBaseName) {
        this.bundleBaseName = bundleBaseName;
    }

    @Override
    public ReportNodeImpl createRoot(String key, Map<String, TypedValue> values, TreeContext treeContext) {
        return ReportNodeImpl.createRootReportNode(key, values, treeContext, this);
    }

    @Override
    public ReportNodeImpl createChild(String key, Map<String, TypedValue> values, ReportNodeImpl parent) {
        return ReportNodeImpl.createChildReportNode(key, values, parent, this);
    }

    @Override
    public MessageTemplateProvider getMessageTemplateProvider() {
        return (key, locale) -> ResourceBundle.getBundle(bundleBaseName, locale).getString(key);
    }
}
