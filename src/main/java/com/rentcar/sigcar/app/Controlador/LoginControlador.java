package com.rentcar.sigcar.app.Controlador;

import com.rentcar.sigcar.app.DTO.AuthResponseDTO;
import com.rentcar.sigcar.app.DTO.LoginDTO;
import com.rentcar.sigcar.app.Servicio.UsuarioServicio;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @GetMapping("/login")
    public String mostrarLogin() { return "login"; }

    @GetMapping("/registro")
    public String mostrarRegistro() { return "registro"; }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String correo,
            @RequestParam String password,
            HttpServletResponse response,
            Model model) {
        try {
            LoginDTO dto = new LoginDTO();
            dto.setCorreo(correo);
            dto.setPassword(password);

            AuthResponseDTO auth = usuarioServicio.login(dto);

            Cookie cookie = new Cookie("jwt_token", auth.getToken());
            cookie.setHttpOnly(false);
            cookie.setPath("/");
            cookie.setMaxAge(86400);
            response.addCookie(cookie);

            return switch (auth.getRol()) {
                case "ADMINISTRADOR" -> "redirect:/dashboard/admin";
                case "AGENTE"        -> "redirect:/dashboard/agente";
                case "CLIENTE"       -> "redirect:/dashboard/cliente";
                case "MECANICO"      -> "redirect:/dashboard/mecanico";
                case "AUDITOR"       -> "redirect:/dashboard/auditor";
                default              -> "redirect:/";
            };
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt_token", null);
        cookie.setHttpOnly(false);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/";
    }

    // ── DASHBOARDS ──────────────────────────────────────
    @GetMapping("/dashboard/admin")
    public String dashboardAdmin() { return "dashboard/admin"; }

    @GetMapping("/dashboard/agente")
    public String dashboardAgente() { return "dashboard/agente"; }

    @GetMapping("/dashboard/cliente")
    public String dashboardCliente() { return "dashboard/cliente"; }

    @GetMapping("/dashboard/mecanico")
    public String dashboardMecanico() { return "dashboard/mecanico"; }

    @GetMapping("/dashboard/auditor")
    public String dashboardAuditor() { return "dashboard/auditor"; }

    // ── USUARIOS ────────────────────────────────────────
    @GetMapping("/usuarios/lista")
    public String listaUsuarios() { return "usuarios/lista"; }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario() { return "usuarios/formulario"; }

    // ── VEHÍCULOS ───────────────────────────────────────
    @GetMapping("/vehiculos/lista")
    public String listaVehiculos() { return "vehiculos/lista"; }

    @GetMapping("/vehiculos/nuevo")
    public String nuevoVehiculo() { return "vehiculos/formulario"; }

    // ── RESERVAS ────────────────────────────────────────
    @GetMapping("/reservas/lista")
    public String listaReservas() { return "reservas/lista"; }

    @GetMapping("/cliente/reservar")
    public String clienteReservar() { return "cliente/reservar"; }

    // ── PAGOS ───────────────────────────────────────────
    @GetMapping("/pagos/lista")
    public String listaPagos() { return "pagos/lista"; }

    // ── MANTENIMIENTOS ───────────────────────────────────
    @GetMapping("/mantenimientos/lista")
    public String listaMantenimientos() { return "mantenimientos/lista"; }

    // ── REPORTES ─────────────────────────────────────────
    @GetMapping("/reportes/lista")
    public String listaReportes() { return "reportes/lista"; }
}