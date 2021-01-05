package com.example.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
  *  @Author Liu Haonan
  *  @Date 2020/10/11 13:16
  *  @Description 实体类，对应es中的文档记录
  */
@Data
@Component
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private String name;
    private int age;

}
