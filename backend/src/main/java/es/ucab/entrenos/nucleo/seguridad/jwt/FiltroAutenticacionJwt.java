package es.ucab.entrenos.nucleo.seguridad.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class FiltroAutenticacionJwt extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public FiltroAutenticacionJwt(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Extraer la cabecera "Authorization"
        final String cabeceraAutorizacion = request.getHeader("Authorization");

        // 2. Si no hay cabecera o no empieza con "Bearer ", ignoramos y dejamos que Spring Security lo bloquee si la ruta es protegida
        if (cabeceraAutorizacion == null || !cabeceraAutorizacion.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token puro
        final String jwt = cabeceraAutorizacion.substring(7);

        try {
            // 4. Si el token es válido y aún no hay nadie autenticado en este hilo
            if (jwtUtil.validarToken(jwt) && SecurityContextHolder.getContext().getAuthentication() == null) {

                String idUsuario = jwtUtil.extraerIdUsuario(jwt);

                // 5. Autenticamos al usuario en el contexto de Spring (Pasamos el ID como "Principal")
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        idUsuario, null, new ArrayList<>() // Aquí podríamos pasar roles de Spring Security si lo deseamos
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Autorizamos la transacción
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Si el token es inválido, simplemente no configuramos el SecurityContext.
            // Spring devolverá un Error 403 o 401.
        }

        filterChain.doFilter(request, response);
    }
}