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
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        return http
//                // Configuración del login vía modal (sin loginPage)
//                .formLogin(form -> form
//                        .loginProcessingUrl("/login")            // URL a la que apunta el form del modal
//                        .defaultSuccessUrl("/", true)            // Redirige al inicio si login es correcto
////                        .failureUrl("/?error=true")
//                        .failureHandler(authenticationFailureHandler())
//                        .permitAll()
//                )
//                // Configuración de permisos
//                .authorizeHttpRequests(authz -> authz
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/cursos/**", "/mis-cursos", "/usuario/**").authenticated()
//                        .anyRequest().permitAll()
//                )
//                // Logout
//                .logout(logout -> logout
//                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
//                        .logoutSuccessUrl("/")
//                        .permitAll()
//                )
//                // Manejo de sesiones
//                .sessionManagement(session -> session
//                        .invalidSessionUrl("/?expired=true")
//                        .maximumSessions(1)
//                        .maxSessionsPreventsLogin(false)
//                )
//                // UserDetailsService
//                .userDetailsService(userDetailsServiceImpl)
//                // Manejo de acceso denegado
//                .exceptionHandling(customizer -> customizer
//                        .accessDeniedHandler(accessDeniedHandlerApp())
//                )
//                .build();
//    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // Configuración del login vía modal (sin loginPage)
                .formLogin(form -> form
                        .loginProcessingUrl("/login")            // URL a la que apunta el form del modal
                        .defaultSuccessUrl("/", true)            // Redirige al inicio si login es correcto
                        .failureUrl("/login?loginError=true")
                        .failureHandler(authenticationFailureHandler()) // Maneja fallo de login
                        .permitAll()
                )
                // Configuración de permisos
                .authorizeHttpRequests(authz -> authz
                        // URLs públicas
                        .requestMatchers("/", "/inicio", "/catalogo", "/personaliza", "/css/**", "/js/**", "/img/**", "/registrar/registro").permitAll()
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

//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
//    {
//        return http
//                //configurar el formulario de inicio de sesion, para que todos puedan acceder
//                .formLogin(form->form.loginPage("/login").permitAll())
//                //configurar los accesos o permisos a las rutas/URL's
//
//                .authorizeHttpRequests(
//                        //el perfil o role: ADMIN
//                        (authz)->authz.requestMatchers("/admin/**")
//                        .hasRole("ADMIN")
//                        //a los usuarios autenticados le damos permiso a la ruta: /cursos
//                        .requestMatchers("/cursos/**", "/mis-cursos", "/usuario/**").authenticated()
//                        //para las otras ruta, el acceso es publico o permitido para todos
//                        .anyRequest().permitAll()
//                )
//                //Configurar el cierre de sesion
//                .logout(logout->logout
//                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
//                        .logoutSuccessUrl("/")
//                        .permitAll()
//                )
//                .sessionManagement(session -> session
//                        .invalidSessionUrl("/login?expired") // Redirige si la sesión caduca
//                        .maximumSessions(1)                  // Limita sesiones por usuario
//                        .maxSessionsPreventsLogin(false)
//                )
//                //enlazar el UserDetailsServiceImpl
//                .userDetailsService(userDetailsServiceImpl)
//                //configurar el acceso denegado
//                .exceptionHandling(customizer->customizer
//                        .accessDeniedHandler(accessDeniedHandlerApp()))
//                //construye con las configuraciones anteriores
//                .build();
//    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            // Agregamos un atributo en la sesión para detectar el error
            request.getSession().setAttribute("loginError", "Usuario o contraseña incorrecta.");
            // Redirigimos al home ("/"), pero ahora sabemos que hay error
            response.sendRedirect("/");
        };
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


    @PostMapping("/login-error-clear")
    @ResponseBody
    public void clearLoginError(HttpSession session) {
        session.removeAttribute("loginError");
    }


}
