/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.datastore;

import java.io.IOException;
import java.util.Optional;
import java.util.Properties;

import javax.annotation.Nullable;

/**
 * @author Giovanni Ferrari <giovanni.ferrari at techrain.eu>
 */
public interface DataResolver {

    Optional<DataPack> resolve(ReadOnlyDataStore store, @Nullable String mainFileName, @Nullable Properties parameters) throws IOException, NonUniqueResultException;

    boolean validate(DataPack pack, @Nullable Properties parameters);
}
