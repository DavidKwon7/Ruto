package com.handylab.ruto.auth

import com.handylab.ruto.domain.AuthState

val AuthState.isGuest: Boolean
    get() = this is AuthState.Guest
