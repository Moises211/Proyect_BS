package ues.edu.sv.proyect_bs;

import com.google.android.material.switchmaterial.SwitchMaterial;

final class ConfiguracionController extends GeneralidadesViewController {
    ConfiguracionController(GeneralidadesActivity host) {
        super(host);
    }

    @Override
    void configure() {
        host.findViewById(R.id.general_settings_back).setOnClickListener(v ->
                host.showScreen(R.layout.general_login));
        SwitchMaterial themeSwitch = host.findViewById(R.id.general_settings_theme_switch);
        SwitchMaterial rememberSwitch = host.findViewById(R.id.general_settings_remember_switch);
        themeSwitch.setChecked(host.isDarkMode());
        themeSwitch.setOnCheckedChangeListener((button, checked) -> {
            if (checked != host.isDarkMode()) {
                host.setDarkMode(checked);
            }
        });
        host.findViewById(R.id.general_settings_save).setOnClickListener(v ->
                host.showScreen(R.layout.general_login));
        applyTheme(host.findViewById(R.id.general_settings_root));
        rememberSwitch.setChecked(false);
    }
}
