package com.centurianrahul.tvision

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val ink = Color.rgb(8, 11, 19)
    private val panel = Color.rgb(20, 25, 37)
    private val panel2 = Color.rgb(28, 35, 50)
    private val text = Color.rgb(247, 249, 255)
    private val secondary = Color.rgb(164, 176, 198)
    private val blue = Color.rgb(117, 170, 255)
    private lateinit var pageScroll: ScrollView
    private lateinit var appGrid: LinearLayout
    private lateinit var clockView: TextView
    private val mainHandler = Handler(Looper.getMainLooper())
    private val clockTick = object : Runnable {
        override fun run() {
            if (::clockView.isInitialized) {
                val now = Date()
                clockView.text = SimpleDateFormat("h:mm", Locale.getDefault()).format(now) +
                    "\n" + SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(now)
            }
            mainHandler.postDelayed(this, 30_000)
        }
    }

    private data class AppEntry(
        val label: String,
        val packageName: String,
        val activityName: String,
        val icon: android.graphics.drawable.Drawable
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
        try {
            buildInterface()
        } catch (error: Throwable) {
            Toast.makeText(this, "TVision couldn't load: ${error.message ?: "unknown error"}", Toast.LENGTH_LONG).show()
            finish()
        }
        mainHandler.post(clockTick)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacks(clockTick)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (::pageScroll.isInitialized && pageScroll.scrollY > 0) {
            pageScroll.smoothScrollTo(0, 0)
        } else {
            super.onBackPressed()
        }
    }

    private fun buildInterface() {
        val apps = discoverApps()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(34), dp(16), dp(34), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(15, 22, 39), ink, Color.rgb(7, 9, 15))
            )
            isFocusableInTouchMode = true
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        brand.addView(TextView(this).apply {
            text = "TVISION"
            textSize = 21f
            letterSpacing = 0.22f
            setTextColor(text)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
        })
        brand.addView(TextView(this).apply {
            text = "YOUR SCREEN. YOUR WORLD."
            textSize = 8f
            letterSpacing = 0.19f
            setTextColor(secondary)
            setPadding(1, dp(2), 0, 0)
        })
        header.addView(brand, LinearLayout.LayoutParams(0, dp(54), 1f))

        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        listOf("Discover", "Apps", "Smart home").forEachIndexed { index, label ->
            val item = TextView(this).apply {
                text = label
                textSize = 13f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                gravity = Gravity.CENTER
                setTextColor(if (index == 0) text else secondary)
                setPadding(dp(17), 0, dp(17), 0)
                background = if (index == 0) pill(Color.rgb(43, 61, 91), blue) else focusDrawable(22)
                isFocusable = true
                isClickable = true
                setOnClickListener {
                    when (index) {
                        0 -> pageScroll.smoothScrollTo(0, 0)
                        1 -> scrollToGrid()
                        2 -> launchPackage("io.homeassistant.companion.android")
                    }
                }
            }
            nav.addView(item, LinearLayout.LayoutParams(-2, dp(39)).apply { marginEnd = dp(5) })
        }
        header.addView(nav)
        clockView = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(text)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(dp(22), 0, dp(13), 0)
            minWidth = dp(88)
        }
        header.addView(clockView)
        val profile = TextView(this).apply {
            text = "R"
            textSize = 14f
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.WHITE)
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(90, 135, 222), Color.rgb(113, 87, 190))).apply {
                shape = GradientDrawable.OVAL
            }
        }
        header.addView(profile, LinearLayout.LayoutParams(dp(36), dp(36)))
        root.addView(header)

        pageScroll = ScrollView(this).apply {
            isFillViewport = false
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
            setPadding(0, dp(12), 0, dp(8))
            descendantFocusability = ViewGroup.FOCUS_AFTER_DESCENDANTS
        }
        val page = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val hero = FrameLayout(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(28, 57, 103), Color.rgb(27, 38, 68), Color.rgb(33, 27, 61))).apply {
                cornerRadius = dp(28).toFloat()
            }
            clipToOutline = true
        }
        hero.addView(AuroraArtwork(this), FrameLayout.LayoutParams(-1, -1))
        val heroShade = View(this).apply {
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(Color.argb(235, 12, 20, 37), Color.argb(150, 12, 20, 37), Color.TRANSPARENT))
        }
        hero.addView(heroShade, FrameLayout.LayoutParams(-1, -1))
        val heroContent = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(34), dp(24), dp(30), dp(24))
        }
        heroContent.addView(TextView(this).apply {
            text = "A NEW WAY TO EXPERIENCE YOUR TV"
            textSize = 10f
            letterSpacing = 0.2f
            setTextColor(Color.rgb(185, 210, 255))
            typeface = Typeface.DEFAULT_BOLD
        })
        heroContent.addView(TextView(this).apply {
            text = "Everything you love.\nAll in one place."
            textSize = 34f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setLineSpacing(dp(1).toFloat(), 0.98f)
            setPadding(0, dp(8), 0, dp(7))
        })
        heroContent.addView(TextView(this).apply {
            text = "A calmer, more beautiful home for your entertainment."
            textSize = 13f
            setTextColor(Color.rgb(211, 222, 244))
        })
        val heroActions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(17), 0, 0)
        }
        heroActions.addView(button("Explore apps", true) { scrollToGrid() },
            LinearLayout.LayoutParams(-2, dp(42)).apply { marginEnd = dp(10) })
        heroActions.addView(button("Home Assistant  ↗", false) {
            launchPackage("io.homeassistant.companion.android")
        }, LinearLayout.LayoutParams(-2, dp(42)))
        heroContent.addView(heroActions)
        hero.addView(heroContent, FrameLayout.LayoutParams(-1, -1))
        page.addView(hero, LinearLayout.LayoutParams(-1, dp(248)).apply { bottomMargin = dp(24) })

        val favorites = chooseFeatured(apps)
        page.addView(sectionTitle("Your favourites", "The apps you come here for"))
        page.addView(featuredRow(favorites), LinearLayout.LayoutParams(-1, dp(154)).apply {
            bottomMargin = dp(22)
        })

        val gridHeader = sectionTitle("All apps", "${apps.size} apps ready when you are")
        gridHeader.tag = "apps_header"
        page.addView(gridHeader)
        appGrid = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        populateAppGrid(apps)
        page.addView(appGrid)

        page.addView(TextView(this).apply {
            text = "TVISION     •     MADE FOR THE BIG SCREEN"
            textSize = 9f
            letterSpacing = 0.16f
            setTextColor(Color.rgb(101, 115, 140))
            gravity = Gravity.CENTER
            setPadding(0, dp(24), 0, dp(12))
        })
        pageScroll.addView(page)
        root.addView(pageScroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
        root.requestFocus()
    }

    private fun discoverApps(): List<AppEntry> {
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved: List<ResolveInfo> = try {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(query, PackageManager.MATCH_ALL)
        } catch (_: Exception) {
            emptyList()
        }
        return resolved.mapNotNull { item ->
            val info = item.activityInfo ?: return@mapNotNull null
            if (info.packageName == packageName) return@mapNotNull null
            try {
                AppEntry(
                    item.loadLabel(packageManager)?.toString() ?: info.packageName,
                    info.packageName,
                    info.name,
                    item.loadIcon(packageManager)
                )
            } catch (_: Exception) { null }
        }.distinctBy { it.packageName }.sortedBy { it.label.lowercase(Locale.getDefault()) }
    }

    private fun chooseFeatured(apps: List<AppEntry>): List<AppEntry> {
        val preferred = listOf("youtube", "netflix", "primevideo", "hotstar", "plex", "vlc",
            "sonyliv", "zee5", "spotify", "jio", "disney")
        val picked = preferred.mapNotNull { key ->
            apps.firstOrNull { it.packageName.lowercase(Locale.ROOT).contains(key) ||
                it.label.lowercase(Locale.ROOT).contains(key) }
        }.distinctBy { it.packageName }
        return (picked + apps).distinctBy { it.packageName }.take(10)
    }

    private fun featuredRow(apps: List<AppEntry>): HorizontalScrollView {
        val scroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            clipToPadding = false
        }
        val strip = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        apps.forEachIndexed { index, app ->
            val palettes = arrayOf(
                intArrayOf(Color.rgb(30, 76, 125), Color.rgb(19, 34, 65)),
                intArrayOf(Color.rgb(112, 48, 74), Color.rgb(49, 27, 58)),
                intArrayOf(Color.rgb(39, 101, 94), Color.rgb(20, 48, 60)),
                intArrayOf(Color.rgb(111, 76, 38), Color.rgb(53, 38, 35)),
                intArrayOf(Color.rgb(76, 63, 133), Color.rgb(35, 33, 72))
            )
            val colors = palettes[index % palettes.size]
            val card = FrameLayout(this).apply {
                background = GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
                    cornerRadius = dp(19).toFloat()
                }
                clipToOutline = true
                isFocusable = true
                isClickable = true
                contentDescription = "Launch ${app.label}"
                background = focusDrawable(19, colors)
                setOnClickListener { launchApp(app) }
                setOnKeyListener { _, key, event ->
                    if (event.action == KeyEvent.ACTION_UP &&
                        (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER)) {
                        launchApp(app); true
                    } else false
                }
            }
            card.addView(PosterArtwork(this, colors[0]), FrameLayout.LayoutParams(-1, -1))
            val bottomShade = View(this).apply {
                background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                    intArrayOf(Color.TRANSPARENT, Color.argb(205, 5, 8, 15)))
            }
            card.addView(bottomShade, FrameLayout.LayoutParams(-1, -1))
            val icon = ImageView(this).apply {
                setImageDrawable(app.icon)
                scaleType = ImageView.ScaleType.FIT_CENTER
                elevation = dp(3).toFloat()
            }
            val iconBg = FrameLayout(this).apply {
                background = rounded(Color.argb(45, 255, 255, 255), 15)
                setPadding(dp(10), dp(10), dp(10), dp(10))
                addView(icon, FrameLayout.LayoutParams(dp(40), dp(40)))
            }
            card.addView(iconBg, FrameLayout.LayoutParams(dp(60), dp(60), Gravity.TOP or Gravity.START).apply {
                leftMargin = dp(15); topMargin = dp(14)
            })
            val title = TextView(this).apply {
                text = app.label
                textSize = 15f
                setTextColor(Color.WHITE)
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                maxLines = 1
                setShadowLayer(8f, 0f, 2f, Color.BLACK)
            }
            card.addView(title, FrameLayout.LayoutParams(-1, -2, Gravity.BOTTOM).apply {
                leftMargin = dp(16); rightMargin = dp(12); bottomMargin = dp(15)
            })
            strip.addView(card, LinearLayout.LayoutParams(dp(210), dp(140)).apply {
                marginEnd = dp(12)
            })
        }
        scroll.addView(strip)
        return scroll
    }

    private fun populateAppGrid(apps: List<AppEntry>) {
        appGrid.removeAllViews()
        if (apps.isEmpty()) {
            appGrid.addView(TextView(this).apply {
                text = "No launchable apps were found. Restart TVision or check Android app visibility settings."
                setTextColor(secondary)
                textSize = 14f
            })
            return
        }
        val columns = if (resources.displayMetrics.widthPixels / resources.displayMetrics.density >= 900) 6 else 5
        var index = 0
        while (index < apps.size) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            for (column in 0 until columns) {
                if (index >= apps.size) break
                val app = apps[index++]
                row.addView(appTile(app), LinearLayout.LayoutParams(0, dp(86), 1f).apply {
                    marginEnd = dp(8)
                    bottomMargin = dp(8)
                })
            }
            appGrid.addView(row)
        }
    }

    private fun appTile(app: AppEntry): View {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(9), dp(8), dp(9))
            background = focusDrawable(15)
            isFocusable = true
            isClickable = true
            contentDescription = "Open ${app.label}"
            setOnClickListener { launchApp(app) }
            setOnKeyListener { _, key, event ->
                if (event.action == KeyEvent.ACTION_UP &&
                    (key == KeyEvent.KEYCODE_DPAD_CENTER || key == KeyEvent.KEYCODE_ENTER)) {
                    launchApp(app); true
                } else false
            }
        }
        tile.addView(ImageView(this).apply {
            setImageDrawable(app.icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(dp(42), dp(42)))
        tile.addView(TextView(this).apply {
            text = app.label
            textSize = 11f
            setTextColor(text)
            maxLines = 2
            ellipsize = android.text.TextUtils.TruncateAt.END
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(10), 0, 0, 0)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }, LinearLayout.LayoutParams(0, -1, 1f))
        return tile
    }

    private fun sectionTitle(title: String, subtitle: String): View {
        val block = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(2), 0, 0, dp(11))
        }
        block.addView(TextView(this).apply {
            text = title
            textSize = 20f
            setTextColor(text)
            typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        })
        block.addView(TextView(this).apply {
            text = subtitle
            textSize = 11f
            setTextColor(secondary)
            setPadding(0, dp(3), 0, 0)
        })
        return block
    }

    private fun button(label: String, primary: Boolean, action: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(dp(18), 0, dp(18), 0)
            setTextColor(if (primary) Color.rgb(13, 25, 45) else Color.WHITE)
            background = if (primary) pill(Color.rgb(220, 234, 255), Color.WHITE) else focusDrawable(24)
            isFocusable = true
            isClickable = true
            setOnClickListener { action() }
        }

    private fun launchApp(app: AppEntry) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(app.packageName, app.activityName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent)
        } catch (error: Exception) {
            launchPackage(app.packageName)
        }
    }

    private fun launchPackage(packageName: String) {
        try {
            val launch = packageManager.getLaunchIntentForPackage(packageName)
            if (launch != null) {
                launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(launch)
            } else {
                Toast.makeText(this, "That app isn't installed or doesn't expose a launch screen.", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Couldn't open that app.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scrollToGrid() {
        val target = pageScroll.findViewWithTag<View>("apps_header")
        if (target != null) pageScroll.post { pageScroll.smoothScrollTo(0, target.top) }
    }

    private fun rounded(color: Int, radius: Int, stroke: Int? = null) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }

    private fun pill(color: Int, stroke: Int? = null) =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(28).toFloat()
            if (stroke != null) setStroke(dp(1), stroke)
        }

    private fun focusDrawable(radius: Int, colors: IntArray? = null): StateListDrawable =
        StateListDrawable().apply {
            addState(intArrayOf(android.R.attr.state_focused),
                if (colors == null) rounded(Color.rgb(48, 66, 96), radius, blue)
                else GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
                    cornerRadius = dp(radius).toFloat()
                    setStroke(dp(2), Color.rgb(190, 216, 255))
                })
            addState(intArrayOf(android.R.attr.state_pressed), rounded(Color.rgb(48, 60, 82), radius))
            addState(intArrayOf(), if (colors == null) rounded(panel, radius)
                else GradientDrawable(GradientDrawable.Orientation.TL_BR, colors).apply {
                    cornerRadius = dp(radius).toFloat()
                })
        }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private inner class AuroraArtwork(context: android.content.Context) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            paint.shader = android.graphics.RadialGradient(w * .82f, h * .38f, w * .45f,
                intArrayOf(Color.argb(150, 93, 151, 255), Color.argb(55, 132, 91, 225), Color.TRANSPARENT),
                null, android.graphics.Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(1).toFloat()
            for (i in 0..5) {
                paint.color = Color.argb(25 + i * 4, 195, 218, 255)
                val radius = w * (.16f + i * .045f)
                canvas.drawCircle(w * .83f, h * .54f, radius, paint)
            }
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(95, 139, 183, 255)
            canvas.drawCircle(w * .83f, h * .54f, dp(4).toFloat(), paint)
        }
    }

    private inner class PosterArtwork(context: android.content.Context, private val tint: Int) : View(context) {
        private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            paint.shader = android.graphics.LinearGradient(0f, 0f, w, h,
                tint, Color.argb(0, Color.red(tint), Color.green(tint), Color.blue(tint)),
                android.graphics.Shader.TileMode.CLAMP)
            canvas.drawRect(0f, 0f, w, h, paint)
            paint.shader = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = dp(1).toFloat()
            paint.color = Color.argb(35, 255, 255, 255)
            val rect = RectF(w * .45f, -h * .45f, w * 1.25f, h * 1.35f)
            canvas.drawOval(rect, paint)
            canvas.drawOval(RectF(rect.left + dp(13), rect.top + dp(13),
                rect.right - dp(13), rect.bottom - dp(13)), paint)
            paint.style = Paint.Style.FILL
        }
    }
}
