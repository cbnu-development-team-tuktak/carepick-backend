package com.callrapport.controller.admin

import org.springframework.web.bind.annotation.* 

@RestController
@RequestMapping("/api/admin")
class AdminController {
    @GetMapping("/dashboard")
    fun getAdminDashBoard(): Map<String, String> {
        return mapOf("message" to "Welcome to the Admin Dashboard")
    }
}