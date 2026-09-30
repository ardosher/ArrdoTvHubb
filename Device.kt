package uz.ardo.tvhub

import android.app.UiModeManager
import android.content.Context
import android.content.res.Configuration

object Device {
    /** Televizormi (true) yoki telefon/planshetmi (false). */
    fun isTv(context: Context): Boolean {
        val um = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
        return um?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
    }
}
