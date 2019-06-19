package com.powsybl.dsl;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharSource;
import groovy.lang.Binding;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class GroovyDslInterpreterTest {

    /**
     * Dummy DSL which gives access to a provided list as a groovy variable.
     */
    private static class ListGroovyDsl implements GroovyDsl<List<Integer>> {

        private final String name;

        ListGroovyDsl(String name) {
            this.name = name;
        }

        @Override
        public void enable(Binding binding, List<Integer> context) {
            binding.setVariable(name, context);
        }
    }

    @Test
    public void checkAggregateOf2Dsls() {
        GroovyDsl<List<Integer>> language = new GroovyAggregateDsl<>(ImmutableList.of(new ListGroovyDsl("list1"), new ListGroovyDsl("list2")));

        GroovyDslInterpreter<List<Integer>> interpreter = new GroovyDslInterpreter<>(language);
        List<Integer> data = new ArrayList<>();
        interpreter.interprete("list1.add(2)\nlist2.add(1)", data);

        assertEquals(2, data.size());
        assertEquals(2, (int) data.get(0));
        assertEquals(1, (int) data.get(1));

        interpreter.interprete(CharSource.wrap("list1.add(8)\nlist2.add(12)"), data);
        assertEquals(4, data.size());
        assertEquals(8, (int) data.get(2));
        assertEquals(12, (int) data.get(3));
    }

}
