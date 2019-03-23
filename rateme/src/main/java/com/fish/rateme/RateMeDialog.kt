package com.fish.rateme

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.system_stars_dialog.view.*
import java.lang.ref.WeakReference

class RateMeDialog(context: Activity, intentReturnCode: Int = 989, var countInvokeToDisplay: Int = 4) {
    companion object {
        private const val KEY_DISPLAYED = "KEY_DISPLAYED"
        private const val KEY_COUNT_INVOKE = "KEY_COUNT_INVOKE"
    }

    val GOOGLE_PLAY_INTENT_CODE = intentReturnCode
    private val context = WeakReference(context)
    private lateinit var rateDialog: AlertDialog

    enum class DISPLAY_POLITIC() {
        FIRST_SHOW,
        PERIODIC
    }
    fun checkNeedDisplay(preferences: SharedPreferences, displayPolitic: DISPLAY_POLITIC)=when (displayPolitic) {
        DISPLAY_POLITIC.PERIODIC -> {
            if (countInvokeToDisplay < 1) {
                true
            } else {
                val prevCountInvoke = preferences.getInt(KEY_COUNT_INVOKE, 0)
                prevCountInvoke > 0 && (prevCountInvoke + 1).rem(countInvokeToDisplay) == 0
            }
        }
        DISPLAY_POLITIC.FIRST_SHOW -> {
            val displayed = preferences.getBoolean(KEY_DISPLAYED, false)
            displayed.not()
        }
        else -> false
    }
    fun invokeIfNeed(preferences: SharedPreferences, displayPolitic: DISPLAY_POLITIC,closeCallBack:(()->Unit)?=null) {
        if (needDisplay(preferences, displayPolitic)) {
            if (::rateDialog.isInitialized.not()) {
                initRateDialog()
            }
            if(closeCallBack!=null) {
                rateDialog.setOnDismissListener {
                    closeCallBack.invoke()
                }
            }
            rateDialog.show()

        }
    }

    private fun initRateDialog() {
        context.get()?.let {
            val viewDialog = LayoutInflater.from(it).inflate(R.layout.system_stars_dialog, null)
            with(viewDialog) {
                star4ImageView.setOnClickListener { goToGooglePlay() }
                star5ImageView.setOnClickListener { goToGooglePlay() }
                closeButton.setOnClickListener { closeRateDialog() }
            }
            rateDialog = AlertDialog.Builder(it, R.style.Theme_AppCompat_Light_Dialog_Alert)
                .setView(viewDialog)
                .setCancelable(false)
                .create()
            rateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }


    private fun needDisplay(preferences: SharedPreferences, displayPolitic: DISPLAY_POLITIC): Boolean =
        when (displayPolitic) {
            DISPLAY_POLITIC.PERIODIC -> {
                if (countInvokeToDisplay < 1) {
                    true
                } else {
                    val prevCountInvoke = preferences.getInt(KEY_COUNT_INVOKE, 0)
                    if (prevCountInvoke > 0 && (prevCountInvoke + 1).rem(countInvokeToDisplay) == 0) {
                        preferences.edit().putInt(KEY_COUNT_INVOKE, 0).apply()
                        true
                    } else {
                        preferences.edit().putInt(KEY_COUNT_INVOKE, prevCountInvoke + 1).apply()
                        false
                    }
                }
            }
            DISPLAY_POLITIC.FIRST_SHOW -> {
                val displayed = preferences.getBoolean(KEY_DISPLAYED, false)
                if (displayed.not()) {
                    preferences.edit().putBoolean(KEY_DISPLAYED, true).apply()
                    true
                } else
                    false
            }
            else -> false
        }


    private fun closeRateDialog() {
        rateDialog.dismiss()
    }

    private fun goToGooglePlay() {

        context.get()?.let {
            val appPackageName = it.packageName
            try {
                it.startActivityForResult(
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")),
                    GOOGLE_PLAY_INTENT_CODE
                )
            } catch (activityNotFoundExp: android.content.ActivityNotFoundException) {
                it.startActivityForResult(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                    ), GOOGLE_PLAY_INTENT_CODE
                )
            }
        }

        rateDialog.dismiss()

    }
}