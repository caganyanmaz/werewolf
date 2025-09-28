package com.caganyanmaz.werewolf.web;

import java.security.Principal;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.caganyanmaz.werewolf.application.room.RoomService;


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
        if (!room_service.is_player_in_lobby(room_id, principal.getName())) {
            model.addAttribute("room_id", room_id);
            return "enter_lobby";
        }
        lobby_view = room_service.get_lobby_view(room_id);
        model.addAttribute("lobby", lobby_view);
        model.addAttribute("player_id", principal.getName());
        return "room";
    }

    public record ReadyPayload(Boolean ready) {}
    
    @PostMapping(path = "/{room_id}/players/{player_id}/ready",
             consumes = "application/json",
             produces = "application/json")
    @ResponseBody
    public ResponseEntity<?> setReady(Principal principal,
                                  @PathVariable("room_id") String room_id,
                                  @PathVariable("player_id") String player_id,
                                  @RequestBody ReadyPayload payload) { 
        if (!is_user_authorized_to_update_readiness(principal, room_id, player_id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "Forbidden"));
 
        }
        if (payload.ready() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing 'ready'"));
        }
        room_service.update_player_ready(room_id, player_id, payload.ready());
        return ResponseEntity.ok(Map.of("ok", true));
    }
    
    private boolean is_user_authorized_to_update_readiness(Principal principal, String room_id, String player_id) {
        return principal.getName().equals(player_id) && room_service.is_player_in_lobby(room_id, player_id);
    }

}
