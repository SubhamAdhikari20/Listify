package com.example.listify.utils

import android.app.Activity
import android.app.AlertDialog
import com.example.listify.R

class LoadingUtils(val activity: Activity) {
    private lateinit var alertDialog: AlertDialog

    fun show(){
        val dialogView = activity.layoutInflater.inflate(R.layout.loading_dialog, null)

        val builder = AlertDialog.Builder(activity)
        builder.setView(dialogView)
        builder.setCancelable(false)
        builder.setTitle("Please! wait")

        alertDialog = builder.create()
        alertDialog.show()
    }

    fun dismiss(){
        alertDialog.dismiss()
    }
}