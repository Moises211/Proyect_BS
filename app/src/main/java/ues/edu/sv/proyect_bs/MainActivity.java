package ues.edu.sv.proyect_bs;

import android.os.Bundle;
import android.net.Uri;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    private java.util.Calendar selectedWeekStart;
    private boolean darkMode;
    private AuthApiClient.AdminProfile loadedAdminProfile;
    private final AdminNavigationController navigationController =
            new AdminNavigationController(this);
    private ImageView promotionImageTarget;
    private final ActivityResultLauncher<String> promotionImagePicker =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null && promotionImageTarget != null) {
                    promotionImageTarget.setImageURI(uri);
                    promotionImageTarget.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    promotionImageTarget.setPadding(0, 0, 0, 0);
                    promotionImageTarget.setColorFilter(null);
                    promotionImageTarget.setContentDescription("Imagen promocional seleccionada");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SessionManager.hasAdminAccess(this)) {
            openGeneralidades();
            return;
        }
        darkMode = getSharedPreferences("app_preferences", MODE_PRIVATE)
                .getBoolean("dark_mode", false);
        showScreen(R.layout.pantalla_inicial);
    }

    void showScreen(int layout) {
        navigationController.navigateTo(layout);
    }

    AdminNavigationController getNavigationController() {
        return navigationController;
    }

    void configureManagementScreen(int layout) {
        createViewController(layout).configure();
        applyDarkModeToCurrentScreen();
    }

    private AdminViewController createViewController(int layout) {
        if (layout == R.layout.pantalla_inicial) {
            return new PantallaInicialController(this);
        }
        if (layout == R.layout.pantalla_control_citas) {
            return new ControlCitasController(this);
        }
        if (layout == R.layout.pantalla_clientes) {
            return new ClientesController(this);
        }
        if (layout == R.layout.pantalla_citas_cliente) {
            return new CitasClienteController(this);
        }
        if (layout == R.layout.pantalla_historial_citas_cliente) {
            return new HistorialCitasClienteController(this);
        }
        if (layout == R.layout.pantalla_citas) {
            return new CitasController(this);
        }
        if (layout == R.layout.pantalla_beneficios) {
            return new BeneficiosController(this);
        }
        if (layout == R.layout.pantalla_beneficios_cliente) {
            return new BeneficiosClienteController(this);
        }
        if (layout == R.layout.pantalla_personal_turnos) {
            return new PersonalTurnosController(this);
        }
        if (layout == R.layout.pantalla_barberos) {
            return new BarberosController(this);
        }
        if (layout == R.layout.pantalla_cronograma_barberos) {
            return new CronogramaBarberosController(this);
        }
        if (layout == R.layout.pantalla_especialidades) {
            return new EspecialidadesController(this);
        }
        if (layout == R.layout.pantalla_cronograma_barbero) {
            return new CronogramaBarberoController(this);
        }
        if (layout == R.layout.pantalla_cortes_servicios) {
            return new CortesServiciosController(this);
        }
        if (layout == R.layout.pantalla_administracion_servicios) {
            return new AdministracionServiciosController(this);
        }
        if (layout == R.layout.pantalla_horarios_servicio) {
            return new HorariosServicioController(this);
        }
        if (layout == R.layout.pantalla_configuracion_horario_base) {
            return new ConfiguracionHorarioBaseController(this);
        }
        if (layout == R.layout.pantalla_promociones) {
            return new PromocionesController(this);
        }
        if (layout == R.layout.pantalla_beneficios_admin) {
            return new BeneficiosAdminController(this);
        }
        if (layout == R.layout.pantalla_perfil_usuario) {
            return new PerfilUsuarioController(this);
        }
        throw new IllegalArgumentException("Layout administrativo no registrado: " + layout);
    }

    void setDarkMode(boolean enabled) {
        darkMode = enabled;
        getSharedPreferences("app_preferences", MODE_PRIVATE).edit()
                .putBoolean("dark_mode", darkMode)
                .apply();
        showScreen(navigationController.getCurrentLayout());
    }

    void openGeneralidades() {
        startActivity(new android.content.Intent(this, GeneralidadesActivity.class));
        finish();
    }

    void applyDarkModeToCurrentScreen() {
        View root = findViewById(android.R.id.content);
        if (root != null) {
            applyDarkModeToView(root);
        }
    }

    boolean isDarkMode() {
        return darkMode;
    }

    void applyDarkModeToView(View view) {
        int darkSurface = android.graphics.Color.rgb(40, 40, 44);
        View contentRoot = findViewById(android.R.id.content);
        if (view.getId() == android.R.id.content
                || (contentRoot != null && view.getParent() == contentRoot)) {
            view.setBackgroundColor(darkMode
                    ? android.graphics.Color.rgb(16, 17, 20)
                    : android.graphics.Color.rgb(228, 228, 228));
        }
        if (view instanceof MaterialToolbar || view instanceof com.google.android.material.button.MaterialButton
                || view instanceof BottomNavigationView) {
            if (view instanceof BottomNavigationView) {
                BottomNavigationView navigation = (BottomNavigationView) view;
                view.setBackgroundColor(darkMode
                        ? android.graphics.Color.rgb(58, 62, 71) : android.graphics.Color.parseColor("#FAF3FC"));
                applyBottomNavigationColors(navigation);
            }
        } else if (view instanceof MaterialCardView) {
            MaterialCardView card = (MaterialCardView) view;
            card.setCardBackgroundColor(darkMode
                    ? android.graphics.Color.rgb(35, 38, 43) : android.graphics.Color.WHITE);
            if (darkMode) {
                card.setStrokeColor(android.graphics.Color.rgb(61, 66, 74));
            }
        } else if (view instanceof TextInputLayout && isSearchField((TextInputLayout) view)) {
            TextInputLayout searchField = (TextInputLayout) view;
            android.graphics.drawable.GradientDrawable searchBackground =
                    new android.graphics.drawable.GradientDrawable();
            searchBackground.setColor(android.graphics.Color.WHITE);
            float radius = getResources().getDisplayMetrics().density * 16f;
            searchBackground.setCornerRadius(radius);
            searchField.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
            searchField.setBackground(searchBackground);
            searchField.setHintTextColor(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.rgb(117, 117, 117)));
            searchField.setEndIconTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.rgb(70, 70, 70)));
            Runnable keepSearchLight = () -> {
                searchField.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_NONE);
                searchField.setBackground(searchBackground);
            };
            searchField.post(keepSearchLight);
            searchField.postDelayed(keepSearchLight, 100);
        } else if (view instanceof EditText) {
            EditText field = (EditText) view;
            boolean searchInput = field.getParent() instanceof TextInputLayout
                    && isSearchField((TextInputLayout) field.getParent());
            field.setTextColor(searchInput || !darkMode
                    ? android.graphics.Color.rgb(51, 51, 51)
                    : android.graphics.Color.rgb(238, 238, 238));
            field.setHintTextColor(searchInput || !darkMode
                    ? android.graphics.Color.rgb(117, 117, 117)
                    : android.graphics.Color.rgb(170, 170, 175));
            if (searchInput) {
                field.setBackground(null);
            } else {
                field.setBackgroundTintList(darkMode
                        ? android.content.res.ColorStateList.valueOf(darkSurface) : null);
            }
        } else if (view instanceof SwitchMaterial) {
            SwitchMaterial toggle = (SwitchMaterial) view;
            if (darkMode) {
                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{-android.R.attr.state_checked}
                };
                int[] thumbColors = new int[]{
                        android.graphics.Color.parseColor("#48B2C7"),
                        android.graphics.Color.rgb(170, 170, 175)
                };
                int[] trackColors = new int[]{
                        android.graphics.Color.parseColor("#397F8E"),
                        android.graphics.Color.rgb(80, 82, 88)
                };
                toggle.setThumbTintList(new android.content.res.ColorStateList(states, thumbColors));
                toggle.setTrackTintList(new android.content.res.ColorStateList(states, trackColors));
            } else {
                toggle.setThumbTintList(getColorStateList(R.color.service_switch_thumb));
                toggle.setTrackTintList(getColorStateList(R.color.service_switch_track));
            }
        } else if (view instanceof ImageView && isDarkCardActionIcon(view)) {
            ((ImageView) view).setImageTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor(
                            isDarkCloseIcon(view) ? "#EC221F" : "#48B2C7")));
        } else if (view instanceof TextView) {
            TextView text = (TextView) view;
            int color = text.getCurrentTextColor();
            if (darkMode && text.getId() == R.id.action_card_transition) {
                text.setTextColor(android.graphics.Color.parseColor("#48B2C7"));
            } else if (darkMode && isLightBodyText(color)) {
                text.setTextColor(getDarkModeTextColor(color));
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                applyDarkModeToView(group.getChildAt(index));
            }
            if (view.getBackground() instanceof android.graphics.drawable.ColorDrawable
                    && !(view instanceof MaterialToolbar)
                    && !(view instanceof BottomNavigationView)
                    && !(view instanceof MaterialCardView)
                    && !(view instanceof TextInputLayout)) {
                int backgroundColor = ((android.graphics.drawable.ColorDrawable) view.getBackground())
                        .getColor();
                if (darkMode && (backgroundColor == android.graphics.Color.WHITE
                        || backgroundColor == android.graphics.Color.rgb(228, 228, 228)
                        || backgroundColor == android.graphics.Color.rgb(250, 243, 252))) {
                    view.setBackgroundColor(darkSurface);
                }
            }
        }
    }

    private boolean isSearchField(TextInputLayout field) {
        return field.getId() == R.id.admin_search_field;
    }

    private void applySearchFieldColors(TextInputLayout searchField, int darkSurface) {
        int background = resolveThemeColor(R.attr.adminSearchBackground,
                android.graphics.Color.WHITE);
        int text = resolveThemeColor(R.attr.adminSearchTextColor,
                android.graphics.Color.rgb(51, 51, 51));
        int hint = resolveThemeColor(R.attr.adminSearchHintColor,
                android.graphics.Color.rgb(117, 117, 117));
        int icon = resolveThemeColor(R.attr.adminSearchIconColor,
                android.graphics.Color.rgb(70, 70, 70));

        searchField.setBoxBackgroundColor(background);
        searchField.setHintTextColor(android.content.res.ColorStateList.valueOf(hint));
        searchField.setEndIconTintList(android.content.res.ColorStateList.valueOf(icon));
        for (int index = 0; index < searchField.getChildCount(); index++) {
            View child = searchField.getChildAt(index);
            if (child instanceof TextInputEditText) {
                TextInputEditText input = (TextInputEditText) child;
                input.setTextColor(text);
                input.setHintTextColor(hint);
                input.setBackground(null);
            }
        }
    }

    private int resolveThemeColor(int attribute, int fallback) {
        android.util.TypedValue value = new android.util.TypedValue();
        if (!getTheme().resolveAttribute(attribute, value, true)) {
            return fallback;
        }
        if (value.resourceId != 0) {
            return androidx.core.content.ContextCompat.getColor(this, value.resourceId);
        }
        return value.data;
    }

    private void applyBottomNavigationColors(BottomNavigationView navigation) {
        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        };
        int[] colors = darkMode
                ? new int[]{android.graphics.Color.parseColor("#B91C1C"),
                android.graphics.Color.WHITE}
                : new int[]{android.graphics.Color.WHITE,
                android.graphics.Color.rgb(32, 32, 32)};
        android.content.res.ColorStateList tint =
                new android.content.res.ColorStateList(states, colors);
        navigation.setItemIconTintList(tint);
        navigation.setItemTextColor(tint);
        navigation.setItemActiveIndicatorColor(android.content.res.ColorStateList.valueOf(
                darkMode ? android.graphics.Color.parseColor("#7A3A171B")
                        : android.graphics.Color.parseColor("#257787")));
    }

    private boolean isLightBodyText(int color) {
        int red = android.graphics.Color.red(color);
        int green = android.graphics.Color.green(color);
        int blue = android.graphics.Color.blue(color);
        int maximum = Math.max(red, Math.max(green, blue));
        int minimum = Math.min(red, Math.min(green, blue));
        int luminance = (red * 299 + green * 587 + blue * 114) / 1000;
        return maximum - minimum <= 60 && luminance < 175;
    }

    private int getDarkModeTextColor(int originalColor) {
        if (originalColor == android.graphics.Color.rgb(117, 117, 117)
                || originalColor == android.graphics.Color.rgb(136, 136, 136)) {
            return android.graphics.Color.rgb(176, 178, 184);
        }
        return android.graphics.Color.rgb(238, 238, 238);
    }

    private boolean isDarkCardActionIcon(View view) {
        if (!darkMode || view.getId() == R.id.action_client_appointments) {
            return false;
        }
        int id = view.getId();
        return id == R.id.action_card_transition
                || id == R.id.action_edit_barber
                || id == R.id.action_edit_daily_shift
                || id == R.id.action_open_schedule
                || id == R.id.action_change_appointment
                || id == R.id.action_cancel_appointment
                || id == R.id.action_view_history_appointment
                || id == R.id.action_delete_history_appointment
                || id == R.id.action_edit_service
                || id == R.id.action_delete_service
                || id == R.id.action_edit_promotion
                || id == R.id.action_edit_benefit
                || id == R.id.action_delete_benefit
                || id == R.id.action_redeem_benefit
                || id == R.id.action_remove_benefit
                || id == R.id.action_delete_specialty
                || id == R.id.action_edit_specialty
                || id == R.id.action_edit_client;
    }

    private boolean isDarkCloseIcon(View view) {
        if (!darkMode) {
            return false;
        }
        int id = view.getId();
        return id == R.id.action_cancel_appointment
                || id == R.id.action_delete_specialty
                || id == R.id.action_remove_benefit;
    }

    void configureClientActions(int itemId) {
            View item = findViewById(itemId);
            item.setClickable(false);
            item.findViewById(R.id.action_edit_client).setOnClickListener(v ->
                    showClientForm(true));
            item.findViewById(R.id.action_client_appointments).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_citas_cliente));
        }

    void configureBenefitActions(int itemId) {
        View item = findViewById(itemId);
        item.setClickable(false);
        item.setFocusable(false);
        item.findViewById(R.id.action_redeem_benefit).setOnClickListener(v ->
                showRedeemConfirmation());
        item.findViewById(R.id.action_remove_benefit).setOnClickListener(v ->
                showDeleteBenefitConfirmation());
    }

    void configureBarberActions(int itemId) {
        View item = findViewById(itemId);
        item.findViewById(R.id.action_edit_barber).setOnClickListener(v ->
                showBarberForm(true));
    }

    void configureSpecialtyActions(int itemId) {
        View item = findViewById(itemId);
        item.findViewById(R.id.action_edit_specialty).setOnClickListener(v ->
                showSpecialtyForm());
        item.findViewById(R.id.action_delete_specialty).setOnClickListener(v ->
                createLightDialogBuilder()
                        .setTitle("Eliminar especialidad")
                        .setMessage("Â¿Deseas eliminar esta especialidad?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar", null)
                        .show());
    }

    void configureServiceActions(int itemId) {
        View item = findViewById(itemId);
        item.findViewById(R.id.action_edit_service).setOnClickListener(v -> showServiceForm());
    }

    void configurePromotionActions(int itemId, String title, String type,
                                           String catalog, boolean notificationsEnabled) {
        View item = findViewById(itemId);
        ((android.widget.TextView) item.findViewById(R.id.promotion_title)).setText(title);
        ((android.widget.TextView) item.findViewById(R.id.promotion_type)).setText("Tipo: " + type);
        ((android.widget.TextView) item.findViewById(R.id.promotion_catalog)).setText(catalog);
        android.widget.ImageView notification = item.findViewById(R.id.promotion_notification);
        notification.setImageResource(notificationsEnabled
                ? R.drawable.ic_notifications : R.drawable.ic_notifications_off);
        notification.setImageTintList(android.content.res.ColorStateList.valueOf(
                notificationsEnabled ? android.graphics.Color.rgb(213, 169, 22)
                        : android.graphics.Color.rgb(117, 117, 117)));
        item.findViewById(R.id.action_edit_promotion).setOnClickListener(v ->
                showPromotionForm());
        item.findViewById(R.id.promotion_image).setOnClickListener(v ->
                showPromotionForm());
    }

    void configureAdminBenefit(int itemId, String title, String points, boolean active) {
        View item = findViewById(itemId);
        ((android.widget.TextView) item.findViewById(R.id.benefit_title)).setText(title);
        ((android.widget.TextView) item.findViewById(R.id.benefit_cost)).setText("Costo    " + points);
        item.findViewById(R.id.action_edit_benefit).setOnClickListener(v -> showBenefitForm());
        item.findViewById(R.id.action_delete_benefit).setOnClickListener(v ->
                createLightDialogBuilder()
                        .setTitle("Eliminar beneficio")
                        .setMessage("Â¿Deseas eliminar este beneficio?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar", null)
                        .show());
        ((CompoundButton) item.findViewById(R.id.admin_benefit_active_switch)).setChecked(active);
    }

    void showBaseRateForm() {
        showStandardForm("Modificar tarifa base", "Guardar",
                new String[]{"Monto de gasto", "Define cuÃ¡nto gasto representa un punto."},
                new String[]{"1.00", ""});
    }

    void setDisabledCard(int itemId, int switchId) {
        View item = findViewById(itemId);
        if (item instanceof com.google.android.material.card.MaterialCardView) {
            ((com.google.android.material.card.MaterialCardView) item)
                    .setCardBackgroundColor(android.graphics.Color.rgb(224, 224, 224));
        }
        item.setAlpha(1f);
        applyDisabledContent(item);
        View serviceIcon = item.findViewById(R.id.service_icon);
        if (serviceIcon instanceof android.widget.ImageView) {
            android.widget.ImageView icon = (android.widget.ImageView) serviceIcon;
            icon.setBackgroundResource(R.drawable.bg_module_cuts_disabled);
            icon.setImageTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.rgb(160, 160, 160)));
            icon.setAlpha(1f);
        }
        if (switchId != -1) {
            View toggle = item.findViewById(switchId);
            if (toggle instanceof CompoundButton) {
                ((CompoundButton) toggle).setChecked(false);
            }
        }
    }

    private void applyDisabledContent(View view) {
        if (isActionIcon(view)) {
            return;
        }
        if (view instanceof android.widget.TextView) {
            android.widget.TextView textView = (android.widget.TextView) view;
            textView.setTextColor(android.graphics.Color.rgb(117, 117, 117));
            textView.setCompoundDrawableTintList(
                    android.content.res.ColorStateList.valueOf(android.graphics.Color.rgb(117, 117, 117)));
            if (textView.getBackground() != null && !isActionIcon(view)) {
                textView.setBackgroundTintList(
                        android.content.res.ColorStateList.valueOf(android.graphics.Color.rgb(190, 190, 190)));
            }
        } else if (view instanceof android.widget.ImageView) {
            view.setAlpha(0.55f);
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                applyDisabledContent(group.getChildAt(index));
            }
        }
    }

    private boolean isActionIcon(View view) {
        int id = view.getId();
        return id == R.id.action_edit_barber
                || id == R.id.action_edit_daily_shift
                || id == R.id.action_open_schedule
                || id == R.id.action_change_appointment
                || id == R.id.action_cancel_appointment
                || id == R.id.action_view_history_appointment
                || id == R.id.action_delete_history_appointment
                || id == R.id.action_edit_service
                || id == R.id.action_delete_service
                || id == R.id.action_edit_promotion
                || id == R.id.action_edit_benefit
                || id == R.id.action_delete_benefit
                || id == R.id.promotion_active_switch
                || id == R.id.promotion_image;
    }

    void configureDailyShiftActions(int itemId) {
        View item = findViewById(itemId);
        item.findViewById(R.id.action_edit_daily_shift).setOnClickListener(v ->
                showDailyShiftForm());
    }

    void configureBaseScheduleDay(int itemId, String day, String initial,
                                          String hours, String breakTime) {
        View item = findViewById(itemId);
        ((android.widget.TextView) item.findViewById(R.id.base_day_name)).setText(day);
        ((android.widget.TextView) item.findViewById(R.id.base_day_avatar)).setText(initial);
        android.widget.TextView hoursView = item.findViewById(R.id.base_day_hours);
        android.widget.TextView breakView = item.findViewById(R.id.base_day_break);
        hoursView.setText(hours);
        breakView.setText(breakTime);
        if (hours.startsWith("DÃ­a")) {
            hoursView.setTextColor(android.graphics.Color.rgb(117, 117, 117));
            hoursView.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.rgb(117, 117, 117)));
        }
        if (breakTime.startsWith("Sin")) {
            breakView.setTextColor(android.graphics.Color.rgb(117, 117, 117));
            breakView.setCompoundDrawableTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.rgb(117, 117, 117)));
        }
        item.findViewById(R.id.action_edit_daily_shift).setOnClickListener(v ->
                showDailyShiftForm());
    }

    void configureSpinner(int viewId, int textColor, String... options) {
        Spinner spinner = findViewById(viewId);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_item, options) {
            @Override
            public View getView(int position, View convertView,
                                android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof android.widget.TextView) {
                    android.widget.TextView text = (android.widget.TextView) view;
                    text.setTextColor(textColor);
                    text.setGravity(android.view.Gravity.CENTER);
                    android.graphics.drawable.Drawable arrow =
                            getDrawable(R.drawable.ic_arrow_drop_down);
                    if (arrow != null) {
                        arrow.setTint(textColor);
                        text.setCompoundDrawablesWithIntrinsicBounds(null, null, arrow, null);
                        text.setCompoundDrawablePadding(4);
                    }
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void showAppointmentFilter(boolean active) {
        showScreen(active
                ? R.layout.pantalla_citas_cliente
                : R.layout.pantalla_historial_citas_cliente);
    }

    void showWeekChangeDialog(int direction) {
        if (selectedWeekStart == null) {
            selectedWeekStart = getWeekStart(java.util.Calendar.getInstance());
        }
        selectedWeekStart.add(java.util.Calendar.DAY_OF_MONTH, direction * 7);
        updateWeekSelector();
    }

    void showWeekCalendar() {
        java.util.Calendar today = java.util.Calendar.getInstance();
        android.widget.DatePicker datePicker = new android.widget.DatePicker(this);
        datePicker.init(
                today.get(java.util.Calendar.YEAR),
                today.get(java.util.Calendar.MONTH),
                today.get(java.util.Calendar.DAY_OF_MONTH),
                null);

        androidx.appcompat.app.AlertDialog picker = new MaterialAlertDialogBuilder(this)
                .setTitle("Seleccionar semana")
                .setView(datePicker)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    java.util.Calendar selectedDate = java.util.Calendar.getInstance();
                    selectedDate.set(
                            datePicker.getYear(),
                            datePicker.getMonth(),
                            datePicker.getDayOfMonth());
                    selectedWeekStart = getWeekStart(selectedDate);
                    updateWeekSelector();
                })
                .create();
        picker.setOnShowListener(dialog -> setPickerWindowWidth(picker));
        picker.show();
    }

    private java.util.Calendar getWeekStart(java.util.Calendar date) {
        java.util.Calendar weekStart = (java.util.Calendar) date.clone();
        weekStart.set(java.util.Calendar.HOUR_OF_DAY, 0);
        weekStart.set(java.util.Calendar.MINUTE, 0);
        weekStart.set(java.util.Calendar.SECOND, 0);
        weekStart.set(java.util.Calendar.MILLISECOND, 0);
        int day = weekStart.get(java.util.Calendar.DAY_OF_WEEK);
        int daysFromMonday = (day + 5) % 7;
        weekStart.add(java.util.Calendar.DAY_OF_MONTH, -daysFromMonday);
        return weekStart;
    }

    private void updateWeekSelector() {
        if (selectedWeekStart == null) {
            return;
        }
        java.util.Calendar weekEnd = (java.util.Calendar) selectedWeekStart.clone();
        weekEnd.add(java.util.Calendar.DAY_OF_MONTH, 6);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                "dd/MM", java.util.Locale.getDefault());
        android.widget.TextView weekLabel = findViewById(R.id.action_select_week);
        if (weekLabel != null) {
            weekLabel.setText("Sem " + format.format(selectedWeekStart.getTime())
                    + "-" + format.format(weekEnd.getTime()));
        }
    }

    void showClientForm(boolean editing) {
        showStandardForm(editing ? "Datos del Cliente" : "Ingresar Nuevo Cliente", "Guardar",
                new String[]{"Nombres", "Apellidos", "Tipo Documento", "Numero Doc.",
                        "Correo Electronico", "Numero Celular"},
                editing ? new String[]{"Juan Carlos", "Canales", "DUI", "000000-0",
                        "admin@gmail.com", "77223344"} : null);
    }

    void showBarberForm(boolean editing) {
        showStandardForm(editing ? "Datos del Barbero" : "Ingresar Nuevo Barbero", "Guardar",
                new String[]{"Nombres", "Apellidos", "Tipo Documento", "Numero Doc.",
                        "Correo Electronico", "Numero Celular", "ContraseÃ±a", "Especialidad"},
                editing ? new String[]{"Jose Carlos", "Canales", "DUI", "000000-0",
                        "Example@ex...", "77223344", "**************", "Corte"} : null);
    }

    void showSpecialtyForm() {
        showStandardForm("Ingresar Especialidad", "Guardar",
                new String[]{"Especialidad"}, null);
    }

    void showBaseShiftForm() {
        showStandardForm("Administracion Turnos Base", "Guardar",
                new String[]{"Apertura", "Cierre", "Inicio Receso", "Fin Receso",
                        "Horario Apertura", "Horario Cierre"},
                new String[]{"9:00 a.m.", "7:00 p.m.", "11:00 a.m.", "12:00 p.m.",
                        "9:00 a.m. - 2:15 p.m.", "1:45 p.m. - 7:00 p.m."});
    }

    private void showDailyShiftForm() {
        showStandardForm("Editar jornada", "Guardar",
                new String[]{"Fecha", "Inicio", "Fin", "Inicio Receso", "Fin Receso"},
                new String[]{"15/09/2026", "9:00 a.m.", "7:00 p.m.",
                        "11:00 a.m.", "12:00 p.m."});
    }

    void showServiceForm() {
        showStandardForm("Ingresar Nuevo Servicio", "Guardar Servicio",
                new String[]{"Nombre del Servicio", "Precio ($)", "DuraciÃ³n (min)", "CategorÃ­a"},
                null);
    }

    void showBaseScheduleForm() {
        showScreen(R.layout.pantalla_configuracion_horario_base);
    }

    void showPromotionForm() {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_promotion, null);
        SwitchMaterial notificationSwitch =
                content.findViewById(R.id.modal_promotion_notification_switch);
        View notificationSection =
                content.findViewById(R.id.modal_promotion_notification_section);
        View notificationFields =
                content.findViewById(R.id.modal_promotion_notification_fields);
        ImageView notificationIcon =
                content.findViewById(R.id.modal_promotion_notification_icon);
        ImageView promotionImage = content.findViewById(R.id.modal_promotion_image);
        View promotionImageContainer = content.findViewById(R.id.modal_promotion_image_container);
        promotionImageTarget = promotionImage;
        View.OnClickListener imagePickerClick = view -> promotionImagePicker.launch("image/*");
        promotionImage.setOnClickListener(imagePickerClick);
        promotionImageContainer.setOnClickListener(imagePickerClick);
        Spinner typeSpinner = content.findViewById(R.id.modal_promotion_type);
        configureModalSpinnerAppearance(typeSpinner);
        TextView valueLabel = content.findViewById(R.id.modal_promotion_value_label);
        android.widget.FrameLayout valueContainer =
                content.findViewById(R.id.modal_promotion_value_container);
        EditText valueInput = content.findViewById(R.id.modal_promotion_value);
        EditText promotionStart = content.findViewById(R.id.modal_promotion_start);
        EditText promotionEnd = content.findViewById(R.id.modal_promotion_end);
        configureModalDateField(modalContext, promotionStart);
        configureModalDateField(modalContext, promotionEnd);

        ArrayAdapter<String> typeAdapter = createModalSpinnerAdapter(modalContext,
                new String[]{"Descuento", "CatÃ¡logo"});
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                    int position, long id) {
                    boolean catalog = position == 1;
                    valueLabel.setText(catalog ? "CatÃ¡logo" : "Porcentaje %");
                    if (catalog) {
                        valueInput.setVisibility(View.GONE);
                        for (int index = valueContainer.getChildCount() - 1; index > 0; index--) {
                            valueContainer.removeViewAt(index);
                        }
                        Spinner serviceSpinner = new Spinner(modalContext);
                        configureModalSpinnerAppearance(serviceSpinner);
                        serviceSpinner.setAdapter(createModalSpinnerAdapter(modalContext,
                                new String[]{"Seleccionar servicio", "Corte de Cabello",
                                        "Combo Barba", "Corte y barba"}));
                        serviceSpinner.setPrompt("Seleccione un servicio");
                        serviceSpinner.setContentDescription("Selector de servicio");
                        valueContainer.addView(serviceSpinner, new android.widget.FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT));
                    } else {
                        valueInput.setVisibility(View.VISIBLE);
                        for (int index = valueContainer.getChildCount() - 1; index > 0; index--) {
                            valueContainer.removeViewAt(index);
                        }
                    }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            applyPromotionNotificationState(notificationFields, notificationIcon, isChecked);
        });
        applyPromotionNotificationState(notificationFields, notificationIcon,
                notificationSwitch.isChecked());

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(modalContext)
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(android.graphics.Color.WHITE);
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setBackgroundColor(android.graphics.Color.parseColor("#287F8D"));
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(android.graphics.Color.WHITE);
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)
                    .setBackgroundColor(android.graphics.Color.parseColor("#C51B20"));
        });
        dialog.show();
    }

    private void applyPromotionNotificationState(View notificationFields,
            ImageView notificationIcon, boolean enabled) {
        notificationFields.setVisibility(enabled ? View.VISIBLE : View.GONE);
        notificationIcon.setImageResource(enabled
                ? R.drawable.ic_notifications : R.drawable.ic_notifications_off);
        notificationIcon.setColorFilter(android.graphics.Color.parseColor(
                enabled ? "#D5A916" : "#8C8C8C"));
    }

    void showBenefitForm() {
        showStandardForm("Asignar Beneficio", "Guardar Servicio",
                new String[]{"CategorÃ­a", "Selecciona Servicio", "Coste Pts"},
                null);
    }

    interface ProfileUpdateListener {
        void onSave(String names, String lastNames, long documentId, String document,
                    String email, String phone, String birthDate);
    }

    void showProfileForm(java.util.List<ues.edu.sv.proyect_bs.modelos.TipoDocumento> documents,
            ProfileUpdateListener listener) {
        AuthApiClient.AdminProfile profile = loadedAdminProfile;
        if (profile == null) {
            android.widget.Toast.makeText(this,
                    "Espera a que carguen los datos del perfil.",
                    android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        ((TextView) content.findViewById(R.id.modal_admin_title))
                .setText((profile.names + " " + profile.lastNames).trim());
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);
        String[] labels = {"Nombres", "Apellidos", "Tipo Documento", "Numero Doc.",
                "Correo Electronico", "Numero Celular", "Fecha Nacimiento"};
        String[] values = {profile.names, profile.lastNames, "", profile.document,
                profile.email, profile.phone, toDisplayDate(profile.birthDate)};
        EditText[] inputs = new EditText[labels.length];
        final Spinner[] documentSpinner = {null};
        for (int i = 0; i < labels.length; i++) {
            TextView label = new TextView(modalContext);
            label.setText(labels[i]);
            label.setTextColor(android.graphics.Color.parseColor("#263248"));
            label.setTextSize(14);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(8);
            fields.addView(label, labelParams);
            if (i == 2) {
                documentSpinner[0] = new Spinner(modalContext);
                configureModalSpinnerAppearance(documentSpinner[0]);
                String[] options = new String[documents.size() + 1];
                options[0] = "Seleccionar tipo documento";
                for (int index = 0; index < documents.size(); index++) {
                    options[index + 1] = documents.get(index).nombredocumento;
                }
                documentSpinner[0].setAdapter(createModalSpinnerAdapter(modalContext, options));
                documentSpinner[0].setPrompt("Seleccione tipo documento");
                documentSpinner[0].setContentDescription("Selector de tipo documento");
                int selected = 0;
                for (int index = 0; index < documents.size(); index++) {
                    if (documents.get(index).iddocumento == profile.documentId) {
                        selected = index + 1;
                        break;
                    }
                }
                documentSpinner[0].setSelection(selected);
                fields.addView(documentSpinner[0], new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
            } else {
                inputs[i] = (EditText) createModalField(
                        modalContext, labels[i], values[i], false);
                if (i == 0 || i == 1) {
                    configureNameInput(inputs[i]);
                } else if (i == 4) {
                    inputs[i].setEnabled(false);
                    inputs[i].setAlpha(0.65f);
                } else if (i == 5) {
                    configurePhoneInput(inputs[i]);
                }
                fields.addView(inputs[i], new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
            }
        }
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(modalContext)
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(android.graphics.Color.WHITE);
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setBackgroundColor(android.graphics.Color.parseColor("#287F8D"));
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(android.graphics.Color.WHITE);
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)
                    .setBackgroundColor(android.graphics.Color.parseColor("#C51B20"));
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        int selected = documentSpinner[0] == null
                                ? 0 : documentSpinner[0].getSelectedItemPosition() - 1;
                        if (selected < 0 || selected >= documents.size()) {
                            android.widget.Toast.makeText(MainActivity.this,
                                    "Selecciona un tipo de documento válido.",
                                    android.widget.Toast.LENGTH_LONG).show();
                            return;
                        }
                        long documentId = selected >= 0 && selected < documents.size()
                                ? documents.get(selected).iddocumento : 0L;
                        String names = inputs[0].getText().toString().trim();
                        String lastNames = inputs[1].getText().toString().trim();
                        String phone = inputs[5].getText().toString().trim();
                        String displayBirthDate = inputs[6].getText().toString().trim();
                        if (!isValidName(names)) {
                            inputs[0].setError("Usa sólo letras y no lo dejes vacío.");
                            return;
                        }
                        if (!isValidName(lastNames)) {
                            inputs[1].setError("Usa sólo letras y no lo dejes vacío.");
                            return;
                        }
                        String document = inputs[3].getText().toString().trim();
                        if (document.length() < 8) {
                            inputs[3].setError(
                                    "El identificador es obligatorio y debe tener al menos 8 caracteres.");
                            return;
                        }
                        if (!phone.matches("\\d{8}")) {
                            inputs[5].setError("Debe contener exactamente 8 números.");
                            return;
                        }
                        if (!isAtLeastSixteen(displayBirthDate)) {
                            showFieldError(inputs[6], "La edad mínima es de 16 años.");
                            return;
                        }
                        listener.onSave(names, lastNames, documentId,
                                document,
                                inputs[4].getText().toString().trim(),
                                phone, toApiDate(displayBirthDate));
                        dialog.dismiss();
                    });
        });
        dialog.show();
    }

    private void configureNameInput(EditText input) {
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        input.setFilters(new android.text.InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            StringBuilder accepted = new StringBuilder();
            for (int index = start; index < end; index++) {
                char character = source.charAt(index);
                if (Character.isLetter(character) || Character.isWhitespace(character)) {
                    accepted.append(character);
                }
            }
            String result = accepted.toString();
            return result.equals(source.subSequence(start, end).toString()) ? null : result;
        }});
    }

    private void configurePhoneInput(EditText input) {
        input.setInputType(android.text.InputType.TYPE_CLASS_PHONE);
        input.setFilters(new android.text.InputFilter[]{
                new android.text.InputFilter.LengthFilter(8),
                (source, start, end, dest, dstart, dend) -> {
                    StringBuilder accepted = new StringBuilder();
                    for (int index = start; index < end; index++) {
                        if (Character.isDigit(source.charAt(index))) {
                            accepted.append(source.charAt(index));
                        }
                    }
                    String result = accepted.toString();
                    return result.equals(source.subSequence(start, end).toString()) ? null : result;
                }
        });
    }

    private void showFieldError(EditText field, String message) {
        android.graphics.drawable.Drawable errorIcon =
                getDrawable(android.R.drawable.ic_dialog_alert);
        field.setError(message, errorIcon);
        field.requestFocus();
    }

    private boolean isValidName(String value) {
        return !value.isEmpty() && value.matches("[\\p{L}]+(?:\\s[\\p{L}]+)*");
    }

    private boolean isAtLeastSixteen(String value) {
        if (value.isEmpty()) {
            return false;
        }
        try {
            java.util.Date birthDate = parseProfileDate(value);
            if (birthDate == null) {
                return false;
            }
            java.util.Calendar minimum = java.util.Calendar.getInstance();
            minimum.set(java.util.Calendar.HOUR_OF_DAY, 0);
            minimum.set(java.util.Calendar.MINUTE, 0);
            minimum.set(java.util.Calendar.SECOND, 0);
            minimum.set(java.util.Calendar.MILLISECOND, 0);
            minimum.add(java.util.Calendar.YEAR, -16);
            java.util.Calendar birth = java.util.Calendar.getInstance();
            birth.setTime(birthDate);
            birth.set(java.util.Calendar.HOUR_OF_DAY, 0);
            birth.set(java.util.Calendar.MINUTE, 0);
            birth.set(java.util.Calendar.SECOND, 0);
            birth.set(java.util.Calendar.MILLISECOND, 0);
            return !birth.after(minimum);
        } catch (Exception exception) {
            return false;
        }
    }

    private java.util.Date parseProfileDate(String value) {
        String[] patterns = {"dd/MM/yyyy", "yyyy-MM-dd"};
        for (String pattern : patterns) {
            try {
                java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                        pattern, java.util.Locale.ROOT);
                format.setLenient(false);
                java.text.ParsePosition position = new java.text.ParsePosition(0);
                java.util.Date parsed = format.parse(value, position);
                if (parsed != null && position.getIndex() == value.length()) {
                    return parsed;
                }
            } catch (Exception ignored) {
                // Probar el siguiente formato permitido.
            }
        }
        return null;
    }

    private String toDisplayDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        try {
            java.text.SimpleDateFormat api = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd", java.util.Locale.ROOT);
            java.text.SimpleDateFormat display = new java.text.SimpleDateFormat(
                    "dd/MM/yyyy", java.util.Locale.getDefault());
            return display.format(api.parse(value));
        } catch (Exception ignored) {
            return value;
        }
    }

    private String toApiDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        try {
            java.text.SimpleDateFormat display = new java.text.SimpleDateFormat(
                    "dd/MM/yyyy", java.util.Locale.getDefault());
            java.text.SimpleDateFormat api = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd", java.util.Locale.ROOT);
            return api.format(display.parse(value));
        } catch (Exception ignored) {
            return value;
        }
    }

    void setLoadedAdminProfile(AuthApiClient.AdminProfile profile) {
        loadedAdminProfile = profile;
    }

    AuthApiClient.AdminProfile getLoadedAdminProfile() {
        return loadedAdminProfile;
    }

    void showPasswordForm() {
        showStandardForm("Cambio ContraseÃ±a", "Guardar",
                new String[]{"ContraseÃ±a Actual", "Nueva ContraseÃ±a"},
                null);
    }

    void showAppointmentForm() {
        showStandardForm("Nueva cita", "Guardar",
                new String[]{"CategorÃ­a Servicio", "Barbero", "Servicio", "Inicio", "Hora"},
                null);
    }

    private void showRedeemConfirmation() {
        createLightDialogBuilder()
                .setTitle("Confirmar consumo")
                .setMessage("Â¿Deseas confirmar el consumo de este beneficio?\n\nSe descontarÃ¡n los puntos correspondientes al cliente.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", null)
                .show();
    }

    private void showDeleteBenefitConfirmation() {
        createLightDialogBuilder()
                .setTitle("Eliminar beneficio")
                .setMessage("Â¿Deseas eliminar este beneficio de la lista disponible?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", null)
                .show();
    }

    private void showStandardForm(String title, String positiveText,
            String[] labels, String[] values) {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        TextView titleView = content.findViewById(R.id.modal_admin_title);
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);
        boolean specialtyAsSelection = !title.equalsIgnoreCase("Ingresar Especialidad");
        titleView.setText(title);
        for (int i = 0; i < labels.length; i++) {
            TextView label = new TextView(modalContext);
            label.setText(labels[i]);
            label.setTextColor(android.graphics.Color.parseColor("#263248"));
            label.setTextSize(14);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            labelParams.topMargin = dp(8);
            fields.addView(label, labelParams);

            String value = values != null && i < values.length ? values[i] : null;
            View field = createModalField(
                    modalContext, labels[i], value, specialtyAsSelection);
            LinearLayout.LayoutParams fieldParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(44));
            fields.addView(field, fieldParams);
        }
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(modalContext)
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton(positiveText, null)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(android.graphics.Color.WHITE);
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setBackgroundColor(android.graphics.Color.parseColor("#287F8D"));
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)
                    .setTextColor(android.graphics.Color.WHITE);
            dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE)
                    .setBackgroundColor(android.graphics.Color.parseColor("#C51B20"));
        });
        dialog.show();
    }

    private View createModalField(android.content.Context context, String label, String value,
            boolean specialtyAsSelection) {
        if (isModalSelection(label, specialtyAsSelection)) {
            Spinner spinner = new Spinner(context);
            configureModalSpinnerAppearance(spinner);
            String[] values = getModalOptions(label);
            String[] options = new String[values.length + 1];
            options[0] = "Seleccionar " + label.toLowerCase(java.util.Locale.ROOT);
            System.arraycopy(values, 0, options, 1, values.length);
            ArrayAdapter<String> adapter = createModalSpinnerAdapter(context, options);
            spinner.setAdapter(adapter);
            spinner.setPrompt("Seleccione " + label.toLowerCase(java.util.Locale.ROOT));
            spinner.setContentDescription("Selector de " + label.toLowerCase(java.util.Locale.ROOT));
            spinner.setSelection(0);
            if (value != null) {
                for (int index = 0; index < values.length; index++) {
                    if (values[index].equalsIgnoreCase(value)) {
                        spinner.setSelection(index + 1);
                        break;
                    }
                }
            }
            return spinner;
        }

        EditText field = new EditText(context);
        field.setSingleLine(true);
        field.setTextSize(16);
        field.setHint(getModalHint(label));
        field.setInputType(getModalInputType(label));
        field.setBackgroundResource(R.drawable.bg_admin_modal_field);
        if (isPasswordField(label)) {
            configurePasswordField(field, context);
        }
        if (isDateField(label) || isTimeField(label)) {
            field.setFocusableInTouchMode(true);
            field.setClickable(true);
            field.setOnClickListener(v -> {
                if (isDateField(label)) {
                    showModalDatePicker(context, field);
                } else {
                    showModalTimePicker(context, field);
                }
            });
        }
        if (value != null && !value.isEmpty()) {
            field.setText(value);
        }
        return field;
    }

    private boolean isPasswordField(String label) {
        return label.toLowerCase(java.util.Locale.ROOT).contains("contraseÃ±a");
    }

    private void configurePasswordField(EditText field, android.content.Context context) {
        final boolean[] passwordVisible = {false};
        field.setTransformationMethod(
                android.text.method.PasswordTransformationMethod.getInstance());
        field.setPadding(dp(12), 0, dp(44), 0);
        updatePasswordVisibilityIcon(field, context, passwordVisible[0]);
        field.setContentDescription("ContraseÃ±a oculta. Tocar el icono para mostrarla");
        field.setOnTouchListener((view, event) -> {
            boolean inVisibilityArea = event.getX() >= field.getWidth() - dp(60);
            if (!inVisibilityArea) {
                return false;
            }
            if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                passwordVisible[0] = !passwordVisible[0];
                int cursorPosition = Math.max(0, field.getSelectionStart());
                field.setTransformationMethod(passwordVisible[0]
                        ? android.text.method.HideReturnsTransformationMethod.getInstance()
                        : android.text.method.PasswordTransformationMethod.getInstance());
                field.setSelection(Math.min(cursorPosition, field.length()));
                updatePasswordVisibilityIcon(field, context, passwordVisible[0]);
                field.setContentDescription(passwordVisible[0]
                        ? "ContraseÃ±a visible. Tocar el icono para ocultarla"
                        : "ContraseÃ±a oculta. Tocar el icono para mostrarla");
            }
            return true;
        });
    }

    private void updatePasswordVisibilityIcon(EditText field,
            android.content.Context context, boolean passwordVisible) {
        int iconId = passwordVisible ? R.drawable.ic_visibility_off : R.drawable.ic_visibility;
        android.graphics.drawable.Drawable icon = context.getDrawable(iconId);
        if (icon != null) {
            icon.setTint(android.graphics.Color.parseColor("#C51B20"));
            field.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        }
    }

    private void configureModalSpinnerAppearance(Spinner spinner) {
        spinner.setBackgroundResource(R.drawable.bg_admin_modal_spinner);
        spinner.setPadding(dp(12), 0, dp(36), 0);
        spinner.setGravity(android.view.Gravity.CENTER_VERTICAL);
        spinner.setDropDownHorizontalOffset(0);
        spinner.post(() -> spinner.setDropDownWidth(spinner.getWidth()));
    }

    private ArrayAdapter<String> createModalSpinnerAdapter(
            android.content.Context context, String[] options) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, options) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) {
                    TextView text = (TextView) view;
                    text.setTextColor(android.graphics.Color.parseColor("#263248"));
                    text.setGravity(android.view.Gravity.CENTER_VERTICAL);
                    android.graphics.drawable.Drawable arrow =
                            context.getDrawable(R.drawable.ic_arrow_drop_down);
                    if (arrow != null) {
                        arrow.setTint(android.graphics.Color.parseColor("#263248"));
                        text.setCompoundDrawablesWithIntrinsicBounds(null, null, arrow, null);
                        text.setCompoundDrawablePadding(dp(4));
                    }
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    private boolean isDateField(String label) {
        String normalized = label.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("fecha") || normalized.equals("inicio");
    }

    private boolean isTimeField(String label) {
        String normalized = label.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("hora") || normalized.contains("apertura")
                || normalized.contains("cierre") || normalized.contains("receso")
                || normalized.equals("fin");
    }

    private void showModalDatePicker(android.content.Context context, EditText target) {
        java.util.Calendar now = java.util.Calendar.getInstance();
        android.widget.DatePicker datePicker = new android.widget.DatePicker(context);
        datePicker.init(
                now.get(java.util.Calendar.YEAR),
                now.get(java.util.Calendar.MONTH),
                now.get(java.util.Calendar.DAY_OF_MONTH),
                null);
        expandDatePickerHeader(datePicker);
        datePicker.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        androidx.appcompat.app.AlertDialog picker = new MaterialAlertDialogBuilder(context)
                .setTitle("Seleccionar fecha")
                .setView(datePicker, 0, 0, 0, 0)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (dialog, which) -> target.setText(String.format(
                        java.util.Locale.getDefault(),
                        "%02d/%02d/%04d",
                        datePicker.getDayOfMonth(),
                        datePicker.getMonth() + 1,
                        datePicker.getYear())))
                .create();
        picker.setOnShowListener(dialog -> {
            android.view.Window window = picker.getWindow();
            if (window != null) {
                setPickerWindowWidth(picker);
            }
            datePicker.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            expandDatePickerHeader(datePicker);
            datePicker.requestLayout();
        });
        picker.show();
    }

    private void showModalTimePicker(android.content.Context context, EditText target) {
        java.util.Calendar now = java.util.Calendar.getInstance();
        android.widget.TimePicker timePicker = new android.widget.TimePicker(context);
        timePicker.setIs24HourView(false);
        timePicker.setHour(now.get(java.util.Calendar.HOUR_OF_DAY));
        timePicker.setMinute(now.get(java.util.Calendar.MINUTE));
        removePickerHorizontalPadding(timePicker);
        timePicker.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        androidx.appcompat.app.AlertDialog picker = new MaterialAlertDialogBuilder(context)
                .setTitle("Seleccionar hora")
                .setView(timePicker, 0, 0, 0, 0)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Aceptar", (dialog, which) -> {
                    java.util.Calendar selected = java.util.Calendar.getInstance();
                    selected.set(java.util.Calendar.HOUR_OF_DAY, timePicker.getHour());
                    selected.set(java.util.Calendar.MINUTE, timePicker.getMinute());
                    target.setText(new java.text.SimpleDateFormat(
                            "hh:mm a", java.util.Locale.getDefault()).format(selected.getTime()));
                })
                .create();
        picker.setOnShowListener(dialog -> {
            android.view.Window window = picker.getWindow();
            if (window != null) {
                setPickerWindowWidth(picker);
            }
            timePicker.getLayoutParams().width = ViewGroup.LayoutParams.MATCH_PARENT;
            removePickerHorizontalPadding(timePicker);
            timePicker.requestLayout();
        });
        picker.show();
    }

    private void setPickerWindowWidth(androidx.appcompat.app.AlertDialog picker) {
        android.view.Window window = picker.getWindow();
        if (window != null) {
            int availableWidth = getResources().getDisplayMetrics().widthPixels - dp(32);
            window.setLayout(
                    availableWidth,
                    android.view.WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    private void removePickerHorizontalPadding(View view) {
        view.setPadding(0, view.getPaddingTop(), 0, view.getPaddingBottom());
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                removePickerHorizontalPadding(group.getChildAt(index));
            }
        }
    }

    private void expandDatePickerHeader(android.widget.DatePicker datePicker) {
        String[] headerIds = {
                "date_picker_header",
                "date_picker_header_container"
        };
        for (String idName : headerIds) {
            int id = datePicker.getResources().getIdentifier(idName, "id", "android");
            if (id != 0) {
                View header = datePicker.findViewById(id);
                if (header != null) {
                    ViewGroup.LayoutParams params = header.getLayoutParams();
                    if (params != null) {
                        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                        header.setLayoutParams(params);
                    }
                    header.setPadding(0, header.getPaddingTop(), 0, header.getPaddingBottom());
                }
            }
        }
    }

    private void configureModalDateField(android.content.Context context, EditText field) {
        field.setFocusable(false);
        field.setClickable(true);
        field.setInputType(android.text.InputType.TYPE_NULL);
        field.setContentDescription("Seleccionar fecha");
        field.setOnClickListener(v -> showModalDatePicker(context, field));
    }

    private boolean isModalSelection(String label, boolean specialtyAsSelection) {
        String normalized = label.toLowerCase(java.util.Locale.ROOT);
        return normalized.contains("tipo documento")
                || normalized.contains("categorÃ­a")
                || normalized.equals("barbero")
                || normalized.equals("servicio")
                || normalized.contains("selecciona servicio")
                || (specialtyAsSelection && normalized.equals("especialidad"));
    }

    private String[] getModalOptions(String label) {
        String normalized = label.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("tipo documento")) {
            return new String[]{"DUI", "Pasaporte", "Carnet de residente"};
        }
        if (normalized.equals("especialidad")) {
            return new String[]{"Corte", "Barba", "Corte y barba"};
        }
        if (normalized.equals("barbero")) {
            return new String[]{"Jose Canales", "Juan Carlos Canales", "Carlos Menjivar"};
        }
        if (normalized.equals("servicio")) {
            return new String[]{"Corte de Cabello", "Combo Barba", "Corte y barba"};
        }
        return new String[]{"Corte", "Barba", "Combo"};
    }

    private String getModalHint(String label) {
        String normalized = label.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("correo")) {
            return "ejemplo@correo.com";
        }
        if (normalized.contains("contraseÃ±a")) {
            return "Ingrese una contraseÃ±a";
        }
        if (normalized.contains("precio") || normalized.contains("coste")) {
            return "0.00";
        }
        if (normalized.contains("duraciÃ³n")) {
            return "Ej. 30";
        }
        if (normalized.contains("fecha")) {
            return "dd/mm/aaaa";
        }
        if (normalized.contains("hora") || normalized.contains("apertura")
                || normalized.contains("cierre") || normalized.contains("receso")
                || normalized.equals("inicio") || normalized.equals("fin")) {
            return "hh:mm a.m./p.m.";
        }
        if (normalized.contains("nombres")) {
            return "Ingrese los nombres";
        }
        if (normalized.contains("apellidos")) {
            return "Ingrese los apellidos";
        }
        return "Ingrese " + label.toLowerCase(java.util.Locale.ROOT);
    }

    private int getModalInputType(String label) {
        String normalized = label.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("correo")) {
            return android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
        }
        if (normalized.contains("contraseÃ±a")) {
            return android.text.InputType.TYPE_CLASS_TEXT
                    | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD;
        }
        if (normalized.contains("precio") || normalized.contains("coste")) {
            return android.text.InputType.TYPE_CLASS_NUMBER
                    | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL;
        }
        if (normalized.contains("duraciÃ³n")) {
            return android.text.InputType.TYPE_CLASS_NUMBER;
        }
        if (normalized.contains("numero doc") || normalized.contains("celular")) {
            return android.text.InputType.TYPE_CLASS_PHONE;
        }
        if (normalized.contains("fecha")) {
            return android.text.InputType.TYPE_CLASS_DATETIME
                    | android.text.InputType.TYPE_DATETIME_VARIATION_DATE;
        }
        if (normalized.contains("hora") || normalized.contains("apertura")
                || normalized.contains("cierre") || normalized.contains("receso")
                || normalized.equals("fin")) {
            return android.text.InputType.TYPE_CLASS_DATETIME
                    | android.text.InputType.TYPE_DATETIME_VARIATION_TIME;
        }
        return android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_NORMAL;
    }

    private android.content.Context createLightModalContext() {
        return new android.view.ContextThemeWrapper(this, R.style.Theme_AdminModalLight);
    }

    MaterialAlertDialogBuilder createLightDialogBuilder() {
        return new MaterialAlertDialogBuilder(createLightModalContext());
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout createForm() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (20 * getResources().getDisplayMetrics().density);
        form.setPadding(padding, 0, padding, 0);
        return form;
    }

    private void addField(LinearLayout form, String hint, String value) {
        EditText field = new EditText(this);
        field.setHint(hint);
        field.setText(value);
        form.addView(field, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
    }
}
