package pe.com.isil.inversioneslazaro.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pe.com.isil.inversioneslazaro.model.Usuario;


import java.util.Collection;
import java.util.Collections;
@SuppressWarnings("unused")

@Getter
public class AppUserDetails implements UserDetails {

    //creamos 2 atributos para mapear el usuario y password
    private final String nombre;
    private final Usuario usuario;

    //constructor
    public AppUserDetails(Usuario usuario) {
        this.usuario = usuario;
        this.nombre = usuario.getNombres();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()));
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

//    public String getNombre(){
//        return nombre;
//    }
}
