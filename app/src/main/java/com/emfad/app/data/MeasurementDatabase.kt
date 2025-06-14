package com.emfad.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// TODO: Define Measurement, Session, and Calibration entities, and their respective DAOs
// For example:
// @Entity(tableName = "measurements")
// data class Measurement(
//     @PrimaryKey(autoGenerate = true) val id: Int = 0,
//     val timestamp: Long,
//     val value: Double,
//     val sessionId: String?
// )
//
// @Dao
// interface MeasurementDao {
//     @Insert
//     suspend fun insertMeasurement(measurement: Measurement)
//
//     @Query("SELECT * FROM measurements")
//     fun getAllMeasurements(): Flow<List<Measurement>>
// }

@Database(entities = [], version = 2) // TODO: Add your entities here, e.g., [Measurement::class, Session::class, Calibration::class]
@TypeConverters(Converters::class) // TODO: Create a Converters class for custom type conversions (e.g., Date to Long)
abstract class MeasurementDatabase : RoomDatabase() {
    // TODO: Add your DAO abstract methods here, e.g.,
    // abstract fun measurementDao(): MeasurementDao
    // abstract fun sessionDao(): SessionDao
    // abstract fun calibrationDao(): CalibrationDao

    companion object {
        @Volatile
        private var INSTANCE: MeasurementDatabase? = null

        fun getInstance(context: Context): MeasurementDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MeasurementDatabase::class.java,
                    "emfad_database"
                )
                .addMigrations(MIGRATION_1_2)
                .build()
                INSTANCE = instance
                instance
            }
        }

        // Migration from version 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // TODO: Implement your migration logic here
                // For example:
                // database.execSQL("ALTER TABLE measurements ADD COLUMN sessionId TEXT")
            }
        }
    }
}

