package com.reddit.surfacecliptest


import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.graphics.Rect
import android.transition.TransitionValues
import android.transition.Visibility
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.addListener

class EnterTransition(private val mediaBounds: Rect) : Visibility() {

    override fun onAppear(
        sceneRoot: ViewGroup?,
        view: View?,
        startValues: TransitionValues?,
        endValues: TransitionValues?,
    ): Animator? {
        return (view as? ViewGroup)?.run {
            val startTranslationY = mediaBounds.top - (resources.displayMetrics.heightPixels - mediaBounds.height()) / 2f
            val startCropBounds = Rect(mediaBounds).apply { offset(0, -startTranslationY.toInt()) }
            clipBounds = startCropBounds
            translationY = startTranslationY
            AnimatorSet().apply {
                playTogether(
                    createTranslationYAnimator(startTranslationY, 0f),
                    createAlphaAnimator(delayMills = 0L, 0f, 1f),
                    createBoundsAnimator(
                        fromBounds = startCropBounds,
                        toBounds = resources.displayMetrics.getScreenBounds(),
                    ).apply {
                        addListener(
                            onEnd = {
                                clipBounds = null
                                translationY = 0f
                            },
                            onCancel = {
                                clipBounds = null
                                translationY = 0f
                            },
                        )
                    },
                )
            }
        }
    }

    override fun onDisappear(
        sceneRoot: ViewGroup?,
        view: View?,
        startValues: TransitionValues?,
        endValues: TransitionValues?,
    ): Animator? {
        return (view as? ViewGroup)?.run {
            val endTranslationY = mediaBounds.top - (resources.displayMetrics.heightPixels - mediaBounds.height()) / 2f
            val endCropBounds = Rect(mediaBounds).apply { offset(0, -endTranslationY.toInt()) }
            AnimatorSet().apply {
                playTogether(
                    createTranslationYAnimator(0f, endTranslationY),
                    createBoundsAnimator(
                        fromBounds = resources.displayMetrics.getScreenBounds(),
                        toBounds = endCropBounds,
                    ),
                    createAlphaAnimator(delayMills = DURATION_MILLIS - ALPHA_DURATION_MILLIS, 1f, 0f),
                )
            }
        }
    }

    private fun ViewGroup.createBoundsAnimator(fromBounds: Rect, toBounds: Rect): Animator {
        return ValueAnimator.ofObject(RectEvaluator(), fromBounds, toBounds).apply {
            duration = DURATION_MILLIS
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                clipBounds = it.animatedValue as Rect
            }
        }
    }

    private fun View.createAlphaAnimator(
        delayMills: Long,
        vararg alphas: Float,
    ): Animator {
        return ObjectAnimator.ofFloat(this, View.ALPHA, *alphas).apply {
            interpolator = LinearInterpolator()
            duration = ALPHA_DURATION_MILLIS
            startDelay = delayMills
        }
    }

    private fun View.createTranslationYAnimator(
        vararg values: Float,
    ): Animator {
        return ObjectAnimator.ofFloat(this, View.TRANSLATION_Y, *values).apply {
            duration = DURATION_MILLIS
            interpolator = AccelerateDecelerateInterpolator()
        }
    }

    private class RectEvaluator : TypeEvaluator<Rect> {
        private val buffer = Rect()

        override fun evaluate(fraction: Float, startValue: Rect, endValue: Rect): Rect {
            return buffer.apply {
                left = evaluateSide(fraction, startValue.left, endValue.left)
                top = evaluateSide(fraction, startValue.top, endValue.top)
                right = evaluateSide(fraction, startValue.right, endValue.right)
                bottom = evaluateSide(fraction, startValue.bottom, endValue.bottom)
            }
        }

        private fun evaluateSide(fraction: Float, startValue: Int, endValue: Int): Int {
            return (startValue + fraction * (endValue - startValue)).toInt()
        }
    }

    companion object {
        private const val DURATION_MILLIS = 400L
        private const val ALPHA_DURATION_MILLIS = DURATION_MILLIS / 2
    }
}

fun DisplayMetrics.getScreenBounds() = Rect(0, 0, widthPixels, heightPixels)
