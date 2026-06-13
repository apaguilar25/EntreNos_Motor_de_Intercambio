package es.ucab.entrenos.nucleo.seguridad.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class ServicioJWT {

    // Esta clave debe estar en tu application.properties (ej. jwt.secret=MiClaveSuperSecretaDeEntreNos)
    @Value("${jwt.secret:ClaveSecretaPorDefectoMuyLargaParaPoderFirmarElToken2024}")
    private String secret;

    // Token válido por 24 horas
    private final long EXPIRATION_TIME = 86400000;

    public String generarToken(String idUsuario, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("rol", rol); // Guardamos el rol dentro del token

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(idUsuario)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }
}