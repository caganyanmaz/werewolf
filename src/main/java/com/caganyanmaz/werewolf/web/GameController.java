package com.caganyanmaz.werewolf.web;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.caganyanmaz.werewolf.application.room.RoomService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/game")
public class GameController {
    private final RoomService room_service;

    public GameController(RoomService room_service) {
        this.room_service = room_service;
    }

    @GetMapping("/{game_id}")
    public String game(Principal principal, @PathVariable String game_id, Model model) {
        
        RoomService.GameView game_view;
        if (!room_service.is_player_in_game(game_id, principal.getName())) {
            return "redirect:/";
        }
        game_view = room_service.get_game_view(game_id);
        model.addAttribute("game", game_view);
        model.addAttribute("player_id", principal.getName());
        return "room";
    }
}