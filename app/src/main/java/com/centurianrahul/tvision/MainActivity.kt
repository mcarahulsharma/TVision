package com.centurianrahul.tvision

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(15, 17, 22)
    private val surface = Color.rgb(30, 33, 40)
    private val surfaceRaised = Color.rgb(43, 47, 56)
    private val white = Color.rgb(246, 247, 249)
    private val muted = Color.rgb(177, 183, 194)
    private val accent = Color.rgb(138, 180, 248)
    private lateinit var contentScroll: ScrollView
    private lateinit var appsAnchor: View
    private lateinit var clock: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val tick = object : Runnable {
        override fun run() {
            if (::clock.isInitialized) clock.text = SimpleDateFormat("h:mm", Locale.getDefault()).format(Date())
            handler.postDelayed(this, 30_000)
        }
    }

    private data class TvApp(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        buildHome()
        handler.post(tick)
    }

    override fun onDestroy() {
        handler.removeCallbacks(tick)
        super.onDestroy()
    }

    private fun buildHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(42), dp(20), dp(42), dp(18))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(20, 24, 33), bg, Color.rgb(12, 14, 18)))
            isFocusableInTouchMode = true
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        top.addView(TextView(this).apply {
            text = "TVision"
            textSize = 24f
            setTextColor(white)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, dp(48), 1f))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        listOf("For you", "Apps", "Home control").forEachIndexed { index, title ->
            val tab = TextView(this).apply {
                text = title
                textSize = 15f
                setTextColor(if (index == 0) white else muted)
                typeface = if (index == 0) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
                gravity = Gravity.CENTER
                setPadding(dp(15), dp(10), dp(15), dp(10))
                background = if (index == 0) rounded(Color.rgb(53, 65, 84), 22) else focusBackground()
                isFocusable = true
                isClickable = true
                setOnClickListener {
                    when (index) {
                        1 -> scrollToApps()
                        2 -> openPackage("io.homeassistant.companion.android")
                    }
                }
            }
            nav.addView(tab, LinearLayout.LayoutParams(-2, dp(42)).apply { marginEnd = dp(5) })
        }
        top.addView(nav)
        clock = TextView(this).apply {
            textSize = 19f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(dp(18), 0, dp(12), 0)
        }
        top.addView(clock)
        top.addView(TextView(this).apply {
            text = "●"
            textSize = 19f
            setTextColor(accent)
            gravity = Gravity.CENTER
            setPadding(dp(10), 0, 0, 0)
            contentDescription = "Profile"
        })
        root.addView(top)

        contentScroll = ScrollView(this).apply {
            isFillViewport = false
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
            setPadding(0, dp(12), 0, dp(4))
        }
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val hero = FrameLayout(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.BL_TR,
                intArrayOf(Color.rgb(49, 70, 104), Color.rgb(35, 47, 68), Color.rgb(27, 31, 42))).apply {
                cornerRadius = dp(24).toFloat()
            }
            setPadding(dp(30), dp(22), dp(30), dp(22))
        }
        val heroContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        heroContent.addView(TextView(this).apply {
            text = "YOUR ENTERTAINMENT, AT HOME"
            textSize = 11f
            letterSpacing = 0.18f
            setTextColor(Color.rgb(190, 211, 244))
            typeface = Typeface.DEFAULT_BOLD
        })
        heroContent.addView(TextView(this).apply {
            text = "A better way to watch."
            textSize = 31f
            setTextColor(white)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setPadding(0, dp(8), 0, dp(5))
        })
        heroContent.addView(TextView(this).apply {
            text = "Your favourite streaming, media and smart-home apps — all from one big-screen home."
            textSize = 14f
            setTextColor(Color.rgb(222, 228, 238))
            maxWidth = dp(520)
        })
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(17), 0, 0)
        }
        actions.addView(actionButton("Explore your apps", true) { scrollToApps() },
            LinearLayout.LayoutParams(-2, dp(44)).apply { marginEnd = dp(12) })
        actions.addView(actionButton("Open Home Assistant", false) {
            openPackage("io.homeassistant.companion.android")
        }, LinearLayout.LayoutParams(-2, dp(44)))
        heroContent.addView(actions)
        hero.addView(heroContent, FrameLayout.LayoutParams(-1, -1))
        page.addView(hero, LinearLayout.LayoutParams(-1, dp(238)).apply { bottomMargin = dp(22) })

        val apps = loadApps()
        val entertainment = apps.filter { app ->
            val p = app.packageName.lowercase(Locale.ROOT)
            listOf("youtube", "netflix", "primevideo", "hotstar", "vlc", "sonyliv", "zee5", "plex", "kodi").any { p.contains(it) }
        }
        val featuredApps = if (entertainment.isNotEmpty()) entertainment else apps.take(8)
        if (featuredApps.isNotEmpty()) {
            page.addView(sectionHeading("Pick up where you left off", "Your entertainment apps"))
            page.addView(appRow(featuredApps), LinearLayout.LayoutParams(-1, dp(158)).apply { bottomMargin = dp(24) })
        }

        appsAnchor = sectionHeading("Your apps", "All your installed apps in one place")
        page.addView(appsAnchor)
        page.addView(appRow(apps), LinearLayout.LayoutParams(-1, dp(158)).apply { bottomMargin = dp(18) })
        page.addView(TextView(this).apply {
            text = "TVision  •  Made for the big screen"
            textSize = 11f
            setTextColor(Color.rgb(120, 126, 138))
            setPadding(0, dp(8), 0, dp(8))
        })
        contentScroll.addView(page)
        root.addView(contentScroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        root.requestFocus()
    }

    private fun sectionHeading(title: String, subtitle: String): View {
        val block = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, dp(10))
        }
        block.addView(TextView(this).apply {
            text = title
            textSize = 21f
            setTextColor(white)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        })
        block.addView(TextView(this).apply {
            text = subtitle
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(3), 0, 0)
        })
        return block
    }

    private fun appRow(apps: List<TvApp>): HorizontalScrollView {
        val row = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
        }
        val strip = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        apps.forEach { app ->
            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(12), dp(12), dp(12), dp(10))
                background = focusBackground()
                isFocusable = true
                isClickable = true
                contentDescription = "Open ${app.label}"
                setOnClickListener { openPackage(app.packageName) }
                setOnKeyListener { _, keyCode, event ->
                    if (event.action == KeyEvent.ACTION_UP &&
                        (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
                        openPackage(app.packageName); true
                    } else false
                }
            }
            val icon = ImageView(this).apply {
                setImageDrawable(app.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            card.addView(icon, LinearLayout.LayoutParams(dp(66), dp(66)))
            card.addView(TextView(this).apply {
                text = app.label
                textSize = 12f
                setTextColor(white)
                gravity = Gravity.CENTER
                maxLines = 2
                ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(0, dp(9), 0, 0)
            }, LinearLayout.LayoutParams(-1, dp(36)))
            strip.addView(card, LinearLayout.LayoutParams(dp(148), dp(136)).apply { marginEnd = dp(12) })
        }
        row.addView(strip)
        return row
    }

    private fun actionButton(label: String, primary: Boolean, action: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(if (primary) Color.rgb(20, 29, 43) else white)
            gravity = Gravity.CENTER
            setPadding(dp(19), 0, dp(19), 0)
            background = if (primary) rounded(Color.rgb(221, 232, 250), 24) else focusBackground()
            isFocusable = true
            isClickable = true
            setOnClickListener { action() }
        }

    private fun loadApps(): List<TvApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val matches = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return matches.mapNotNull { info ->
            val pkg = info.activityInfo.packageName
            if (pkg == packageName) null else try {
                TvApp(info.loadLabel(packageManager).toString(), pkg, info.loadIcon(packageManager))
            } catch (_: Exception) { null }
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun focusBackground() = android.graphics.drawable.StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), rounded(Color.rgb(62, 78, 103), 18, accent))
        addState(intArrayOf(android.R.attr.state_pressed), rounded(surfaceRaised, 18))
        addState(intArrayOf(), rounded(surface, 18))
    }

    private fun rounded(color: Int, radius: Int, stroke: Int? = null) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) setStroke(dp(2), stroke)
        }

    private fun openPackage(pkg: String) {
        val launch = packageManager.getLaunchIntentForPackage(pkg) ?: return
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { startActivity(launch) } catch (_: Exception) { }
    }

    private fun scrollToApps() {
        if (::contentScroll.isInitialized && ::appsAnchor.isInitialized) {
            contentScroll.post { contentScroll.smoothScrollTo(0, appsAnchor.top) }
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
