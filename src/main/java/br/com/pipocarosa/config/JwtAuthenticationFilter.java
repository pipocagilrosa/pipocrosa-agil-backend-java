package br.com.pipocarosa.config;

import br.com.pipocarosa.models.UserModel;
import br.com.pipocarosa.repositories.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1 - obtain a header that contains jwt

        String authHeader = request.getHeader("Authorization"); // Bearer jwt

        if(authHeader == null || !authHeader.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        // 2 - obtain jwt token

        String jwt = authHeader.split(" ")[1];

        // 3 - obtain subject/username in jwt

        String email = jwtService.extractEmail(jwt);

        // 4 - set authenticate object inside our security context

        UserModel user = userRepository.findByEmail(email).get();

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                email, "", user.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 5 execute rest of the filters

        filterChain.doFilter(request, response);
    }
}
