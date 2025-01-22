package app.aluracursos.challenge_foro_alura.controller;

import app.aluracursos.challenge_foro_alura.domain.usuario.Usuario;
import app.aluracursos.challenge_foro_alura.domain.usuario.DatosAutenticacionUsuario;
import app.aluracursos.challenge_foro_alura.infra.security.DatosJWTToken;
import app.aluracursos.challenge_foro_alura.infra.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
public class AutenticacionController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping
    public ResponseEntity<ApiResponse<DatosJWTToken>> autenticarUsuario(@RequestBody @Valid DatosAutenticacionUsuario datosAutenticacionUsuario) {
        try {
            Authentication authToken = new UsernamePasswordAuthenticationToken(
                    datosAutenticacionUsuario.email(), datosAutenticacionUsuario.contrasenha());

            var usuarioAutenticado = authenticationManager.authenticate(authToken);
            var JWTtoken = tokenService.generarToken((Usuario) usuarioAutenticado.getPrincipal());

            DatosJWTToken datosJWTToken = new DatosJWTToken(JWTtoken);
            return ResponseEntity.ok(new ApiResponse<>(datosJWTToken, "Autenticación exitosa"));

        } catch (Exception e) {
            return ResponseEntity.status(401).body(new ApiResponse<>(null, "Credenciales inválidas"));
        }
    }
}
