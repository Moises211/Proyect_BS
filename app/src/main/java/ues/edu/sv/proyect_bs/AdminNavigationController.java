package ues.edu.sv.proyect_bs;

import android.graphics.Color;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

final class AdminNavigationController {
    private final MainActivity host;
    private int selectedMenuItemId = R.id.menu_admin_home;
    private int currentLayout = R.layout.pantalla_inicial;
    private boolean syncingBottomNavigation;

    AdminNavigationController(MainActivity host) {
        this.host = host;
    }

    void navigateTo(int layout) {
        currentLayout = layout;
        selectedMenuItemId = getMenuItemForLayout(layout);
        host.setContentView(layout);
        syncBottomNavigation(layout);
        configureNavigation();
        host.configureManagementScreen(layout);
    }

    int getCurrentLayout() {
        return currentLayout;
    }

    void configureToolbar(int toolbarId) {
        View toolbarView = host.findViewById(toolbarId);
        if (toolbarView instanceof MaterialToolbar) {
            ((MaterialToolbar) toolbarView).setNavigationOnClickListener(this::showAdminMenu);
        }
    }

    private void configureNavigation() {
        BottomNavigationView navigation = host.findViewById(R.id.admin_bottom_navigation);
        if (navigation == null) {
            return;
        }
        navigation.setOnItemSelectedListener(item -> {
            if (syncingBottomNavigation) {
                return true;
            }
            if (item.getItemId() == R.id.nav_home) {
                navigateTo(R.layout.pantalla_inicial);
            } else if (item.getItemId() == R.id.nav_cuts) {
                navigateTo(R.layout.pantalla_cortes_servicios);
            } else if (item.getItemId() == R.id.nav_staff) {
                navigateTo(R.layout.pantalla_personal_turnos);
            } else if (item.getItemId() == R.id.nav_clients) {
                navigateTo(R.layout.pantalla_control_citas);
            } else if (item.getItemId() == R.id.nav_profile) {
                navigateTo(R.layout.pantalla_perfil_usuario);
            } else {
                return false;
            }
            return true;
        });
    }

    private void syncBottomNavigation(int layout) {
        BottomNavigationView navigation = host.findViewById(R.id.admin_bottom_navigation);
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

    private void showAdminMenu(View anchor) {
        final android.app.Dialog dialog = new android.app.Dialog(host);
        dialog.setContentView(R.layout.admin_side_menu);
        dialog.show();
        applyMenuSelection(dialog.findViewById(android.R.id.content));
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setDimAmount(0.35f);
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setGravity(Gravity.START);
            window.setLayout(
                    (int) (host.getResources().getDisplayMetrics().widthPixels * 0.94f),
                    WindowManager.LayoutParams.MATCH_PARENT);
        }
        View menuRoot = dialog.findViewById(android.R.id.content);
        if (host.isDarkMode() && menuRoot != null) {
            menuRoot.setBackgroundColor(Color.rgb(18, 19, 21));
            host.applyDarkModeToView(menuRoot);
            applyMenuSelection(menuRoot);
        }
        setMenuAction(dialog, R.id.menu_admin_home, R.layout.pantalla_inicial);
        setMenuAction(dialog, R.id.menu_admin_clients, R.layout.pantalla_control_citas);
        setMenuAction(dialog, R.id.menu_admin_staff, R.layout.pantalla_personal_turnos);
        setMenuAction(dialog, R.id.menu_admin_services, R.layout.pantalla_cortes_servicios);
        setMenuAction(dialog, R.id.menu_admin_profile, R.layout.pantalla_perfil_usuario);
        View logout = dialog.findViewById(R.id.menu_admin_logout);
        if (logout != null) {
            logout.setOnClickListener(v -> {
                dialog.dismiss();
                String token = SessionManager.getToken(host);
                SessionManager.clear(host);
                new AuthApiClient().logout(token, () -> host.openGeneralidades());
            });
        }
    }

    private void setMenuAction(android.app.Dialog dialog, int menuId, int layout) {
        View menuItem = dialog.findViewById(menuId);
        if (menuItem != null) {
            menuItem.setOnClickListener(v -> {
                dialog.dismiss();
                navigateTo(layout);
            });
        }
    }

    private void applyMenuSelection(View menuRoot) {
        if (menuRoot == null) {
            return;
        }
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
                    : (host.isDarkMode() ? R.drawable.bg_menu_item_dark : R.drawable.bg_menu_item));
            ImageView icon = findMenuIcon(item);
            if (icon != null) {
                icon.setImageTintList(ColorStateList.valueOf(
                        Color.parseColor(selected ? "#C51B20" : "#197A8A")));
            }
            TextView title = findFirstMenuText(item);
            if (title != null) {
                title.setTextColor(Color.parseColor(
                        selected ? "#C51B20" : (host.isDarkMode() ? "#F4F4F4" : "#242424")));
            }
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
}
