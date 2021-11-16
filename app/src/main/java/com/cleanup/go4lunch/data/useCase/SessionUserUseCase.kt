package com.cleanup.go4lunch.data.useCase

import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.users.SessionUser
import com.cleanup.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionUserUseCase
@Inject constructor(
    settingsRepository: SettingsRepository,
    usersRepository: UsersRepository
){
    val sessionUserFlow: Flow<SessionUser?> = settingsRepository.idStateFlow.map {
        if (it == null) {
            null
        } else {
            val user = usersRepository.getSessionUser(it)
            if (user == null) null
            else SessionUser(
                user,
                longArrayOf(),  // todo usersRepository.getLikedById(it).toLongArray(),
                settingsRepository.getConnectionType()
            )
        }
    }


}