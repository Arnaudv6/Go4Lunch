package com.cleanup.go4lunch.data.settings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setBox(boxEntity: BoxEntity)

    @Query("SELECT * FROM box LIMIT 1")
    fun getBox(): Flow<BoxEntity?>
    // todo Nino : là, j'ai le droit de ne pas faire un flow de liste ?

}
