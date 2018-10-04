package com.powsybl.cgmes;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 */
public enum Subset {
    EQUIPMENT("EQ"),
    TOPOLOGY("TP"),
    STATE_VARIABLES("SV"),
    STEADY_STATE_HYPOTHESIS("SSH"),
    DYNAMIC("DY"),
    DIAGRAM_LAYOUT("DL"),
    GEOGRAPHICAL_LOCATION("GL");

    private final String identifier;

    private Subset(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Get the identifier of a subset
     */
    public String getIdentifier() {
        return identifier;
    }
}
