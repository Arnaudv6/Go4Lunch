package com.cleanup.go4lunch.data.useCase

import com.cleanup.go4lunch.data.pois.PoiEntity
import com.cleanup.go4lunch.data.pois.PoiRepository
import com.cleanup.go4lunch.data.settings.BoxEntity
import com.cleanup.go4lunch.data.settings.SettingsRepository
import com.cleanup.go4lunch.data.users.SessionUser
import com.cleanup.go4lunch.data.users.User
import com.cleanup.go4lunch.data.users.UsersRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import org.osmdroid.util.BoundingBox
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateUserUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    private val matesListMutableStateFlow = MutableStateFlow<List<User>>(emptyList())
    val matesListFlow: Flow<List<User>> = matesListMutableStateFlow.asStateFlow()

    suspend operator fun invoke() {
        matesListMutableStateFlow.tryEmit(emptyList())
    }
}


