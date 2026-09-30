package uz.ardo.tvhub

import android.content.Context
import org.json.JSONObject

object AppRepository {

    fun load(context: Context): List<Category> {
        val text = context.assets.open("apps.json").bufferedReader().use { it.readText() }
        val root = JSONObject(text)
        val cats = root.getJSONArray("categories")
        val result = ArrayList<Category>()
        for (i in 0 until cats.length()) {
            val c = cats.getJSONObject(i)
            val appsJson = c.getJSONArray("apps")
            val apps = ArrayList<AppEntry>()
            for (j in 0 until appsJson.length()) {
                val a = appsJson.getJSONObject(j)
                val pkgArr = a.optJSONArray("packages")
                val pkgs = ArrayList<String>()
                if (pkgArr != null) {
                    for (k in 0 until pkgArr.length()) pkgs.add(pkgArr.getString(k))
                }
                apps.add(
                    AppEntry(
                        name = a.getString("name"),
                        packages = pkgs,
                        color = a.optString("color", "#3B5BDB"),
                        action = if (a.has("action")) a.getString("action") else null,
                        url = if (a.has("url")) a.getString("url") else null
                    )
                )
            }
            result.add(Category(c.getString("title"), apps))
        }
        return result
    }
}
