package com.caganyanmaz.werewolf.web;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.caganyanmaz.werewolf.application.room.RoomService;
import com.caganyanmaz.werewolf.application.room.RoomService.PlayerView;

import org.springframework.web.bind.annotation.RequestBody;




@Controller
@RequestMapping("/room")
public class RoomController {
    private final RoomService room_service;
    public RoomController(RoomService room_service) {
        this.room_service = room_service;
    }

    @PostMapping("/create")
    public String create(Principal principal) {
        System.out.println("Creating room for " + principal.getName());
        String room_id = room_service.create_room(principal.getName());
        return "redirect:/room/" + room_id;
    }

    @GetMapping("/enter")
    public String getMethodName(@RequestParam String room_id) {
        return "redirect:/room/" + room_id;
    }

    @PostMapping("/join/{room_id}")
    public String join_room(Principal principal, @RequestParam String nickname, @PathVariable String room_id) {
        room_service.add_participant_to_room(room_id, principal.getName(), nickname);
        return "redirect:/room/" + room_id;
    }
    

    @GetMapping("/{room_id}")
    public String room(Principal principal, @PathVariable String room_id, Model model) {
        
        RoomService.LobbyView lobby_view;
        lobby_view = room_service.get_lobby_view(room_id);
        if (!is_player_in_lobby(principal.getName(), lobby_view.players())) {
            model.addAttribute("room_id", room_id);
            return "enter_lobby";
        }
        model.addAttribute("lobby", lobby_view);
        return "room";
    }

    public boolean is_player_in_lobby(String player_id, List<RoomService.LobbyPlayer> players) {
        for (RoomService.LobbyPlayer player : players) {
            if (player.id() == player_id)
                return true;
        }
        return false;
    }
    
}
