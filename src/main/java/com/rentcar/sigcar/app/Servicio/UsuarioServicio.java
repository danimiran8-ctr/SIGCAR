package com.rentcar.sigcar.app.Servicio;

import com.rentcar.sigcar.app.DTO.AuthResponseDTO;
import com.rentcar.sigcar.app.DTO.LoginDTO;
import com.rentcar.sigcar.app.DTO.RegistroUsuarioDTO;
import com.rentcar.sigcar.app.Modelo.Usuario;
import com.rentcar.sigcar.app.Repositorio.UsuarioRepositorio;
import com.rentcar.sigcar.app.Seguridad.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioServicio {

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Registrar usuario
    public AuthResponseDTO registrar(RegistroUsuarioDTO dto) {
        if (usuarioRepositorio.existsByCorreo(dto.getCorreo())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setCorreo(dto.getCorreo());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(dto.getRol());
        usuario.setEstado(true);
        usuario.setFechaCreacion(LocalDateTime.now());
        usuario.setFechaActualizacion(LocalDateTime.now());

        usuarioRepositorio.save(usuario);

        String token = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol());
        return new AuthResponseDTO(token, usuario.getCorreo(), usuario.getRol(), usuario.getNombres());
    }

    // Login
    public AuthResponseDTO login(LoginDTO dto) {
        Usuario usuario = usuarioRepositorio.findByCorreo(dto.getCorreo())
                .orElseThrow(() -> new RuntimeException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(dto.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales incorrectas");
        }

        if (!usuario.getEstado()) {
            throw new RuntimeException("Usuario inactivo. Contacte al administrador.");
        }

        String token = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol());
        return new AuthResponseDTO(token, usuario.getCorreo(), usuario.getRol(), usuario.getNombres());
    }

    // Listar todos
    public List<Usuario> listarTodos() {
        return usuarioRepositorio.findAll();
    }

    // Buscar por ID
    public Usuario buscarPorId(String id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // Buscar por correo
    public Usuario buscarPorCorreo(String correo) {
        return usuarioRepositorio.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con correo: " + correo));
    }

    // Actualizar usuario
    public Usuario actualizar(String id, RegistroUsuarioDTO dto) {
        Usuario usuario = buscarPorId(id);
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(dto.getRol());
        usuario.setFechaActualizacion(LocalDateTime.now());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return usuarioRepositorio.save(usuario);
    }

    // Activar usuario
    public void activar(String id) {
        Usuario usuario = buscarPorId(id);
        usuario.setEstado(true);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepositorio.save(usuario);
    }

    // Desactivar usuario
    public void desactivar(String id) {
        Usuario usuario = buscarPorId(id);
        usuario.setEstado(false);
        usuario.setFechaActualizacion(LocalDateTime.now());
        usuarioRepositorio.save(usuario);
    }

    // Eliminar usuario
    public void eliminar(String id) {
        usuarioRepositorio.deleteById(id);
    }
    
    public List<Usuario> listarClientesActivos() {
        return usuarioRepositorio.findAll()
                .stream()
                .filter(u -> "CLIENTE".equals(u.getRol()) && Boolean.TRUE.equals(u.getEstado()))
                .collect(java.util.stream.Collectors.toList());
    }
}