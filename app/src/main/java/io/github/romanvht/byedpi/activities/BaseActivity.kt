package io.github.romanvht.byedpi.activities

import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import io.github.romanvht.byedpi.R

abstract class BaseActivity : AppCompatActivity() {

    protected fun setupToolbar() {
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
    }
}
