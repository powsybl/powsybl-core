package com.powsybl.iidm.modification.scalable;

import java.util.Collection;
import java.util.Set;

public interface CompoundScalable extends Scalable {

    Collection<Scalable> getScalables();

    Collection<Scalable> getActiveScalables();

    void deactivateScalables(Set<Scalable> scalablesToDeactivate);

    void activateAllScalables();

    void activateScalables(Set<Scalable> scalablesToActivate);

    CompoundScalable shallowCopy();
}
