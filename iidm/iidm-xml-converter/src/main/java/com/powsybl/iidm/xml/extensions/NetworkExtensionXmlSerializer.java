/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.xml.extensions;

import com.powsybl.commons.extensions.Extendable;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionXmlSerializer;
import com.powsybl.iidm.xml.IidmXmlVersion;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
public interface NetworkExtensionXmlSerializer<T extends Extendable, E extends Extension<T>> extends ExtensionXmlSerializer<T, E> {

    String getVersion(IidmXmlVersion version);
}
