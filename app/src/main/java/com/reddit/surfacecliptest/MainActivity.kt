package com.reddit.surfacecliptest

import android.app.ActivityOptions
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.view)?.let { view ->

            view.setOnClickListener {
                val locationOnScreen = Rect()
                view.getGlobalVisibleRect(locationOnScreen)

                val intent = Intent(this, FullScreenActivity::class.java).apply {
                    putExtra(FullScreenActivity.ARG_BOUNDS, locationOnScreen)
                    putExtra(
                        FullScreenActivity.ARG_USE_SV,
                        findViewById<CheckBox>(R.id.sv_enabled).isChecked
                    )
                }

                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
            }
        }
    }
}
