package pe.com.isil.inversioneslazaro.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/direccion")
public class DireccionController {

    @GetMapping("")
    String checkout(){
        return"checkout";
    }
}
