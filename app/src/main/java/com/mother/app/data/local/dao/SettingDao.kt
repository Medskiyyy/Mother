package com.mother.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mother.app.data.local.entity.AppSettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingDao {

    @Query("SELECT * FROM app_setting WHERE id = 1")
    fun observeSetting(): Flow<AppSettingEntity?>

    @Query("SELECT * FROM app_setting WHERE id = 1")
    suspend fun getSetting(): AppSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(setting: AppSettingEntity)

    @Query("UPDATE app_setting SET theme = :theme WHERE id = 1")
    suspend fun updateTheme(theme: String)
}