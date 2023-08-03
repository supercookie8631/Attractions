package com.example.attractions

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModelProvider
import com.example.attractions.model.AttractionsViewModel

class AttractionsActivity : AppCompatActivity() {

    private lateinit var mAttractionsViewModel: AttractionsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attractions)
        mAttractionsViewModel = ViewModelProvider(this)[AttractionsViewModel::class.java]
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }
}