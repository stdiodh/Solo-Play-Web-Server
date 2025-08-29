package com.example.solo_play_web_server.auth.entity

import com.example.solo_play_web_server.auth.enum.AuthProvider
import com.example.solo_play_web_server.auth.enum.MemberRole
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document("users")
data class Member(
    @Id
    val id : String? = null,
    val email : String,
    val password : String,
    val nickname : String,
    val imageUrl : String? = null,
    val provider : AuthProvider,
    val role : Set<MemberRole>,
    val agreement : Agreement,
    val verified: Boolean
)

data class Agreement(
    val isOver14 : Boolean,
    val isAgreedToTerms : Boolean,
    val isAgreedToMarketing : Boolean,
    val isConsentedToAds : Boolean
)

data class PendingMember(
    val email: String,
    val password: String,
    val nickname: String,
    val agreement: Agreement
)

data class VerificationData (
    val pendingMember: PendingMember,
    val code: String
)