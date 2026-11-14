package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.widget.Toast;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import ues.edu.sv.proyect_bs.data.AppDB;
import ues.edu.sv.proyect_bs.modelos.Admins;
import ues.edu.sv.proyect_bs.modelos.Persona;
import ues.edu.sv.proyect_bs.modelos.TipoDocumento;

final class PerfilUsuarioController implements AdminViewController {
    private final MainActivity host;
    private final AuthApiClient authApiClient = new AuthApiClient();
    private final AppDB database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    PerfilUsuarioController(MainActivity host) {
        this.host = host;
        database = AppDB.getInstance(host);
    }

    @Override
    public void configure() {
        host.getNavigationController().configureToolbar(R.id.toolbar_profile);
        host.findViewById(R.id.action_edit_profile).setOnClickListener(v ->
                showProfileForm());
        host.findViewById(R.id.action_change_password).setOnClickListener(v ->
                host.showPasswordForm());
        host.configureSpinner(R.id.profile_language_spinner, Color.WHITE,
                "Español", "English");
        SwitchMaterial themeSwitch = host.findViewById(R.id.profile_theme_switch);
        TextView themeLabel = host.findViewById(R.id.profile_theme_label);
        themeSwitch.setChecked(host.isDarkMode());
        themeLabel.setText(host.isDarkMode()
                ? "Modo Oscuro\nAlternar Contraste de la interfaz"
                : "Modo Claro\nAlternar Contraste de la interfaz");
        themeSwitch.setOnCheckedChangeListener((buttonView, checked) ->
                host.setDarkMode(checked));
        loadProfile();
    }

    private void showProfileForm() {
        long personId = SessionManager.getPersonId(host);
        executor.execute(() -> {
            java.util.List<TipoDocumento> documents = database.tipoDocumentoDAO().obtenerTodos();
            Persona person = database.personaDAO().obtenerPorId(personId);
            AuthApiClient.AdminProfile current = getLoadedProfile();
            long localDocumentId = person != null && person.iddocumento != null
                    ? person.iddocumento : 0L;
            if (localDocumentId > 0L || current == null) {
                if (current == null) {
                    String token = SessionManager.getToken(host);
                    authApiClient.loadAdminProfile(token, SessionManager.getUserId(host),
                            personId, new AuthApiClient.AdminProfileCallback() {
                                @Override
                                public void onSuccess(AuthApiClient.AdminProfile profile) {
                                    host.setLoadedAdminProfile(profile);
                                    host.runOnUiThread(() ->
                                            host.showProfileForm(documents,
                                                    PerfilUsuarioController.this::saveProfile));
                                }

                                @Override
                                public void onError(String message) {
                                    host.runOnUiThread(() -> Toast.makeText(host, message,
                                            Toast.LENGTH_LONG).show());
                                }
                            });
                    return;
                }
                host.runOnUiThread(() -> host.showProfileForm(documents, this::saveProfile));
                return;
            }
            String token = SessionManager.getToken(host);
            authApiClient.loadAdminProfile(token, SessionManager.getUserId(host), personId,
                    new AuthApiClient.AdminProfileCallback() {
                        @Override
                        public void onSuccess(AuthApiClient.AdminProfile profile) {
                            if (person != null && profile.documentId > 0L) {
                                person.iddocumento = profile.documentId;
                                executor.execute(() -> database.personaDAO().insertar(person));
                            }
                            host.setLoadedAdminProfile(profile);
                            host.runOnUiThread(() ->
                                    host.showProfileForm(documents, PerfilUsuarioController.this::saveProfile));
                        }

                        @Override
                        public void onError(String message) {
                            host.runOnUiThread(() -> {
                                Toast.makeText(host, message, Toast.LENGTH_LONG).show();
                                host.showProfileForm(documents, PerfilUsuarioController.this::saveProfile);
                            });
                        }
                    });
        });
    }

    private AuthApiClient.AdminProfile getLoadedProfile() {
        return host.getLoadedAdminProfile();
    }

    private void saveProfile(String names, String lastNames, long documentId,
                             String document, String email, String phone, String birthDate) {
        if (names.isEmpty() || lastNames.isEmpty() || email.isEmpty()) {
            Toast.makeText(host, "Completa nombres, apellidos y correo.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        long userId = SessionManager.getUserId(host);
        long personId = SessionManager.getPersonId(host);
        String token = SessionManager.getToken(host);
        executor.execute(() -> {
            Persona currentPerson = database.personaDAO().obtenerPorId(personId);
            String personEmail = currentPerson == null ? "" : value(currentPerson.correo_electronico);
            Admins currentAdmin = database.adminsDAO().obtenerPorId(userId);
            if (currentAdmin == null) {
                currentAdmin = new Admins();
                currentAdmin.idadmin = userId;
                currentAdmin.idpersona = personId;
                currentAdmin.estado = true;
            }
            Admins adminForUpdate = currentAdmin;
            authApiClient.updateAdminProfile(token, userId, personId, documentId,
                    names, lastNames, document, email, personEmail, phone, birthDate,
                    new AuthApiClient.ProfileUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        Persona updated = currentPerson == null ? new Persona() : currentPerson;
                        updated.idpersona = personId;
                        updated.nombres = names;
                        updated.apellidos = lastNames;
                        updated.identificadordoc = document;
                        updated.correo_electronico = personEmail;
                        updated.numero_celular = phone;
                        updated.fecha_nacimiento = birthDate;
                        updated.iddocumento = documentId > 0L ? documentId : null;
                        adminForUpdate.correoelectronico = email;
                        Admins updatedAdmin = adminForUpdate;
                        AuthApiClient.AdminProfile profile =
                                new AuthApiClient.AdminProfile(names, lastNames, email,
                                        document, phone, birthDate, documentId);
                        executor.execute(() -> {
                            database.personaDAO().insertar(updated);
                            database.adminsDAO().insertar(updatedAdmin);
                            host.runOnUiThread(() -> {
                                showProfile(profile);
                                Toast.makeText(host, "Perfil actualizado correctamente.",
                                        Toast.LENGTH_LONG).show();
                            });
                        });
                    }

                    @Override
                    public void onError(String message) {
                        Toast.makeText(host, message, Toast.LENGTH_LONG).show();
                    }
                });
        });
    }

    private void loadProfile() {
        TextView identity = host.findViewById(R.id.profile_identity);
        TextView avatar = host.findViewById(R.id.profile_avatar);
        identity.setText("Cargando perfil...");
        avatar.setText("--");
        String token = SessionManager.getToken(host);
        if (token == null || token.isEmpty()) {
            identity.setText("No hay una sesión activa.");
            return;
        }
        long userId = SessionManager.getUserId(host);
        long personId = SessionManager.getPersonId(host);
        executor.execute(() -> {
            Admins admin = database.adminsDAO().obtenerPorId(userId);
            Persona person = database.personaDAO().obtenerPorId(personId);
            if (admin != null && person != null) {
                AuthApiClient.AdminProfile profile = new AuthApiClient.AdminProfile(
                        value(person.nombres), value(person.apellidos),
                        firstNonEmpty(admin.correoelectronico, person.correo_electronico),
                        value(person.identificadordoc), value(person.numero_celular),
                        value(person.fecha_nacimiento),
                        person.iddocumento == null ? 0L : person.iddocumento);
                host.runOnUiThread(() -> showProfile(profile));
                return;
            }
            authApiClient.loadAdminProfile(token, userId, personId,
                    new AuthApiClient.AdminProfileCallback() {
                        @Override
                        public void onSuccess(AuthApiClient.AdminProfile profile) {
                            showProfile(profile);
                        }

                        @Override
                        public void onError(String message) {
                            identity.setText("No se pudo cargar el perfil.");
                            avatar.setText("--");
                            Toast.makeText(host, message, Toast.LENGTH_LONG).show();
                        }
                    });
        });
    }

    private String value(String text) {
        return text == null ? "" : text;
    }

    private String firstNonEmpty(String first, String second) {
        return first != null && !first.trim().isEmpty() ? first : value(second);
    }

    private void showProfile(AuthApiClient.AdminProfile profile) {
        host.setLoadedAdminProfile(profile);
        TextView identity = host.findViewById(R.id.profile_identity);
        String fullName = (profile.names + " " + profile.lastNames).trim();
        String email = profile.email.isEmpty() ? "Correo no disponible" : profile.email;
        identity.setText("●  " + fullName
                + "\n☆  Administrador\n✉  " + email);
        TextView avatar = host.findViewById(R.id.profile_avatar);
        avatar.setText(initials(profile.names, profile.lastNames));
    }

    private String initials(String names, String lastNames) {
        String first = names == null || names.trim().isEmpty()
                ? "" : names.trim().substring(0, 1);
        String last = lastNames == null || lastNames.trim().isEmpty()
                ? "" : lastNames.trim().substring(0, 1);
        return (first + last).toUpperCase();
    }
}
