package com.cleanup.go4lunch.data.settings

import androidx.room.ColumnInfo
import androidx.room.Entity;
import androidx.room.Ignore
import androidx.room.PrimaryKey;

@Entity(
    tableName = "int_store",
)
data class IntEntity (
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "key_string")
    val keyString: String,
    val data: Int
){
    companion object{
        const val NAV_NUM = "nav_num"
        // todo Nino : c'est le bon endroit pour mettre ca?
    }
}

