package fr.sirs.util.javafx;

import java.text.DecimalFormat;
import java.text.ParseException;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.control.SpinnerValueFactory;
import javafx.util.StringConverter;
import org.apache.sis.util.ObjectConverter;
import org.apache.sis.util.ObjectConverters;
import org.apache.sis.util.UnconvertibleObjectException;

/**
 * A {@link SpinnerValueFactory} which work on float values.
 * 
 * @author Alexis Manin (Geomatys)
 */
public class FloatSpinnerValueFactory extends SpinnerValueFactory<Float> {

    protected static final DecimalFormat FLOAT_TO_STRING = new DecimalFormat("#.##");
    protected static final ObjectConverter<? super String, ? extends Float> STRING_TO_FLOAT = ObjectConverters.find(String.class, Float.class);
    
    public FloatSpinnerValueFactory(float min, float max) {
        this(min, max, 0);
    }
    
    public FloatSpinnerValueFactory(float min, float max, float initialValue) {
        this(min, max, initialValue, 0.1f);
    }
    
    public FloatSpinnerValueFactory(float min, float max, float initialValue, float step) {
        setMin(min);
        setMax(max);
        setAmountToStepBy(step);

            setConverter(new StringConverter<Float>() {
                @Override
                public String toString(Float object) {
                    return object == null? 
                            "" : FLOAT_TO_STRING.format(object);
                }

                @Override
                public Float fromString(String string) {
                    if (string == null || string.isEmpty())
                        return null;
                    try {
                        return STRING_TO_FLOAT.apply(string);
                    } catch (UnconvertibleObjectException e) {
                        try {
                            return FLOAT_TO_STRING.parse(string).floatValue();
                        } catch (ParseException e1) {
                            e.addSuppressed(e1);
                            throw e;
                        }
                    }
                }
            });

            valueProperty().addListener((o, oldValue, newValue) -> {
                // when the value is set, we need to react to ensure it is a
                // valid value (and if not, blow up appropriately)
                if (newValue < getMin()) {
                    setValue(getMin());
                } else if (newValue > getMax()) {
                    setValue(getMax());
                }
            });
            
            setValue(StrictMath.min(getMax(), StrictMath.max(getMin(), initialValue)));
        }



        /***********************************************************************
         *                                                                     *
         * Properties                                                          *
         *                                                                     *
         **********************************************************************/

        // --- min
        private SimpleFloatProperty min = new SimpleFloatProperty(this, "min") {
            @Override protected void invalidated() {
                Float currentValue = FloatSpinnerValueFactory.this.getValue();
                if (currentValue == null) {
                    return;
                }

                final float newMin = get();
                if (newMin > getMax()) {
                    setMin(getMax());
                    return;
                }

                if (currentValue < newMin) {
                    FloatSpinnerValueFactory.this.setValue(newMin);
                }
            }
        };

        public final void setMin(float value) {
            min.set(value);
        }
        public final float getMin() {
            return min.get();
        }
        /**
         * Sets the minimum allowable value for this value factory
         */
        public final FloatProperty minProperty() {
            return min;
        }

        // --- max
        private FloatProperty max = new SimpleFloatProperty(this, "max") {
            @Override protected void invalidated() {
                Float currentValue = FloatSpinnerValueFactory.this.getValue();
                if (currentValue == null) {
                    return;
                }

                final float newMax = get();
                if (newMax < getMin()) {
                    setMax(getMin());
                    return;
                }

                if (currentValue > newMax) {
                    FloatSpinnerValueFactory.this.setValue(newMax);
                }
            }
        };

        public final void setMax(float value) {
            max.set(value);
        }
        public final float getMax() {
            return max.get();
        }
        /**
         * Sets the maximum allowable value for this value factory
         */
        public final FloatProperty maxProperty() {
            return max;
        }

        // --- amountToStepBy
        private FloatProperty amountToStepBy = new SimpleFloatProperty(this, "amountToStepBy");
        public final void setAmountToStepBy(float value) {
            amountToStepBy.set(value);
        }
        public final float getAmountToStepBy() {
            return amountToStepBy.get();
        }
        
        /**
         * Sets the amount to increment or decrement by, per step.
         */
        public final FloatProperty amountToStepByProperty() {
            return amountToStepBy;
        }

        /** {@inheritDoc} */
        @Override public void decrement(int steps) {
            final float newValue = getValue() - (float)steps*amountToStepBy.floatValue();
            setValue(StrictMath.min(getMax(), StrictMath.max(getMin(), newValue)));
        }

        /** {@inheritDoc} */
        @Override public void increment(int steps) {
            final float newValue = getValue() + (float)steps*amountToStepBy.floatValue();
            setValue(StrictMath.min(getMax(), StrictMath.max(getMin(), newValue)));
        }
}
