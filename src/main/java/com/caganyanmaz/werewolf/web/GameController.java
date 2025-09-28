package com.caganyanmaz.werewolf.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.caganyanmaz.werewolf.application.room.RoomService;

@Controller
@RequestMapping("/game")
public class GameController {
    RoomService service;

}
