package uz.ardo.tvhub

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.net.Uri
import android.provider.Settings
import android.widget.Toast

object AppLauncher {

    const val HELP_URL = "https://ardosher.uz/ArdoTvHub"

    /** O'rnatilgan birinchi paketni qaytaradi (yo'q bo'lsa null). */
    fun installedPackage(context: Context, entry: AppEntry): String? {
        val pm = context.packageManager
        for (p in entry.packages) {
            val intent = pm.getLeanbackLaunchIntentForPackage(p) ?: pm.getLaunchIntentForPackage(p)
            if (intent != null) return p
        }
        return null
    }

    fun launch(context: Context, entry: AppEntry) {
        val action = entry.action
        if (action != null) {
            when {
                action == "profile" -> start(context, Intent(context, ProfileActivity::class.java))
                action == "help" -> {
                    val u = HELP_URL
                    if (!start(context, Intent(Intent.ACTION_VIEW, Uri.parse(u)))) {
                        start(context, Intent(context, WebActivity::class.java).putExtra("url", u))
                    }
                }
                action == "settings" -> start(context, Intent(Settings.ACTION_SETTINGS))
                action == "wifi" -> {
                    if (!start(context, Intent(Settings.ACTION_WIFI_SETTINGS))) {
                        start(context, Intent(Settings.ACTION_SETTINGS))
                    }
                }
                action.startsWith("hdmi:") -> {
                    val id = action.removePrefix("hdmi:")
                    val uri = TvContract.buildChannelUriForPassthroughInput(id)
                    if (!start(context, Intent(Intent.ACTION_VIEW, uri))) {
                        Toast.makeText(context, "${entry.name} ochilmadi", Toast.LENGTH_LONG).show()
                    }
                }
            }
            return
        }

        val pm = context.packageManager
        for (p in entry.packages) {
            val intent = pm.getLeanbackLaunchIntentForPackage(p) ?: pm.getLaunchIntentForPackage(p)
            if (intent != null) {
                start(context, intent)
                return
            }
        }

        // O'rnatilmagan: paket nomi bo'lsa Play Market, aks holda havola, aks holda nom bo'yicha qidiruv.
        if (entry.packages.isEmpty() && !entry.url.isNullOrBlank()) {
            if (!start(context, Intent(Intent.ACTION_VIEW, Uri.parse(entry.url)))) {
                start(context, Intent(context, WebActivity::class.java).putExtra("url", entry.url))
            }
            return
        }

        Toast.makeText(context, "${entry.name} o'rnatilmagan. Play Market ochilmoqda", Toast.LENGTH_LONG).show()
        val marketUri = if (entry.packages.isNotEmpty())
            Uri.parse("market://details?id=${entry.packages.first()}")
        else
            Uri.parse("market://search?q=${Uri.encode(entry.name)}")

        if (!start(context, Intent(Intent.ACTION_VIEW, marketUri))) {
            val web = if (entry.packages.isNotEmpty())
                "https://play.google.com/store/apps/details?id=${entry.packages.first()}"
            else
                "https://play.google.com/store/search?q=${Uri.encode(entry.name)}&c=apps"
            start(context, Intent(Intent.ACTION_VIEW, Uri.parse(web)))
        }
    }

    private fun start(context: Context, intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }
}
