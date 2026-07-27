package az.saha.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurements WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeByUser(userId: String): Flow<List<MeasurementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: MeasurementEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<MeasurementEntity>)

    @Query("DELETE FROM measurements WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM measurements WHERE userId = :userId AND synced = 0")
    suspend fun unsynced(userId: String): List<MeasurementEntity>
}
