package pe.com.isil.inversioneslazaro.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

@SuppressWarnings("unused")
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository
                .findByEmail(username)
                .orElseThrow(()->new UsernameNotFoundException("El usuario no ha sido encontrado para: " + username));

        if (!usuario.isEstado()) {
            throw new DisabledException("La cuenta ha sido deshabilitada. Contacte al administrador.");
        }

        return new AppUserDetails(usuario);
    }

}
