package com.example.demo.service;

import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    public List<User> getAllUsers(){
        return userMapper.findAll();
    }

    public void createUser(User user){
        userMapper.insert(user);
    }

    public User getUserById(Long id){
        return userMapper.findById(id);
    }

    public void updateUser(User user){
        userMapper.update(user);
    }

    public void deleteUserById(Long id){
        userMapper.deleteById(id);
    }

}
