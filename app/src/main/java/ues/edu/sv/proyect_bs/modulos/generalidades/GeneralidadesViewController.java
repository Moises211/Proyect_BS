package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

abstract class GeneralidadesViewController {
    protected final GeneralidadesActivity host;

    GeneralidadesViewController(GeneralidadesActivity host) {
        this.host = host;
    }

    abstract void configure();

    protected void configureField(int rootId, String label, String hint) {
        View root = host.findViewById(rootId);
        if (root == null) {
            return;
        }
        TextView labelView = root.findViewById(R.id.general_field_label);
        EditText input = root.findViewById(R.id.general_field_input);
        labelView.setText(label);
        input.setHint(hint);
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT);
    }

    protected EditText getField(int rootId) {
        View root = host.findViewById(rootId);
        if (root instanceof EditText) {
            return (EditText) root;
        }
        return root == null ? null : root.findViewById(R.id.general_field_input);
    }

    protected void setInputType(int rootId, int inputType) {
        EditText input = getField(rootId);
        if (input != null) {
            input.setInputType(inputType);
        }
    }

    protected void configurePasswordField(int rootId) {
        EditText input = getField(rootId);
        if (input == null) {
            return;
        }
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        input.setPadding(dp(12), 0, dp(44), 0);
        input.setCompoundDrawablePadding(dp(8));
        final boolean[] visible = {false};
        updatePasswordIcon(input, visible[0]);
        input.setOnTouchListener((view, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP
                    || event.getX() < input.getWidth() - dp(56)) {
                return false;
            }
            visible[0] = !visible[0];
            int cursor = Math.max(0, input.getSelectionStart());
            input.setTransformationMethod(visible[0]
                    ? HideReturnsTransformationMethod.getInstance()
                    : PasswordTransformationMethod.getInstance());
            input.setSelection(Math.min(cursor, input.length()));
            updatePasswordIcon(input, visible[0]);
            return true;
        });
    }

    protected void configureSpinnerField(int rootId, String[] options) {
        long[] ids = new long[options.length];
        for (int index = 0; index < options.length; index++) {
            ids[index] = index + 1L;
        }
        configureSpinnerField(rootId, options, ids);
    }

    protected void configureSpinnerField(int rootId, String[] options, long[] ids) {
        View root = host.findViewById(rootId);
        if (!(root instanceof ViewGroup)) {
            return;
        }
        ViewGroup container = (ViewGroup) root;
        View current = root.findViewById(R.id.general_field_input);
        if (current == null) {
            return;
        }
        int index = container.indexOfChild(current);
        container.removeView(current);

        Spinner spinner = new Spinner(host);
        spinner.setId(R.id.general_field_input);
        spinner.setBackgroundResource(host.isDarkMode()
                ? R.drawable.bg_general_field_dark : R.drawable.bg_general_field_light);
        spinner.setPadding(dp(12), 0, dp(12), 0);
        spinner.setGravity(Gravity.CENTER_VERTICAL);
        spinner.setContentDescription("Selector");
        String[] values = new String[options.length + 1];
        values[0] = "Seleccionar";
        System.arraycopy(options, 0, values, 1, options.length);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                host, android.R.layout.simple_spinner_item, values) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView text = (TextView) super.getView(position, convertView, parent);
                text.setTextColor(host.isDarkMode() ? Color.WHITE : Color.DKGRAY);
                text.setGravity(Gravity.CENTER_VERTICAL);
                Drawable arrow = host.getDrawable(R.drawable.ic_arrow_drop_down);
                if (arrow != null) {
                    arrow.setTint(host.isDarkMode() ? Color.WHITE : Color.DKGRAY);
                    text.setCompoundDrawablesWithIntrinsicBounds(null, null, arrow, null);
                    text.setCompoundDrawablePadding(dp(4));
                }
                return text;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setTag(R.id.general_field_input, ids);
        container.addView(spinner, index);
    }

    protected long getSelectedSpinnerId(int rootId) {
        View root = host.findViewById(rootId);
        View value = root == null ? null : root.findViewById(R.id.general_field_input);
        if (!(value instanceof Spinner)) {
            return 0L;
        }
        Spinner spinner = (Spinner) value;
        long[] ids = (long[]) spinner.getTag(R.id.general_field_input);
        int position = spinner.getSelectedItemPosition() - 1;
        return ids == null || position < 0 || position >= ids.length ? 0L : ids[position];
    }

    protected void configureDateField(int rootId) {
        EditText input = getField(rootId);
        if (input == null) {
            return;
        }
        input.setInputType(InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
        input.setFocusable(false);
        input.setClickable(true);
        input.setOnClickListener(v -> showDatePicker(input));
    }

    protected boolean validateRequired(int rootId, String message) {
        View root = host.findViewById(rootId);
        if (root == null) {
            return false;
        }
        View value = root.findViewById(R.id.general_field_input);
        String text = value instanceof Spinner
                ? ((Spinner) value).getSelectedItem().toString()
                : ((EditText) value).getText().toString().trim();
        boolean invalid = text.isEmpty() || text.equals("Seleccionar");
        if (value instanceof EditText) {
            ((EditText) value).setError(invalid ? message : null);
        }
        return !invalid;
    }

    protected boolean validatePattern(int rootId, String regex, String message) {
        EditText input = getField(rootId);
        if (input == null) {
            return false;
        }
        boolean valid = input.getText().toString().trim().matches(regex);
        input.setError(valid ? null : message);
        return valid;
    }

    protected boolean validateBirthDate(int rootId) {
        EditText input = getField(rootId);
        if (input == null) {
            return false;
        }
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            format.setLenient(false);
            Calendar selected = Calendar.getInstance();
            selected.setTime(format.parse(input.getText().toString().trim()));
            Calendar today = Calendar.getInstance();
            boolean valid = !selected.after(today);
            input.setError(valid ? null : "Ingresa una fecha válida que no sea futura");
            return valid;
        } catch (ParseException exception) {
            input.setError("Usa el formato dd/MM/aaaa");
            return false;
        }
    }

    private void updatePasswordIcon(EditText input, boolean visible) {
        Drawable icon = host.getDrawable(visible
                ? R.drawable.ic_visibility_off : R.drawable.ic_visibility);
        if (icon != null) {
            icon.setTint(Color.parseColor("#C51B20"));
            input.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, icon, null);
            input.setCompoundDrawablePadding(dp(8));
        }
    }

    private void showDatePicker(EditText target) {
        Calendar today = Calendar.getInstance();
        DatePicker pickerView = new DatePicker(host);
        pickerView.setMaxDate(today.getTimeInMillis());
        AlertDialog dialog = new MaterialAlertDialogBuilder(host)
                .setTitle("Fecha de nacimiento")
                .setView(pickerView)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (dialogInterface, which) -> target.setText(String.format(
                        Locale.getDefault(), "%02d/%02d/%04d",
                        pickerView.getDayOfMonth(), pickerView.getMonth() + 1,
                        pickerView.getYear())))
                .create();
        dialog.show();
    }

    protected int dp(int value) {
        return Math.round(value * host.getResources().getDisplayMetrics().density);
    }

    protected void applyTheme(View root) {
        boolean dark = host.isDarkMode();
        int background = Color.parseColor(dark ? "#101114" : "#E4E4E4");
        int text = Color.parseColor(dark ? "#F3F4F6" : "#252525");
        int secondary = Color.parseColor(dark ? "#A8ADB6" : "#777777");
        root.setBackgroundColor(background);
        applyThemeToChildren(root, dark, text, secondary);
    }

    private void applyThemeToChildren(View view, boolean dark, int text, int secondary) {
        if (view instanceof TextView && !(view instanceof EditText)) {
            TextView label = (TextView) view;
            if (label.getCurrentTextColor() != Color.WHITE
                    && label.getId() != R.id.general_login_submit
                    && label.getId() != R.id.general_register_submit
                    && label.getId() != R.id.general_recover_submit
                    && label.getId() != R.id.general_verify_submit
                    && label.getId() != R.id.general_settings_save) {
                label.setTextColor(label.getText().toString().contains("¿")
                        || label.getText().toString().contains("Volver")
                        ? Color.parseColor("#48B2C7") : text);
            }
        } else if (view instanceof EditText) {
            EditText input = (EditText) view;
            input.setTextColor(text);
            input.setHintTextColor(secondary);
            input.setBackgroundResource(dark
                    ? R.drawable.bg_general_field_dark : R.drawable.bg_general_field_light);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            Drawable background = group.getBackground();
            if (background != null && view.getId() != R.id.general_login_header
                    && view.getId() != R.id.general_register_toolbar
                    && view.getId() != R.id.general_recover_toolbar
                    && view.getId() != R.id.general_verify_toolbar
                    && view.getId() != R.id.general_settings_toolbar) {
                if (view.getId() == R.id.general_register_card
                        || view.getId() == R.id.general_settings_theme_card
                        || view.getId() == R.id.general_settings_remember_card) {
                    view.setBackgroundResource(dark
                            ? R.drawable.bg_general_card_dark : R.drawable.bg_general_card_light);
                }
            }
            for (int index = 0; index < group.getChildCount(); index++) {
                applyThemeToChildren(group.getChildAt(index), dark, text, secondary);
            }
        }
    }
}
