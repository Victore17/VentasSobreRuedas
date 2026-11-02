package victor.mexico.ventassobreruedas.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class Sale(
    val id: String,
    val productName: String,
    val priceCents: Long,
    val createdAt: Instant,
)

/** Resumen de un día en el que hubo ventas. */
data class SalesDay(
    val date: LocalDate,
    val count: Int,
    val totalCents: Long,
)

/**
 * Ventas del usuario guardadas en Firestore, en `users/{uid}/sales`.
 * Firestore mantiene una copia local: las lecturas y escrituras funcionan sin internet
 * y se sincronizan solas cuando vuelve la conexión.
 */
class SalesRepository(
    firestore: FirebaseFirestore,
    val uid: String,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    private val sales: CollectionReference =
        firestore.collection("users").document(uid).collection("sales")

    fun today(): LocalDate = LocalDate.now(clock)

    fun observeSales(day: LocalDate): Flow<List<Sale>> {
        val (from, to) = day.toTimestampRange()
        return sales
            .whereGreaterThanOrEqualTo(FIELD_CREATED_AT, from)
            .whereLessThan(FIELD_CREATED_AT, to)
            .orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING)
            .observe()
    }

    fun observeTotalCents(day: LocalDate): Flow<Long> =
        observeSales(day).map { list -> list.sumOf { it.priceCents } }

    /** Días con al menos una venta, del más reciente al más antiguo. */
    fun observeSalesDays(): Flow<List<SalesDay>> =
        sales.orderBy(FIELD_CREATED_AT, Query.Direction.DESCENDING).observe().map { list ->
            list
                .groupBy { it.createdAt.atZone(clock.zone).toLocalDate() }
                .map { (date, sales) -> SalesDay(date, sales.size, sales.sumOf { it.priceCents }) }
        }

    // Las escrituras no esperan al servidor: Firestore las aplica localmente al instante
    // y las sube cuando hay conexión. Esperarlas dejaría la app trabada sin internet.

    /** Registra la venta; la fecha se asigna automáticamente con la hora actual. */
    fun addSale(productName: String, priceCents: Long) {
        sales.document().set(saleFields(productName, priceCents, Instant.now(clock)))
    }

    /** Corrige el producto y el precio; la fecha original de la venta se conserva. */
    fun updateSale(id: String, productName: String, priceCents: Long) {
        sales.document(id).update(
            mapOf(FIELD_PRODUCT_NAME to productName, FIELD_PRICE_CENTS to priceCents),
        )
    }

    fun deleteSale(id: String) {
        sales.document(id).delete()
    }

    private fun Query.observe(): Flow<List<Sale>> =
        snapshots()
            .map { snapshot -> snapshot.documents.mapNotNull { it.toSale() } }
            .catch { e ->
                Log.e(TAG, "Error al leer las ventas", e)
                emit(emptyList())
            }

    private fun LocalDate.toTimestampRange(): Pair<Timestamp, Timestamp> {
        val zone: ZoneId = clock.zone
        val from = atStartOfDay(zone).toInstant()
        val to = plusDays(1).atStartOfDay(zone).toInstant()
        return Timestamp(from) to Timestamp(to)
    }

    private fun DocumentSnapshot.toSale(): Sale? {
        val name = getString(FIELD_PRODUCT_NAME) ?: return null
        val price = getLong(FIELD_PRICE_CENTS) ?: return null
        val createdAt = getTimestamp(FIELD_CREATED_AT) ?: return null
        return Sale(id, name, price, createdAt.toInstant())
    }

    companion object {
        private const val TAG = "SalesRepository"
        private const val FIELD_PRODUCT_NAME = "productName"
        private const val FIELD_PRICE_CENTS = "priceCents"
        private const val FIELD_CREATED_AT = "createdAt"

        private fun saleFields(productName: String, priceCents: Long, createdAt: Instant) = mapOf(
            FIELD_PRODUCT_NAME to productName,
            FIELD_PRICE_CENTS to priceCents,
            FIELD_CREATED_AT to Timestamp(createdAt),
        )
    }
}
