package app.aluracursos.challenge_foro_alura.domain.respuesta;

import app.aluracursos.challenge_foro_alura.domain.topico.TopicoRepository;
import app.aluracursos.challenge_foro_alura.domain.usuario.UsuarioRepository;
import app.aluracursos.challenge_foro_alura.infra.errores.ValidacionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccionDeLaRespuesta {

    @Autowired
    private RespuestaRepository respuestaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TopicoRepository topicoRepository;

    public DatosConfirmacionRespuesta crearNuevaRespuesta(DatosNuevaRespuesta datos) {
        if (!usuarioRepository.existsById(datos.usuarioId())) {
            throw new ValidacionException("El id de usuario ingresado no existe.");
        }
        if (!topicoRepository.existsById(datos.topicoId())) {
            throw new ValidacionException("El id de tópico ingresado no existe.");
        }

        Respuesta respuesta = new Respuesta(datos, usuarioRepository, topicoRepository);
        respuestaRepository.save(respuesta);
        return new DatosConfirmacionRespuesta(respuesta);
    }
}

