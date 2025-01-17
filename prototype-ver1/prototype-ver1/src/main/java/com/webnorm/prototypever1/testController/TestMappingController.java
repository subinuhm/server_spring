package com.webnorm.prototypever1.testController;

import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class TestMappingController {
    // 회원가입 링킹(테스트용)
    @RequestMapping("/members/signup")
    public String signup() {
        return "/members/signupForm";
    }

    @RequestMapping("/members/memberList")
    public String memberList() {
        return "/members/memberList";
    }

    @RequestMapping("/members/loginPage")
    public String login() {
        return "/members/loginForm";
    }
}
