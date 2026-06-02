package com.example.samplesizecalculator.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object PdfExportUtils {

    /**
     * Requires FileProvider setup (I list it below).
     */
    fun exportAndSharePdf(context: Context, title: String, content: String) {
        val pdfFile = createPdf(context, title, content)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Export PDF"))
    }

    private fun createPdf(context: Context, title: String, content: String): File {
        val document = PdfDocument()
        val paint = Paint().apply { textSize = 12f }

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4-ish
        val page = document.startPage(pageInfo)

        var y = 40f
        page.canvas.drawText(title, 40f, y, Paint().apply { textSize = 16f })
        y += 30f

        val lines = content.split("\n")
        for (line in lines) {
            if (y > 820f) break // simple: one page only for now
            page.canvas.drawText(line, 40f, y, paint)
            y += 18f
        }

        document.finishPage(page)

        val outFile = File(context.cacheDir, "BioCalX_Result_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outFile).use { document.writeTo(it) }
        document.close()

        return outFile
    }
}
