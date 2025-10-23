package pe.com.isil.inversioneslazaro.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import pe.com.isil.inversioneslazaro.security.UserDetailsServiceImpl;

@SuppressWarnings("unused")
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
                        // URL públicas
                        .requestMatchers("/", "/inicio", "/catalogo/**", "/personaliza", "/css/**", "/js/**", "/img/**", "/registrar/**", "/uploads/**").permitAll()
                        // URLs privadas
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // URLs Autenticadas. Ojo: todo usuario autenticado puede ingresar a cualquier url si no esta debidamente mapeado
                        //por ejemplo si bien es cierto al asignarles un rol en el  front
                        //es para que no se muestre si el usuario no tiene como rol Admin
                        //esa url no se mostrar pero sinembargo si el usuario autenticado lo escribre la url
                        // de manera manual en el buscador tendra acceso a dicha informacion por ello las rutas de admin estan
                        // como admin/** siendo asi privada solo las rutas que estan en admin/** cualquier otra ruta solo
                        // basta con autenticarse
                        .requestMatchers( "/usuario/**").authenticated()
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
