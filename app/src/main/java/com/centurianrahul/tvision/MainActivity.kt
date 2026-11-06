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
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val bg = Color.rgb(9, 11, 16)
    private val panel = Color.rgb(24, 28, 37)
    private val blue = Color.rgb(138, 180, 248)
    private lateinit var clock: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val clockTick = object : Runnable {
        override fun run() {
            if (::clock.isInitialized) clock.text = SimpleDateFormat("h:mm a  ·  EEE, d MMM", Locale.getDefault()).format(Date())
            handler.postDelayed(this, 30_000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        buildHome()
        handler.post(clockTick)
    }

    override fun onDestroy() {
        handler.removeCallbacks(clockTick)
        super.onDestroy()
    }

    private fun buildHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(34), dp(22), dp(34), dp(18))
            setBackgroundColor(bg)
            isFocusableInTouchMode = true
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = TextView(this).apply {
            text = "TVision"
            textSize = 27f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        }
        top.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))
        clock = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.LTGRAY)
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
        }
        top.addView(clock)
        root.addView(top)

        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(18), 0, dp(14))
        }
        listOf("For you", "Apps", "Home control").forEachIndexed { index, label ->
            val tab = TextView(this).apply {
                text = label
                textSize = 15f
                setTextColor(if (index == 0) blue else Color.LTGRAY)
                setPadding(dp(2), dp(7), dp(24), dp(7))
                if (index == 0) typeface = Typeface.DEFAULT_BOLD
                isFocusable = true
                isClickable = true
                background = focusBackground()
                setOnClickListener {
                    if (label == "Home control") openPackage("io.homeassistant.companion.android")
                    else if (label == "Apps") scrollToApps()
                }
            }
            tabs.addView(tab)
        }
        root.addView(tabs)

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(24), dp(18), dp(24), dp(18))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(35, 53, 83), Color.rgb(21, 27, 42))).apply { cornerRadius = dp(18).toFloat() }
        }
        hero.addView(TextView(this).apply {
            text = "YOUR TV. YOUR WAY."
            textSize = 11f
            letterSpacing = 0.16f
            setTextColor(blue)
            typeface = Typeface.DEFAULT_BOLD
        })
        hero.addView(TextView(this).apply {
            text = "Welcome to your big screen"
            textSize = 25f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            setPadding(0, dp(6), 0, dp(4))
        })
        hero.addView(TextView(this).apply {
            text = "Your favourite apps, ready from one place. Use touch or your remote to explore."
            textSize = 13f
            setTextColor(Color.rgb(205, 214, 229))
        })
        root.addView(hero, LinearLayout.LayoutParams(-1, dp(132)))

        val heading = TextView(this).apply {
            text = "Your apps"
            textSize = 20f
            setTextColor(Color.WHITE)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, dp(20), 0, dp(10))
        }
        root.addView(heading)

        val scroll = ScrollView(this).apply { isFillViewport = false; overScrollMode = View.OVER_SCROLL_NEVER }
        val grid = GridLayout(this).apply {
            columnCount = 5
            useDefaultMargins = false
        }
        val apps = loadApps()
        if (apps.isEmpty()) {
            grid.addView(TextView(this).apply {
                text = "No launchable apps found."
                setTextColor(Color.WHITE)
                textSize = 16f
            })
        } else {
            apps.forEach { app -> grid.addView(appTile(app)) }
        }
        scroll.addView(grid)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val footer = TextView(this).apply {
            text = "TVision  •  Press Home anytime to return here"
            textSize = 11f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, 0)
        }
        root.addView(footer)
        setContentView(root)
        root.requestFocus()
        if (grid.childCount > 0) grid.getChildAt(0).requestFocus()
    }

    private data class LaunchableApp(val label: String, val packageName: String, val icon: android.graphics.drawable.Drawable)

    @Suppress("DEPRECATION")
    private fun loadApps(): List<LaunchableApp> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val matches = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        return matches.mapNotNull { info ->
            val pkg = info.activityInfo.packageName
            if (pkg == packageName) null else try {
                LaunchableApp(info.loadLabel(packageManager).toString(), pkg, info.loadIcon(packageManager))
            } catch (_: Exception) { null }
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun appTile(app: LaunchableApp): View {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(10), dp(8), dp(8))
            background = focusBackground()
            isFocusable = true
            isClickable = true
            contentDescription = "Open ${app.label}"
            setOnClickListener { openPackage(app.packageName) }
            setOnKeyListener { _, keyCode, event ->
                if (event.action == KeyEvent.ACTION_UP && (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER)) {
                    openPackage(app.packageName); true
                } else false
            }
        }
        val icon = ImageView(this).apply {
            setImageDrawable(app.icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        tile.addView(icon, LinearLayout.LayoutParams(dp(58), dp(58)))
        tile.addView(TextView(this).apply {
            text = app.label
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(8), 0, 0)
        }, LinearLayout.LayoutParams(-1, dp(38)))
        tile.layoutParams = GridLayout.LayoutParams().apply {
            width = dp(132)
            height = dp(122)
            setMargins(dp(4), dp(3), dp(4), dp(3))
        }
        return tile
    }

    private fun focusBackground() = android.graphics.drawable.StateListDrawable().apply {
        addState(intArrayOf(android.R.attr.state_focused), GradientDrawable().apply {
            setColor(Color.rgb(43, 57, 79)); setStroke(dp(2), blue); cornerRadius = dp(14).toFloat()
        })
        addState(intArrayOf(android.R.attr.state_pressed), GradientDrawable().apply {
            setColor(Color.rgb(43, 57, 79)); cornerRadius = dp(14).toFloat()
        })
        addState(intArrayOf(), GradientDrawable().apply {
            setColor(panel); cornerRadius = dp(14).toFloat()
        })
    }

    private fun openPackage(pkg: String) {
        val launch = packageManager.getLaunchIntentForPackage(pkg) ?: return
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try { startActivity(launch) } catch (_: Exception) { }
    }

    private fun scrollToApps() { /* Apps are already visible on the home screen in v0.1. */ }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
