package app.aluracursos.challenge_foro_alura.controller;

import app.aluracursos.challenge_foro_alura.domain.respuesta.RespuestaRepository;
import app.aluracursos.challenge_foro_alura.domain.topico.*;
import app.aluracursos.challenge_foro_alura.domain.usuario.UsuarioRepository;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/topicos")
@SecurityRequirement(name = "bearer-key")
public class TopicoController {

    @Autowired
    private TopicoRepository topicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RespuestaRepository respuestaRepository;

    @Autowired
    private AccionDelTopico validar;

    @PostMapping
    public ResponseEntity<ApiResponse<DatosConfirmacionPostTopico>> enviarNuevoTopico(@RequestBody @Valid DatosTopicoNuevo datosTopicoNuevo,
                                                                                      UriComponentsBuilder uriComponentsBuilder) {
        DatosConfirmacionPostTopico datosConfirmacion = validar.postearTopico(datosTopicoNuevo);
        URI url = uriComponentsBuilder.path("/topicos/{id}").buildAndExpand(datosConfirmacion.id()).toUri();
        return ResponseEntity.created(url).body(new ApiResponse<>(datosConfirmacion, "Tópico creado con éxito"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<DatosConfirmacionPostTopico>>> listarTodosLosTopicos(
            @PageableDefault
            @SortDefault(sort = "status", direction = Sort.Direction.ASC)
            @SortDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<DatosConfirmacionPostTopico> topicos = topicoRepository.findAll(pageable).map(DatosConfirmacionPostTopico::new);
        return ResponseEntity.ok(new ApiResponse<>(topicos, "Lista de tópicos obtenida con éxito"));
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<DatosConfirmacionPostTopico>> actualizarTopico(@PathVariable("id") Long id, @RequestBody @Valid DatosActualizarTopico datosActualizar) {
        DatosConfirmacionPostTopico datosActualizados = validar.actualizarTopico(id, datosActualizar);
        return ResponseEntity.ok(new ApiResponse<>(datosActualizados, "Tópico actualizado con éxito"));
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> eliminarTopico(@PathVariable Long id) {
        validar.eliminarTopico(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/topicoPorId/{id}")
    public ResponseEntity<ApiResponse<DatosCompletosTopico>> buscarTopicoPorId(@PathVariable Long id) {
        DatosCompletosTopico topico = validar.topicoPorId(id);
        return ResponseEntity.ok(new ApiResponse<>(topico, "Tópico encontrado"));
    }

    @PostMapping("/buscar")
    public ResponseEntity<ApiResponse<List<DatosDevolucionPorTopico>>> buscarTopicoPorPalabraClave(@RequestBody
                                                                                                   @Schema(description = "Request para la búsqueda de tópicos") DatosBusquedaTopicos datos) {

        if (!usuarioRepository.existsById(datos.usuarioId())) {
            throw new ForoException("Usuario no encontrado");
        }

        List<Topico> topicos = topicoRepository.buscarPorPalabraClave(datos.busqueda());

        if (topicos.isEmpty()) {
            throw new ForoException("No se encontraron tópicos para la palabra clave proporcionada");
        }

        List<DatosDevolucionPorTopico> datosTopicos = topicos
                .stream()
                .map(t -> new DatosDevolucionPorTopico(t, respuestaRepository, usuarioRepository))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(datosTopicos, "Búsqueda de tópicos realizada con éxito"));
    }
}
