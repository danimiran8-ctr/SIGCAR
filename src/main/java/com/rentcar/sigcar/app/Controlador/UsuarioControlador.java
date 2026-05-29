package com.rentcar.sigcar.app.Controlador;

import com.rentcar.sigcar.app.DTO.RegistroUsuarioDTO;
import com.rentcar.sigcar.app.Modelo.Usuario;
import com.rentcar.sigcar.app.Servicio.UsuarioServicio;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Usuarios", description = "Gestión de usuarios y roles del sistema SIGCAR")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioControlador {

    @Autowired
    private UsuarioServicio usuarioServicio;

    @Operation(summary = "Listar todos los usuarios del sistema")
    @GetMapping
    public ResponseEntity<List<Usuario>> listar() {
        return ResponseEntity.ok(usuarioServicio.listarTodos());
    }

    @Operation(summary = "Listar solo clientes activos — para uso del Agente")
    @GetMapping("/clientes")
    public ResponseEntity<List<Usuario>> listarClientes() {
        return ResponseEntity.ok(usuarioServicio.listarClientesActivos());
    }

    @Operation(summary = "Buscar usuario por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable String id) {
        try {
            return ResponseEntity.ok(usuarioServicio.buscarPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Actualizar datos de un usuario",
               description = "Si se envía contraseña vacía, no se modifica la contraseña actual")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable String id,
                                        @RequestBody RegistroUsuarioDTO dto) {
        try {
            return ResponseEntity.ok(usuarioServicio.actualizar(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Activar usuario")
    @PatchMapping("/{id}/activar")
    public ResponseEntity<?> activar(@PathVariable String id) {
        try {
            usuarioServicio.activar(id);
            return ResponseEntity.ok("Usuario activado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Desactivar usuario")
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivar(@PathVariable String id) {
        try {
            usuarioServicio.desactivar(id);
            return ResponseEntity.ok("Usuario desactivado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Eliminar usuario permanentemente")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        try {
            usuarioServicio.eliminar(id);
            return ResponseEntity.ok("Usuario eliminado correctamente");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}