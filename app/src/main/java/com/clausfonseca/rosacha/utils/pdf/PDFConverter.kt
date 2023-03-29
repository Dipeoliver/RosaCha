package com.clausfonseca.rosacha.utils.pdf

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.clausfonseca.rosacha.R
import java.io.File
import java.io.FileOutputStream

class PDFConverter {

    @SuppressLint("SetTextI18n")
    private fun createBitmapFromView(
        context: Context,
        view: View,
        pdfDetails: PdfDetails,
        adapter: MarksRecyclerAdapter,
        activity: Activity
    ): Bitmap {
        val invoice = view.findViewById<TextView>(R.id.txt_invoice_number)
        val costumerName = view.findViewById<TextView>(R.id.txt_costumer_name)
        val date = view.findViewById<TextView>(R.id.txt_date_pdf)
        val subTotal = view.findViewById<TextView>(R.id.txt_sub_total)
        val discount = view.findViewById<TextView>(R.id.txt_discount_pdf)
        val total = view.findViewById<TextView>(R.id.txt_total_value)
        val moneyPaid = view.findViewById<TextView>(R.id.txt_paid)
        val recyclerView = view.findViewById<RecyclerView>(R.id.pdf_marks)

        // texto rodapé
        val qtyParcelFinal = view.findViewById<TextView>(R.id.txt_qty_parcel)
        val parcelText = view.findViewById<TextView>(R.id.txt_parcel_text)
        val parcelText2 = view.findViewById<TextView>(R.id.txt_parcel_text2)
        val parcelDay = view.findViewById<TextView>(R.id.txt_parcel_day)
        val parcelValue = view.findViewById<TextView>(R.id.txt_parcel_value)

        invoice.text = pdfDetails.invoiceNumber
        costumerName.text = pdfDetails.costumerName
        date.text = pdfDetails.date
        subTotal.text = String.format("%.2f", pdfDetails.subTotal)
        discount.text = String.format("%.2f", pdfDetails.discount)
        total.text = String.format("%.2f", pdfDetails.total)
        moneyPaid.text = String.format("%.2f", pdfDetails.moneyPaid)
        recyclerView.adapter = adapter
        parcelValue.text = String.format("%.2f", pdfDetails.parcelValue)

        // texto rodapé
        qtyParcelFinal.text = pdfDetails.qtyParcel.toString() + "X"
        parcelDay.text = pdfDetails.date.substring(0, 2)

        val checkParcel: Int = pdfDetails.qtyParcel
        if (checkParcel == 1) {
            parcelText.text = context.getString(R.string.cash_sales_pdf)
            parcelText2.visibility = View.GONE
            qtyParcelFinal.visibility = View.GONE
            parcelDay.visibility = View.GONE
            parcelValue.visibility = View.GONE
        } else {

        }
        return createBitmap(context, view, activity)
    }

    private fun createBitmap(
        context: Context,
        view: View,
        activity: Activity,
    ): Bitmap {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            context.display?.getRealMetrics(displayMetrics)
            displayMetrics.densityDpi
        } else {
            activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        }
        view.measure(
            View.MeasureSpec.makeMeasureSpec(
                displayMetrics.widthPixels, View.MeasureSpec.EXACTLY
            ),
            View.MeasureSpec.makeMeasureSpec(
                displayMetrics.heightPixels, View.MeasureSpec.EXACTLY
            )
        )
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
        val bitmap = Bitmap.createBitmap(
            view.measuredWidth,
            view.measuredHeight, Bitmap.Config.ARGB_8888
        )

        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return Bitmap.createScaledBitmap(bitmap, 600, 900, true)
    }

    private fun convertBitmapToPdf(bitmap: Bitmap, context: Context) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0F, 0F, null)
        pdfDocument.finishPage(page)
        val filePath = File(context.getExternalFilesDir(null), "RosaCha.pdf")
        pdfDocument.writeTo(FileOutputStream(filePath))
        pdfDocument.close()
        renderPdf(context, filePath)
    }

    fun createPdf(
        context: Context,
        pdfDetails: PdfDetails,
        activity: Activity
    ) {
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.layout_pdf_page, null)

        val adapter = MarksRecyclerAdapter(pdfDetails.itemDetailsList)
        val bitmap = createBitmapFromView(context, view, pdfDetails, adapter, activity)
        convertBitmapToPdf(bitmap, activity)
    }

    private fun renderPdf(context: Context, filePath: File) {
        val uri = FileProvider.getUriForFile(
            context,
            context.applicationContext.packageName + ".provider",
            filePath
        )
        // compartilhar geral mas nao aparece Whats
        val intentPDF = Intent(Intent.ACTION_VIEW)
        intentPDF.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        intentPDF.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intentPDF.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intentPDF.setDataAndType(uri, "application/pdf")

        try {
            context.startActivity(intentPDF)
        } catch (e: ActivityNotFoundException) {

        }
    }
}