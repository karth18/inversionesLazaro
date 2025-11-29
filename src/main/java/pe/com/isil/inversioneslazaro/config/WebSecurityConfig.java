package pe.com.isil.inversioneslazaro.config;


import org.springframework.beans.factory.annotation.Autowired; // Añadido si no estaba
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Para el CSRF
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StringUtils;
import pe.com.isil.inversioneslazaro.security.UserDetailsServiceImpl;
import pe.com.isil.inversioneslazaro.service.CarritoService;


@SuppressWarnings("unused")
@Configuration
@EnableWebSecurity

public class WebSecurityConfig  {

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Autowired private CarritoService carritoService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .formLogin(form -> form
                        .loginProcessingUrl("/login") // La URL a la que apunta el JS

                        // 1. En caso de ÉXITO (login correcto)
                        // Devuelve un JSON con la URL de redirección
                        .successHandler((request, response, authentication) -> {
                            carritoService.migrarCarritoSesionADb();
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

                // Configuración de permisos
                .authorizeHttpRequests(authz -> authz
                        // URL públicas
                        .requestMatchers("/", "/inicio", "/catalogo/**",
                                "/personaliza", "/css/**", "/js/**", "/img/**",
                                "/registrar/**", "/uploads/**","carrito/**",
                                "/api/v1/consulta/dni/**","/cotizador/**","/media/**").permitAll()
                        // URLs privadas
                        .requestMatchers("/admin/banners/**","/admin/home-editor/**").hasAnyRole("ADMIN","MARKETING")
                        .requestMatchers("admin/pedidos/**").hasAnyRole("ADMIN","DESPACHO")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
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

}
