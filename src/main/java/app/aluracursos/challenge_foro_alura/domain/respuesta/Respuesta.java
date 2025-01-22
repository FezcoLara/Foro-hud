package app.aluracursos.challenge_foro_alura.domain.respuesta;

import app.aluracursos.challenge_foro_alura.domain.topico.Topico;
import app.aluracursos.challenge_foro_alura.domain.topico.TopicoRepository;
import app.aluracursos.challenge_foro_alura.domain.usuario.Usuario;
import app.aluracursos.challenge_foro_alura.domain.usuario.UsuarioRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity(name = "Respuesta")
@Table(name = "respuestas")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")
@SecurityRequirement(name = "bearer-key")
public class Respuesta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "topico_id", nullable = false)
    private Topico topico;

    @Column(nullable = false)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fechaDeCreacion;

    @Column(nullable = false)
    private Boolean solucion;

    public Respuesta(@Valid DatosNuevaRespuesta datos, UsuarioRepository usuarioRepository, TopicoRepository topicoRepository) {
        this.usuario = usuarioRepository.findById(datos.usuarioId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con el ID: " + datos.usuarioId()));
        this.topico = topicoRepository.findById(datos.topicoId())
                .orElseThrow(() -> new IllegalArgumentException("Topico no encontrado con el ID: " + datos.topicoId()));

        this.nombre = this.usuario.getNombre();
        this.mensaje = datos.mensaje();
        this.fechaDeCreacion = LocalDateTime.now();
        this.solucion = false;
    }

    public void actualizarSolucion() {
        this.solucion = true;
    }
}
