package com.mortyyjt.sagesense.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import com.mortyyjt.sagesense.MainActivity
import com.mortyyjt.sagesense.R
import com.mortyyjt.sagesense.data.RiskEventEntity
import com.mortyyjt.sagesense.risk.RiskLevel

/**
 * Owns the one system overlay entry point used by every SageSense risk source.
 * It never reads or captures the screen and only exists after explicit SAW approval.
 */
object RiskOverlayController {
    const val EXTRA_SHOW_COGNITIVE_PAUSE = "com.mortyyjt.sagesense.extra.SHOW_COGNITIVE_PAUSE"

    private const val SAGE_NAVY = "#11146B"
    private const val SAGE_SKY = "#DDEBFF"
    private const val SAGE_AMBER = "#9A5B00"
    private const val SAGE_RED = "#A61E35"

    private val mainHandler = Handler(Looper.getMainLooper())
    private var windowManager: WindowManager? = null
    private var overlayView: FrameLayout? = null
    private var overlayIcon: ImageView? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var currentEventId: String? = null
    private val hapticEventIds = LinkedHashSet<String>()

    fun showResting(context: Context) {
        val appContext = context.applicationContext
        mainHandler.post {
            if (currentEventId == null) render(appContext, event = null)
        }
    }

    fun showRisk(context: Context, event: RiskEventEntity) {
        if (event.riskLevel == RiskLevel.LOW) return
        val appContext = context.applicationContext
        mainHandler.post {
            if (hapticEventIds.add(event.id)) {
                if (hapticEventIds.size > MAX_HAPTIC_EVENT_IDS) {
                    hapticEventIds.remove(hapticEventIds.first())
                }
                performOneShotHaptic(appContext)
            }
            render(appContext, event)
        }
    }

    fun hide() {
        mainHandler.post(::removeOverlay)
    }

    private fun render(context: Context, event: RiskEventEntity?) {
        if (!Settings.canDrawOverlays(context)) {
            removeOverlay()
            return
        }

        val view = overlayView ?: createOverlayView(context).also { overlayView = it }
        val icon = overlayIcon ?: return
        val sizeDp = when (event?.riskLevel) {
            RiskLevel.HIGH -> 80
            RiskLevel.MEDIUM -> 68
            else -> 56
        }
        val backgroundColor = when (event?.riskLevel) {
            RiskLevel.HIGH -> Color.parseColor(SAGE_RED)
            RiskLevel.MEDIUM -> Color.parseColor(SAGE_AMBER)
            else -> Color.parseColor(SAGE_SKY).let {
                Color.argb(190, Color.red(it), Color.green(it), Color.blue(it))
            }
        }
        val iconTint = if (event == null) Color.parseColor(SAGE_NAVY) else Color.WHITE
        val iconResource = if (event?.riskLevel == RiskLevel.MEDIUM) {
            R.drawable.ic_overlay_warning
        } else {
            R.drawable.ic_overlay_shield
        }

        currentEventId = event?.id
        view.background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(backgroundColor)
        }
        view.contentDescription = when (event?.riskLevel) {
            RiskLevel.HIGH -> "SageSense high-risk warning. Tap to pause and review."
            RiskLevel.MEDIUM -> "SageSense medium-risk warning. Tap to pause and review."
            else -> "SageSense protection is active."
        }
        icon.setImageResource(iconResource)
        icon.setColorFilter(iconTint)

        val sizePx = context.dp(sizeDp)
        val params = layoutParams ?: createLayoutParams(context, sizePx).also { layoutParams = it }
        params.width = sizePx
        params.height = sizePx

        val manager = windowManager ?: context.getSystemService(WindowManager::class.java).also { windowManager = it }
        runCatching {
            if (view.isAttachedToWindow) manager.updateViewLayout(view, params) else manager.addView(view, params)
        }.onFailure {
            overlayView = null
            overlayIcon = null
            layoutParams = null
        }
    }

    private fun createOverlayView(context: Context): FrameLayout {
        val icon = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        overlayIcon = icon
        return FrameLayout(context).apply {
            elevation = context.dp(8).toFloat()
            setPadding(context.dp(14), context.dp(14), context.dp(14), context.dp(14))
            isClickable = true
            isFocusable = false
            addView(
                icon,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    Gravity.CENTER,
                ),
            )
            setOnClickListener {
                val eventId = currentEventId
                hide()
                context.startActivity(
                    Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        if (eventId != null) {
                            data = Uri.parse("sagesense://event/$eventId")
                            putExtra(EXTRA_SHOW_COGNITIVE_PAUSE, true)
                        }
                    },
                )
            }
        }
    }

    private fun createLayoutParams(context: Context, sizePx: Int) = WindowManager.LayoutParams(
        sizePx,
        sizePx,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.END or Gravity.CENTER_VERTICAL
        x = context.dp(12)
        y = 0
        title = "SageSense risk warning"
    }

    private fun removeOverlay() {
        val view = overlayView
        val manager = windowManager
        if (view != null && manager != null && view.isAttachedToWindow) {
            runCatching { manager.removeViewImmediate(view) }
        }
        overlayView = null
        overlayIcon = null
        layoutParams = null
        currentEventId = null
    }

    @Suppress("DEPRECATION")
    private fun performOneShotHaptic(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        }
        if (!vibrator.hasVibrator()) return
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK)
        } else {
            VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            vibrator.vibrate(effect, VibrationAttributes.createForUsage(VibrationAttributes.USAGE_NOTIFICATION))
        } else {
            vibrator.vibrate(effect)
        }
    }

    private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

    private const val MAX_HAPTIC_EVENT_IDS = 64
}
