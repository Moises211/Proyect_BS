package ues.edu.sv.proyect_bs;

import android.os.Bundle;
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

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {
    private java.util.Calendar selectedWeekStart;
    private int selectedMenuItemId = R.id.menu_admin_home;
    private boolean syncingBottomNavigation;
    private int currentLayout = R.layout.pantalla_inicial;
    private boolean darkMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        darkMode = getPreferences(MODE_PRIVATE).getBoolean("dark_mode", false);
        setContentView(R.layout.pantalla_inicial);
        configureNavigation();
        syncBottomNavigation(R.layout.pantalla_inicial);
        configureHome();
        applyDarkModeToCurrentScreen();
    }

    private void configureHome() {
        configureMenuToolbar(R.id.admin_top_app_bar);
        findViewById(R.id.module_cuts_services).setOnClickListener(v ->
                showScreen(R.layout.pantalla_cortes_servicios));
        findViewById(R.id.module_client_control).setOnClickListener(v ->
                showScreen(R.layout.pantalla_control_citas));
        findViewById(R.id.module_user_profile).setOnClickListener(v ->
                showScreen(R.layout.pantalla_perfil_usuario));
        findViewById(R.id.module_staff_shifts).setOnClickListener(v ->
                showScreen(R.layout.pantalla_personal_turnos));
    }

    private void showScreen(int layout) {
        currentLayout = layout;
        selectedMenuItemId = getMenuItemForLayout(layout);
        setContentView(layout);
        syncBottomNavigation(layout);
        configureNavigation();

        if (layout == R.layout.pantalla_inicial) {
            configureHome();
        } else if (layout == R.layout.pantalla_control_citas) {
            configureMenuToolbar(R.id.toolbar_control_citas);
            findViewById(R.id.card_client_directory).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_clientes));
            findViewById(R.id.card_benefits).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_beneficios));
        } else if (layout == R.layout.pantalla_personal_turnos) {
            configureMenuToolbar(R.id.toolbar_personal_turnos);
            findViewById(R.id.card_staff_barbers).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_barberos));
            findViewById(R.id.card_staff_schedule).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_cronograma_barberos));
            findViewById(R.id.card_staff_specialties).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_especialidades));
        } else if (layout == R.layout.pantalla_cortes_servicios) {
            MaterialToolbar toolbar = findViewById(R.id.toolbar_admin_menu);
            toolbar.setSubtitle("Cortes y Servicios");
            configureMenuToolbar(R.id.toolbar_admin_menu);
            findViewById(R.id.card_admin_services).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_administracion_servicios));
            findViewById(R.id.card_service_hours).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_horarios_servicio));
            findViewById(R.id.card_promotions).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_promociones));
            findViewById(R.id.card_admin_benefits).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_beneficios_admin));
        } else if (layout == R.layout.pantalla_administracion_servicios) {
            findViewById(R.id.toolbar_services).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_cortes_servicios));
            findViewById(R.id.action_add_service).setOnClickListener(v -> showServiceForm());
            configureSpinner(R.id.service_status_spinner, android.graphics.Color.DKGRAY,
                    "Todos", "Activos", "Inactivos");
            configureServiceActions(R.id.service_item_one);
            configureServiceActions(R.id.service_item_two);
            configureServiceActions(R.id.service_item_three);
            setDisabledCard(R.id.service_item_three, R.id.service_active_switch);
        } else if (layout == R.layout.pantalla_horarios_servicio) {
            findViewById(R.id.toolbar_service_hours).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_cortes_servicios));
            findViewById(R.id.action_previous_week).setOnClickListener(v ->
                    showWeekChangeDialog(-1));
            findViewById(R.id.action_select_week).setOnClickListener(v ->
                    showWeekCalendar());
            findViewById(R.id.week_days_summary).setOnClickListener(v ->
                    showWeekCalendar());
            findViewById(R.id.action_next_week).setOnClickListener(v ->
                    showWeekChangeDialog(1));
            findViewById(R.id.action_service_shifts).setOnClickListener(v ->
                    showBaseShiftForm());
            findViewById(R.id.action_base_schedule).setOnClickListener(v ->
                    showBaseScheduleForm());
            configureDailyShiftActions(R.id.service_daily_shift_one);
            configureDailyShiftActions(R.id.service_daily_shift_two);
            configureDailyShiftActions(R.id.service_daily_shift_three);
            configureDailyShiftActions(R.id.service_daily_shift_four);
            configureDailyShiftActions(R.id.service_daily_shift_five);
            configureDailyShiftActions(R.id.service_daily_shift_six);
            setDisabledCard(R.id.service_daily_shift_two, R.id.daily_shift_active);
        } else if (layout == R.layout.pantalla_configuracion_horario_base) {
            findViewById(R.id.toolbar_base_schedule).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_horarios_servicio));
            findViewById(R.id.action_base_schedule_cancel).setOnClickListener(v ->
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Cancelar configuración")
                            .setMessage("¿Deseas cancelar la configuración del horario base?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Sí, cancelar",
                                    (dialog, which) -> showScreen(R.layout.pantalla_horarios_servicio))
                            .show());
            findViewById(R.id.action_schedule_save).setOnClickListener(v ->
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Guardar horario base")
                            .setMessage("¿Deseas guardar esta configuración como horario base?")
                            .setNegativeButton("No", null)
                            .setPositiveButton("Guardar", null)
                            .show());
            configureBaseScheduleDay(R.id.base_day_one, "Lunes", "L",
                    "9:00 a.m. - 7:00 p.m.", "11:00 a.m. - 12:00 p.m.");
            configureBaseScheduleDay(R.id.base_day_two, "Martes", "M",
                    "Día de Descanso", "Sin Receso");
            configureBaseScheduleDay(R.id.base_day_three, "Miércoles", "X",
                    "9:00 a.m. - 7:00 p.m.", "Sin Receso");
            configureBaseScheduleDay(R.id.base_day_four, "Jueves", "J",
                    "9:00 a.m. - 7:00 p.m.", "11:00 a.m. - 12:00 p.m.");
            configureBaseScheduleDay(R.id.base_day_five, "Viernes", "V",
                    "9:00 a.m. - 7:00 p.m.", "11:00 a.m. - 12:00 p.m.");
            configureBaseScheduleDay(R.id.base_day_six, "Sábado", "S",
                    "9:00 a.m. - 7:00 p.m.", "Sin Receso");
            configureBaseScheduleDay(R.id.base_day_seven, "Domingo", "D",
                    "9:00 a.m. - 7:00 p.m.", "Sin Receso");
            setDisabledCard(R.id.base_day_two, R.id.daily_shift_active);
        } else if (layout == R.layout.pantalla_promociones) {
            findViewById(R.id.toolbar_promotions).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_cortes_servicios));
            configureSpinner(R.id.promotion_status_spinner, android.graphics.Color.WHITE,
                    "Todas", "Activas", "Inactivas");
            findViewById(R.id.action_add_promotion).setOnClickListener(v ->
                    showPromotionForm());
            configurePromotionActions(R.id.promotion_item_one, "Especial Mes del Padre",
                    "Catálogo", "Combo Papá e Hijo", true);
            configurePromotionActions(R.id.promotion_item_two, "Descuento Corte Fo...",
                    "Descuento", "10% en cortes", true);
            configurePromotionActions(R.id.promotion_item_three, "Descuento Corte Fo...",
                    "Descuento", "15% en cortes", false);
            setDisabledCard(R.id.promotion_item_three, R.id.promotion_active_switch);
        } else if (layout == R.layout.pantalla_beneficios_admin) {
            findViewById(R.id.toolbar_admin_benefits).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_cortes_servicios));
            configureSpinner(R.id.admin_benefit_status_spinner, android.graphics.Color.WHITE,
                    "Todas", "Activas", "Inactivas");
            findViewById(R.id.action_edit_base_rate).setOnClickListener(v ->
                    showBaseRateForm());
            findViewById(R.id.action_add_admin_benefit).setOnClickListener(v ->
                    showBenefitForm());
            configureAdminBenefit(R.id.admin_benefit_item_one, "Corte Clasico Barba", "45 Pts", true);
            configureAdminBenefit(R.id.admin_benefit_item_two, "Tinte & Mascarill...", "65 Pts", true);
            configureAdminBenefit(R.id.admin_benefit_item_three, "Combo VIP (Corte...", "70 Pts", false);
            setDisabledCard(R.id.admin_benefit_item_three, R.id.admin_benefit_active_switch);
            View disabledBenefit = findViewById(R.id.admin_benefit_item_three);
            android.widget.ImageView disabledBenefitIcon =
                    disabledBenefit.findViewById(R.id.benefit_icon);
            disabledBenefitIcon.setBackgroundResource(R.drawable.bg_benefit_avatar_disabled);
            disabledBenefitIcon.setImageTintList(android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.rgb(170, 170, 170)));
        } else if (layout == R.layout.pantalla_barberos) {
            MaterialToolbar toolbar = findViewById(R.id.toolbar_barbers);
            toolbar.setNavigationOnClickListener(v ->
                    showScreen(R.layout.pantalla_personal_turnos));
            configureSpinner(R.id.barbers_status_spinner, android.graphics.Color.DKGRAY,
                    "Todos", "Activos", "Inactivos");
            findViewById(R.id.action_add_barber).setOnClickListener(v ->
                    showBarberForm(false));
            configureBarberActions(R.id.barber_item_one);
            configureBarberActions(R.id.barber_item_two);
            configureBarberActions(R.id.barber_item_three);
            setDisabledCard(R.id.barber_item_two, R.id.barber_active_switch);
        } else if (layout == R.layout.pantalla_cronograma_barberos) {
            MaterialToolbar toolbar = findViewById(R.id.toolbar_schedule);
            toolbar.setNavigationOnClickListener(v ->
                    showScreen(R.layout.pantalla_personal_turnos));
            configureSpinner(R.id.schedule_status_spinner, android.graphics.Color.DKGRAY,
                    "Todos", "Activos", "Inactivos");
            findViewById(R.id.schedule_barber_one).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_cronograma_barbero));
            findViewById(R.id.schedule_barber_two).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_cronograma_barbero));
            findViewById(R.id.schedule_barber_three).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_cronograma_barbero));
            setDisabledCard(R.id.schedule_barber_two, -1);
        } else if (layout == R.layout.pantalla_especialidades) {
            MaterialToolbar toolbar = findViewById(R.id.toolbar_admin_back);
            toolbar.setSubtitle("Administración Especialidades");
            toolbar.setNavigationOnClickListener(v ->
                    showScreen(R.layout.pantalla_personal_turnos));
            findViewById(R.id.action_add_specialty).setOnClickListener(v ->
                    showSpecialtyForm());
            configureSpecialtyActions(R.id.specialty_item_one);
            configureSpecialtyActions(R.id.specialty_item_two);
            configureSpecialtyActions(R.id.specialty_item_three);
        } else if (layout == R.layout.pantalla_cronograma_barbero) {
            MaterialToolbar toolbar = findViewById(R.id.toolbar_personal_schedule);
            toolbar.setNavigationOnClickListener(v ->
                    showScreen(R.layout.pantalla_cronograma_barberos));
            findViewById(R.id.action_base_shift).setOnClickListener(v ->
                    showBaseShiftForm());
            configureDailyShiftActions(R.id.daily_shift_one);
            configureDailyShiftActions(R.id.daily_shift_two);
            configureDailyShiftActions(R.id.daily_shift_three);
            configureDailyShiftActions(R.id.daily_shift_four);
            setDisabledCard(R.id.daily_shift_two, R.id.daily_shift_active);
        } else if (layout == R.layout.pantalla_clientes) {
            findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_control_citas));
            findViewById(R.id.action_add_client).setOnClickListener(v ->
                    showClientForm(false));
            configureSpinner(R.id.dropdown_client_status, android.graphics.Color.DKGRAY,
                    "Todos", "Activos", "Inactivos");
            configureClientActions(R.id.client_item_one);
            configureClientActions(R.id.client_item_two);
            configureClientActions(R.id.client_item_three);
        } else if (layout == R.layout.pantalla_citas_cliente) {
            findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_clientes));
            findViewById(R.id.filter_active_appointments).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_citas_cliente));
            findViewById(R.id.filter_history_appointments).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_historial_citas_cliente));
            findViewById(R.id.action_change_appointment).setOnClickListener(v ->
                    showAppointmentForm());
        } else if (layout == R.layout.pantalla_historial_citas_cliente) {
            MaterialToolbar toolbar = findViewById(R.id.toolbar_history_appointments);
            toolbar.setNavigationOnClickListener(v ->
                    showScreen(R.layout.pantalla_citas_cliente));
            configureSpinner(R.id.history_all_dropdown, android.graphics.Color.DKGRAY,
                    "Todas", "Activas", "Canceladas");
        } else if (layout == R.layout.pantalla_citas) {
            findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_control_citas));
            findViewById(R.id.action_add_appointment).setOnClickListener(v ->
                    showAppointmentForm());
            configureSpinner(R.id.dropdown_appointment_status, android.graphics.Color.DKGRAY,
                    "Historial", "Activas", "Canceladas");
            findViewById(R.id.appointment_item_one).setOnClickListener(v ->
                    showAppointmentForm());
        } else if (layout == R.layout.pantalla_beneficios) {
            findViewById(R.id.toolbar_admin_back).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_control_citas));
            findViewById(R.id.benefit_client_one).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_beneficios_cliente));
            findViewById(R.id.benefit_client_two).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_beneficios_cliente));
            findViewById(R.id.benefit_client_three).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_beneficios_cliente));
        } else if (layout == R.layout.pantalla_beneficios_cliente) {
            findViewById(R.id.toolbar_benefits_client).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_beneficios));
            configureSpinner(R.id.dropdown_benefit_status, android.graphics.Color.WHITE,
                    "Todas", "Activas", "Canjeadas");
            configureBenefitActions(R.id.benefit_available_one);
            configureBenefitActions(R.id.benefit_available_two);
        } else if (layout == R.layout.pantalla_perfil_usuario) {
            configureMenuToolbar(R.id.toolbar_profile);
            findViewById(R.id.action_edit_profile).setOnClickListener(v ->
                    showProfileForm());
            findViewById(R.id.action_change_password).setOnClickListener(v ->
                    showPasswordForm());
            configureSpinner(R.id.profile_language_spinner, android.graphics.Color.WHITE,
                    "Español", "English");
            SwitchMaterial themeSwitch = findViewById(R.id.profile_theme_switch);
            TextView themeLabel = findViewById(R.id.profile_theme_label);
            themeSwitch.setChecked(darkMode);
            themeLabel.setText(darkMode
                    ? "Modo Oscuro\nAlternar Contraste de la interfaz"
                    : "Modo Claro\nAlternar Contraste de la interfaz");
            themeSwitch.setOnCheckedChangeListener((buttonView, checked) -> {
                darkMode = checked;
                getPreferences(MODE_PRIVATE).edit()
                        .putBoolean("dark_mode", darkMode)
                        .apply();
                showScreen(currentLayout);
            });
        }
        applyDarkModeToCurrentScreen();
    }

    private void applyDarkModeToCurrentScreen() {
        View root = findViewById(android.R.id.content);
        if (root != null) {
            applyDarkModeToView(root);
        }
    }

    private void applyDarkModeToView(View view) {
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

    private void configureMenuToolbar(int toolbarId) {
        View toolbarView = findViewById(toolbarId);
        if (toolbarView instanceof MaterialToolbar) {
            ((MaterialToolbar) toolbarView).setNavigationOnClickListener(this::showAdminMenu);
        }
    }

    private void showAdminMenu(View anchor) {
        final android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.admin_side_menu);
        dialog.show();
        applyMenuSelection(dialog.findViewById(android.R.id.content));
        android.view.Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.35f);
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setGravity(android.view.Gravity.START);
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.94f),
                    android.view.WindowManager.LayoutParams.MATCH_PARENT);
        }
        View menuRoot = dialog.findViewById(android.R.id.content);
        if (darkMode && menuRoot != null) {
            menuRoot.setBackgroundColor(android.graphics.Color.rgb(18, 19, 21));
            applyDarkModeToView(menuRoot);
            applyMenuSelection(menuRoot);
        }
        View home = dialog.findViewById(R.id.menu_admin_home);
        View clients = dialog.findViewById(R.id.menu_admin_clients);
        View staff = dialog.findViewById(R.id.menu_admin_staff);
        View services = dialog.findViewById(R.id.menu_admin_services);
        View profile = dialog.findViewById(R.id.menu_admin_profile);
        View logout = dialog.findViewById(R.id.menu_admin_logout);
        if (home != null) {
            home.setOnClickListener(v -> {
                dialog.dismiss();
                showScreen(R.layout.pantalla_inicial);
            });
        }
        if (clients != null) {
            clients.setOnClickListener(v -> {
                dialog.dismiss();
                showScreen(R.layout.pantalla_control_citas);
            });
        }
        if (staff != null) {
            staff.setOnClickListener(v -> {
                dialog.dismiss();
                showScreen(R.layout.pantalla_personal_turnos);
            });
        }
        if (services != null) {
            services.setOnClickListener(v -> {
                dialog.dismiss();
                showScreen(R.layout.pantalla_cortes_servicios);
            });
        }
        if (profile != null) {
            profile.setOnClickListener(v -> {
                dialog.dismiss();
                showScreen(R.layout.pantalla_perfil_usuario);
            });
        }
        if (logout != null) {
            logout.setOnClickListener(v -> dialog.dismiss());
        }
    }

    private int getMenuItemForLayout(int layout) {
        if (layout == R.layout.pantalla_control_citas
                || layout == R.layout.pantalla_clientes
                || layout == R.layout.pantalla_citas
                || layout == R.layout.pantalla_beneficios
                || layout == R.layout.pantalla_citas_cliente
                || layout == R.layout.pantalla_historial_citas_cliente
                || layout == R.layout.pantalla_beneficios_cliente) {
            return R.id.menu_admin_clients;
        }
        if (layout == R.layout.pantalla_personal_turnos
                || layout == R.layout.pantalla_barberos
                || layout == R.layout.pantalla_cronograma_barberos
                || layout == R.layout.pantalla_cronograma_barbero
                || layout == R.layout.pantalla_especialidades) {
            return R.id.menu_admin_staff;
        }
        if (layout == R.layout.pantalla_cortes_servicios
                || layout == R.layout.pantalla_administracion_servicios
                || layout == R.layout.pantalla_horarios_servicio
                || layout == R.layout.pantalla_configuracion_horario_base
                || layout == R.layout.pantalla_promociones
                || layout == R.layout.pantalla_beneficios_admin) {
            return R.id.menu_admin_services;
        }
        if (layout == R.layout.pantalla_perfil_usuario) {
            return R.id.menu_admin_profile;
        }
        return R.id.menu_admin_home;
    }

    private void applyMenuSelection(View menuRoot) {
        int[] menuIds = {
                R.id.menu_admin_home, R.id.menu_admin_services, R.id.menu_admin_staff,
                R.id.menu_admin_clients, R.id.menu_admin_profile
        };
        for (int menuId : menuIds) {
            View item = menuRoot.findViewById(menuId);
            if (item == null) {
                continue;
            }
            boolean selected = menuId == selectedMenuItemId;
            item.setBackgroundResource(selected
                    ? R.drawable.bg_menu_selected
                    : (darkMode ? R.drawable.bg_menu_item_dark : R.drawable.bg_menu_item));
            ImageView icon = findMenuIcon(item);
            if (icon != null) {
                icon.setImageTintList(android.content.res.ColorStateList.valueOf(
                        android.graphics.Color.parseColor(selected ? "#C51B20" : "#197A8A")));
            }
            TextView title = findFirstMenuText(item);
            if (title != null) {
                title.setTextColor(android.graphics.Color.parseColor(
                        selected ? "#C51B20" : (darkMode ? "#F4F4F4" : "#242424")));
            }
        }
    }

    private ImageView findMenuIcon(View view) {
        if (view instanceof ImageView) {
            return (ImageView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                ImageView icon = findMenuIcon(group.getChildAt(index));
                if (icon != null) {
                    return icon;
                }
            }
        }
        return null;
    }

    private TextView findFirstMenuText(View view) {
        if (view instanceof TextView && !(view instanceof ImageView)) {
            return (TextView) view;
        }
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int index = 0; index < group.getChildCount(); index++) {
                TextView text = findFirstMenuText(group.getChildAt(index));
                if (text != null) {
                    return text;
                }
            }
        }
        return null;
    }

    private void configureClientActions(int itemId) {
            View item = findViewById(itemId);
            item.setClickable(false);
            item.findViewById(R.id.action_edit_client).setOnClickListener(v ->
                    showClientForm(true));
            item.findViewById(R.id.action_client_appointments).setOnClickListener(v ->
                    showScreen(R.layout.pantalla_citas_cliente));
        }

    private void configureBenefitActions(int itemId) {
        View item = findViewById(itemId);
        item.setClickable(false);
        item.setFocusable(false);
        item.findViewById(R.id.action_redeem_benefit).setOnClickListener(v ->
                showRedeemConfirmation());
        item.findViewById(R.id.action_remove_benefit).setOnClickListener(v ->
                showDeleteBenefitConfirmation());
    }

    private void configureBarberActions(int itemId) {
        View item = findViewById(itemId);
        item.findViewById(R.id.action_edit_barber).setOnClickListener(v ->
                showBarberForm(true));
    }

    private void configureSpecialtyActions(int itemId) {
        View item = findViewById(itemId);
        item.findViewById(R.id.action_edit_specialty).setOnClickListener(v ->
                showSpecialtyForm());
        item.findViewById(R.id.action_delete_specialty).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Eliminar especialidad")
                        .setMessage("¿Deseas eliminar esta especialidad?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar", null)
                        .show());
    }

    private void configureServiceActions(int itemId) {
        View item = findViewById(itemId);
        item.findViewById(R.id.action_edit_service).setOnClickListener(v -> showServiceForm());
    }

    private void configurePromotionActions(int itemId, String title, String type,
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

    private void configureAdminBenefit(int itemId, String title, String points, boolean active) {
        View item = findViewById(itemId);
        ((android.widget.TextView) item.findViewById(R.id.benefit_title)).setText(title);
        ((android.widget.TextView) item.findViewById(R.id.benefit_cost)).setText("Costo    " + points);
        item.findViewById(R.id.action_edit_benefit).setOnClickListener(v -> showBenefitForm());
        item.findViewById(R.id.action_delete_benefit).setOnClickListener(v ->
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Eliminar beneficio")
                        .setMessage("¿Deseas eliminar este beneficio?")
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Eliminar", null)
                        .show());
        ((CompoundButton) item.findViewById(R.id.admin_benefit_active_switch)).setChecked(active);
    }

    private void showBaseRateForm() {
        showStandardForm("Modificar tarifa base", "Guardar",
                new String[]{"Monto de gasto", "Define cuánto gasto representa un punto."},
                new String[]{"1.00", ""});
    }

    private void setDisabledCard(int itemId, int switchId) {
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

    private void configureDailyShiftActions(int itemId) {
        View item = findViewById(itemId);
        item.findViewById(R.id.action_edit_daily_shift).setOnClickListener(v ->
                showDailyShiftForm());
    }

    private void configureBaseScheduleDay(int itemId, String day, String initial,
                                          String hours, String breakTime) {
        View item = findViewById(itemId);
        ((android.widget.TextView) item.findViewById(R.id.base_day_name)).setText(day);
        ((android.widget.TextView) item.findViewById(R.id.base_day_avatar)).setText(initial);
        android.widget.TextView hoursView = item.findViewById(R.id.base_day_hours);
        android.widget.TextView breakView = item.findViewById(R.id.base_day_break);
        hoursView.setText(hours);
        breakView.setText(breakTime);
        if (hours.startsWith("Día")) {
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

    private void configureSpinner(int viewId, int textColor, String... options) {
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

    private void showWeekChangeDialog(int direction) {
        if (selectedWeekStart == null) {
            selectedWeekStart = getWeekStart(java.util.Calendar.getInstance());
        }
        selectedWeekStart.add(java.util.Calendar.DAY_OF_MONTH, direction * 7);
        updateWeekSelector();
    }

    private void showWeekCalendar() {
        java.util.Calendar today = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(this, (view, year, month, day) -> {
            java.util.Calendar selectedDate = java.util.Calendar.getInstance();
            selectedDate.set(year, month, day);
            selectedWeekStart = getWeekStart(selectedDate);
            updateWeekSelector();
        }, today.get(java.util.Calendar.YEAR),
                today.get(java.util.Calendar.MONTH),
                today.get(java.util.Calendar.DAY_OF_MONTH)).show();
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

        private void configureNavigation() {
        BottomNavigationView navigation = findViewById(R.id.admin_bottom_navigation);
        navigation.setOnItemSelectedListener(item -> {
            if (syncingBottomNavigation) {
                return true;
            }
            if (item.getItemId() == R.id.nav_home) {
                showScreen(R.layout.pantalla_inicial);
            } else if (item.getItemId() == R.id.nav_cuts) {
                showScreen(R.layout.pantalla_cortes_servicios);
            } else if (item.getItemId() == R.id.nav_staff) {
                showScreen(R.layout.pantalla_personal_turnos);
            } else if (item.getItemId() == R.id.nav_clients) {
                showScreen(R.layout.pantalla_control_citas);
            } else if (item.getItemId() == R.id.nav_profile) {
                showScreen(R.layout.pantalla_perfil_usuario);
            } else {
                return false;
            }
            return true;
        });
    }

    private void syncBottomNavigation(int layout) {
        BottomNavigationView navigation = findViewById(R.id.admin_bottom_navigation);
        if (navigation == null) {
            return;
        }
        int selectedId = getBottomNavigationItemForLayout(layout);
        syncingBottomNavigation = true;
        try {
            navigation.getMenu().setGroupCheckable(0, true, true);
            navigation.getMenu().findItem(selectedId).setChecked(true);
            navigation.setItemActiveIndicatorEnabled(false);
            navigation.setSelectedItemId(selectedId);
            navigation.setItemActiveIndicatorEnabled(true);
        } finally {
            syncingBottomNavigation = false;
        }
    }

    private int getBottomNavigationItemForLayout(int layout) {
        if (layout == R.layout.pantalla_control_citas
                || layout == R.layout.pantalla_clientes
                || layout == R.layout.pantalla_citas
                || layout == R.layout.pantalla_beneficios
                || layout == R.layout.pantalla_citas_cliente
                || layout == R.layout.pantalla_historial_citas_cliente
                || layout == R.layout.pantalla_beneficios_cliente) {
            return R.id.nav_clients;
        }
        if (layout == R.layout.pantalla_personal_turnos
                || layout == R.layout.pantalla_barberos
                || layout == R.layout.pantalla_cronograma_barberos
                || layout == R.layout.pantalla_cronograma_barbero
                || layout == R.layout.pantalla_especialidades) {
            return R.id.nav_staff;
        }
        if (layout == R.layout.pantalla_cortes_servicios
                || layout == R.layout.pantalla_administracion_servicios
                || layout == R.layout.pantalla_horarios_servicio
                || layout == R.layout.pantalla_configuracion_horario_base
                || layout == R.layout.pantalla_promociones
                || layout == R.layout.pantalla_beneficios_admin) {
            return R.id.nav_cuts;
        }
        if (layout == R.layout.pantalla_perfil_usuario) {
            return R.id.nav_profile;
        }
        return R.id.nav_home;
    }

    private void showClientForm(boolean editing) {
        showStandardForm(editing ? "Datos del Cliente" : "Ingresar Nuevo Cliente", "Guardar",
                new String[]{"Nombres", "Apellidos", "Tipo Documento", "Numero Doc.",
                        "Correo Electronico", "Numero Celular"},
                new String[]{"Juan Carlos", "Canales", "DUI", "000000-0",
                        "admin@gmail.com", "77223344"});
    }

    private void showBarberForm(boolean editing) {
        showStandardForm(editing ? "Datos del Barbero" : "Ingresar Nuevo Barbero", "Guardar",
                new String[]{"Nombres", "Apellidos", "Tipo Documento", "Numero Doc.",
                        "Correo Electronico", "Numero Celular", "Contraseña", "Especialidad"},
                new String[]{"Jose Carlos", "Canales", "DUI", "000000-0",
                        "Example@ex...", "77223344", "**************", "Corte"});
    }

    private void showSpecialtyForm() {
        showStandardForm("Ingresar Especialidad", "Guardar",
                new String[]{"Especialidad"}, new String[]{"Corte"});
    }

    private void showBaseShiftForm() {
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

    private void showServiceForm() {
        showStandardForm("Ingresar Nuevo Servicio", "Guardar Servicio",
                new String[]{"Nombre del Servicio", "Precio ($)", "Duración (min)", "Categoría"},
                new String[]{"Ej. Corte de Cabello", "12.00", "30", "Corte"});
    }

    private void showBaseScheduleForm() {
        showScreen(R.layout.pantalla_configuracion_horario_base);
    }

    private void showPromotionForm() {
        View content = getLayoutInflater().inflate(R.layout.modal_admin_promotion, null);
        SwitchMaterial notificationSwitch =
                content.findViewById(R.id.modal_promotion_notification_switch);
        View notificationSection =
                content.findViewById(R.id.modal_promotion_notification_section);
        ImageView notificationIcon =
                content.findViewById(R.id.modal_promotion_notification_icon);
        Spinner typeSpinner = content.findViewById(R.id.modal_promotion_type);
        TextView valueLabel = content.findViewById(R.id.modal_promotion_value_label);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Descuento", "Catálogo"});
        typeSpinner.setAdapter(typeAdapter);
        typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view,
                    int position, long id) {
                valueLabel.setText(position == 0 ? "Porcentaje %" : "Catálogo");
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });
        notificationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notificationSection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            notificationIcon.setImageResource(isChecked
                    ? R.drawable.ic_notifications : R.drawable.ic_notifications_off);
            notificationIcon.setColorFilter(android.graphics.Color.parseColor(
                    isChecked ? "#D5A916" : "#8C8C8C"));
        });

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
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

    private void showBenefitForm() {
        showStandardForm("Asignar Beneficio", "Guardar Servicio",
                new String[]{"Categoría", "Selecciona Servicio", "Coste Pts"},
                new String[]{"Corte", "Corte de Cabello", "25.00"});
    }

    private void showProfileForm() {
        showStandardForm("Juan Carlos Canales", "Guardar",
                new String[]{"Nombres", "Apellidos", "Tipo Documento", "Numero Doc.",
                        "Correo Electronico", "Numero Celular"},
                new String[]{"Juan Carlos", "Canales", "DUI", "000000-0",
                        "admin@gmail.com", "77223344"});
    }

    private void showPasswordForm() {
        showStandardForm("Cambio Contraseña", "Guardar",
                new String[]{"Contraseña Actual", "Nueva Contraseña"},
                new String[]{"**************", "**************"});
    }

    private void showAppointmentForm() {
        showStandardForm("Nueva cita", "Guardar",
                new String[]{"Categoría Servicio", "Barbero", "Servicio", "Inicio", "Hora"},
                new String[]{"Combo", "Jose Canales", "Combo Barba", "01/09/26",
                        "12:30 p.m."});
    }

    private void showRedeemConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Confirmar consumo")
                .setMessage("¿Deseas confirmar el consumo de este beneficio?\n\nSe descontarán los puntos correspondientes al cliente.")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Confirmar", null)
                .show();
    }

    private void showDeleteBenefitConfirmation() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Eliminar beneficio")
                .setMessage("¿Deseas eliminar este beneficio de la lista disponible?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", null)
                .show();
    }

    private void showStandardForm(String title, String positiveText,
            String[] labels, String[] values) {
        View content = getLayoutInflater().inflate(R.layout.modal_admin_standard, null);
        TextView titleView = content.findViewById(R.id.modal_admin_title);
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);
        titleView.setText(title);
        for (int i = 0; i < labels.length; i++) {
            EditText field = new EditText(this);
            field.setHint(labels[i]);
            if (values != null && i < values.length && values[i] != null
                    && !values[i].isEmpty()) {
                field.setText(values[i]);
            }
            field.setSingleLine(true);
            field.setTextSize(16);
            field.setBackgroundResource(R.drawable.bg_admin_modal_field);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, dp(44));
            params.topMargin = dp(8);
            fields.addView(field, params);
        }
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
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