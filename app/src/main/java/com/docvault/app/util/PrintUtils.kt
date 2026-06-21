package com.docvault.app.util

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.print.PrintHelper
import com.docvault.app.data.Document
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

object PrintUtils {

    fun printDocument(context: Context, document: Document) {
        if (document.fileType == "image") {
            printImage(context, document)
        } else {
            printPdf(context, document)
        }
    }

    private fun printImage(context: Context, document: Document) {
        val printHelper = PrintHelper(context)
        printHelper.scaleMode = PrintHelper.SCALE_MODE_FIT
        val bitmap = BitmapFactory.decodeFile(document.filePath)
        if (bitmap != null) {
            printHelper.printBitmap("${document.customName} print job", bitmap)
        }
    }

    private fun printPdf(context: Context, document: Document) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = document.customName

        val adapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes?,
                cancellationSignal: CancellationSignal?,
                callback: LayoutResultCallback?,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback?.onLayoutCancelled()
                    return
                }
                val info = PrintDocumentInfo.Builder(document.customName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build()
                callback?.onLayoutFinished(info, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor?,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback?
            ) {
                try {
                    FileInputStream(File(document.filePath)).use { input ->
                        FileOutputStream(destination?.fileDescriptor).use { output ->
                            input.copyTo(output)
                        }
                    }
                    callback?.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: IOException) {
                    callback?.onWriteFailed(e.message)
                }
            }
        }
        printManager.print(jobName, adapter, null)
    }
}
