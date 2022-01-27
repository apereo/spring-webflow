package org.springframework.webflow.action;

import org.junit.jupiter.api.Test;
import org.springframework.binding.expression.support.StaticExpression;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

public class ExternalRedirectActionTests {
    private ExternalRedirectAction action;

    @Test
    public void testExecute() throws Exception {
        action = new ExternalRedirectAction(new StaticExpression("/wherever"));
        MockRequestContext context = new MockRequestContext();
        action.execute(context);
        assertEquals("/wherever", context.getMockExternalContext().getExternalRedirectUrl());
    }

    @Test
    public void testExecuteWithNullResourceUri() {
        try {
            action = new ExternalRedirectAction(null);
            fail("Should have failed");
        } catch (IllegalArgumentException e) {

        }
    }
}
