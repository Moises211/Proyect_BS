package ues.edu.sv.proyect_bs;

final class RegistroController extends GeneralidadesViewController {
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
        configureSpinnerField(R.id.register_document_type,
                new String[]{"DUI", "Pasaporte", "Carnet de residente"});
        configureSpinnerField(R.id.register_service,
                new String[]{"Corte de cabello", "Barba", "Corte y barba"});
        configureDateField(R.id.register_birth_date);
        configurePasswordField(R.id.register_password);
        host.findViewById(R.id.general_register_submit).setOnClickListener(v -> submit());
        applyTheme(host.findViewById(R.id.general_register_root));
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
        if (valid) {
            host.showScreen(R.layout.general_login);
        }
    }
}
