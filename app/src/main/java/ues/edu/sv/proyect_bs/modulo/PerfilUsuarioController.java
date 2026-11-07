package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.widget.TextView;
import com.google.android.material.switchmaterial.SwitchMaterial;

final class PerfilUsuarioController implements AdminViewController {
    private final MainActivity host;

    PerfilUsuarioController(MainActivity host) {
        this.host = host;
    }

    @Override
    public void configure() {
        host.getNavigationController().configureToolbar(R.id.toolbar_profile);
        host.findViewById(R.id.action_edit_profile).setOnClickListener(v ->
                host.showProfileForm());
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
    }
}
