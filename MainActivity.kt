package uz.ardo.tvhub

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.tv.TvInputInfo
import android.media.tv.TvInputManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextClock
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : Activity() {

    private val staticAdapters = ArrayList<AppAdapter>()
    private var staticEntries: List<AppEntry> = emptyList()

    private lateinit var recentsBox: LinearLayout
    private lateinit var recentsAdapter: AppAdapter
    private lateinit var hdmiBox: LinearLayout
    private lateinit var hdmiAdapter: AppAdapter
    private lateinit var allBox: LinearLayout
    private lateinit var allAdapter: AppAdapter
    private lateinit var scroll: ScrollView

    private var isHome = false
    private val tv by lazy { Device.isTv(this) }
    private fun sp(t: Float, p: Float) = if (tv) t else p

    private val prefs by lazy { getSharedPreferences("hub", Context.MODE_PRIVATE) }

    private val onOpen: (AppEntry) -> Unit = {
        pushRecent(it.key)
        AppLauncher.launch(this, it)
    }

    private fun px(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isHome = intent?.hasCategory(Intent.CATEGORY_HOME) == true

        if (tv) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }

        val categories = try {
            AppRepository.load(this)
        } catch (e: Exception) {
            emptyList()
        }
        staticEntries = categories.flatMap { it.apps }

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            if (tv) setPadding(px(48), px(28), px(48), px(40)) else setPadding(px(12), px(16), px(12), px(28))
        }

        content.addView(buildHeader())

        // Oxirgi ochilganlar
        val recents = addSection(content, "Oxirgi ochilganlar", emptyList())
        recentsBox = recents.first
        recentsAdapter = recents.second
        recentsBox.visibility = View.GONE

        // HDMI va boshqa kirishlar
        val hdmi = addSection(content, "Kirishlar (HDMI)", emptyList())
        hdmiBox = hdmi.first
        hdmiAdapter = hdmi.second
        hdmiBox.visibility = View.GONE

        if (categories.isEmpty()) {
            content.addView(TextView(this).apply {
                text = "Ilovalar ro'yxati (apps.json) o'qilmadi."
                textSize = 20f
                setTextColor(Color.WHITE)
                setPadding(0, px(40), 0, 0)
            })
        }

        for (cat in categories) {
            val section = addSection(content, cat.title, cat.apps)
            staticAdapters.add(section.second)
        }

        // Telefondagi barcha ilovalar
        val all = addSection(content, "Barcha ilovalar", emptyList())
        allBox = all.first
        allAdapter = all.second
        allBox.visibility = View.GONE

        scroll = ScrollView(this).apply {
            isFillViewport = true
            clipChildren = false
            isVerticalScrollBarEnabled = false
            addView(content)
        }
        setContentView(scroll)
    }

    private fun buildHeader(): View {
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        }
        titleBox.addView(TextView(this).apply {
            text = "ArdoTv Hub"
            textSize = sp(40f, 28f)
            setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL))
            setTextColor(getColor(R.color.ink))
        })
        titleBox.addView(TextView(this).apply {
            text = "Televizoringiz uchun ilovalar"
            textSize = sp(16f, 13f)
            setTextColor(getColor(R.color.ink_dim))
        })

        val timeBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }
        timeBox.addView(TextClock(this).apply {
            format24Hour = "HH:mm"
            format12Hour = "HH:mm"
            textSize = sp(34f, 24f)
            setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL))
            setTextColor(getColor(R.color.ink))
            gravity = Gravity.END
        })
        timeBox.addView(TextClock(this).apply {
            format24Hour = "d MMMM, EEEE"
            format12Hour = "d MMMM, EEEE"
            textSize = sp(15f, 12f)
            setTextColor(getColor(R.color.ink_dim))
            gravity = Gravity.END
        })

        header.addView(titleBox)
        header.addView(timeBox)
        return header
    }

    private fun addSection(
        parent: LinearLayout,
        title: String,
        entries: List<AppEntry>
    ): Pair<LinearLayout, AppAdapter> {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            clipChildren = false
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        box.addView(TextView(this).apply {
            text = title
            textSize = sp(22f, 18f)
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(getColor(R.color.ink))
            setPadding(px(10), px(if (tv) 26 else 18), 0, px(2))
        })

        val adapter = AppAdapter(entries, onOpen)
        val row = RecyclerView(this).apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
            this.adapter = adapter
            clipToPadding = false
            clipChildren = false
            setPadding(0, 0, px(if (tv) 40 else 16), 0)
            overScrollMode = View.OVER_SCROLL_NEVER
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        box.addView(row)
        parent.addView(box)
        return Pair(box, adapter)
    }

    // ---- Oxirgi ochilganlar ----

    private fun recentKeys(): List<String> =
        (prefs.getString("recent", "") ?: "").split("\n").filter { it.isNotBlank() }

    private fun pushRecent(key: String) {
        val list = (listOf(key) + recentKeys().filter { it != key }).take(8)
        prefs.edit().putString("recent", list.joinToString("\n")).apply()
    }

    // ---- Dinamik ro'yxatlar ----

    private fun hdmiEntries(): List<AppEntry> {
        return try {
            val tm = getSystemService(Context.TV_INPUT_SERVICE) as TvInputManager
            tm.tvInputList
                .filter { it.isPassthroughInput }
                .map { info: TvInputInfo ->
                    val label = info.loadLabel(this)?.toString() ?: "HDMI"
                    AppEntry(
                        name = label,
                        packages = emptyList(),
                        color = "#0F766E",
                        action = "hdmi:${info.id}",
                        key = "hdmi:${info.id}"
                    )
                }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun allAppEntries(): List<AppEntry> {
        val pm = packageManager
        val seen = HashSet<String>()
        val out = ArrayList<AppEntry>()
        for (cat in listOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val query = Intent(Intent.ACTION_MAIN).addCategory(cat)
            val found = try { pm.queryIntentActivities(query, 0) } catch (e: Exception) { emptyList() }
            for (ri in found) {
                val pkg = ri.activityInfo.packageName
                if (pkg == packageName || !seen.add(pkg)) continue
                val label = ri.loadLabel(pm).toString()
                val hue = ((pkg.hashCode() and 0x7fffffff) % 360).toFloat()
                val rgb = Color.HSVToColor(floatArrayOf(hue, 0.55f, 0.75f)) and 0xFFFFFF
                out.add(
                    AppEntry(
                        name = label,
                        packages = listOf(pkg),
                        color = String.format("#%06X", rgb),
                        key = "p:$pkg"
                    )
                )
            }
        }
        return out.sortedBy { it.name.lowercase() }
    }

    private fun refresh() {
        val all = allAppEntries()
        allAdapter.setItems(all)
        allBox.visibility = if (all.isEmpty()) View.GONE else View.VISIBLE

        val hdmi = hdmiEntries()
        hdmiAdapter.setItems(hdmi)
        hdmiBox.visibility = if (hdmi.isEmpty()) View.GONE else View.VISIBLE

        val lookup = (staticEntries + all + hdmi).associateBy { it.key }
        val rec = recentKeys().mapNotNull { lookup[it] }
        recentsAdapter.setItems(rec)
        recentsBox.visibility = if (rec.isEmpty()) View.GONE else View.VISIBLE

        staticAdapters.forEach { it.notifyDataSetChanged() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        isHome = intent?.hasCategory(Intent.CATEGORY_HOME) == true
        if (isHome) scroll.smoothScrollTo(0, 0)
    }

    override fun onBackPressed() {
        // Launcher sifatida ochilganda "Orqaga" ilovadan chiqarib yubormasin
        if (!isHome) super.onBackPressed()
    }
}
