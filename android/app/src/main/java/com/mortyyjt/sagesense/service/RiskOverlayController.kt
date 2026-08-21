package com.mortyyjt.sagesense.service

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.net.toUri
import com.mortyyjt.sagesense.MainActivity
import com.mortyyjt.sagesense.R
import com.mortyyjt.sagesense.data.RiskEventEntity
import java.lang.ref.WeakReference

/**
 * Optional, event-triggered warning entry point shown above other apps.
 *
 * The overlay never reads or captures screen content. It is intentionally
 * transient and remains additive to Android's normal high-priority warning.
 */
object RiskOverlayController {
    const val EXTRA_SHOW_COGNITIVE_PAUSE = "com.mortyyjt.sagesense.extra.SHOW_COGNITIVE_PAUSE"

    private val SAGE_NAVY = 0xFF11146B.toInt()
    private val SAGE_AMBER = 0xFF9A5B00.toInt()
    private val SAGE_RED = 0xFFA61E35.toInt()
    private val SAGE_PREVIEW = 0xFFDDEBFF.toInt()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var windowManager: WindowManager? = null
    private var overlayViewRef: WeakReference<FrameLayout>? = null
    private var overlayIconRef: WeakReference<ImageView>? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var currentEventId: String? = null
    private var renderGeneration = 0L

    fun showRisk(context: Context, event: RiskEventEntity, locale: String) {
        val presentation = riskOverlayPresentation(event.riskLevel, locale) ?: return
        render(context.applicationContext, presentation, event.id)
    }

    fun showPreview(context: Context, locale: String) {
        val presentation = riskOverlayPresentation(null, locale, preview = true) ?: return
        render(context.applicationContext, presentation, eventId = null)
    }

    fun hide() {
        mainHandler.post(::removeOverlay)
    }

    private fun render(
        context: Context,
        presentation: RiskOverlayPresentation,
        eventId: String?,
    ) {
        mainHandler.post {
            if (!Settings.canDrawOverlays(context)) {
                removeOverlay()
                return@post
            }

            val view = overlayViewRef?.get() ?: createOverlayView(context).also {
                overlayViewRef = WeakReference(it)
            }
            val icon = overlayIconRef?.get() ?: return@post
            val backgroundColor = when (presentation.kind) {
                RiskOverlayKind.HIGH -> SAGE_RED
                RiskOverlayKind.MEDIUM -> SAGE_AMBER
                RiskOverlayKind.PREVIEW -> SAGE_PREVIEW
            }
            val iconTint = if (presentation.kind == RiskOverlayKind.PREVIEW) {
                SAGE_NAVY
            } else {
                Color.WHITE
            }

            currentEventId = eventId
            view.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(backgroundColor)
            }
            view.contentDescription = presentation.contentDescription
            icon.setImageResource(
                if (presentation.kind == RiskOverlayKind.MEDIUM) {
                    R.drawable.ic_overlay_warning
                } else {
                    R.drawable.ic_overlay_shield
                },
            )
            icon.setColorFilter(iconTint)

            val sizePx = context.dp(presentation.sizeDp)
            val params = layoutParams ?: createLayoutParams(context, sizePx).also { layoutParams = it }
            params.width = sizePx
            params.height = sizePx
            params.title = presentation.windowTitle

            val manager = windowManager
                ?: context.getSystemService(WindowManager::class.java).also { windowManager = it }
            runCatching {
                if (view.isAttachedToWindow) manager.updateViewLayout(view, params) else manager.addView(view, params)
            }.onFailure {
                removeOverlay()
                return@post
            }

            val generation = ++renderGeneration
            mainHandler.postDelayed(
                {
                    if (renderGeneration == generation) removeOverlay()
                },
                presentation.visibleMillis,
            )
        }
    }

    private fun createOverlayView(context: Context): FrameLayout {
        val icon = ImageView(context).apply {
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_NO
        }
        overlayIconRef = WeakReference(icon)
        return FrameLayout(context).apply {
            elevation = context.dp(8).toFloat()
            setPadding(context.dp(14), context.dp(14), context.dp(14), context.dp(14))
            isClickable = true
            isFocusable = true
            importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
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
                            data = "sagesense://event/$eventId".toUri()
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
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.END or Gravity.CENTER_VERTICAL
        x = context.dp(12)
        y = 0
    }

    private fun removeOverlay() {
        renderGeneration += 1
        val view = overlayViewRef?.get()
        val manager = windowManager
        if (view != null && manager != null && view.isAttachedToWindow) {
            runCatching { manager.removeViewImmediate(view) }
        }
        overlayViewRef = null
        overlayIconRef = null
        layoutParams = null
        currentEventId = null
    }

    private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
