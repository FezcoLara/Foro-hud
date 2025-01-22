package app.aluracursos.challenge_foro_alura.domain.respuesta;

import java.time.LocalDateTime;

public record DatosConfirmacionRespuesta(
        Long id,
        String nombreUsuario,
        String tituloTopico,
        String mensaje,
        LocalDateTime fechaDeCreacion
) {
    public DatosConfirmacionRespuesta(Respuesta respuesta) {
        this(
                respuesta.getId(),
                respuesta.getUsuario().getNombre(),
                respuesta.getTopico().getTitulo(),
                respuesta.getMensaje(),
                respuesta.getFechaDeCreacion()
        );
    }
}
