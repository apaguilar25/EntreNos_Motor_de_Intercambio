package es.ucab.entrenos.modulos.reputacion.dtos;

import es.ucab.entrenos.modulos.reputacion.modelos.Resena;
import java.util.List;

public class ResenasPorUsuarioDTO {

    private String idUsuario;
    private List<Resena> resenas;

    public ResenasPorUsuarioDTO() {}

    public ResenasPorUsuarioDTO(String idUsuario, List<Resena> resenas) {
        this.idUsuario = idUsuario;
        this.resenas = resenas;
    }

    public String getIdUsuario() { return idUsuario; }
    public void setIdUsuario(String idUsuario) { this.idUsuario = idUsuario; }

    public List<Resena> getResenas() { return resenas; }
    public void setResenas(List<Resena> resenas) { this.resenas = resenas; }
}
