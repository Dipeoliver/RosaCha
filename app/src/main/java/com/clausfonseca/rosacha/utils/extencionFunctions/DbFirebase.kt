package com.clausfonseca.rosacha.utils.extencionFunctions

import android.content.Context
import com.clausfonseca.rosacha.R

fun getDbClient (
    context: Context
) =  context.getString(R.string.db_client)

fun getDbProduct (
    context: Context
) =  context.getString(R.string.db_product)

fun getDbSales (
    context: Context
) =  context.getString(R.string.db_sales)

fun getDbMonthSales (
    context: Context
) =  context.getString(R.string.db_month_sales)

