package app.aluracursos.challenge_foro_alura.controller;

import app.aluracursos.challenge_foro_alura.domain.respuesta.*;
import app.aluracursos.challenge_foro_alura.domain.topico.Topico;
import app.aluracursos.challenge_foro_alura.domain.topico.TopicoRepository;
import app.aluracursos.challenge_foro_alura.domain.usuario.Usuario;
import app.aluracursos.challenge_foro_alura.domain.usuario.UsuarioRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/topicos")
@SecurityRequirement(name = "bearer-key")
public class RespuestaController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private RespuestaRepository respuestaRepository;

    @Autowired
    private AccionDeLaRespuesta validar;

    @PostMapping("/{topicoId}/respuestas")
    public ResponseEntity<ApiResponse<DatosConfirmacionRespuesta>> nuevaRespuesta(
            @PathVariable Long topicoId,
            @RequestBody @Valid DatosNuevaRespuesta datos,
            UriComponentsBuilder uriComponentsBuilder) {

        Usuario usuario = usuarioRepository.findById(datos.getUsuarioId())
                .orElseThrow(() -> new ForoException("Usuario no encontrado"));
        Topico topico = topicoRepository.findById(topicoId)
                .orElseThrow(() -> new ForoException("Tópico no encontrado"));

        Respuesta respuesta = new Respuesta(datos, usuario, topico);
        respuestaRepository.save(respuesta);

        URI url = uriComponentsBuilder.path("/topicos/{topicoId}/respuestas/{respuestaId}")
                .buildAndExpand(topicoId, respuesta.getId()).toUri();

        DatosConfirmacionRespuesta datosConfirmacion = new DatosConfirmacionRespuesta(respuesta);
        return ResponseEntity.created(url).body(new ApiResponse<>(datosConfirmacion, "Respuesta creada con éxito"));
    }

    @DeleteMapping("/respuestas/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> eliminarRespuesta(@PathVariable Long id) {
        if (!respuestaRepository.existsById(id)) {
            throw new ForoException("La respuesta no existe");
        }

        Respuesta respuesta = respuestaRepository.getReferenceById(id);
        respuestaRepository.delete(respuesta);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/respuestas/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<DatosLimitadosRespuesta>> actualizarSolucionRespuesta(@RequestBody DatosSolucionRespuesta datos) {
        Topico topico = topicoRepository.findById(datos.topicoId())
                .orElseThrow(() -> new ForoException("Tópico no encontrado"));

        if ("Cerrado".equals(topico.getStatus())) {
            throw new ForoException("El tópico ya se encuentra cerrado");
        }

        Respuesta respuesta = respuestaRepository.findById(datos.respuestaId())
                .orElseThrow(() -> new ForoException("Respuesta no encontrada"));

        if (!topico.getUsuario().getId().equals(datos.usuarioId())) {
            throw new ForoException("El usuario no tiene permisos para actualizar este tópico");
        }

        respuesta.actualizarSolucion();
        topico.actualizarStatus(respuesta.getSolucion());

        DatosLimitadosRespuesta datosLimitados = new DatosLimitadosRespuesta(respuesta);
        return ResponseEntity.ok(new ApiResponse<>(datosLimitados, "Respuesta actualizada correctamente"));
    }
}

// Clase ApiResponse para estandarizar las respuestas
public class ApiResponse<T> {
    private T data;
    private String message;

    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    // Getters y setters
    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

// Clase ForoException para manejar excepciones personalizadas
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ForoException extends RuntimeException {
    public ForoException(String mensaje) {
        super(mensaje);
    }
}

// Manejo de excepciones globales
@RestControllerAdvice
public class ForoExceptionHandler {

    @ExceptionHandler(ForoException.class)
    public ResponseEntity<ApiResponse<Void>> handleForoException(ForoException ex) {
        return ResponseEntity.badRequest().body(new ApiResponse<>(null, ex.getMessage()));
    }
}
