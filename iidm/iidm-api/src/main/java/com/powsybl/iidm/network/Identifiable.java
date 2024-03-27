/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import java.util.*;

import com.powsybl.commons.extensions.Extendable;

/**
 * An object that is part of the network model and that is identified uniquely
 * by a <code>String</code> id.
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface Identifiable<I extends Identifiable<I>> extends Extendable<I> {

    /**
     * Get the network associated to the object.
     */
    Network getNetwork();

    /**
     * Get the parent network, i.e. the smallest network containing the object.
     */
    default Network getParentNetwork() {
        return getNetwork();
    }

    /**
     * Get the unique identifier of the object.
     */
    String getId();

    /**
     * Get the aliases of the object.
     */
    default Set<String> getAliases() {
        return Collections.emptySet();
    }

    /**
     * Get the alias of the object with a given alias type if it exists. Else return an empty optional.
     *
     * @throws {@link com.powsybl.commons.PowsyblException} if the aliasType is null or empty
     */
    default Optional<String> getAliasFromType(String aliasType) {
        return Optional.empty();
    }

    /**
     * Get the alias type of an alias if it exists, an empty optional otherwise.
     * If the alias does not exist, return an empty optional.
     */
    default Optional<String> getAliasType(String alias) {
        return Optional.empty();
    }

    /**
     * Add an alias to the object. Aliases must be unique in associated Network, and different
     * from any identifiable ID. No alias type is associated to this alias.
     *
     * If the alias already exists (i.e. is not unique) or equals an identifiable ID, throw a {@link com.powsybl.commons.PowsyblException}
     */
    default void addAlias(String alias) {
    }

    default void addAlias(String alias, boolean ensureAliasUnicity) {
        addAlias(alias);
    }

    /**
     * Add an alias to the object. Aliases must be unique in associated Network, and different
     * from any identifiable ID. This alias is associated to a given alias type.
     * If the given alias type is null or empty, no alias type is considered associated to the alias.
     * Only one alias can be associated to a non null given alias type for one object.
     *
     * If the alias already exists (i.e. is not unique) or equals an identifiable ID, throw a {@link com.powsybl.commons.PowsyblException}
     * If a given non null type already has an associated alias for this object, throw a {@link com.powsybl.commons.PowsyblException}
     */
    default void addAlias(String alias, String aliasType) {
    }

    default void addAlias(String alias, String aliasType, boolean ensureAliasUnicity) {
        addAlias(alias, aliasType);
    }

    /**
     * Remove an alias of the object.
     *
     * If the alias does not exist for this object, throw a {@link com.powsybl.commons.PowsyblException}
     */
    default void removeAlias(String alias) {
    }

    /**
     * Return true if identifiable has aliases.
     */
    default boolean hasAliases() {
        return false;
    }

    /**
     * Return an optional containing the name  of the object if it exists. If not, return an empty optional.
     */
    default Optional<String> getOptionalName() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get the name of the object if it exists. If not, get the unique identifier of the object.
     */
    default String getNameOrId() {
        return getOptionalName().orElseGet(this::getId);
    }

    /**
     * Update object name.
     *
     * @param name new name of the object (could be null to erase the name)
     * @return the object itself
     */
    default I setName(String name) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Check that this object has some properties.
     */
    boolean hasProperty();

    /**
     * Check that this object has property with specified name.
     */
    boolean hasProperty(String key);

    /**
     * Get property associated to specified key.
     */
    String getProperty(String key);

    /**
     * Get property associated to specified key, with default value.
     */
    String getProperty(String key, String defaultValue);

    /**
     * Set property value associated to specified key.
     */
    String setProperty(String key, String value);

    /**
     * Remove property with specified key.
     *
     * @param key the property key
     * @return {@code true} if property exists and has been removed, {@code false} otherwise
     */
    boolean removeProperty(String key);

    /**
     * Get properties key values.
     */
    Set<String> getPropertyNames();

    /**
     * Get the fictitious status
     */
    default boolean isFictitious() {
        return false;
    }

    /**
     * Set the fictitious status
     */
    default void setFictitious(boolean fictitious) {
        throw new UnsupportedOperationException();
    }

    /**
     * Get identifiable type.
     *
     * @return the identifiable type
     */
    IdentifiableType getType();
}
