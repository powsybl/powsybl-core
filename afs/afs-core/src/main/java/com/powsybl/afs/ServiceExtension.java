/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs;

import java.util.Objects;

/**
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

    ServiceKey<U> getServiceKey();

    U createService();
}
