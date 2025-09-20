package com.caganyanmaz.werewolf.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;



@Controller
public class HomeController {
	@GetMapping("/")
	public String index() {
		return "index";
	}

	@GetMapping("/new-game")
	public String new_game() {
		return "new_game";
	}

	@GetMapping("join-game")
	public String join_game() {
		return "join_game";
	}
	@GetMapping("/enter")
    public String enterPage() {
        return "enter.html"; 
    }
}
