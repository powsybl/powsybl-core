package com.powsybl.mixed.security.analysis.criteria;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SwitchDecisionTest {
    @Test
    void testSwitchDecisionTrue() {
        SwitchDecision decision = new SwitchDecision(true, "Test reason");
        assertTrue(decision.shouldSwitch());
        assertEquals("Test reason", decision.getReason());
    }
    @Test
    void testSwitchDecisionFalse() {
        SwitchDecision decision = new SwitchDecision(false, "No switch");
        assertFalse(decision.shouldSwitch());
        assertEquals("No switch", decision.getReason());
    }
    @Test
    void testSwitchDecisionNullReasonThrows() {
        assertThrows(NullPointerException.class, () -> new SwitchDecision(true, null));
    }
    @Test
    void testSwitchDecisionToString() {
        SwitchDecision decision = new SwitchDecision(true, "Test reason");
        String str = decision.toString();
        assertNotNull(str);
        assertTrue(str.contains("shouldSwitch=true"));
        assertTrue(str.contains("Test reason"));
    }
    @Test
    void testSwitchDecisionEquals() {
        SwitchDecision decision1 = new SwitchDecision(true, "Test reason");
        SwitchDecision decision2 = new SwitchDecision(true, "Test reason");
        SwitchDecision decision3 = new SwitchDecision(false, "Test reason");
        assertEquals(decision1, decision2);
        assertNotEquals(decision1, decision3);
    }
    @Test
    void testSwitchDecisionHashCode() {
        SwitchDecision decision1 = new SwitchDecision(true, "Test reason");
        SwitchDecision decision2 = new SwitchDecision(true, "Test reason");
        assertEquals(decision1.hashCode(), decision2.hashCode());
    }
}
