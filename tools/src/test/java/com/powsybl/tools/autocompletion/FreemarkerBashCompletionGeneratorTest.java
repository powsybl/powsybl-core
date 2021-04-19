package com.powsybl.tools.autocompletion;

import freemarker.template.TemplateException;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class FreemarkerBashCompletionGeneratorTest {

    @Test
    public void test() throws IOException, TemplateException {
        new FreemarkerBashCompletionGenerator().test(new OutputStreamWriter(System.out));
    }

}
