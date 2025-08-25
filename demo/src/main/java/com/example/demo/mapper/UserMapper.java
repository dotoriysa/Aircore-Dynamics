package com.example.demo.mapper;


import com.example.demo.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> findAll();

    void insert(User user);

    User findById(@Param("id") Long id);

    void update(User user);

    void deleteById(@Param("id") Long id);


}
