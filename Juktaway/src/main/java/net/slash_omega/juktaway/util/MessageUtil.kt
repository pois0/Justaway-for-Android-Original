package net.slash_omega.juktaway.util

import android.app.ProgressDialog
import android.content.Context
import android.widget.Toast
import net.slash_omega.juktaway.app
import org.jetbrains.anko.toast

fun showToast(text: String) = app.toast(text)

fun showToast(id: Int) = app.toast(id)

object MessageUtil {
    private var sProgressDialog: ProgressDialog? = null

    fun showToast(text: String) {
        Toast.makeText(app, text, Toast.LENGTH_SHORT).show()
    }

    fun showToast(id: Int) {
        app.toast(id)
    }

    fun showToast(id: Int, description: String) {
        val text = app.getString(id) + "\n" + description
        Toast.makeText(app, text, Toast.LENGTH_SHORT).show()
    }

    fun showProgressDialog(context: Context, message: String) {
        sProgressDialog = ProgressDialog(context)
        sProgressDialog!!.setMessage(message)
        sProgressDialog!!.show()
    }

    fun dismissProgressDialog() {
        if (sProgressDialog != null)
            try {
                sProgressDialog!!.dismiss()
            } finally {
                sProgressDialog = null
            }
    }
}
