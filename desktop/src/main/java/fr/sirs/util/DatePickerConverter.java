package fr.sirs.util;

import com.sun.javafx.scene.control.skin.ComboBoxPopupControl;
import fr.sirs.ui.Growl;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javafx.scene.control.DatePicker;
import javafx.util.StringConverter;

/**
 * HACK for task SYM-1433. The query is to display an error message when user
 * text is not a date. As parsing error fails silently on text change (see
 * {@link ComboBoxPopupControl#setTextFromTextFieldIntoComboBoxValue() }), the
 * only solution I found is to override text conversion to insert error
 * management in it.
 *
 * To use this converter, simply call {@link #register(javafx.scene.control.DatePicker) },
 * passing wanted date picker.
 *
 * @author Alexis Manin (Geomatys)
 */
public class DatePickerConverter extends StringConverter<LocalDate> {

    final DatePicker dp;
    final StringConverter<LocalDate> originalConverter;

    protected DatePickerConverter(final DatePicker dp) {
        this.dp = dp;
        originalConverter = dp.getConverter();
    }

    @Override
    public String toString(LocalDate object) {
        return originalConverter.toString(object);
    }

    @Override
    public LocalDate fromString(String string) {
        try {
            return originalConverter.fromString(string);
        } catch (DateTimeParseException e) {
            new Growl(Growl.Type.WARNING, "Impossible de mettre à jour la date. La saisie doit être au format dd/MM/YYYY").showAndFade();
            throw e;
        }
    }

    /**
     * Modify given date picker to override its {@link DatePicker#converterProperty()}
     * to provide error message when string to date conversion fails. If given
     * date picker has no converter (which should never happen, as date picker
     * normally provides a default converter), this method do nothing.
     *
     * @param dp To override string conversion behaviour for.
     */
    public static void register(final DatePicker dp) {
        final StringConverter<LocalDate> converter = dp.getConverter();
        if (converter == null || (converter instanceof DatePickerConverter))
            return;

        dp.setConverter(new DatePickerConverter(dp));
    }
}
