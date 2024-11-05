package com.example.solo_play_web_server.common.dto

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

class CustomUser(
    val id : Long,
    email : String,
    password : String,
    authority : Collection<GrantedAuthority>,
) : User(email, password, authority)