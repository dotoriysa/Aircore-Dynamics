package com.example.demo.controller;


import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:4200"})
public class UserController {

    @Autowired
    private UserService userService;

//    @GetMapping
//    public ResponseEntity<List<User>> getAllUsers(){
//        List<User> users = userService.getAllUsers();
//        return ResponseEntity.ok(users);
//    }
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

//    @PostMapping
//    public ResponseEntity<String> createUser(@RequestBody User user){
//        userService.createUser(user);
//    }

    @PostMapping
    public void createUser(@RequestBody User user){
        userService.createUser(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return user;
    }

    @PutMapping("/{id}")
    public void updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        userService.updateUser(user);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id){
        userService.deleteUserById(id);
    }



}
