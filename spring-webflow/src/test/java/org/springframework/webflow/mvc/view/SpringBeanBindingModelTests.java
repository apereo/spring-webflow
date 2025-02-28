package org.springframework.webflow.mvc.view;

import org.junit.jupiter.api.Test;
import org.springframework.binding.expression.ExpressionParser;
import org.springframework.binding.expression.beanwrapper.BeanWrapperExpressionParser;

import java.beans.PropertyEditor;

import static org.junit.jupiter.api.Assertions.*;

public class SpringBeanBindingModelTests extends AbstractBindingModelTests {

    // See SWF-1132
    @Test
    public void testFindPropertyEditorForUndeterminableType() {
        PropertyEditor editor = model.findEditor("emptyMap['foo']", null);
        assertNull(editor);
    }

    // BeanWrapper-based EL does not accept result type hints.
    // Hence it requires a conversion service.
    @Test
    public void testGetFieldValueNonStringNoConversionService() {
        model = new BindingModel("testBean", testBean, getExpressionParser(), null, messages);
        testBean.datum2 = 3;
        assertEquals(3, model.getFieldValue("datum2"));
    }

    protected ExpressionParser getExpressionParser() {
        return new BeanWrapperExpressionParser();
    }

}
