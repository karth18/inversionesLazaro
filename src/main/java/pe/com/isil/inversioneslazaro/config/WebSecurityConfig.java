package pe.com.isil.inversioneslazaro.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired; // Añadido si no estaba
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Para el CSRF
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler; // Añadido si no estaba
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import pe.com.isil.inversioneslazaro.security.UserDetailsServiceImpl; // Ya lo tenías

import java.io.IOException;

@SuppressWarnings("unused")
@Configuration
@EnableWebSecurity

public class WebSecurityConfig  {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // --- INICIO DE LA CORRECCIÓN ---
                .formLogin(form -> form
                        .loginProcessingUrl("/login") // La URL a la que apunta el JS

                        // 1. En caso de ÉXITO (login correcto)
                        // Devuelve un JSON con la URL de redirección
                        .successHandler((request, response, authentication) -> {
                            String targetUrl = request.getParameter("targetUrl");
                            if (!StringUtils.hasText(targetUrl) || !targetUrl.startsWith("/")) {
                                targetUrl = "/"; // URL por defecto si no hay target
                            }
                            response.setStatus(HttpStatus.OK.value());
                            response.setContentType("application/json");
                            response.getWriter().write("{\"redirectUrl\":\"" + targetUrl + "\"}");
                        })

                        // 2. En caso de FALLO (contraseña incorrecta)
                        // Devuelve un error 401 (No Autorizado)
                        .failureHandler((request, response, exception) -> {
                            response.setStatus(HttpStatus.UNAUTHORIZED.value());
                            response.setContentType("application/json");
                            // Puedes enviar un mensaje de error si quieres, pero el 401 es suficiente
                            response.getWriter().write("{\"error\":\"Credenciales invalidas\"}");
                        })
                        .permitAll()
                )
                // --- FIN DE LA CORRECCIÓN ---
//                // Configuración del login vía modal (sin loginPage)
//                .formLogin(form -> form
//                               // .loginPage("/")
//                                .loginProcessingUrl("/login")            // URL a la que apunta el form del modal
//                                //.defaultSuccessUrl("/", true)            // Redirige al inicio si login es correcto
//                                .successHandler(myAuthenticationSuccessHandler())
//                                .failureUrl("/?error=true")
//                                .permitAll()
//                )
                // Configuración de permisos
                .authorizeHttpRequests(authz -> authz
                        // URL públicas
                        .requestMatchers("/", "/inicio", "/catalogo/**", "/personaliza", "/css/**", "/js/**", "/img/**", "/registrar/**", "/uploads/**","carrito/**").permitAll()
                        // URLs privadas
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // URLs Autenticadas. Ojo: todo usuario autenticado puede ingresar a cualquier url si no esta debidamente mapeado
                        //por ejemplo si bien es cierto al asignarles un rol en el  front
                        //es para que no se muestre si el usuario no tiene como rol Admin
                        //esa url no se mostrar pero sinembargo si el usuario autenticado lo escribre la url
                        // de manera manual en el buscador tendra acceso a dicha informacion por ello las rutas de admin estan
                        // como admin/** siendo asi privada solo las rutas que estan en admin/** cualquier otra ruta solo
                        // basta con autenticarse
                        .requestMatchers( "/usuario/**","/api/direccion/**","/compra/**").authenticated()
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
                .csrf(AbstractHttpConfigurer::disable
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


//    // codigo nuevo **********************
//    @Bean
//    public AuthenticationSuccessHandler myAuthenticationSuccessHandler() {
//        return new CustomAuthenticationSuccessHandler();
//    }
//
//    public static class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
//
//        private RequestCache requestCache = new HttpSessionRequestCache();
//
//        @Override
//        public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                            Authentication authentication) throws IOException, ServletException {
//
//            // 1. Intenta obtener la URL guardada por Spring Security
//            SavedRequest savedRequest = requestCache.getRequest(request, response);
//            if (savedRequest != null) {
//                String targetUrlAfterLogin = savedRequest.getRedirectUrl();
//                requestCache.removeRequest(request, response);
//                response.sendRedirect(targetUrlAfterLogin);
//                System.out.println("Redirigiendo a SavedRequest: " + targetUrlAfterLogin); // Log para depurar
//                return;
//            }
//
//            // 2. Busca nuestro parámetro 'targetUrl'
//            String targetUrlParameter = request.getParameter("targetUrl");
//            System.out.println("Valor del parámetro targetUrl recibido: " + targetUrlParameter); // Log para depurar
//
//            if (targetUrlParameter != null) {
//                targetUrlParameter = targetUrlParameter.trim(); // Limpiar espacios
//            }
//
//
//            if (StringUtils.hasText(targetUrlParameter) && targetUrlParameter.startsWith("/")) {
//
//                response.sendRedirect(targetUrlParameter); // Redirige a /direccion
//                System.out.println("Redirigiendo a targetUrl: " + targetUrlParameter); // Log para depurar
//                return;
//            }
//
//            // 3. Si no hay nada, redirige a la página principal
//            System.out.println("No se encontró SavedRequest ni targetUrl. Redirigiendo a /"); // Log para depurar
//            response.sendRedirect("/");
//        }
//    }

}
