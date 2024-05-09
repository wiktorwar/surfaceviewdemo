package com.reddit.surfacecliptest

import android.graphics.Color.*
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.Window
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.toAndroidRect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntRect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class FullScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.requestFeature(Window.FEATURE_ACTIVITY_TRANSITIONS)
        window.setBackgroundDrawable(ColorDrawable(TRANSPARENT))

        val transitionBounds = intent.getParcelableExtra(ARG_BOUNDS) as? Rect
        val useSurfaceView = intent.getBooleanExtra(ARG_USE_SV, false)
        window.enterTransition = EnterTransition(transitionBounds!!)

        setContentView(R.layout.activity_full_screen)
        findViewById<ComposeView>(R.id.compose).apply {
            setContent {
                AndroidView(
                    factory = {
                        if (useSurfaceView) {
                            /**  problem: clipBounds are not applied to SV during enter transition,
                            see [EnterTransition.createBoundsAnimator]*/
                            SurfaceView(it).apply { drawStuffOnSurface(this) }
                        } else {
                            /** any other view works fine, clipBounds are applied correctly
                             */
                            val view = View(it)
                            view.setBackgroundColor(BLACK)
                            view
                        }
                    },
                    Modifier
                        .fillMaxSize()
                )
            }
        }
    }

    private fun drawStuffOnSurface(surfaceView: SurfaceView) {
        var surfaceHolder: SurfaceHolder? = null
        var size: IntSize? = null
        surfaceView.holder.addCallback(
            object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
                    surfaceHolder = holder
                }

                override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int
                ) {
                    surfaceHolder = holder
                    size = IntSize(width, height)
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    surfaceHolder = null
                }
            }
        )
        lifecycleScope.launch {
            while (isActive) {
                awaitFrame()
                if (surfaceHolder != null) {
                    val canvas = surfaceHolder!!.lockCanvas()
                    canvas.drawRect(
                        size!!.toIntRect().toAndroidRect(),
                        Paint().apply { color = Color.Black }.asFrameworkPaint()
                    )
                    surfaceHolder!!.unlockCanvasAndPost(canvas)
                }
            }
        }
    }

    companion object {
        const val ARG_BOUNDS = "rect"
        const val ARG_USE_SV = "useSurfaceView"
    }
}