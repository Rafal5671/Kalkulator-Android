package com.example.kalkulator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class ChooseCalculatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_calculator)

        val simpleCalculatorButton: Button = findViewById(R.id.simpleCalculatorButton)
        val scientificCalculatorButton: Button = findViewById(R.id.scientificCalculatorButton)
        val closeAppButton: Button = findViewById(R.id.closeAppButton)

        simpleCalculatorButton.setOnClickListener {
            val intent = Intent(this, SimpleCalculator::class.java)
            startActivity(intent)
        }

        scientificCalculatorButton.setOnClickListener {
            val intent = Intent(this, ScientificCalculator::class.java)
            startActivity(intent)
        }
        closeAppButton.setOnClickListener {
            finish()
        }
        val aboutMeButton: Button = findViewById(R.id.aboutMeButton)
        aboutMeButton.setOnClickListener {
            val intent = Intent(this, AboutMeActivity::class.java)
            startActivity(intent)
        }
    }
}
