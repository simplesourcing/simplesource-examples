package io.simplesource.example.demo.web.controller;

import io.simplesource.example.demo.domain.AccountTransaction;
import io.simplesource.example.demo.service.AccountService;
import io.simplesource.example.demo.web.form.CreateAccountForm;
import io.simplesource.example.demo.web.form.DepositForm;
import io.simplesource.example.demo.web.form.WithdrawForm;
import io.simplesource.example.demo.web.viewobject.AccountTransactionRow;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class AccountController {
    private AccountService accountService;


    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    //
    // CREATE mappings
    //
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
        return accountService.createAccount(form.getAccountName(), form.getAccountBalance())
            .map(error -> {
                Map model = new HashMap();
                model.put("form", form);
                model.put("errors", new String[] { error.message() });
                return new ModelAndView("account_create", model);
             })
             .orElseGet(() -> new ModelAndView("redirect:/account/create/success", Collections.emptyMap()));
    }

    @GetMapping("/account/create/success")
    @ResponseBody
    public ModelAndView viewCreateSuccessPage() {
        return new ModelAndView("account_create_success", new HashMap<>());
    }



    //
    // DEPOSIT MAPPINGS
    //
    @GetMapping("/account/deposit/{account}")
    public ModelAndView viewDepositAccountPage(@PathVariable String account) {
        Map model = new HashMap();

        // Note accountSummary is eventually consistent so depending on end-to-end delay of projection
        // generation this may cause optimistic concurrency exceptions from simple sourcing.
        //
        // For deposit the read before write isn't perfect, using sequence/version here only solves duplicate
        // form submissions from happening
        return accountService.getAccountSummary(account)
                .map(accountSummary -> {
                    model.put("form", new DepositForm(0, accountSummary.version));
                    model.put("account", accountSummary.accountName);
                    model.put("errors", new String[] {});
                    return new ModelAndView("account_deposit", model);
                })
                .orElse(new ModelAndView("redirect:/", model)); // TODO should redirect to genric error page with link to home
    }

    @GetMapping("/account/deposit/success")
    @ResponseBody
    public ModelAndView viewDepositSuccessPage() {
        return new ModelAndView("account_deposit_success", new HashMap<>());
    }

    @PostMapping("/account/deposit/{account}")
    public ModelAndView handleDepositSubmit(@ModelAttribute DepositForm form, @PathVariable("account") String account) {
        Map model = new HashMap();

        // TODO we shouldn't have this logic here, accountService.withdraw should tell us account doesn't exist
        if (!accountService.accountExists(account)) {
            model.put("form", form);
            model.put("account", account);
            model.put("errors", new String[] { "Account does not exist"});
            return new ModelAndView("account_deposit", model);
        }

        accountService.deposit(account, form.getAmount(), form.getSequence());

        return new ModelAndView("redirect:/account/deposit/success", Collections.emptyMap());
    }

    //
    // WITHDRAW MAPPINGS
    //
    @GetMapping("/account/withdraw/{account}")
    public ModelAndView viewWithdrawAccountPage(@PathVariable String account) {
        Map model = new HashMap();

        // Note accountSummary is eventually consistent so depending on end-to-end delay of projection
        // generation this may cause optimistic concurrency exceptions from simple sourcing.
        //
        // For deposit the read before write isn't perfect, using sequence/version here only solves duplicate
        // form submissions from happening
        return accountService.getAccountSummary(account)
                .map(accountSummary -> {
                    model.put("form", new WithdrawForm(0, accountSummary.version));
                    model.put("account", accountSummary.accountName);
                    model.put("errors", new String[] {});
                    return new ModelAndView("account_withdraw", model);
                })
                .orElse(new ModelAndView("redirect:/", model));
    }

    @GetMapping("/account/withdraw/success")
    @ResponseBody
    public ModelAndView viewWithdrawSuccessPage() {
        return new ModelAndView("account_withdraw_success", new HashMap<>());
    }

    @PostMapping("/account/withdraw/{account}")
    public ModelAndView handleWithdawSubmit(@ModelAttribute WithdrawForm form, @PathVariable("account") String account) {
        Map model = new HashMap();

        // TODO we shouldn't have this logic here, accountService.withdraw should tell us account doesn't exist
        if (!accountService.accountExists(account)) {
            model.put("form", form);
            model.put("account", account);
            model.put("errors", new String[] { "Account does not exist"});
            return new ModelAndView("account_withdraw", model);
        }

        accountService.withdraw(account, form.getAmount(), form.getSequence());

        return new ModelAndView("redirect:/account/withdraw/success", Collections.emptyMap());
    }

    //
    // Transaction MAPPINGS
    //
    @GetMapping("/account/{account}/transactions")
    @ResponseBody
    public ModelAndView viewTransactions(@PathVariable("account") String account) {
        Map model = new HashMap();

        List<AccountTransactionRow> transactions = accountService
                .getTransactions(account)
                .stream()
                .map(tx -> new AccountTransactionRow(tx.ts, tx.amount))
                .collect(Collectors.toList());

        System.out.println("TRANSACTIONS SIZE: " + transactions.size());

        model.put("account", account);
        model.put("transactions", transactions);
        return new ModelAndView("account_transactions", model);
    }

}
