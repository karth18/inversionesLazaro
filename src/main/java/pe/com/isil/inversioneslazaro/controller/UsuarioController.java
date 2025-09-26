package pe.com.isil.inversioneslazaro.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pe.com.isil.inversioneslazaro.model.Usuario;
import pe.com.isil.inversioneslazaro.repository.UsuarioRepository;

import java.util.List;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("")
    String index(Model model, @PageableDefault(size=5) Pageable pageable, @RequestParam(required = false) String dni)
    {
        //1. listado tipo Page de todos curso registrados en la base de datos
        Page<Usuario> usu;
        if (dni != null && !dni.trim().isEmpty())
        {
            usu = usuarioRepository.findByDniContainingIgnoreCase(dni, pageable);
        }
        else
        {
            usu = usuarioRepository.findAll(pageable);
        }

        //2. creamos un atributo en el model a enviar a la vista
        model.addAttribute("usu", usu);

        //3. retornamos la vista o HTML a mostrar
        return "usuario/index"; //al archivo: index.html
    }
}
