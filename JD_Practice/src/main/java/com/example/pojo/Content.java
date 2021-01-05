package com.example.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
  *  @Author Liu Haonan
  *  @Date 2020/10/11 15:19
  *  @Description 爬取下来的每一个记录对象
  */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    private String title;
    private String img;
    private String price;
}
