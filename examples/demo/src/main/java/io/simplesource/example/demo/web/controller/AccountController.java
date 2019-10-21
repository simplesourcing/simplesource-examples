package io.simplesource.example.demo.web.controller;

import io.simplesource.example.demo.service.AccountService;
import io.simplesource.example.demo.web.form.CreateAccountForm;
import io.simplesource.example.demo.web.form.DepositForm;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Controller
public class AccountController {
    private AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/account/create")
    public ModelAndView viewCreateAccountPage() {
        Map model = new HashMap();
        model.put("form", CreateAccountForm.EMPTY);
        model.put("errors", new String[] {});
        return new ModelAndView("account_create", model);
    }


    @PostMapping("/account/create")
    @ResponseBody
    public ModelAndView handleCreateFormSubmit(@ModelAttribute CreateAccountForm form) {
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
             .orElseGet(() ->new ModelAndView("redirect:/account/create/success", Collections.emptyMap()));

    }

    @GetMapping("/account/create/success")
    @ResponseBody
    public ModelAndView viewCreateSuccessPage() {
        return new ModelAndView("account_create_success", new HashMap<>());
    }



    @GetMapping("/account/deposit/{account}")
    public ModelAndView viewDepositAccountPage(@PathVariable String account) {
        Map model = new HashMap();
        model.put("form", new DepositForm(0));
        model.put("account", account);
        model.put("errors", new String[] {});
        return new ModelAndView("account_deposit", model);
    }

    @GetMapping("/account/deposit/success")
    @ResponseBody
    public ModelAndView viewDepositSuccessPage() {
        return new ModelAndView("account_deposit_success", new HashMap<>());
    }

    @PostMapping("/account/deposit/{account}")
    public ModelAndView handleDepositSubmit(@ModelAttribute DepositForm form, @PathVariable("account") String account) {
        Map model = new HashMap();

        if (!accountService.accountExists(account)) {
            System.out.println("***" + account + "***");
            model.put("form", form);
            model.put("account", account);
            model.put("errors", new String[] { "Account does not exist"});
            return new ModelAndView("account_deposit", model);
        }
        return new ModelAndView("redirect:/account/deposit/success", Collections.emptyMap());
    }

}
