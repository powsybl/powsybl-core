package com.powsybl.afs;

public interface Dependent {

    /**
     *
     * returns the key value associated with the dependency.
     */
    String getDependencyKeyName(String dependencyNodeId);
}
