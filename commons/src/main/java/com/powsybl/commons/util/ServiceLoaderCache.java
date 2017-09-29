/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.util;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

/**
 * A thread safe service loader.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class ServiceLoaderCache<S> {

    private final Class<S> serviceClass;

    private List<S> services;

    public ServiceLoaderCache(Class<S> serviceClass) {
        this.serviceClass = Objects.requireNonNull(serviceClass);
    }

    public synchronized List<S> getServices() {
        if (services == null) {
            services = Lists.newArrayList(ServiceLoader.load(serviceClass));
        }
        return services;
    }

}
