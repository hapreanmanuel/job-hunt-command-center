package org.manu.jobhunt.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Server-rendered shell pages. They render once and then fetch and change data client-side
 * through the /api endpoints, so they hold no user data.
 */
@Controller
public class PageController {

    private final boolean signInEnabled;
    private final String siteOwner;
    private final String contactEmail;
    private final String coffeeUrl;
    private final String accessRequestUrl;

    PageController(
            @Value("${app.sign-in-enabled}") boolean signInEnabled,
            @Value("${app.site.owner}") String siteOwner,
            @Value("${app.site.contact-email}") String contactEmail,
            @Value("${app.site.coffee-url}") String coffeeUrl,
            @Value("${app.site.access-request-url}") String accessRequestUrl) {
        this.signInEnabled = signInEnabled;
        this.siteOwner = siteOwner;
        this.contactEmail = contactEmail;
        this.coffeeUrl = coffeeUrl;
        this.accessRequestUrl = accessRequestUrl;
    }

    /** Pages hide what only makes sense for someone who signed in, such as Sign out. */
    @ModelAttribute
    void pageSettings(Model model) {
        model.addAttribute("signInEnabled", signInEnabled);
        model.addAttribute("siteOwner", siteOwner);
        model.addAttribute("contactEmail", contactEmail);
        model.addAttribute("coffeeUrl", coffeeUrl);
        model.addAttribute("accessRequestUrl", accessRequestUrl);
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/jobs/{id}")
    public String job(@PathVariable Long id) {
        return "job";
    }

    @GetMapping("/login")
    public String login() {
        return signInEnabled ? "login" : "redirect:/";
    }

    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }

    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }
}
