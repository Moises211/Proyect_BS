package ues.edu.sv.proyect_bs;

import android.os.Bundle;
import android.net.Uri;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
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
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.TarifaPuntos;

public class MainActivity extends AppCompatActivity {
    private java.util.Calendar selectedWeekStart;
    private boolean darkMode;
    private AuthApiClient.AdminProfile loadedAdminProfile;
    private long selectedBenefitClientId;
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
        item.findViewById(R.id.action_edit_barber).setOnClickListener(v -> {
            // BarberosController supplies the selected barber and its loaded catalogs.
        });
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
        java.util.concurrent.ExecutorService executor =
                java.util.concurrent.Executors.newSingleThreadExecutor();
        AppDB database = AppDB.getInstance(this);
        executor.execute(() -> {
            TarifaPuntos selected =
                    database.tarifaPuntosDAO().obtenerPrimera();
            if (selected == null) selected = new TarifaPuntos();
            TarifaPuntos current = selected;
            if (current.puntos == null) current.puntos = 1;
            if (current.tarifamonetaria == null) current.tarifamonetaria = 1d;
            TarifaPuntos modalTariff = current;
            runOnUiThread(() -> showBaseRateDialog(modalTariff));
        });
    }

    private void showBaseRateDialog(TarifaPuntos current) {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        ((TextView) content.findViewById(R.id.modal_admin_title))
                .setText("Modificar tarifa base");
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);

        LinearLayout valuesRow = createClientModalRow(modalContext);
        EditText points = (EditText) createLabeledClientModalField(
                modalContext, valuesRow, "Puntos", String.valueOf(current.puntos));
        points.setHint("Puntos");
        points.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        EditText rate = (EditText) createLabeledClientModalField(
                modalContext, valuesRow, "Tarifa",
                String.format(java.util.Locale.US, "%.2f",
                        current.tarifamonetaria));
        rate.setHint("Tarifa");
        rate.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        fields.addView(valuesRow);

        androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(modalContext)
                        .setView(content)
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Guardar", null)
                        .create();
        dialog.setOnShowListener(ignored -> {
            styleModalButtons(dialog);
            Button save = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
            save.setOnClickListener(v -> {
                if (current.idtarifa <= 0) {
                    android.widget.Toast.makeText(MainActivity.this,
                            "No hay una tarifa disponible para actualizar.",
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }
                int pointsValue;
                double rateValue;
                try {
                    pointsValue = Integer.parseInt(points.getText().toString().trim());
                    rateValue = Double.parseDouble(rate.getText().toString().trim());
                    if (pointsValue <= 0 || rateValue <= 0) {
                        throw new NumberFormatException();
                    }
                } catch (NumberFormatException exception) {
                    android.widget.Toast.makeText(this,
                            "Ingresa Puntos y Tarifa válidos.",
                            android.widget.Toast.LENGTH_SHORT).show();
                    return;
                }

                TarifaPuntos updated = new TarifaPuntos();
                updated.idtarifa = current.idtarifa;
                updated.puntos = pointsValue;
                updated.tarifamonetaria = rateValue;
                save.setEnabled(false);
                new AuthApiClient().saveBaseRate(
                        SessionManager.getToken(this), updated,
                        new AuthApiClient.BaseRateCallback() {
                            @Override
                            public void onSuccess(TarifaPuntos savedTariff) {
                                java.util.concurrent.ExecutorService saveExecutor =
                                        java.util.concurrent.Executors.newSingleThreadExecutor();
                                saveExecutor.execute(() -> {
                                    AppDB.getInstance(MainActivity.this)
                                            .tarifaPuntosDAO().insertar(savedTariff);
                                    runOnUiThread(() -> {
                                        TextView rateView =
                                                findViewById(R.id.benefit_base_rate);
                                        if (rateView != null) {
                                            rateView.setText("Gasto de $" +
                                                    String.format(java.util.Locale.US,
                                                            "%.2f", savedTariff.tarifamonetaria)
                                                    + " = " + savedTariff.puntos + " Pts.");
                                        }
                                        dialog.dismiss();
                                        android.widget.Toast.makeText(MainActivity.this,
                                                "Tarifa base guardada.",
                                                android.widget.Toast.LENGTH_SHORT).show();
                                    });
                                });
                            }

                            @Override
                            public void onError(String message) {
                                save.setEnabled(true);
                                android.widget.Toast.makeText(MainActivity.this,
                                        message, android.widget.Toast.LENGTH_SHORT).show();
                            }
                        });
            });
        });
        dialog.show();
    }

    void setDisabledCard(int itemId, int switchId) {
        View item = findViewById(itemId);
        setDisabledCard(item, switchId);
    }

    void setDisabledCard(View item, int switchId) {
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
                || id == R.id.promotion_image
                || id == R.id.action_edit_client
                || id == R.id.action_client_appointments;
    }

    void configureDailyShiftActions(int itemId) {
        View item = findViewById(itemId);
        item.findViewById(R.id.action_edit_daily_shift).setOnClickListener(v ->
                showDailyShiftForm());
    }

    void configureBaseScheduleDay(int itemId, String day, String initial,
                                          String hours, String breakTime) {
        configureBaseScheduleDay(itemId, day, initial, hours, breakTime, null, null);
    }

    void configureBaseScheduleDay(int itemId, String day, String initial,
                                          String hours, String breakTime,
                                          ues.edu.sv.proyect_bs.modelos.HorarioDiaBase row,
                                          Runnable onChanged) {
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
        item.findViewById(R.id.action_edit_daily_shift).setOnClickListener(v -> {
            if (row == null) {
                showDailyShiftForm();
            } else {
                showBaseScheduleDayForm(row, onChanged);
            }
        });
    }

    private void showBaseScheduleDayForm(
            ues.edu.sv.proyect_bs.modelos.HorarioDiaBase row,
            Runnable onChanged) {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        ((TextView) content.findViewById(R.id.modal_admin_title))
                .setText("Editar jornada - " + row.dia);
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);

        LinearLayout timesRow = createClientModalRow(modalContext);
        EditText opening = (EditText) createLabeledClientModalField(
                modalContext, timesRow, "Inicio", toDisplayTime(row.apertura));
        configureModalTimeField(modalContext, opening);
        EditText closing = (EditText) createLabeledClientModalField(
                modalContext, timesRow, "Fin", toDisplayTime(row.cierre));
        configureModalTimeField(modalContext, closing);
        fields.addView(timesRow);

        LinearLayout recesoHeader = new LinearLayout(modalContext);
        recesoHeader.setOrientation(LinearLayout.HORIZONTAL);
        recesoHeader.setGravity(android.view.Gravity.CENTER_VERTICAL);
        recesoHeader.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        recesoHeader.setPadding(0, dp(8), 0, dp(4));

        TextView recesoLabel = new TextView(modalContext);
        recesoLabel.setText("Receso");
        recesoLabel.setTextColor(android.graphics.Color.parseColor("#263248"));
        recesoLabel.setTextSize(16);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        recesoHeader.addView(recesoLabel, labelParams);

        SwitchMaterial recesoSwitch = new SwitchMaterial(modalContext);
        recesoSwitch.setChecked(row.receso);
        recesoSwitch.setThumbTintList(android.content.res.ColorStateList.valueOf(
                android.graphics.Color.WHITE));
        recesoSwitch.setTrackTintList(new android.content.res.ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        android.graphics.Color.parseColor("#287F8D"),
                        android.graphics.Color.parseColor("#B7BDC5")
                }));
        recesoHeader.addView(recesoSwitch);
        fields.addView(recesoHeader);

        LinearLayout recesoRow = createClientModalRow(modalContext);
        EditText breakStart = (EditText) createLabeledClientModalField(
                modalContext, recesoRow, "Inicio Receso", toDisplayTime(row.inicio));
        configureModalTimeField(modalContext, breakStart);
        EditText breakEnd = (EditText) createLabeledClientModalField(
                modalContext, recesoRow, "Fin Receso", toDisplayTime(row.fin));
        configureModalTimeField(modalContext, breakEnd);
        fields.addView(recesoRow);
        recesoRow.setVisibility(row.receso ? View.VISIBLE : View.GONE);
        recesoSwitch.setOnCheckedChangeListener((button, checked) ->
                recesoRow.setVisibility(checked ? View.VISIBLE : View.GONE));

        androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(modalContext)
                        .setView(content)
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton("Guardar", null)
                        .create();
        dialog.setOnShowListener(ignored -> {
            styleModalButtons(dialog);
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String openingValue = fromDisplayTime(opening.getText().toString());
                        String closingValue = fromDisplayTime(closing.getText().toString());
                        String breakStartValue = fromDisplayTime(breakStart.getText().toString());
                        String breakEndValue = fromDisplayTime(breakEnd.getText().toString());
                        if (openingValue.isEmpty() || closingValue.isEmpty()) {
                            showFieldError(opening, "Ingresa el horario de inicio y fin.");
                            return;
                        }
                        boolean hasBreak = recesoSwitch.isChecked();
                        if (hasBreak
                                && (breakStartValue.isEmpty() || breakEndValue.isEmpty())) {
                            showFieldError(breakStart, "Selecciona el inicio y fin del receso.");
                            return;
                        }
                        row.apertura = openingValue;
                        row.cierre = closingValue;
                        row.receso = hasBreak;
                        if (hasBreak) {
                            row.inicio = breakStartValue;
                            row.fin = breakEndValue;
                        }
                        dialog.dismiss();
                        if (onChanged != null) onChanged.run();
                    });
        });
        dialog.show();
    }

    private String toDisplayTime(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        String[] parts = value.trim().split(":");
        if (parts.length < 2) return value;
        try {
            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);
            int displayHour = hour % 12;
            if (displayHour == 0) displayHour = 12;
            return String.format(java.util.Locale.US, "%d:%02d %s",
                    displayHour, minute, hour >= 12 ? "p.m." : "a.m.");
        } catch (NumberFormatException exception) {
            return value;
        }
    }

    private String fromDisplayTime(String value) {
        if (value == null) return "";
        String normalized = value.trim().toLowerCase(java.util.Locale.US)
                .replace(".", "").replace(" ", "");
        if (normalized.isEmpty()) return "";
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d{1,2}):(\\d{2})(am|pm)?")
                .matcher(normalized);
        if (!matcher.find()) return "";
        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        String suffix = matcher.group(3);
        if ("pm".equals(suffix) && hour < 12) hour += 12;
        if ("am".equals(suffix) && hour == 12) hour = 0;
        return String.format(java.util.Locale.US, "%02d:%02d:00", hour, minute);
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
        ensureSelectedWeekStart();
        selectedWeekStart.add(java.util.Calendar.DAY_OF_MONTH, direction * 7);
        clampSelectedWeekStart();
        updateWeekSelector();
    }

    void showWeekCalendar() {
        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar horizonEnd = java.util.Calendar.getInstance();
        horizonEnd.add(java.util.Calendar.MONTH, 1);
        horizonEnd.set(java.util.Calendar.DAY_OF_MONTH,
                horizonEnd.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        java.util.Calendar minimum = getWeekStart(today);
        android.widget.DatePicker datePicker = new android.widget.DatePicker(this);
        datePicker.init(
                today.get(java.util.Calendar.YEAR),
                today.get(java.util.Calendar.MONTH),
                today.get(java.util.Calendar.DAY_OF_MONTH),
                null);
        datePicker.setMinDate(minimum.getTimeInMillis());
        datePicker.setMaxDate(horizonEnd.getTimeInMillis());

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
                    clampSelectedWeekStart();
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
        clampSelectedWeekStart();
        java.util.Calendar weekEnd = (java.util.Calendar) selectedWeekStart.clone();
        weekEnd.add(java.util.Calendar.DAY_OF_MONTH, 6);
        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(
                "dd/MM", java.util.Locale.getDefault());
        android.widget.TextView weekLabel = findViewById(R.id.action_select_week);
        if (weekLabel != null) {
            weekLabel.setText("Sem " + format.format(selectedWeekStart.getTime())
                    + "-" + format.format(weekEnd.getTime()));
        }
        refreshSelectedWeek();
    }

    private void ensureSelectedWeekStart() {
        if (selectedWeekStart == null) {
            selectedWeekStart = getWeekStart(java.util.Calendar.getInstance());
        }
        clampSelectedWeekStart();
    }

    private void clampSelectedWeekStart() {
        java.util.Calendar minimum = getWeekStart(java.util.Calendar.getInstance());
        java.util.Calendar horizonEnd = java.util.Calendar.getInstance();
        horizonEnd.add(java.util.Calendar.MONTH, 1);
        horizonEnd.set(java.util.Calendar.DAY_OF_MONTH,
                horizonEnd.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        java.util.Calendar maximum = getWeekStart(horizonEnd);

        selectedWeekStart.set(java.util.Calendar.HOUR_OF_DAY, 0);
        selectedWeekStart.set(java.util.Calendar.MINUTE, 0);
        selectedWeekStart.set(java.util.Calendar.SECOND, 0);
        selectedWeekStart.set(java.util.Calendar.MILLISECOND, 0);
        if (selectedWeekStart.before(minimum)) {
            selectedWeekStart = (java.util.Calendar) minimum.clone();
        } else if (selectedWeekStart.after(maximum)) {
            selectedWeekStart = (java.util.Calendar) maximum.clone();
        }
    }

    void refreshSelectedWeek() {
        ensureSelectedWeekStart();
        java.util.Calendar first = (java.util.Calendar) selectedWeekStart.clone();
        java.text.SimpleDateFormat isoFormat = new java.text.SimpleDateFormat(
                "yyyy-MM-dd", java.util.Locale.US);
        String desde = isoFormat.format(first.getTime());
        java.util.Calendar last = (java.util.Calendar) first.clone();
        last.add(java.util.Calendar.DAY_OF_MONTH, 6);
        String hasta = isoFormat.format(last.getTime());
        int[] cardIds = {
                R.id.service_daily_shift_one, R.id.service_daily_shift_two,
                R.id.service_daily_shift_three, R.id.service_daily_shift_four,
                R.id.service_daily_shift_five, R.id.service_daily_shift_six,
                R.id.service_daily_shift_seven
        };

        new Thread(() -> {
            ues.edu.sv.proyect_bs.data.AppDB database =
                    ues.edu.sv.proyect_bs.data.AppDB.getInstance(this);
            java.util.List<ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario> rows =
                    database.horarioAtencionDiarioDAO().obtenerEntreFechas(desde, hasta);
            java.util.Map<String, ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario> byDate =
                    new java.util.HashMap<>();
            for (ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario row : rows) {
                byDate.put(row.fecha, row);
            }
            runOnUiThread(() -> {
                int activeDays = 0;
                java.text.SimpleDateFormat labelFormat = new java.text.SimpleDateFormat(
                        "EEE dd/MM/yyyy", new java.util.Locale("es", "ES"));
                for (int index = 0; index < cardIds.length; index++) {
                    java.util.Calendar date = (java.util.Calendar) first.clone();
                    date.add(java.util.Calendar.DAY_OF_MONTH, index);
                    String iso = isoFormat.format(date.getTime());
                    ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario row = byDate.get(iso);
                    bindDailyShiftCard(cardIds[index], date, row);
                    if (row != null && Boolean.TRUE.equals(row.estado)) {
                        activeDays++;
                    }
                }
                android.widget.TextView summary = findViewById(R.id.week_days_summary);
                if (summary != null) {
                    summary.setText("Días " + activeDays + "/7");
                }
            });
        }).start();
    }

    private void bindDailyShiftCard(int cardId, java.util.Calendar date,
                                    ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario row) {
        View card = findViewById(cardId);
        if (card == null) {
            return;
        }
        java.text.SimpleDateFormat labelFormat = new java.text.SimpleDateFormat(
                "EEE dd/MM/yyyy", new java.util.Locale("es", "ES"));
        TextView dateView = card.findViewById(R.id.daily_shift_date);
        TextView hoursView = card.findViewById(R.id.daily_shift_hours);
        TextView breakView = card.findViewById(R.id.daily_shift_break);
        SwitchMaterial activeSwitch = card.findViewById(R.id.daily_shift_active);
        dateView.setText(labelFormat.format(date.getTime()));

        if (row == null) {
            hoursView.setText("Sin horario generado");
            breakView.setText("Sin receso");
            activeSwitch.setChecked(false);
            card.findViewById(R.id.action_edit_daily_shift).setOnClickListener(null);
            return;
        }

        hoursView.setText(formatTimeRange(row.apertura, row.cierre));
        breakView.setText(row.receso
                ? formatTimeRange(row.inicio, row.fin)
                : "Sin receso");
        activeSwitch.setOnCheckedChangeListener(null);
        activeSwitch.setChecked(Boolean.TRUE.equals(row.estado));
        activeSwitch.setOnCheckedChangeListener((button, checked) -> {
            row.estado = checked;
            persistDailyShift(row);
        });
        card.findViewById(R.id.action_edit_daily_shift).setOnClickListener(v ->
                showDailyShiftForm(row));
    }

    interface ClientFormListener {
        void onSave(String names, String lastNames, long documentId, String document,
                    String email, String phone, String birthDate, String password,
                    boolean active, long specialtyId);
    }

    void showClientForm(boolean editing,
            ues.edu.sv.proyect_bs.modelos.Cliente client,
            ues.edu.sv.proyect_bs.modelos.Persona person,
            java.util.List<ues.edu.sv.proyect_bs.modelos.TipoDocumento> documents,
            java.util.List<ues.edu.sv.proyect_bs.modelos.CategoriaServicio> specialties,
            ClientFormListener listener) {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        ((TextView) content.findViewById(R.id.modal_admin_title))
                .setText(editing ? "Datos del Cliente" : "Ingresar Nuevo Cliente");
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);
        EditText[] inputs = new EditText[8];
        Spinner[] documentSpinner = {null};
        Spinner[] specialtySpinner = {null};

        LinearLayout namesRow = createClientModalRow(modalContext);
        inputs[0] = (EditText) createLabeledClientModalField(modalContext, namesRow,
                "Nombres", person == null ? "" : safeModalValue(person.nombres));
        configureNameInput(inputs[0]);
        inputs[1] = (EditText) createLabeledClientModalField(modalContext, namesRow,
                "Apellidos", person == null ? "" : safeModalValue(person.apellidos));
        configureNameInput(inputs[1]);
        fields.addView(namesRow);

        LinearLayout documentRow = createClientModalRow(modalContext);
        documentSpinner[0] = (Spinner) createLabeledClientModalSpinner(modalContext,
                documentRow, "Tipo Documento");
        String[] documentOptions = new String[documents.size() + 1];
        documentOptions[0] = "Seleccionar";
        for (int index = 0; index < documents.size(); index++) {
            documentOptions[index + 1] = documents.get(index).nombredocumento;
        }
        documentSpinner[0].setAdapter(createModalSpinnerAdapter(modalContext, documentOptions));
        for (int index = 0; index < documents.size(); index++) {
            if (person != null && documents.get(index).iddocumento
                    == (person.iddocumento == null ? 0L : person.iddocumento)) {
                documentSpinner[0].setSelection(index + 1);
                break;
            }
        }
        inputs[2] = (EditText) createLabeledClientModalField(modalContext, documentRow,
                "Numero Doc.", person == null ? "" : safeModalValue(person.identificadordoc));
        fields.addView(documentRow);

        LinearLayout contactRow = createClientModalRow(modalContext);
        inputs[3] = (EditText) createLabeledClientModalField(modalContext, contactRow,
                "Correo Electronico", person == null ? "" : safeModalValue(person.correo_electronico));
        inputs[3].setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        if (editing) {
            inputs[3].setEnabled(false);
            inputs[3].setFocusable(false);
            inputs[3].setAlpha(0.65f);
            inputs[3].setTextColor(android.graphics.Color.parseColor("#6F6F6F"));
        }
        inputs[4] = (EditText) createLabeledClientModalField(modalContext, contactRow,
                "Numero Celular", person == null ? "" : safeModalValue(person.numero_celular));
        configurePhoneInput(inputs[4]);
        fields.addView(contactRow);

        LinearLayout dateRow = createClientModalRow(modalContext);
        inputs[5] = (EditText) createLabeledClientModalField(modalContext, dateRow,
                "Fecha Nacimiento", person == null ? ""
                        : formatModalBirthDate(person.fecha_nacimiento));
        configureModalDateField(modalContext, inputs[5]);
        LinearLayout activeColumn = new LinearLayout(modalContext);
        activeColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams activeParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        activeParams.setMarginStart(dp(6));
        activeColumn.setLayoutParams(activeParams);
        TextView activeLabel = new TextView(modalContext);
        activeLabel.setText("Activo");
        activeLabel.setTextColor(android.graphics.Color.parseColor("#263248"));
        activeLabel.setTextSize(14);
        activeLabel.setPadding(0, dp(8), 0, dp(4));
        activeColumn.addView(activeLabel);
        com.google.android.material.switchmaterial.SwitchMaterial activeSwitch =
                new com.google.android.material.switchmaterial.SwitchMaterial(modalContext);
        activeSwitch.setChecked(client == null || Boolean.TRUE.equals(client.estado));
        activeColumn.addView(activeSwitch);
        dateRow.addView(activeColumn);
        fields.addView(dateRow);

        LinearLayout passwordSpecialtyRow = createClientModalRow(modalContext);
        inputs[6] = (EditText) createLabeledClientModalField(modalContext,
                passwordSpecialtyRow, "Contraseña", "");
        configurePasswordField(inputs[6], modalContext);
        if (editing) {
            inputs[6].setText("********");
            inputs[6].setTag("existing-password-mask");
            inputs[6].setEnabled(false);
            inputs[6].setFocusable(false);
        }
        specialtySpinner[0] = (Spinner) createLabeledClientModalSpinner(modalContext,
                passwordSpecialtyRow, "Servicio Preferencial");
        String[] specialtyOptions = new String[specialties.size() + 1];
        specialtyOptions[0] = "Seleccionar servicio";
        for (int index = 0; index < specialties.size(); index++) {
            specialtyOptions[index + 1] = specialties.get(index).nombre;
        }
        specialtySpinner[0].setAdapter(createModalSpinnerAdapter(modalContext, specialtyOptions));
        for (int index = 0; index < specialties.size(); index++) {
            if (client != null && specialties.get(index).idcategoria_servicio
                    == (client.preferencia_especialidad == null
                    ? 0L : client.preferencia_especialidad)) {
                specialtySpinner[0].setSelection(index + 1);
                break;
            }
        }
        fields.addView(passwordSpecialtyRow);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(modalContext)
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            styleModalButtons(dialog);
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        int documentPosition = documentSpinner[0].getSelectedItemPosition() - 1;
                        int specialtyPosition = specialtySpinner[0].getSelectedItemPosition() - 1;
                        if (documentPosition < 0 || documentPosition >= documents.size()) {
                            showFieldError(inputs[2], "Selecciona un tipo de documento válido.");
                            return;
                        }
                        if (specialtyPosition < 0 || specialtyPosition >= specialties.size()) {
                            showFieldError(specialtySpinner[0], "Selecciona un servicio preferencial válido.");
                            return;
                        }
                        String names = inputs[0].getText().toString().trim();
                        String lastNames = inputs[1].getText().toString().trim();
                        String document = inputs[2].getText().toString().trim();
                        String email = inputs[3].getText().toString().trim();
                        String phone = inputs[4].getText().toString().trim();
                        String birthDate = inputs[5].getText().toString().trim();
                        boolean keepingExistingPassword =
                                editing && "existing-password-mask".equals(inputs[6].getTag());
                        String password = keepingExistingPassword ? "" : inputs[6].getText().toString();
                        if (!isValidName(names)) {
                            showFieldError(inputs[0], "Usa sólo letras y no lo dejes vacío.");
                            return;
                        }
                        if (!isValidName(lastNames)) {
                            showFieldError(inputs[1], "Usa sólo letras y no lo dejes vacío.");
                            return;
                        }
                        if (document.length() < 8) {
                            showFieldError(inputs[2], "Debe tener al menos 8 caracteres.");
                            return;
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            showFieldError(inputs[3], "Ingresa un correo válido.");
                            return;
                        }
                        if (!phone.matches("\\d{8}")) {
                            showFieldError(inputs[4], "Debe contener exactamente 8 números.");
                            return;
                        }
                        if (birthDate.isEmpty()) {
                            showFieldError(inputs[5], "Selecciona la fecha de nacimiento.");
                            return;
                        }
                        if (!editing && password.trim().isEmpty()) {
                            showFieldError(inputs[6], "La contraseña es obligatoria.");
                            return;
                        }
                        listener.onSave(names, lastNames,
                                documents.get(documentPosition).iddocumento, document,
                                email, phone, birthDate, password, activeSwitch.isChecked(),
                                specialties.get(specialtyPosition).idcategoria_servicio);
                        dialog.dismiss();
                    });
        });
        dialog.show();
    }

    interface BarberFormListener {
        void onSave(String names, String lastNames, long documentId, String document,
                    String email, String phone, String password, String birthDate,
                    double salary, boolean active, long specialtyId);
    }

    interface ServiceFormListener {
        void onSave(ues.edu.sv.proyect_bs.modelos.Servicios service);
    }

    interface BenefitFormListener {
        void onSave(ues.edu.sv.proyect_bs.modelos.Beneficios benefit,
                    Runnable onSuccess, Runnable onFailure);
    }

    void showBarberForm(boolean editing,
            ues.edu.sv.proyect_bs.modelos.Barberos barber,
            ues.edu.sv.proyect_bs.modelos.Persona person,
            java.util.List<ues.edu.sv.proyect_bs.modelos.TipoDocumento> documents,
            java.util.List<ues.edu.sv.proyect_bs.modelos.CategoriaServicio> specialties,
            BarberFormListener listener) {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        ((TextView) content.findViewById(R.id.modal_admin_title))
                .setText(editing ? "Datos del Barbero" : "Ingresar Nuevo Barbero");
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);
        EditText[] inputs = new EditText[8];
        Spinner[] documentSpinner = {null};
        Spinner[] specialtySpinner = {null};

        LinearLayout identityRow = createClientModalRow(modalContext);
        LinearLayout photoColumn = new LinearLayout(modalContext);
        photoColumn.setOrientation(LinearLayout.VERTICAL);
        photoColumn.setLayoutParams(new LinearLayout.LayoutParams(dp(118),
                LinearLayout.LayoutParams.WRAP_CONTENT));
        TextView photoLabel = new TextView(modalContext);
        photoLabel.setText("Foto Perfil");
        photoLabel.setTextColor(android.graphics.Color.parseColor("#263248"));
        photoLabel.setTextSize(14);
        photoLabel.setPadding(0, dp(8), 0, dp(4));
        photoColumn.addView(photoLabel);
        android.widget.ImageView photo = new android.widget.ImageView(modalContext);
        photo.setLayoutParams(new LinearLayout.LayoutParams(dp(110), dp(110)));
        photo.setImageResource(R.drawable.ic_person);
        photo.setScaleType(android.widget.ImageView.ScaleType.CENTER);
        photo.setPadding(dp(24), dp(24), dp(24), dp(24));
        photo.setBackgroundResource(R.drawable.bg_profile_avatar);
        photoColumn.addView(photo);
        identityRow.addView(photoColumn);

        LinearLayout nameColumn = new LinearLayout(modalContext);
        nameColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        nameParams.setMarginStart(dp(6));
        nameColumn.setLayoutParams(nameParams);
        inputs[0] = (EditText) createModalField(modalContext, "Nombres",
                person == null ? "" : safeModalValue(person.nombres), false);
        configureNameInput(inputs[0]);
        TextView namesLabel = new TextView(modalContext);
        namesLabel.setText("Nombres");
        namesLabel.setTextColor(android.graphics.Color.parseColor("#263248"));
        namesLabel.setTextSize(14);
        namesLabel.setPadding(0, dp(8), 0, dp(4));
        nameColumn.addView(namesLabel);
        nameColumn.addView(inputs[0], new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
        inputs[1] = (EditText) createModalField(modalContext, "Apellidos",
                person == null ? "" : safeModalValue(person.apellidos), false);
        configureNameInput(inputs[1]);
        TextView lastNamesLabel = new TextView(modalContext);
        lastNamesLabel.setText("Apellidos");
        lastNamesLabel.setTextColor(android.graphics.Color.parseColor("#263248"));
        lastNamesLabel.setTextSize(14);
        lastNamesLabel.setPadding(0, dp(8), 0, dp(4));
        nameColumn.addView(lastNamesLabel);
        nameColumn.addView(inputs[1], new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
        identityRow.addView(nameColumn);
        fields.addView(identityRow);

        LinearLayout documentRow = createClientModalRow(modalContext);
        documentSpinner[0] = (Spinner) createLabeledClientModalSpinner(
                modalContext, documentRow, "Tipo Documento");
        String[] documentOptions = new String[documents.size() + 1];
        documentOptions[0] = "Seleccionar";
        for (int index = 0; index < documents.size(); index++) {
            documentOptions[index + 1] = documents.get(index).nombredocumento;
        }
        documentSpinner[0].setAdapter(createModalSpinnerAdapter(modalContext, documentOptions));
        for (int index = 0; index < documents.size(); index++) {
            if (person != null && documents.get(index).iddocumento
                    == (person.iddocumento == null ? 0L : person.iddocumento)) {
                documentSpinner[0].setSelection(index + 1);
                break;
            }
        }
        inputs[2] = (EditText) createLabeledClientModalField(modalContext, documentRow,
                "Numero Doc.", person == null ? "" : safeModalValue(person.identificadordoc));
        fields.addView(documentRow);

        LinearLayout contactRow = createClientModalRow(modalContext);
        inputs[3] = (EditText) createLabeledClientModalField(modalContext, contactRow,
                "Correo Electronico", barber == null ? ""
                        : safeModalValue(barber.correo_electronico));
        inputs[3].setInputType(android.text.InputType.TYPE_CLASS_TEXT
                | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        inputs[4] = (EditText) createLabeledClientModalField(modalContext, contactRow,
                "Numero Celular", person == null ? "" : safeModalValue(person.numero_celular));
        configurePhoneInput(inputs[4]);
        fields.addView(contactRow);

        inputs[5] = (EditText) createModalField(modalContext, "Contraseña", "", false);
        configurePasswordField(inputs[5], modalContext);
        TextView passwordLabel = new TextView(modalContext);
        passwordLabel.setText("Contraseña");
        passwordLabel.setTextColor(android.graphics.Color.parseColor("#263248"));
        passwordLabel.setTextSize(14);
        passwordLabel.setPadding(0, dp(8), 0, dp(4));
        fields.addView(passwordLabel);
        fields.addView(inputs[5], new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
        if (editing) {
            inputs[5].setText("********");
            inputs[5].setTag("existing-password-mask");
            inputs[5].setEnabled(false);
            inputs[5].setFocusable(false);
        }

        LinearLayout detailsRow = createClientModalRow(modalContext);
        inputs[6] = (EditText) createLabeledClientModalField(modalContext, detailsRow,
                "Fecha Nacimiento", person == null ? ""
                        : formatModalBirthDate(person.fecha_nacimiento));
        configureModalDateField(modalContext, inputs[6]);
        inputs[7] = (EditText) createLabeledClientModalField(modalContext, detailsRow,
                "Salario", barber == null || barber.salario == null
                        ? "" : new java.text.DecimalFormat("0.00").format(barber.salario));
        inputs[7].setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        fields.addView(detailsRow);

        LinearLayout specialtyActiveRow = createClientModalRow(modalContext);
        specialtySpinner[0] = (Spinner) createLabeledClientModalSpinner(
                modalContext, specialtyActiveRow, "Especialidad");
        String[] specialtyOptions = new String[specialties.size() + 1];
        specialtyOptions[0] = "Seleccionar especialidad";
        for (int index = 0; index < specialties.size(); index++) {
            specialtyOptions[index + 1] = specialties.get(index).nombre;
        }
        specialtySpinner[0].setAdapter(createModalSpinnerAdapter(modalContext, specialtyOptions));
        for (int index = 0; index < specialties.size(); index++) {
            if (barber != null && specialties.get(index).idcategoria_servicio
                    == (barber.especialidad == null ? 0L : barber.especialidad)) {
                specialtySpinner[0].setSelection(index + 1);
                break;
            }
        }

        LinearLayout activeColumn = new LinearLayout(modalContext);
        activeColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams activeParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        activeParams.setMarginStart(dp(6));
        activeColumn.setLayoutParams(activeParams);
        TextView activeLabel = new TextView(modalContext);
        activeLabel.setText("Activo");
        activeLabel.setTextColor(android.graphics.Color.parseColor("#263248"));
        activeLabel.setTextSize(14);
        activeLabel.setPadding(0, dp(8), 0, dp(4));
        activeColumn.addView(activeLabel);
        com.google.android.material.switchmaterial.SwitchMaterial activeSwitch =
                new com.google.android.material.switchmaterial.SwitchMaterial(modalContext);
        activeSwitch.setChecked(barber == null || Boolean.TRUE.equals(barber.estado));
        activeColumn.addView(activeSwitch);
        specialtyActiveRow.addView(activeColumn);
        fields.addView(specialtyActiveRow);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(modalContext)
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            styleModalButtons(dialog);
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        int documentPosition = documentSpinner[0].getSelectedItemPosition() - 1;
                        int specialtyPosition = specialtySpinner[0].getSelectedItemPosition() - 1;
                        if (documentPosition < 0 || documentPosition >= documents.size()) {
                            showFieldError(inputs[2], "Selecciona un tipo de documento válido.");
                            return;
                        }
                        if (specialtyPosition < 0 || specialtyPosition >= specialties.size()) {
                            showFieldError(specialtySpinner[0], "Selecciona una especialidad válida.");
                            return;
                        }
                        String names = inputs[0].getText().toString().trim();
                        String lastNames = inputs[1].getText().toString().trim();
                        String document = inputs[2].getText().toString().trim();
                        String email = inputs[3].getText().toString().trim();
                        String phone = inputs[4].getText().toString().trim();
                        String birthDate = inputs[6].getText().toString().trim();
                        boolean keepingExistingPassword =
                                editing && "existing-password-mask".equals(inputs[5].getTag());
                        String password = keepingExistingPassword
                                ? "" : inputs[5].getText().toString();
                        if (!isValidName(names)) {
                            showFieldError(inputs[0], "Usa sólo letras y no lo dejes vacío.");
                            return;
                        }
                        if (!isValidName(lastNames)) {
                            showFieldError(inputs[1], "Usa sólo letras y no lo dejes vacío.");
                            return;
                        }
                        if (document.length() < 8) {
                            showFieldError(inputs[2], "Debe tener al menos 8 caracteres.");
                            return;
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            showFieldError(inputs[3], "Ingresa un correo válido.");
                            return;
                        }
                        if (!phone.matches("\\d{8}")) {
                            showFieldError(inputs[4], "Debe contener exactamente 8 números.");
                            return;
                        }
                        if (!editing && password.trim().isEmpty()) {
                            showFieldError(inputs[5], "La contraseña es obligatoria.");
                            return;
                        }
                        if (birthDate.isEmpty()) {
                            showFieldError(inputs[6], "Selecciona la fecha de nacimiento.");
                            return;
                        }
                        double salary;
                        try {
                            salary = Double.parseDouble(inputs[7].getText().toString().trim());
                        } catch (Exception exception) {
                            showFieldError(inputs[7], "Ingresa un salario válido.");
                            return;
                        }
                        listener.onSave(names, lastNames,
                                documents.get(documentPosition).iddocumento, document,
                                email, phone, password, birthDate, salary,
                                activeSwitch.isChecked(),
                                specialties.get(specialtyPosition).idcategoria_servicio);
                        dialog.dismiss();
                    });
        });
        dialog.show();
    }

    private String safeModalValue(String value) {
        return value == null ? "" : value;
    }

    private LinearLayout createClientModalRow(android.content.Context context) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        return row;
    }

    private View createLabeledClientModalField(android.content.Context context,
                                               LinearLayout row, String label, String value) {
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMarginEnd(dp(6));
        column.setLayoutParams(params);
        TextView labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextColor(android.graphics.Color.parseColor("#263248"));
        labelView.setTextSize(14);
        labelView.setPadding(0, dp(8), 0, dp(4));
        column.addView(labelView);
        EditText field = (EditText) createModalField(context, label, value, false);
        column.addView(field, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
        row.addView(column);
        return field;
    }

    private View createLabeledClientModalSpinner(android.content.Context context,
                                                 LinearLayout row, String label) {
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMarginEnd(dp(6));
        column.setLayoutParams(params);
        TextView labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextColor(android.graphics.Color.parseColor("#263248"));
        labelView.setTextSize(14);
        labelView.setPadding(0, dp(8), 0, dp(4));
        column.addView(labelView);
        Spinner spinner = new Spinner(context);
        configureModalSpinnerAppearance(spinner);
        column.addView(spinner, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
        row.addView(column);
        return spinner;
    }

    private String formatModalBirthDate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "";
        }
        String date = value.trim();
        if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            String[] parts = date.split("-");
            return parts[2] + "/" + parts[1] + "/" + parts[0];
        }
        return date;
    }

    private void styleModalButtons(androidx.appcompat.app.AlertDialog dialog) {
        Button positive = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
        Button negative = dialog.getButton(android.content.DialogInterface.BUTTON_NEGATIVE);
        configureModalButton(positive, "#287F8D");
        configureModalButton(negative, "#C51B20");
    }

    private void configureModalButton(Button button, String color) {
        if (button == null) return;
        android.graphics.drawable.GradientDrawable background =
                new android.graphics.drawable.GradientDrawable();
        background.setColor(android.graphics.Color.parseColor(color));
        background.setCornerRadius(dp(10));
        button.setBackground(background);
        button.setBackgroundTintList(null);
        button.setTextColor(android.graphics.Color.WHITE);
        button.setTextSize(14);
        button.setAllCaps(false);
        button.setTypeface(button.getTypeface(), android.graphics.Typeface.BOLD);
        button.setMinWidth(dp(96));
        button.setPadding(dp(14), dp(9), dp(14), dp(9));
        button.setStateListAnimator(null);
        button.setElevation(0);
        ViewGroup.LayoutParams layoutParams = button.getLayoutParams();
        if (layoutParams instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams params =
                    (LinearLayout.LayoutParams) layoutParams;
            params.height = dp(44);
            params.bottomMargin = dp(2);
            button.setLayoutParams(params);
        }
    }

    void showSpecialtyForm() {
        showSpecialtyForm(false, null, null);
    }

    interface SpecialtyFormListener {
        void onSave(ues.edu.sv.proyect_bs.modelos.CategoriaServicio specialty);
    }

    void showSpecialtyForm(boolean editing,
            ues.edu.sv.proyect_bs.modelos.CategoriaServicio specialty,
            SpecialtyFormListener listener) {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        ((TextView) content.findViewById(R.id.modal_admin_title)).setText(
                editing ? "Datos de la Especialidad" : "Ingresar Especialidad");
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);
        TextView label = new TextView(modalContext);
        label.setText("Especialidad");
        label.setTextColor(android.graphics.Color.parseColor("#263248"));
        label.setTextSize(14);
        fields.addView(label, new LinearLayout.LayoutParams(-1, dp(28)));
        EditText name = (EditText) createModalField(modalContext, "Especialidad",
                specialty == null ? "" : safeModalValue(specialty.nombre), false);
        fields.addView(name, new LinearLayout.LayoutParams(-1, dp(44)));
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(modalContext)
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            styleModalButtons(dialog);
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        String value = name.getText().toString().trim();
                        if (value.isEmpty()) {
                            showFieldError(name, "Ingresa el nombre de la especialidad.");
                            return;
                        }
                        if (listener != null) {
                            ues.edu.sv.proyect_bs.modelos.CategoriaServicio result =
                                    specialty == null
                                            ? new ues.edu.sv.proyect_bs.modelos.CategoriaServicio()
                                            : specialty;
                            result.nombre = value;
                            listener.onSave(result);
                        }
                        dialog.dismiss();
                    });
        });
        dialog.show();
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

    private void showDailyShiftForm(
            ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario row) {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        ((TextView) content.findViewById(R.id.modal_admin_title)).setText("Editar jornada");
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);
        String[] labels = {"Fecha", "Apertura", "Cierre", "Inicio Receso", "Fin Receso"};
        String[] values = {
                row.fecha,
                formatTime(row.apertura),
                formatTime(row.cierre),
                formatTime(row.inicio),
                formatTime(row.fin)
        };
        android.widget.EditText[] inputs = new android.widget.EditText[labels.length];
        for (int index = 0; index < labels.length; index++) {
            TextView label = new TextView(modalContext);
            label.setText(labels[index]);
            label.setTextColor(android.graphics.Color.parseColor("#263248"));
            label.setTextSize(14);
            LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(-1, -2);
            labelParams.topMargin = dp(8);
            fields.addView(label, labelParams);
            inputs[index] = (android.widget.EditText) createModalField(
                    modalContext, labels[index], values[index], false);
            fields.addView(inputs[index], new LinearLayout.LayoutParams(-1, dp(44)));
        }
        inputs[0].setEnabled(false);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(modalContext)
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            styleModalButtons(dialog);
            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(button -> {
                        String apertura = normalizeTime(inputs[1].getText().toString());
                        String cierre = normalizeTime(inputs[2].getText().toString());
                        String inicio = normalizeTime(inputs[3].getText().toString());
                        String fin = normalizeTime(inputs[4].getText().toString());
                        if (apertura == null || cierre == null) {
                            android.widget.Toast.makeText(this,
                                    "Ingrese apertura y cierre válidas", android.widget.Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }
                        row.apertura = apertura;
                        row.cierre = cierre;
                        row.receso = inicio != null && fin != null;
                        row.inicio = row.receso ? inicio : null;
                        row.fin = row.receso ? fin : null;
                        persistDailyShift(row);
                        dialog.dismiss();
                    });
        });
        dialog.show();
    }

    private void persistDailyShift(
            ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario row) {
        new Thread(() -> {
            ues.edu.sv.proyect_bs.data.AppDB.getInstance(this)
                    .horarioAtencionDiarioDAO().actualizar(row);
        }).start();

        AuthApiClient apiClient = new AuthApiClient();
        apiClient.saveDailyShift(SessionManager.getToken(this), row,
                new AuthApiClient.DailyShiftCallback() {
                    @Override
                    public void onSuccess(
                            ues.edu.sv.proyect_bs.modelos.HorarioAtencionDiario saved) {
                        runOnUiThread(() -> refreshSelectedWeek());
                    }

                    @Override
                    public void onError(String message) {
                        runOnUiThread(() -> android.widget.Toast.makeText(MainActivity.this,
                                message, android.widget.Toast.LENGTH_SHORT).show());
                    }
                });
    }

    private String formatTimeRange(String start, String end) {
        if (start == null || end == null || start.isEmpty() || end.isEmpty()) {
            return "Sin horario";
        }
        return formatTime(start) + " - " + formatTime(end);
    }

    private String formatTime(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String time = value.contains("T") ? value.substring(value.indexOf('T') + 1) : value;
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        String suffix = hour >= 12 ? "p.m." : "a.m.";
        int displayHour = hour % 12;
        if (displayHour == 0) {
            displayHour = 12;
        }
        return displayHour + ":" + String.format(java.util.Locale.US, "%02d", minute) + " " + suffix;
    }

    private String normalizeTime(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        try {
            java.util.Locale locale = java.util.Locale.US;
            java.text.SimpleDateFormat parser12 =
                    new java.text.SimpleDateFormat("h:mm a", locale);
            parser12.setLenient(false);
            java.util.Date parsed = parser12.parse(trimmed.replace("p.m.", "PM").replace("a.m.", "AM"));
            java.text.SimpleDateFormat output =
                    new java.text.SimpleDateFormat("HH:mm", java.util.Locale.US);
            return output.format(parsed);
        } catch (Exception ignored) {
            try {
                java.text.SimpleDateFormat parser24 =
                        new java.text.SimpleDateFormat("H:mm", java.util.Locale.US);
                parser24.setLenient(false);
                java.util.Date parsed = parser24.parse(trimmed);
                java.text.SimpleDateFormat output =
                        new java.text.SimpleDateFormat("HH:mm", java.util.Locale.US);
                return output.format(parsed);
            } catch (Exception ignoredAgain) {
                return null;
            }
        }
    }

    void showServiceForm() {
        showServiceForm(false, null, java.util.Collections.emptyList(), null);
    }

    void showServiceForm(boolean editing, ues.edu.sv.proyect_bs.modelos.Servicios service,
            java.util.List<ues.edu.sv.proyect_bs.modelos.CategoriaServicio> categories,
            ServiceFormListener listener) {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        ((TextView) content.findViewById(R.id.modal_admin_title)).setText(
                editing ? "Datos del Servicio" : "Ingresar Nuevo Servicio");
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);
        EditText name = (EditText) createModalField(modalContext, "Nombre del Servicio",
                service == null ? "" : safeModalValue(service.nombre_servicio), false);
        EditText price = (EditText) createModalField(modalContext, "Precio ($)",
                service == null || service.precio == null ? "" : String.valueOf(service.precio), false);
        EditText duration = (EditText) createModalField(modalContext, "Duración (min)",
                service == null || service.duracion == null ? "" : String.valueOf(service.duracion), false);
        price.setInputType(android.text.InputType.TYPE_CLASS_NUMBER
                | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        duration.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        addModalFieldLabel(fields, modalContext, "Nombre del Servicio");
        fields.addView(name, new LinearLayout.LayoutParams(-1, dp(44)));
        addModalFieldLabel(fields, modalContext, "Precio ($)");
        fields.addView(price, new LinearLayout.LayoutParams(-1, dp(44)));
        addModalFieldLabel(fields, modalContext, "Duración (min)");
        fields.addView(duration, new LinearLayout.LayoutParams(-1, dp(44)));
        TextView categoryLabel = new TextView(modalContext);
        categoryLabel.setText("Categoría");
        categoryLabel.setTextColor(android.graphics.Color.parseColor("#263248"));
        categoryLabel.setTextSize(14);
        fields.addView(categoryLabel, new LinearLayout.LayoutParams(-1, dp(28)));
        Spinner categorySpinner = new Spinner(modalContext);
        configureModalSpinnerAppearance(categorySpinner);
        String[] options = new String[categories.size() + 1];
        options[0] = "Seleccionar categoría";
        for (int index = 0; index < categories.size(); index++) {
            options[index + 1] = categories.get(index).nombre;
        }
        categorySpinner.setAdapter(createModalSpinnerAdapter(modalContext, options));
        if (service != null && service.idcategoria_servicio != null) {
            for (int index = 0; index < categories.size(); index++) {
                if (categories.get(index).idcategoria_servicio
                        == service.idcategoria_servicio) {
                    categorySpinner.setSelection(index + 1);
                    break;
                }
            }
        }
        fields.addView(categorySpinner, new LinearLayout.LayoutParams(-1, dp(44)));
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(modalContext)
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            styleModalButtons(dialog);
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        if (name.getText().toString().trim().isEmpty()) {
                            showFieldError(name, "Ingresa el nombre del servicio.");
                            return;
                        }
                        double parsedPrice;
                        try {
                            parsedPrice = Double.parseDouble(price.getText().toString().trim());
                            if (parsedPrice < 0) {
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException exception) {
                            showFieldError(price, "Ingresa un precio válido.");
                            return;
                        }
                        try {
                            int parsedDuration = Integer.parseInt(
                                    duration.getText().toString().trim());
                            if (parsedDuration <= 0) {
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException exception) {
                            showFieldError(duration, "Ingresa un tiempo válido en minutos.");
                            return;
                        }
                        if (categorySpinner.getSelectedItemPosition() <= 0) {
                            showFieldError(duration, "Selecciona una categoría válida.");
                            return;
                        }
                        if (listener != null) {
                            ues.edu.sv.proyect_bs.modelos.Servicios value =
                                    service == null
                                            ? new ues.edu.sv.proyect_bs.modelos.Servicios()
                                            : service;
                            value.nombre_servicio = name.getText().toString().trim();
                            value.precio = parsedPrice;
                            value.duracion = Integer.parseInt(
                                    duration.getText().toString().trim());
                            if (value.estado == null) {
                                value.estado = Boolean.TRUE;
                            }
                            value.idcategoria_servicio = categories.get(
                                    categorySpinner.getSelectedItemPosition() - 1)
                                    .idcategoria_servicio;
                            listener.onSave(value);
                        }
                        dialog.dismiss();
                    });
        });
        dialog.show();
    }

    private void addModalFieldLabel(LinearLayout fields, android.content.Context context,
            String text) {
        TextView label = new TextView(context);
        label.setText(text);
        label.setTextColor(android.graphics.Color.parseColor("#263248"));
        label.setTextSize(14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(28));
        params.topMargin = dp(8);
        fields.addView(label, params);
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
        showStandardForm("Asignar Beneficio", "Guardar Beneficio",
                new String[]{"Categoría", "Selecciona Servicio", "Coste Pts"},
                null);
    }

    void showBenefitForm(boolean editing, ues.edu.sv.proyect_bs.modelos.Beneficios benefit,
            java.util.List<ues.edu.sv.proyect_bs.modelos.CategoriaServicio> categories,
            java.util.List<ues.edu.sv.proyect_bs.modelos.Servicios> services,
            BenefitFormListener listener) {
        android.content.Context modalContext = createLightModalContext();
        View content = android.view.LayoutInflater.from(modalContext)
                .inflate(R.layout.modal_admin_standard, null);
        ((TextView) content.findViewById(R.id.modal_admin_title)).setText("Asignar Beneficio");
        LinearLayout fields = content.findViewById(R.id.modal_admin_fields);

        LinearLayout categoryActiveRow = createClientModalRow(modalContext);
        Spinner categorySpinner = (Spinner) createLabeledClientModalSpinner(
                modalContext, categoryActiveRow, "Categoría");
        String[] categoryOptions = new String[categories.size() + 1];
        categoryOptions[0] = "Seleccionar categoría";
        for (int index = 0; index < categories.size(); index++) {
            categoryOptions[index + 1] = categories.get(index).nombre;
        }
        categorySpinner.setAdapter(createModalSpinnerAdapter(modalContext, categoryOptions));
        if (benefit == null && !categories.isEmpty()) {
            categorySpinner.setSelection(1);
        }

        LinearLayout activeColumn = new LinearLayout(modalContext);
        activeColumn.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams activeParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        activeParams.setMarginStart(dp(6));
        activeColumn.setLayoutParams(activeParams);
        TextView activeLabel = new TextView(modalContext);
        activeLabel.setText("Activo");
        activeLabel.setTextColor(android.graphics.Color.parseColor("#263248"));
        activeLabel.setTextSize(14);
        activeLabel.setPadding(0, dp(8), 0, dp(4));
        activeColumn.addView(activeLabel);
        SwitchMaterial activeSwitch = new SwitchMaterial(modalContext);
        activeSwitch.setChecked(benefit == null || Boolean.TRUE.equals(benefit.estado));
        activeSwitch.setThumbTintList(android.content.res.ColorStateList.valueOf(
                android.graphics.Color.WHITE));
        activeSwitch.setTrackTintList(new android.content.res.ColorStateList(
                new int[][]{
                        new int[]{android.R.attr.state_checked},
                        new int[]{}
                },
                new int[]{
                        android.graphics.Color.parseColor("#287F8D"),
                        android.graphics.Color.parseColor("#B7BDC5")
                }));
        activeSwitch.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                dp(44)));
        activeColumn.addView(activeSwitch);
        categoryActiveRow.addView(activeColumn);
        fields.addView(categoryActiveRow);

        LinearLayout serviceRow = createClientModalRow(modalContext);
        Spinner serviceSpinner = (Spinner) createLabeledClientModalSpinner(
                modalContext, serviceRow, "Selecciona Servicio");
        java.util.List<ues.edu.sv.proyect_bs.modelos.Servicios> filteredServices =
                new java.util.ArrayList<>();
        java.lang.Runnable[] updateServiceOptions = new java.lang.Runnable[1];
        updateServiceOptions[0] = () -> {
            filteredServices.clear();
            int categoryPosition = categorySpinner.getSelectedItemPosition();
            if (categoryPosition > 0) {
                long categoryId = categories.get(categoryPosition - 1).idcategoria_servicio;
                for (ues.edu.sv.proyect_bs.modelos.Servicios service : services) {
                    if (service.idcategoria_servicio != null
                            && service.idcategoria_servicio == categoryId) {
                        filteredServices.add(service);
                    }
                }
            }
            String[] options = new String[filteredServices.size() + 1];
            options[0] = "Sin servicio";
            for (int index = 0; index < filteredServices.size(); index++) {
                options[index + 1] = safeModalValue(
                        filteredServices.get(index).nombre_servicio);
            }
            serviceSpinner.setAdapter(createModalSpinnerAdapter(modalContext, options));
        };
        updateServiceOptions[0].run();
        categorySpinner.setOnItemSelectedListener(
                new android.widget.AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(android.widget.AdapterView<?> parent,
                                               android.view.View view,
                                               int position, long id) {
                        updateServiceOptions[0].run();
                    }
                    @Override
                    public void onNothingSelected(android.widget.AdapterView<?> parent) {}
                });
        fields.addView(serviceRow);

        LinearLayout costRow = createClientModalRow(modalContext);
        EditText cost = (EditText) createLabeledClientModalField(
                modalContext, costRow, "Coste Pts",
                benefit == null || benefit.costepuntos == null
                        ? "" : String.valueOf(benefit.costepuntos));
        cost.setHint("25.00");
        cost.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        View costColumn = costRow.getChildAt(0);
        if (costColumn != null) {
            LinearLayout.LayoutParams costParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.5f);
            costParams.setMarginEnd(dp(6));
            costColumn.setLayoutParams(costParams);
        }
        fields.addView(costRow);

        if (benefit != null) {
            if (benefit.categoria != null) {
                for (int index = 0; index < categories.size(); index++) {
                    if (categories.get(index).idcategoria_servicio == benefit.categoria) {
                        categorySpinner.setSelection(index + 1);
                        break;
                    }
                }
            }
            if (benefit.servicio != null) {
                for (int index = 0; index < filteredServices.size(); index++) {
                    if (filteredServices.get(index).idservicio == benefit.servicio) {
                        serviceSpinner.setSelection(index + 1);
                        break;
                    }
                }
            }
        }

        androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(modalContext)
                        .setView(content)
                        .setNegativeButton("Cancelar", null)
                        .setPositiveButton(editing ? "Guardar Cambios" : "Guardar Beneficio", null)
                        .create();
        dialog.setOnShowListener(ignored -> {
            styleModalButtons(dialog);
            Button saveButton = dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
            saveButton.setOnClickListener(v -> {
                        int costPoints;
                        try {
                            costPoints = Integer.parseInt(cost.getText().toString().trim());
                            if (costPoints <= 0) throw new NumberFormatException();
                        } catch (NumberFormatException exception) {
                            showFieldError(cost, "Ingresa un coste válido.");
                            return;
                        }
                        if (categorySpinner.getSelectedItemPosition() <= 0) {
                            showFieldError(cost, "Selecciona una categoría.");
                            return;
                        }
                        ues.edu.sv.proyect_bs.modelos.Beneficios value =
                                new ues.edu.sv.proyect_bs.modelos.Beneficios();
                        if (benefit != null) {
                            value.idbeneficio = benefit.idbeneficio;
                            value.estado = benefit.estado;
                            value.costepuntos = benefit.costepuntos;
                            value.categoria = benefit.categoria;
                            value.servicio = benefit.servicio;
                        }
                        value.costepuntos = costPoints;
                        value.estado = activeSwitch.isChecked();
                        value.categoria = categorySpinner.getSelectedItemPosition() <= 0
                                ? null : categories.get(
                                        categorySpinner.getSelectedItemPosition() - 1)
                                        .idcategoria_servicio;
                        value.servicio = serviceSpinner.getSelectedItemPosition() <= 0
                                ? null : filteredServices.get(
                                        serviceSpinner.getSelectedItemPosition() - 1)
                                        .idservicio;
                        if (listener != null) {
                            saveButton.setEnabled(false);
                            listener.onSave(value, dialog::dismiss,
                                    () -> saveButton.setEnabled(true));
                        }
                    });
        });
        dialog.show();
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
        EditText[] inputs = new EditText[7];
        final Spinner[] documentSpinner = {null};

        LinearLayout nameRow = createClientModalRow(modalContext);
        inputs[0] = (EditText) createLabeledClientModalField(
                modalContext, nameRow, "Nombres", profile.names);
        inputs[1] = (EditText) createLabeledClientModalField(
                modalContext, nameRow, "Apellidos", profile.lastNames);
        configureNameInput(inputs[0]);
        configureNameInput(inputs[1]);
        fields.addView(nameRow);

        LinearLayout documentRow = createClientModalRow(modalContext);
        documentSpinner[0] = (Spinner) createLabeledClientModalSpinner(
                modalContext, documentRow, "Tipo Documento");
        String[] documentOptions = new String[documents.size() + 1];
        documentOptions[0] = "Seleccionar";
        for (int index = 0; index < documents.size(); index++) {
            documentOptions[index + 1] = documents.get(index).nombredocumento;
        }
        documentSpinner[0].setAdapter(createModalSpinnerAdapter(modalContext, documentOptions));
        int selectedDocument = 0;
        for (int index = 0; index < documents.size(); index++) {
            if (documents.get(index).iddocumento == profile.documentId) {
                selectedDocument = index + 1;
                break;
            }
        }
        documentSpinner[0].setSelection(selectedDocument);
        inputs[3] = (EditText) createLabeledClientModalField(
                modalContext, documentRow, "Numero Doc.", profile.document);
        fields.addView(documentRow);

        LinearLayout contactRow = createClientModalRow(modalContext);
        inputs[4] = (EditText) createLabeledClientModalField(
                modalContext, contactRow, "Correo Electronico", profile.email);
        inputs[4].setEnabled(false);
        inputs[4].setFocusable(false);
        inputs[4].setAlpha(0.65f);
        inputs[4].setTextColor(android.graphics.Color.parseColor("#6F6F6F"));
        inputs[5] = (EditText) createLabeledClientModalField(
                modalContext, contactRow, "Numero Celular", profile.phone);
        configurePhoneInput(inputs[5]);
        fields.addView(contactRow);

        LinearLayout birthRow = createClientModalRow(modalContext);
        inputs[6] = (EditText) createLabeledClientModalField(
                modalContext, birthRow, "Fecha Nacimiento", toDisplayDate(profile.birthDate));
        configureModalDateField(modalContext, inputs[6]);
        fields.addView(birthRow);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(modalContext)
                .setView(content)
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Guardar", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            styleModalButtons(dialog);
            dialog.getButton(android.content.DialogInterface.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {
                        int selected = documentSpinner[0] == null
                                ? 0 : documentSpinner[0].getSelectedItemPosition() - 1;
                        if (selected < 0 || selected >= documents.size()) {
                            showFieldError(documentSpinner[0],
                                    "Selecciona un tipo de documento válido.");
                            return;
                        }
                        long documentId = selected >= 0 && selected < documents.size()
                                ? documents.get(selected).iddocumento : 0L;
                        String names = inputs[0].getText().toString().trim();
                        String lastNames = inputs[1].getText().toString().trim();
                        String phone = inputs[5].getText().toString().trim();
                        String displayBirthDate = inputs[6].getText().toString().trim();
                        if (!isValidName(names)) {
                            showFieldError(inputs[0], "Usa sólo letras y no lo dejes vacío.");
                            return;
                        }
                        if (!isValidName(lastNames)) {
                            showFieldError(inputs[1], "Usa sólo letras y no lo dejes vacío.");
                            return;
                        }
                        String document = inputs[3].getText().toString().trim();
                        if (document.length() < 8) {
                            showFieldError(inputs[3],
                                    "El identificador es obligatorio y debe tener al menos 8 caracteres.");
                            return;
                        }
                        if (!phone.matches("\\d{8}")) {
                            showFieldError(inputs[5], "Debe contener exactamente 8 números.");
                            return;
                        }
                        if (!isAtLeastSixteen(displayBirthDate)) {
                            showFieldError(inputs[6], "La edad mínima es de 16 años.");
                            return;
                        }
                        listener.onSave(names, lastNames, documentId,
                                document,
                                profile.email,
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

    private void showFieldError(View field, String message) {
        if (field instanceof EditText) {
            showFieldError((EditText) field, message);
            return;
        }
        field.setContentDescription(message);
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show();
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

    void setSelectedBenefitClientId(long clientId) {
        selectedBenefitClientId = clientId;
    }

    long getSelectedBenefitClientId() {
        return selectedBenefitClientId;
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
            styleModalButtons(dialog);
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

    private void configureModalTimeField(android.content.Context context, EditText field) {
        field.setFocusable(false);
        field.setFocusableInTouchMode(false);
        field.setClickable(true);
        field.setInputType(android.text.InputType.TYPE_NULL);
        field.setContentDescription("Seleccionar hora");
        field.setOnClickListener(v -> showModalTimePicker(context, field));
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
