/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.Objects;

/**
 *
 * Registers a new implementation for service of type &lt;U&gt;, to be retrieved with {@link AppData#findService}.
 *
 * <p>
 * A service is identified by its interface type and by a "remote" boolean.
 * Therefore you may only have one local and one remote implementation of the same service registered with your AFS.
 *
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface ServiceExtension<U> {

    class ServiceKey<U> {

        private final Class<U> serviceClass;

        private final boolean remote;

        public ServiceKey(Class<U> serviceClass, boolean remote) {
            this.serviceClass = Objects.requireNonNull(serviceClass);
            this.remote = remote;
        }

        public Class<U> getServiceClass() {
            return serviceClass;
        }

        public boolean isRemote() {
            return remote;
        }

        @Override
        public int hashCode() {
            return Objects.hash(serviceClass, remote);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ServiceKey) {
                ServiceKey<U> other = (ServiceKey<U>) obj;
                return serviceClass.equals(other.serviceClass) && remote == other.remote;
            }
            return false;
        }

        @Override
        public String toString() {
            return "ServiceKey(" + serviceClass.getName() + ", " + remote + ")";
        }
    }

    /**
     * Key identifying the service in AFS.
     */
    ServiceKey<U> getServiceKey();

    /**
     * Creates the service instance.
     */
    U createService(ServiceCreationContext context);
}
