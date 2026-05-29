package com.rentcar.sigcar.app.Configuracion;

import com.rentcar.sigcar.app.Seguridad.JwtFiltro;
import com.rentcar.sigcar.app.Servicio.UsuarioDetallesServicio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtFiltro jwtFiltro;

    @Autowired
    private UsuarioDetallesServicio usuarioDetallesServicio;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ── Rutas públicas ──────────────────────────────────
                .requestMatchers("/", "/login", "/registro").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                .requestMatchers("/dashboard/**").permitAll()

                // Auth: login y registro públicos, perfil requiere token
                .requestMatchers("/api/auth/login", "/api/auth/registro").permitAll()
                .requestMatchers("/api/auth/perfil").authenticated()

                // Vehículos públicos para Landing Page
                .requestMatchers("/api/vehiculos/disponibles").permitAll()
                .requestMatchers("/api/vehiculos/categoria/**").permitAll()
                .requestMatchers("/api/vehiculos/disponibilidad").permitAll()

                // ── Usuarios ────────────────────────────────────────
                .requestMatchers("/api/usuarios/clientes").hasAnyRole("ADMINISTRADOR", "AGENTE")
                .requestMatchers("/api/usuarios/**").hasRole("ADMINISTRADOR")
                .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")

                // ── Vehículos — Admin, Agente y Mecánico ───────────
                .requestMatchers("/api/vehiculos/**").hasAnyRole("ADMINISTRADOR", "AGENTE", "MECANICO")
                .requestMatchers("/vehiculos/**").hasAnyRole("ADMINISTRADOR", "AGENTE")

                // ── Reservas — Admin, Agente, Cliente y Auditor ────
                .requestMatchers("/api/reservas/**").hasAnyRole("ADMINISTRADOR", "AGENTE", "CLIENTE", "AUDITOR")
                .requestMatchers("/reservas/**").hasAnyRole("ADMINISTRADOR", "AGENTE", "CLIENTE")

                // ── Pagos — Admin, Agente y Auditor ────────────────
                .requestMatchers("/api/pagos/reserva/*/factura").hasAnyRole("ADMINISTRADOR", "AGENTE", "CLIENTE")
                .requestMatchers("/api/pagos/reserva/*").hasAnyRole("ADMINISTRADOR", "AGENTE", "CLIENTE")
                .requestMatchers("/api/pagos/ingresos/**").hasAnyRole("ADMINISTRADOR", "AGENTE", "AUDITOR")
                .requestMatchers("/api/pagos/**").hasAnyRole("ADMINISTRADOR", "AGENTE")
                .requestMatchers("/pagos/**").hasAnyRole("ADMINISTRADOR", "AGENTE")

                // ── Mantenimientos — Admin y Mecánico ───────────────
                .requestMatchers("/api/mantenimientos/**").hasAnyRole("ADMINISTRADOR", "MECANICO")
                .requestMatchers("/mantenimientos/**").hasAnyRole("ADMINISTRADOR", "MECANICO")

                // ── Reportes — Admin y Auditor ──────────────────────
                .requestMatchers("/api/reportes/**").hasAnyRole("ADMINISTRADOR", "AUDITOR")
                .requestMatchers("/reportes/**").hasAnyRole("ADMINISTRADOR", "AUDITOR")

                // ── Cliente ─────────────────────────────────────────
                .requestMatchers("/api/cliente/**").hasAnyRole("ADMINISTRADOR", "AGENTE", "CLIENTE")
                .requestMatchers("/cliente/**").hasAnyRole("ADMINISTRADOR", "AGENTE", "CLIENTE")

                // ── Todo lo demás requiere autenticación ────────────
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFiltro, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioDetallesServicio);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}