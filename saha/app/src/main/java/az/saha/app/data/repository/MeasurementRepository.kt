package az.saha.app.data.repository

import az.saha.app.data.local.MeasurementDao
import az.saha.app.data.local.MeasurementEntity
import az.saha.app.data.model.Measurement
import az.saha.app.data.remote.FirebaseMeasurementSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class MeasurementRepository(
    private val dao: MeasurementDao,
    private val remote: FirebaseMeasurementSource
) {
    fun observe(userId: String): Flow<List<Measurement>> =
        dao.observeByUser(userId).map { list -> list.map { it.toDomain() } }

    suspend fun save(measurement: Measurement): Measurement {
        val withId = if (measurement.id.isBlank()) {
            measurement.copy(id = UUID.randomUUID().toString())
        } else measurement

        val local = withId.copy(synced = false, updatedAt = System.currentTimeMillis())
        dao.upsert(MeasurementEntity.fromDomain(local))

        return try {
            remote.upsert(local)
            val synced = local.copy(synced = true)
            dao.upsert(MeasurementEntity.fromDomain(synced))
            synced
        } catch (_: Exception) {
            local
        }
    }

    suspend fun delete(userId: String, id: String) {
        dao.deleteById(id)
        runCatching { remote.delete(userId, id) }
    }

    suspend fun sync(userId: String) {
        val remoteItems = remote.fetchAll(userId)
        dao.upsertAll(remoteItems.map { MeasurementEntity.fromDomain(it.copy(synced = true)) })

        dao.unsynced(userId).forEach { entity ->
            runCatching {
                remote.pushLocal(entity)
                dao.upsert(entity.copy(synced = true))
            }
        }
    }
}
