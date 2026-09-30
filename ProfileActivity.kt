package uz.ardo.tvhub

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

/**
 * Profil: telefonda — kirish + ro'yxatdan o'tish, televizorda — faqat kirish.
 */
class ProfileActivity : Activity() {

    private val tv by lazy { Device.isTv(this) }
    private lateinit var box: LinearLayout
    private var registerMode = false
    private var busy = false

    private fun px(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val width = minOf(px(if (tv) 520 else 440), resources.displayMetrics.widthPixels - px(40))
        val frame = FrameLayout(this).apply {
            setPadding(px(20), px(28), px(20), px(28))
            addView(box, FrameLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL))
        }
        setContentView(ScrollView(this).apply {
            isFillViewport = true
            addView(frame)
        })
        render()
    }

    private fun render() {
        box.removeAllViews()
        box.addView(label("Profil", if (tv) 34f else 28f, Color.WHITE, bold = true))

        val email = Session.email(this)
        if (email != null) renderLoggedIn(email) else renderForm()
    }

    private fun renderLoggedIn(email: String) {
        box.addView(label("Siz hisobga kirgansiz:", 16f, getColor(R.color.ink_dim), top = 18))
        box.addView(label(email, 22f, Color.WHITE, bold = true, top = 4))
        val out = button("Chiqish", primary = false)
        out.setOnClickListener {
            Session.clear(this)
            render()
        }
        box.addView(out, lp(top = 28))
        out.requestFocus()
    }

    private fun renderForm() {
        val canRegister = !tv
        val register = canRegister && registerMode

        box.addView(
            label(
                if (register) "Yangi hisob yaratish" else "Hisobingizga kiring",
                18f, getColor(R.color.ink_dim), top = 6
            )
        )

        val email = input("Email", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        val pass = input("Parol", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        val pass2 = if (register)
            input("Parolni takrorlang", InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
        else null

        box.addView(email, lp(top = 20))
        box.addView(pass, lp(top = 12))
        if (pass2 != null) box.addView(pass2, lp(top = 12))

        val status = label("", 15f, Color.parseColor("#FF8A80"), top = 12)
        box.addView(status)

        val submit = button(if (register) "Ro'yxatdan o'tish" else "Kirish", primary = true)
        box.addView(submit, lp(top = 16))

        submit.setOnClickListener {
            if (busy) return@setOnClickListener
            val e = email.text.toString().trim()
            val p = pass.text.toString()
            when {
                e.isEmpty() || !e.contains("@") -> { status.text = "Email to'g'ri yozilsin."; return@setOnClickListener }
                p.length < 6 -> { status.text = "Parol kamida 6 ta belgi bo'lsin."; return@setOnClickListener }
                register && p != pass2?.text.toString() -> { status.text = "Parollar bir xil emas."; return@setOnClickListener }
            }
            busy = true
            submit.isEnabled = false
            status.setTextColor(getColor(R.color.ink_dim))
            status.text = "Iltimos, kuting..."
            Thread {
                try {
                    val r = if (register) AuthApi.signUp(e, p) else AuthApi.signIn(e, p)
                    Session.save(this, r)
                    runOnUiThread { busy = false; render() }
                } catch (ex: AuthException) {
                    runOnUiThread { fail(status, submit, ex.message ?: "Xatolik") }
                } catch (ex: Exception) {
                    runOnUiThread { fail(status, submit, "Internetga ulanib bo'lmadi.") }
                }
            }.start()
        }

        if (canRegister) {
            val toggle = button(
                if (register) "Hisobim bor — Kirish" else "Hisobingiz yo'qmi? Ro'yxatdan o'tish",
                primary = false
            )
            toggle.setOnClickListener { registerMode = !registerMode; render() }
            box.addView(toggle, lp(top = 10))
        } else {
            box.addView(
                label(
                    "Ro'yxatdan o'tish faqat telefondagi ArdoTv Hub ilovasida. " +
                        "Telefonda hisob oching, so'ng shu email va parol bilan bu yerda kiring.",
                    15f, getColor(R.color.ink_dim), top = 22
                )
            )
        }
    }

    private fun fail(status: TextView, submit: Button, msg: String) {
        busy = false
        submit.isEnabled = true
        status.setTextColor(Color.parseColor("#FF8A80"))
        status.text = msg
    }

    // ---- UI yordamchilari ----

    private fun lp(top: Int = 0) = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    ).apply { topMargin = px(top) }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false, top: Int = 0) =
        TextView(this).apply {
            this.text = text
            setTextSize(TypedValue.COMPLEX_UNIT_SP, size)
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
            layoutParams = lp(top)
        }

    private fun rounded(fill: Int, stroke: Int? = null) = GradientDrawable().apply {
        cornerRadius = px(14).toFloat()
        setColor(fill)
        if (stroke != null) setStroke(px(3), stroke)
    }

    private fun input(hint: String, type: Int) = EditText(this).apply {
        this.hint = hint
        inputType = type
        setSingleLine(true)
        setTextColor(Color.WHITE)
        setHintTextColor(getColor(R.color.ink_dim))
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        setPadding(px(16), px(14), px(16), px(14))
        background = rounded(Color.argb(40, 255, 255, 255))
        onFocusChangeListener = View.OnFocusChangeListener { v, focus ->
            v.background = if (focus) rounded(Color.argb(60, 255, 255, 255), getColor(R.color.focus_ring))
            else rounded(Color.argb(40, 255, 255, 255))
        }
    }

    private fun button(text: String, primary: Boolean) = Button(this).apply {
        this.text = text
        isAllCaps = false
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        isFocusable = true
        stateListAnimator = null
        val normal = if (primary) getColor(R.color.focus_ring) else Color.argb(50, 255, 255, 255)
        setTextColor(if (primary) Color.parseColor("#111427") else Color.WHITE)
        background = rounded(normal)
        setPadding(px(16), px(14), px(16), px(14))
        onFocusChangeListener = View.OnFocusChangeListener { v, focus ->
            v.background = if (focus) rounded(normal, Color.WHITE) else rounded(normal)
        }
    }
}
