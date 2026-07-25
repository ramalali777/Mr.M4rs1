package az.saha.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MeasurementEntity::class], version = 1, exportSchema = false)
abstract class SahaDatabase : RoomDatabase() {
    abstract fun measurementDao(): MeasurementDao

    companion object {
        @Volatile private var instance: SahaDatabase? = null

        fun get(context: Context): SahaDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    SahaDatabase::class.java,
                    "saha.db"
                ).build().also { instance = it }
            }
    }
}
