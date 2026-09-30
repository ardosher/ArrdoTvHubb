package uz.ardo.tvhub

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(
    private var items: List<AppEntry>,
    private val onClick: (AppEntry) -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    class VH(
        val card: FrameLayout,
        val icon: ImageView,
        val letter: TextView,
        val label: TextView,
        val badge: TextView
    ) : RecyclerView.ViewHolder(card)

    private fun dp(v: View, value: Int): Int = (value * v.resources.displayMetrics.density).toInt()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val d = ctx.resources.displayMetrics.density
        fun px(v: Int) = (v * d).toInt()
        val tv = Device.isTv(ctx)

        val card = FrameLayout(ctx).apply {
            layoutParams = RecyclerView.LayoutParams(if (tv) px(220) else px(158), if (tv) px(140) else px(104)).apply {
                setMargins(px(10), px(14), px(10), px(14))
            }
            isFocusable = true
            isClickable = true
            isFocusableInTouchMode = false
        }

        val icon = ImageView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(px(56), px(56)).apply {
                gravity = Gravity.TOP or Gravity.START
                setMargins(px(18), px(16), 0, 0)
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val letter = TextView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(px(56), px(56)).apply {
                gravity = Gravity.TOP or Gravity.START
                setMargins(px(18), px(16), 0, 0)
            }
            gravity = Gravity.CENTER
            textSize = 30f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(Color.argb(60, 255, 255, 255))
            }
        }

        val label = TextView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM
                setMargins(px(18), 0, px(14), px(16))
            }
            textSize = if (tv) 18f else 15f
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            maxLines = 1
        }

        val badge = TextView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.END
                setMargins(0, px(14), px(14), 0)
            }
            textSize = 11f
            setTextColor(Color.WHITE)
            setPadding(px(8), px(3), px(8), px(3))
            text = "O'rnatish"
            background = GradientDrawable().apply {
                cornerRadius = px(10).toFloat()
                setColor(Color.argb(110, 0, 0, 0))
            }
        }

        card.addView(icon)
        card.addView(letter)
        card.addView(label)
        card.addView(badge)
        return VH(card, icon, letter, label, badge)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val entry = items[position]
        val ctx = holder.card.context
        val density = ctx.resources.displayMetrics.density
        val base = try { Color.parseColor(entry.color) } catch (e: IllegalArgumentException) { Color.parseColor("#3B5BDB") }
        val dark = ColorUtils.blendARGB(base, Color.BLACK, 0.45f)

        fun makeBg(focused: Boolean) = GradientDrawable(
            GradientDrawable.Orientation.TL_BR, intArrayOf(base, dark)
        ).apply {
            cornerRadius = 18 * density
            if (focused) setStroke((4 * density).toInt(), ctx.getColor(R.color.focus_ring))
        }

        holder.card.background = makeBg(false)
        holder.label.text = entry.name

        val pkg = AppLauncher.installedPackage(ctx, entry)
        val installed = pkg != null || entry.action != null
        holder.badge.visibility = if (installed) View.GONE else View.VISIBLE

        var iconSet = false
        if (pkg != null) {
            try {
                holder.icon.setImageDrawable(ctx.packageManager.getApplicationIcon(pkg))
                iconSet = true
            } catch (e: Exception) {
                iconSet = false
            }
        }
        holder.icon.visibility = if (iconSet) View.VISIBLE else View.GONE
        holder.letter.visibility = if (iconSet) View.GONE else View.VISIBLE
        holder.letter.text = entry.name.take(1).uppercase()

        holder.card.setOnFocusChangeListener { v, hasFocus ->
            v.background = makeBg(hasFocus)
            val s = if (hasFocus) 1.12f else 1f
            v.animate().scaleX(s).scaleY(s).setDuration(140).start()
            v.elevation = if (hasFocus) 24 * density else 0f
        }
        holder.card.setOnClickListener { onClick(entry) }
    }

    fun setItems(newItems: List<AppEntry>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size
}
