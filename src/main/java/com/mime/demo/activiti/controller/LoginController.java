package com.mime.demo.activiti.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author SlumDuck
 * @create 2018-03-09 10:38
 * @desc
 */
@Controller
@RequestMapping
public class LoginController {

    @RequestMapping("login")
    @ResponseBody
    public String login(){
        System.out.println("hello world");
        return "index.html";
    }
}
