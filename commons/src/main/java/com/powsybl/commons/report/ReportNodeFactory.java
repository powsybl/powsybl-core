/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.report;

import java.util.Map;

public interface ReportNodeFactory<T extends ReportNode> {

    T createRoot(String key, Map<String, TypedValue> values, TreeContext treeContext);

    T createChild(String key, Map<String, TypedValue> values, T parent);

    MessageTemplateProvider getMessageTemplateProvider();
}
