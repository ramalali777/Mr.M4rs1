package az.saha.app.data.remote

import az.saha.app.data.local.MeasurementEntity
import az.saha.app.data.model.Measurement
import az.saha.app.domain.AreaUnit
import az.saha.app.domain.GeoPoint
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseMeasurementSource(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private fun col(userId: String) =
        firestore.collection("users").document(userId).collection("measurements")

    suspend fun fetchAll(userId: String): List<Measurement> {
        val snap = col(userId).get().await()
        return snap.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            val points = (data["points"] as? List<*>)?.mapNotNull { raw ->
                val map = raw as? Map<*, *> ?: return@mapNotNull null
                val lat = (map["lat"] as? Number)?.toDouble() ?: return@mapNotNull null
                val lng = (map["lng"] as? Number)?.toDouble() ?: return@mapNotNull null
                GeoPoint(lat, lng)
            }.orEmpty()

            Measurement(
                id = doc.id,
                userId = userId,
                title = data["title"] as? String ?: "",
                points = points,
                areaSquareMeters = (data["areaSquareMeters"] as? Number)?.toDouble() ?: 0.0,
                perimeterMeters = (data["perimeterMeters"] as? Number)?.toDouble() ?: 0.0,
                preferredUnit = runCatching {
                    AreaUnit.valueOf(data["preferredUnit"] as? String ?: AreaUnit.SOT.name)
                }.getOrDefault(AreaUnit.SOT),
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: 0L,
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: 0L,
                synced = true
            )
        }
    }

    suspend fun upsert(measurement: Measurement) {
        val payload = mapOf(
            "title" to measurement.title,
            "points" to measurement.points.map {
                mapOf("lat" to it.latitude, "lng" to it.longitude)
            },
            "areaSquareMeters" to measurement.areaSquareMeters,
            "perimeterMeters" to measurement.perimeterMeters,
            "preferredUnit" to measurement.preferredUnit.name,
            "createdAt" to measurement.createdAt,
            "updatedAt" to measurement.updatedAt
        )
        col(measurement.userId)
            .document(measurement.id)
            .set(payload, SetOptions.merge())
            .await()
    }

    suspend fun delete(userId: String, id: String) {
        col(userId).document(id).delete().await()
    }

    suspend fun pushLocal(entity: MeasurementEntity) {
        upsert(entity.toDomain().copy(synced = true))
    }
}
