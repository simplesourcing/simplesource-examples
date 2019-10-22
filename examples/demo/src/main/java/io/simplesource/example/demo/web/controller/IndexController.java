package io.simplesource.example.demo.web.controller;

import io.simplesource.example.demo.HealthcheckService;
import io.simplesource.example.demo.service.AccountService;
import io.simplesource.example.demo.web.viewobject.AccountListRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class IndexController {
    private static final Logger log = LoggerFactory.getLogger(IndexController.class);

    private final HealthcheckService healthcheckService;
    private final AccountService accountService;

    public IndexController(HealthcheckService healthcheckService, AccountService accountService) {
        this.healthcheckService = healthcheckService;
        this.accountService = accountService;
    }

    @GetMapping("/")
    public ModelAndView index() {
        if(!healthcheckService.isHealthy()) {
            throw new UnhealthyException();
        }

        List<AccountListRow> items = accountService.list().stream().map(a -> new AccountListRow(a.accountName, a.balance)).collect(Collectors.toList());

        return new ModelAndView("index", Collections.singletonMap("accounts", items));
    }


}
