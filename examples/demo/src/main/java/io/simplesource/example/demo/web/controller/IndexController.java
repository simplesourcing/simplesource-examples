package io.simplesource.example.demo.web.controller;

import io.simplesource.example.demo.HealthcheckService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;

@Controller
public class IndexController {
    private final HealthcheckService healthcheckService;

    public IndexController(HealthcheckService healthcheckService) {
        this.healthcheckService = healthcheckService;
    }

    @GetMapping("/")
    public ModelAndView index() {
        if(!healthcheckService.isHealthy()) {
            throw new UnhealthyException();
        }
        return new ModelAndView("index", Collections.emptyMap());
    }


}
