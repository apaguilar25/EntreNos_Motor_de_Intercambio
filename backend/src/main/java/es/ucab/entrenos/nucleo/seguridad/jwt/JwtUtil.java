package es.ucab.entrenos.nucleo.seguridad.jwt;

import es.ucab.entrenos.modulos.identidad.modelos.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Llave secreta encriptada (En producción debe venir de variables de entorno)
    // Debe tener al menos 256 bits para HS256
    private static final String SECRETO = "EntreNosSuperSecretaLlaveDeAutenticacion2026ParaElMotorDeSubastas";
    private final Key llaveFirma = Keys.hmacShaKeyFor(SECRETO.getBytes());

    // Tiempo de expiración: 24 horas en milisegundos
    private static final long TIEMPO_EXPIRACION = 86400000L;

    /**
     * Fabrica un token inyectando el ID del usuario como "Subject" y su Rol como un Claim extra.
     */
    public String generarToken(Usuario usuario) {
        return Jwts.builder()
                .setSubject(usuario.getId()) // Guardamos el ID del usuario en el token
                .claim("rol", usuario.getRol().name()) // Guardamos el rol
                .claim("correo", usuario.getCorreoElectronico())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + TIEMPO_EXPIRACION))
                .signWith(llaveFirma, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Abre el token de forma segura y extrae el ID del usuario.
     * Si el token fue manipulado o expiró, esto lanzará una excepción automática.
     */
    public String extraerIdUsuario(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(llaveFirma)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(llaveFirma).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // El token es inválido, expiró o fue falsificado
        }
    }
}