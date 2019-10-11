package io.simplesource.example.demo.web.controller;

import io.simplesource.example.demo.HealthcheckService;
import io.simplesource.example.demo.repository.write.CreateAccountError;
import io.simplesource.example.demo.service.AccountService;
import io.simplesource.example.demo.web.form.CreateAccountForm;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class AccountController {
    private AccountService accountService;
    private final HealthcheckService healthcheckService;


    public AccountController(AccountService accountService, HealthcheckService healthcheckService) {
        this.accountService = accountService;
        this.healthcheckService = healthcheckService;
    }

    @GetMapping("/account/create")
    public ModelAndView viewCreateAccountPage() {
        if(!healthcheckService.isHealthy()) {
            throw new UnhealthyException();
        }

        Map model = new HashMap();
        model.put("form", CreateAccountForm.EMPTY);
        model.put("errors", new String[] {});
        return new ModelAndView("account_create", model);
    }


    @PostMapping("/account/create")
    @ResponseBody
    public ModelAndView handleCreateFormSubmit(@ModelAttribute CreateAccountForm form) {
        if(!healthcheckService.isHealthy()) {
            throw new UnhealthyException();
        }

        Map model = new HashMap();

        if (form.getAccountName() == null || form.getAccountName().trim().isEmpty()) {
            model.put("form", form);
            model.put("errors", new String[] { "Account name cannot be empty"});
            return new ModelAndView("account_create", model);
        }

        if (accountService.accountExists(form.getAccountName())) {
            model.put("form", form);
            model.put("errors", new String[] { "Account with that name already exists"});
            return new ModelAndView("account_create", model);
        }

        return accountService.createAccount(form.getAccountName(), form.getAccountBalance())
            .map(error -> {
                model.put("form", form);
                model.put("errors", new String[] { error.message() });
                return new ModelAndView("account_create", model);
             })
             .orElseGet(() ->new ModelAndView("redirect:/", Collections.emptyMap())); // Todo redirect to intermediate page as index is eventually consistent

    }

}
