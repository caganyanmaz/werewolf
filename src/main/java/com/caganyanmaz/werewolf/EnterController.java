package com.caganyanmaz.werewolf;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EnterController {
    @GetMapping("/enter")
    public String enterPage() {
        return "enter.html"; 
    }
}
