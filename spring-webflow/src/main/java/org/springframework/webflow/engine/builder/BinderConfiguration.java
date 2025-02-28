package org.springframework.webflow.engine.builder;

import org.springframework.core.style.ToStringCreator;
import org.springframework.util.Assert;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Contains the information needed to bind model to a view. This information consists of one or more {@link Binding
 * bindings} that connect properties of the model to UI elements of the view.
 *
 * @author Keith Donald
 * @see ViewFactoryCreator
 */
public class BinderConfiguration {

    private Set<Binding> bindings = new LinkedHashSet<>();

    /**
     * Adds a new binding to this binding configuration.
     *
     * @param binding the binding
     */
    public void addBinding(Binding binding) {
        bindings.add(binding);
    }

    /**
     * Returns the set of bindings associated with this binding configuration.
     *
     * @return
     */
    public Set<Binding> getBindings() {
        return bindings;
    }

    /**
     * Gets the binding with the specified name, or returns null if no such binding is found.
     *
     * @param name the name of the binding.
     * @return the binding
     */
    public Binding getBinding(String name) {
        for (Binding binding : bindings) {
            if (name.equals(binding.getProperty())) {
                return binding;
            }
        }
        return null;
    }

    /**
     * Gets the converterId for the binding with the specified name. Returns null if either a binding or a converterId
     * for the given name is not found.
     *
     * @param name the name of the binding.
     * @return the binding
     */
    public String getConverterId(String name) {
        Binding binding = getBinding(name);
        if (binding != null) {
            return binding.getConverter();
        } else {
            return null;
        }
    }

    /**
     * A binding that provides the information needed to connect an element of the view to a property of the model.
     *
     * @author Keith Donald
     */
    public static final class Binding {

        private String property;

        private String converter;

        private boolean required;

        /**
         * Creates a new view binding
         *
         * @param property  the model property to bind to
         * @param converter the id of a custom converter to apply type conversion during binding
         * @param required  whether this binding is required
         */
        public Binding(String property, String converter, boolean required) {
            Assert.hasText(property, "The property is required");
            this.property = property;
            this.converter = converter;
            this.required = required;
        }

        public boolean equals(Object object) {
            if (!(object instanceof Binding)) {
                return false;
            }
            Binding binding = (Binding) object;
            return property.equals(binding.property);
        }

        public int hashCode() {
            return property.hashCode();
        }

        /**
         * The name of the bound property.
         *
         * @return the property
         */
        public String getProperty() {
            return property;
        }

        /**
         * The id of the custom converter to use to convert bound property values.
         *
         * @return the converter id, or null
         */
        public String getConverter() {
            return converter;
        }

        /**
         * Whether a non-empty value is required for each binding attempt.
         *
         * @return the required status
         */
        public boolean getRequired() {
            return required;
        }

        public String toString() {
            return new ToStringCreator(this).append("property", property).append("converter", converter)
                .append("required", required).toString();
        }
    }

}
