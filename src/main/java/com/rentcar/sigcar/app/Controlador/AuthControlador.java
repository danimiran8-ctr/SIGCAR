package com.rentcar.sigcar.app.Controlador;

import com.rentcar.sigcar.app.DTO.AuthResponseDTO;
import com.rentcar.sigcar.app.DTO.LoginDTO;
import com.rentcar.sigcar.app.DTO.RegistroUsuarioDTO;
import com.rentcar.sigcar.app.Servicio.UsuarioServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Endpoints de registro y login de usuarios")
@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Operation(summary = "Registrar nuevo usuario", 
               description = "Crea un nuevo usuario en el sistema con el rol especificado")
    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody RegistroUsuarioDTO dto) {
        try {
            AuthResponseDTO response = usuarioServicio.registrar(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Iniciar sesión", 
               description = "Autentica el usuario y retorna un token JWT para usar en los demás endpoints")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        try {
            AuthResponseDTO response = usuarioServicio.login(dto);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @Operation(summary = "Obtener perfil del usuario autenticado")
    @GetMapping("/perfil")
    public ResponseEntity<?> perfil(Authentication auth) {
        try {
            String correo = auth.getName();
            return ResponseEntity.ok(usuarioServicio.buscarPorCorreo(correo));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
}
