package victor.mexico.ventassobreruedas.export

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import victor.mexico.ventassobreruedas.R
import victor.mexico.ventassobreruedas.data.repository.PeriodSummary
import victor.mexico.ventassobreruedas.util.PeriodType
import victor.mexico.ventassobreruedas.util.changePercent
import victor.mexico.ventassobreruedas.util.formatCents
import victor.mexico.ventassobreruedas.util.formatCentsShort
import victor.mexico.ventassobreruedas.util.formatLongDate
import victor.mexico.ventassobreruedas.util.formatPeriodLabel
import victor.mexico.ventassobreruedas.util.formatShortDate
import victor.mexico.ventassobreruedas.util.formatShortDayOfWeek
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Genera el PDF del resumen (totales, gráfica, tabla por día y detalle de cada venta) con
 * [PdfDocument] del sistema, sin librerías extra, y lo comparte con el selector de Android.
 * Los textos salen de strings.xml, así el PDF queda en el idioma del teléfono.
 */
object ReportPdf {

    /** Escribe el PDF en la caché de la app; llamar fuera del hilo principal. */
    fun write(
        context: Context,
        summary: PeriodSummary,
        previousTotalCents: Long,
        userName: String?,
        zone: ZoneId,
    ): File {
        val dir = File(context.cacheDir, EXPORT_DIR).apply { mkdirs() }
        // Solo se conserva el último PDF; los anteriores ya se compartieron.
        dir.listFiles()?.forEach { it.delete() }
        val period = summary.period
        val name = when (period.type) {
            PeriodType.WEEK -> "resumen-semana-${period.start}.pdf"
            PeriodType.MONTH -> "resumen-mes-${period.start.year}-%02d.pdf".format(period.start.monthValue)
        }
        val file = File(dir, name)
        val document = PdfDocument()
        try {
            ReportPdfRenderer(context, document, summary, previousTotalCents, userName, zone).render()
            file.outputStream().use { document.writeTo(it) }
        } finally {
            document.close()
        }
        return file
    }

    fun share(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            clipData = ClipData.newRawUri(title, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(send, context.getString(R.string.pdf_share_chooser)))
    }

    fun title(context: Context, type: PeriodType): String = context.getString(
        when (type) {
            PeriodType.WEEK -> R.string.pdf_title_week
            PeriodType.MONTH -> R.string.pdf_title_month
        },
    )

    private const val EXPORT_DIR = "exports"
}

// Tamaño carta en puntos (1/72 de pulgada).
private const val PAGE_WIDTH = 612
private const val PAGE_HEIGHT = 792
private const val MARGIN = 40f
private const val FOOTER_SPACE = 24f
private const val CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN

private const val COLOR_PRIMARY = 0xFF00838F.toInt()
private const val COLOR_PRIMARY_LIGHT = 0xFFE0F5F7.toInt()
private const val COLOR_TEXT = 0xFF161D1D.toInt()
private const val COLOR_MUTED = 0xFF5F6969.toInt()
private const val COLOR_LINE = 0xFFBEC8C9.toInt()
private const val COLOR_ROW = 0xFFF2F6F6.toInt()

private class ReportPdfRenderer(
    private val context: Context,
    private val document: PdfDocument,
    private val summary: PeriodSummary,
    private val previousTotalCents: Long,
    private val userName: String?,
    private val zone: ZoneId,
) {
    private val locale = Locale.forLanguageTag(context.getString(R.string.date_locale))
    private val datePattern = context.getString(R.string.date_format)
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val weekly = summary.period.type == PeriodType.WEEK

    private var page: PdfDocument.Page? = null
    private var pageNumber = 0
    private var y = MARGIN
    private val canvas: Canvas get() = checkNotNull(page).canvas

    fun render() {
        newPage()
        drawHeader()
        drawStats()
        drawChart()
        if (summary.activeDays.isEmpty()) {
            ensureSpace(20f)
            text(context.getString(R.string.no_sales_in_period), MARGIN, y + 12f, paint(11f, COLOR_MUTED))
            y += 20f
        } else {
            drawDayTable()
            drawDetails()
        }
        finishPage()
    }

    private fun drawHeader() {
        text(context.getString(R.string.app_name), MARGIN, y + 10f, paint(10f, COLOR_PRIMARY, bold = true))
        y += 16f
        text(ReportPdf.title(context, summary.period.type), MARGIN, y + 20f, paint(20f, COLOR_TEXT, bold = true))
        y += 28f
        text(formatPeriodLabel(summary.period, locale), MARGIN, y + 13f, paint(13f, COLOR_TEXT))
        y += 20f
        val now = LocalDateTime.now(zone)
        val generated = context.getString(
            R.string.pdf_generated,
            "${formatLongDate(now.toLocalDate(), datePattern, locale)} ${timeFormat.format(now)}",
        )
        val meta = listOfNotNull(userName?.takeIf { it.isNotBlank() }, generated).joinToString(" · ")
        text(meta, MARGIN, y + 10f, paint(10f, COLOR_MUTED))
        y += 18f
        line(y)
        y += 14f
    }

    private fun drawStats() {
        val best = summary.bestDay
        val stats = listOf(
            context.getString(R.string.stat_total) to formatCents(summary.totalCents),
            context.getString(R.string.stat_sales) to summary.count.toString(),
            context.getString(R.string.stat_average) to formatCents(summary.averagePerActiveDayCents),
            context.getString(R.string.stat_best_day) to
                (best?.let { "${formatShortDate(it.date, locale)} · ${formatCentsShort(it.totalCents)}" } ?: "—"),
        )
        val gap = 8f
        val boxWidth = (CONTENT_WIDTH - gap * (stats.size - 1)) / stats.size
        val boxHeight = 50f
        val labelPaint = paint(9f, COLOR_MUTED)
        val valuePaint = paint(13f, COLOR_TEXT, bold = true)
        stats.forEachIndexed { i, (label, value) ->
            val left = MARGIN + i * (boxWidth + gap)
            canvas.drawRoundRect(RectF(left, y, left + boxWidth, y + boxHeight), 6f, 6f, fill(COLOR_PRIMARY_LIGHT))
            text(label, left + 8f, y + 16f, labelPaint, boxWidth - 16f)
            text(value, left + 8f, y + 36f, valuePaint, boxWidth - 16f)
        }
        y += boxHeight + 10f
        changePercent(summary.totalCents, previousTotalCents)?.let { percent ->
            val res = if (weekly) R.string.vs_previous_week else R.string.vs_previous_month
            val color = if (percent >= 0) COLOR_PRIMARY else 0xFFBA1A1A.toInt()
            text(context.getString(res, "%+d%%".format(percent)), MARGIN, y + 10f, paint(10f, color, bold = true))
            y += 18f
        }
        y += 8f
    }

    private fun drawChart() {
        val valuesHeight = if (weekly) 12f else 0f
        val barsHeight = 130f
        val labelsHeight = 14f
        ensureSpace(valuesHeight + barsHeight + labelsHeight + 16f)
        val days = summary.days
        val max = days.maxOfOrNull { it.totalCents }?.takeIf { it > 0 } ?: 1
        val slot = CONTENT_WIDTH / days.size
        val barWidth = slot * if (weekly) 0.6f else 0.7f
        val top = y + valuesHeight
        val baseline = top + barsHeight
        val valuePaint = paint(8f, COLOR_TEXT, align = Paint.Align.CENTER)
        val labelPaint = paint(if (weekly) 9f else 7f, COLOR_MUTED, align = Paint.Align.CENTER)
        val barPaint = fill(COLOR_PRIMARY)
        days.forEachIndexed { i, day ->
            val center = MARGIN + slot * i + slot / 2
            if (day.totalCents > 0) {
                val barTop = baseline - barsHeight * day.totalCents / max
                canvas.drawRect(center - barWidth / 2, barTop, center + barWidth / 2, baseline, barPaint)
                if (weekly) canvas.drawText(formatCentsShort(day.totalCents), center, barTop - 3f, valuePaint)
            }
            val label = if (weekly) formatShortDayOfWeek(day.date, locale) else day.date.dayOfMonth.toString()
            canvas.drawText(label, center, baseline + 11f, labelPaint)
        }
        line(baseline)
        y = baseline + labelsHeight + 16f
    }

    private fun drawDayTable() {
        sectionTitle(context.getString(R.string.pdf_by_day))
        val columns = floatArrayOf(MARGIN + 8f, MARGIN + CONTENT_WIDTH * 0.68f, MARGIN + CONTENT_WIDTH - 8f)
        tableRow(
            listOf(context.getString(R.string.date), context.getString(R.string.stat_sales), context.getString(R.string.stat_total)),
            columns,
            bold = true,
            shaded = false,
        )
        summary.activeDays.forEachIndexed { i, day ->
            tableRow(
                listOf(formatLongDate(day.date, datePattern, locale), day.count.toString(), formatCents(day.totalCents)),
                columns,
                bold = false,
                shaded = i % 2 == 0,
            )
        }
        line(y)
        tableRow(
            listOf(context.getString(R.string.stat_total), summary.count.toString(), formatCents(summary.totalCents)),
            columns,
            bold = true,
            shaded = false,
        )
        y += 16f
    }

    private fun drawDetails() {
        sectionTitle(context.getString(R.string.pdf_details))
        val columns = floatArrayOf(MARGIN + 8f, MARGIN + 60f, MARGIN + CONTENT_WIDTH - 8f)
        tableRow(
            listOf(context.getString(R.string.pdf_time), context.getString(R.string.product), context.getString(R.string.price)),
            columns,
            bold = true,
            shaded = false,
        )
        line(y)
        val byDay = summary.sales.groupBy { it.createdAt.atZone(zone).toLocalDate() }
        summary.activeDays.forEach { day ->
            // El encabezado del día no se queda solo al final de una página.
            ensureSpace(22f + ROW_HEIGHT)
            y += 6f
            text(formatLongDate(day.date, datePattern, locale), MARGIN, y + 11f, paint(11f, COLOR_TEXT, bold = true))
            text(formatCents(day.totalCents), MARGIN + CONTENT_WIDTH, y + 11f, paint(11f, COLOR_PRIMARY, bold = true, align = Paint.Align.RIGHT))
            y += 16f
            byDay[day.date].orEmpty().forEachIndexed { i, sale ->
                tableRow(
                    listOf(timeFormat.format(sale.createdAt.atZone(zone)), sale.productName, formatCents(sale.priceCents)),
                    columns,
                    bold = false,
                    shaded = i % 2 == 0,
                )
            }
            y += 6f
        }
    }

    private fun sectionTitle(title: String) {
        ensureSpace(28f + ROW_HEIGHT * 2)
        text(title, MARGIN, y + 14f, paint(14f, COLOR_PRIMARY, bold = true))
        y += 24f
    }

    /**
     * Fila de 3 columnas: [columns] son la x de la primera (izquierda), de la segunda (izquierda)
     * y el borde derecho de la última, que se alinea a la derecha (montos).
     */
    private fun tableRow(cells: List<String>, columns: FloatArray, bold: Boolean, shaded: Boolean) {
        ensureSpace(ROW_HEIGHT)
        if (shaded) canvas.drawRect(MARGIN, y, MARGIN + CONTENT_WIDTH, y + ROW_HEIGHT, fill(COLOR_ROW))
        val base = y + ROW_HEIGHT - 6f
        val left = paint(10f, COLOR_TEXT, bold)
        val right = paint(10f, COLOR_TEXT, bold, Paint.Align.RIGHT)
        text(cells[0], columns[0], base, left, columns[1] - columns[0] - 8f)
        text(cells[1], columns[1], base, left, columns[2] - columns[1] - 80f)
        canvas.drawText(cells[2], columns[2], base, right)
        y += ROW_HEIGHT
    }

    private fun ensureSpace(height: Float) {
        if (y + height > PAGE_HEIGHT - MARGIN - FOOTER_SPACE) {
            finishPage()
            newPage()
        }
    }

    private fun newPage() {
        pageNumber++
        page = document.startPage(PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create())
        y = MARGIN
    }

    private fun finishPage() {
        val footer = paint(9f, COLOR_MUTED, align = Paint.Align.RIGHT)
        canvas.drawText(
            context.getString(R.string.pdf_page, pageNumber),
            MARGIN + CONTENT_WIDTH,
            PAGE_HEIGHT - MARGIN + 8f,
            footer,
        )
        document.finishPage(checkNotNull(page))
        page = null
    }

    private fun line(atY: Float) {
        canvas.drawLine(MARGIN, atY, MARGIN + CONTENT_WIDTH, atY, fill(COLOR_LINE).apply { strokeWidth = 0.8f })
    }

    /** Dibuja [value] recortándolo con "…" si no cabe en [maxWidth]. */
    private fun text(value: String, x: Float, baseline: Float, paint: TextPaint, maxWidth: Float = CONTENT_WIDTH) {
        val fitted = TextUtils.ellipsize(value, paint, maxWidth, TextUtils.TruncateAt.END).toString()
        canvas.drawText(fitted, x, baseline, paint)
    }

    private fun paint(size: Float, color: Int, bold: Boolean = false, align: Paint.Align = Paint.Align.LEFT) =
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            this.color = color
            typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
            textAlign = align
        }

    private fun fill(color: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

    private companion object {
        const val ROW_HEIGHT = 18f
    }
}
