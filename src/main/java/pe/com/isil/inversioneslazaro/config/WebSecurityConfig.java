package pe.com.isil.inversioneslazaro.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import pe.com.isil.inversioneslazaro.security.UserDetailsServiceImpl;

import javax.naming.AuthenticationException;
import java.io.IOException;


@Configuration
@EnableWebSecurity
public class WebSecurityConfig  {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Configuración del login vía modal (sin loginPage)
                .formLogin(form -> form
                                .loginPage("/")
                                .loginProcessingUrl("/login")            // URL a la que apunta el form del modal
                                .defaultSuccessUrl("/", true)            // Redirige al inicio si login es correcto
                                .failureUrl("/?error=true")
                                .permitAll()
                )
                // Configuración de permisos
                .authorizeHttpRequests(authz -> authz
                        // URLs públicas
                        .requestMatchers("/", "/inicio", "/catalogo", "/personaliza", "/css/**", "/js/**", "/img/**", "/registrar/**").permitAll()
                        // URLs privadas
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/cursos/**", "/mis-cursos", "/usuario/**").authenticated()
                        .anyRequest().authenticated()
                )
                // Logout
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                // Manejo de sesiones
                .sessionManagement(session -> session
                        .invalidSessionUrl("/?expired=true")
                        .maximumSessions(1)
                        .maxSessionsPreventsLogin(false)
                )
                // UserDetailsService
                .userDetailsService(userDetailsServiceImpl)
                // Manejo de acceso denegado
                .exceptionHandling(customizer -> customizer
                        .accessDeniedHandler(accessDeniedHandlerApp())
                )
                // CSRF (si usas formularios normales, puedes dejarlo activo)
                .csrf(csrf -> csrf
                        .disable()
                )
                .build();
    }


    @Bean
    AccessDeniedHandler accessDeniedHandlerApp()
    {
        return (
                ((request, response, accessDeniedException)
                        -> response.sendRedirect(request.getContextPath() + "/403"))
                );
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


//    @PostMapping("/login-error-clear")
//    @ResponseBody
//    public void clearLoginError(HttpSession session) {
//        session.removeAttribute("loginError");
//    }
//

}
