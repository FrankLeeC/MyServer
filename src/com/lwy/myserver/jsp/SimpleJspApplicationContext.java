package com.lwy.myserver.jsp;

import javax.el.ELContextListener;
import javax.el.ELResolver;
import javax.el.ExpressionFactory;
import javax.servlet.jsp.JspApplicationContext;

/**
 * Created by frank lee on 2016/7/28.
 */
public class SimpleJspApplicationContext implements JspApplicationContext{

    /**
     * Adds an <code>ELResolver</code> to affect the way EL variables
     * and properties are resolved for EL expressions appearing in JSP pages
     * and tag files.
     *
     * <p>For example, in the EL expression ${employee.lastName}, an
     * <code>ELResolver</code> determines what object "employee"
     * references and how to find its "lastName" property.</p>
     *
     * <p>When evaluating an expression, the JSP container will consult a
     * set of standard resolvers as well as any resolvers registered via
     * this method. The set of resolvers are consulted in the following
     * order:
     * <ul>
     *   <li>{@link javax.servlet.jsp.el.ImplicitObjectELResolver}</li>
     *   <li><code>ELResolver</code>s registered via this method, in the
     *       order in which they are registered.</li>
     *   <li>{@link javax.el.MapELResolver}</li>
     *   <li>{@link javax.el.ListELResolver}</li>
     *   <li>{@link javax.el.ArrayELResolver}</li>
     *   <li>{@link javax.el.BeanELResolver}</li>
     *   <li>{@link javax.servlet.jsp.el.ScopedAttributeELResolver}</li>
     * </ul></p>
     *
     * <p>It is illegal to register an <code>ELResolver</code> after the
     * application has received any request from the client. If an
     * attempt is made to register an <code>ELResolver</code> after that time,
     * an <code>IllegalStateException</code> is thrown.</p>
     * This restriction is
     * in place to allow the JSP container to optimize for the common
     * case where no additional <code>ELResolver</code>s are in the chain,
     * aside from the standard ones. It is permissible to add
     * <code>ELResolver</code>s before or after initialization to
     * a <code>CompositeELResolver</code> that is already in the chain.</p>
     *
     * <p>It is not possible to remove an <code>ELResolver</code> registered
     * with this method, once it has been registered.</p>
     *
     * @param resolver The new <code>ELResolver</code>
     * @throws IllegalStateException if an attempt is made to
     *     call this method after all <code>ServletContextListener</code>s
     *     have had their <code>contextInitialized</code> methods invoked.
     */
    @Override
    public void addELResolver(ELResolver resolver) {

    }

    /**
     * Returns a factory used to create <code>ValueExpression</code>s and
     * <code>MethodExpression</code>s so that EL expressions can be
     * parsed and evaluated.
     *
     * @return A concrete implementation of the
     *     an <code>ExpressionFactory</code>.
     */
    @Override
    public ExpressionFactory getExpressionFactory() {
        return null;
    }

    @Override
    public void addELContextListener(ELContextListener listener) {

    }
}
