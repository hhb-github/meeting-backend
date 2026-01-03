package com.meeting.manager.controller;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Web页面控制器，处理前端路由回退
 * 设置为最低优先级，确保API路由优先处理
 */
@Controller
@Order(999)
public class WebController {

    /**
     * 前端首页 - 根路径回退
     * 让Spring Boot的默认静态资源处理器处理静态文件
     */

    /**
     * 前端SPA路由 - 处理前端路由
     * 只处理不包含/api前缀且不是静态文件的路由
     */
    @RequestMapping(value = {"/dashboard", "/participants", "/meetings", "/action-items", "/follow-ups"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}