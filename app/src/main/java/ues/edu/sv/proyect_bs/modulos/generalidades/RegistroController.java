package ues.edu.sv.proyect_bs;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class RegistroController extends GeneralidadesViewController {
    private final AuthApiClient authApiClient = new AuthApiClient();
    private final List<AuthApiClient.CatalogItem> documentTypes = new ArrayList<>();
    private final List<AuthApiClient.CatalogItem> serviceCategories = new ArrayList<>();

    RegistroController(GeneralidadesActivity host) {
        super(host);
    }

    @Override
    void configure() {
        host.findViewById(R.id.general_register_back).setOnClickListener(v ->
                host.showScreen(R.layout.general_login));
        configureField(R.id.register_names, "Nombres", "Jose Carlos");
        configureField(R.id.register_last_names, "Apellidos", "Canales");
        configureField(R.id.register_document_type, "Tipo Documento", "DUI");
        configureField(R.id.register_document_number, "Numero Doc.", "000000-0");
        configureField(R.id.register_email, "Correo Electronico", "Example@ex...");
        configureField(R.id.register_phone, "Numero Celular", "77223344");
        configureField(R.id.register_birth_date, "Fecha Nacimiento", "03/02/2000");
        configureField(R.id.register_service, "Servicio Preferencial", "Corte");
        configureField(R.id.register_password, "Contraseña", "**************");
        setInputType(R.id.register_document_number,
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_NORMAL);
        setInputType(R.id.register_email,
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        setInputType(R.id.register_phone, android.text.InputType.TYPE_CLASS_PHONE);
        configureSpinnerField(R.id.register_document_type, new String[0], new long[0]);
        configureSpinnerField(R.id.register_service, new String[0], new long[0]);
        configureDateField(R.id.register_birth_date);
        configurePasswordField(R.id.register_password);
        host.findViewById(R.id.general_register_submit).setOnClickListener(v -> submit());
        applyTheme(host.findViewById(R.id.general_register_root));
        loadCatalogs();
    }

    private void loadCatalogs() {
        authApiClient.loadCatalog("/tipos-documento/catalogo", new AuthApiClient.CatalogCallback() {
            @Override
            public void onSuccess(List<AuthApiClient.CatalogItem> items) {
                documentTypes.clear();
                documentTypes.addAll(items);
                configureCatalogSpinner(R.id.register_document_type, documentTypes);
            }

            @Override
            public void onError(String message) {
                host.showRegistrationError(message);
            }
        });
        authApiClient.loadCatalog("/categorias-servicio/catalogo", new AuthApiClient.CatalogCallback() {
            @Override
            public void onSuccess(List<AuthApiClient.CatalogItem> items) {
                serviceCategories.clear();
                serviceCategories.addAll(items);
                configureCatalogSpinner(R.id.register_service, serviceCategories);
            }

            @Override
            public void onError(String message) {
                host.showRegistrationError(message);
            }
        });
    }

    private void configureCatalogSpinner(int rootId, List<AuthApiClient.CatalogItem> items) {
        String[] names = new String[items.size()];
        long[] ids = new long[items.size()];
        for (int index = 0; index < items.size(); index++) {
            names[index] = items.get(index).name;
            ids[index] = items.get(index).id;
        }
        configureSpinnerField(rootId, names, ids);
    }

    private void submit() {
        boolean valid = true;
        valid &= validateRequired(R.id.register_names, "Ingresa tus nombres");
        valid &= validatePattern(R.id.register_names, "[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,}",
                "Ingresa un nombre válido");
        valid &= validateRequired(R.id.register_last_names, "Ingresa tus apellidos");
        valid &= validatePattern(R.id.register_last_names, "[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,}",
                "Ingresa apellidos válidos");
        valid &= validateRequired(R.id.register_document_type, "Selecciona un tipo");
        valid &= validateRequired(R.id.register_document_number, "Ingresa el documento");
        valid &= validatePattern(R.id.register_document_number, "[0-9A-Za-z-]{5,20}",
                "Ingresa un documento válido");
        valid &= validateRequired(R.id.register_email, "Ingresa tu correo");
        valid &= validatePattern(R.id.register_email,
                "[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}",
                "Ingresa un correo válido");
        valid &= validateRequired(R.id.register_phone, "Ingresa tu número");
        valid &= validatePattern(R.id.register_phone, "[0-9]{8}", "Usa 8 dígitos");
        valid &= validateRequired(R.id.register_birth_date, "Selecciona tu fecha");
        valid &= validateBirthDate(R.id.register_birth_date);
        valid &= validateRequired(R.id.register_service, "Selecciona un servicio");
        valid &= validateRequired(R.id.register_password, "Ingresa una contraseña");
        valid &= validatePattern(R.id.register_password, ".{8,}",
                "Usa al menos 8 caracteres");
        long idDocumento = getSelectedSpinnerId(R.id.register_document_type);
        long idCategoria = getSelectedSpinnerId(R.id.register_service);
        if (idDocumento == 0L || idCategoria == 0L) {
            host.showRegistrationError("Espera a que carguen los catálogos y selecciona las opciones.");
            return;
        }
        if (!valid) {
            return;
        }

        EditText birthDate = getField(R.id.register_birth_date);
        String fechaNacimiento;
        try {
            SimpleDateFormat apiFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat screenFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            screenFormat.setLenient(false);
            fechaNacimiento = apiFormat.format(screenFormat.parse(
                    birthDate.getText().toString().trim()));
        } catch (ParseException exception) {
            host.showRegistrationError("La fecha de nacimiento no es válida.");
            return;
        }

        View submit = host.findViewById(R.id.general_register_submit);
        submit.setEnabled(false);
        ((TextView) submit).setText("Registrando...");
        authApiClient.register(
                getField(R.id.register_names).getText().toString().trim(),
                getField(R.id.register_last_names).getText().toString().trim(),
                idDocumento,
                getField(R.id.register_document_number).getText().toString().trim(),
                getField(R.id.register_email).getText().toString().trim(),
                getField(R.id.register_phone).getText().toString().trim(),
                fechaNacimiento,
                idCategoria,
                getField(R.id.register_password).getText().toString(),
                new AuthApiClient.RegisterCallback() {
                    @Override
                    public void onSuccess() {
                        submit.setEnabled(true);
                        ((TextView) submit).setText("Crear cuenta");
                        Toast.makeText(host, "Cuenta creada correctamente.",
                                Toast.LENGTH_LONG).show();
                        host.showScreen(R.layout.general_login);
                    }

                    @Override
                    public void onError(String message) {
                        submit.setEnabled(true);
                        ((TextView) submit).setText("Crear cuenta");
                        host.showRegistrationError(message);
                    }
                });
    }
}
