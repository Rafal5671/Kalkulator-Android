package com.example.kalkulator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat
import android.widget.Toast


class SimpleCalculator : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private var currentNumber: String = ""
    private val validOperators = setOf("+", "-", "x", "/")
    private val maxLength = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.simple_calculator)

        if (savedInstanceState != null) {
            currentNumber = savedInstanceState.getString("CURRENT_NUMBER", "")
        }

        resultTextView = findViewById(R.id.resultTV)
        resultTextView.text = currentNumber

        val numberAndDecimalButtonIDs = listOf(
            R.id.buttonZero, R.id.buttonOne, R.id.buttonTwo, R.id.buttonThree,
            R.id.buttonFour, R.id.buttonFive, R.id.buttonSix, R.id.buttonSeven,
            R.id.buttonEight, R.id.buttonNine, R.id.buttonDecimal
        )

        val operatorButtonIDs = listOf(
            R.id.buttonPlus, R.id.buttonMinus, R.id.buttonMultiply, R.id.buttonDivide
        )

        val operationIDs = listOf(R.id.buttonEquals, R.id.buttonAC, R.id.buttonC)

        numberAndDecimalButtonIDs.forEach { id ->
            findViewById<Button>(id).setOnClickListener {button ->
                val buttonText = (button as Button).text.toString()
                handleButtonInput(buttonText)
            }
        }
        findViewById<Button>(R.id.buttonPlusMinus).setOnClickListener {
            changeLastNumberSign()
        }

        operatorButtonIDs.forEach { id ->
            findViewById<Button>(id).setOnClickListener {button ->
                val buttonText = (button as Button).text.toString()
                handleOperatorInput(buttonText, validOperators)
            }
        }

        operationIDs.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                when ((it as Button).text.toString()) {
                    "C" -> backspaceOperation()
                    "AC" -> clearCalculator()
                    "=" -> performOperation()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("CURRENT_NUMBER", currentNumber)
    }
    private fun handleOperatorInput(buttonText: String, validOperators: Set<String>) {
        if (currentNumber.isEmpty() || currentNumber == "-") {
            if (buttonText == "-" && currentNumber.isEmpty()) {
                currentNumber += buttonText
            }
        } else if (currentNumber.length >= maxLength && !validOperators.contains(currentNumber.last().toString())) {
            return
        } else {
            if (validOperators.contains(currentNumber.last().toString()) && !(currentNumber.length == 1 && currentNumber == "-")) {
                currentNumber = currentNumber.dropLast(1) + buttonText
            } else if (currentNumber.isNotEmpty()) {
                currentNumber += buttonText
            }
        }
        resultTextView.text = currentNumber
    }
    private fun handleButtonInput(buttonText: String) {
        if (currentNumber.length >= maxLength) {
            return
        }

        if (buttonText == ".") {
            val lastNumberSegment = currentNumber.split(Regex("[+\\-x/]")).last()
            if (!lastNumberSegment.contains(".")) {
                currentNumber += buttonText
            }
        } else {
            val lastNumberSegment = currentNumber.split(Regex("[+\\-x/]")).last()
            if (lastNumberSegment == "0" && currentNumber.matches(Regex(".*[+\\-x/].*"))) {
                currentNumber = currentNumber.dropLast(1) + buttonText
            } else if (lastNumberSegment.matches(Regex("^0$")) && buttonText != "0") {
                currentNumber = currentNumber.dropLast(1) + buttonText
            } else if (!lastNumberSegment.matches(Regex("^0+$"))) {
                currentNumber += buttonText
            }
        }
        resultTextView.text = currentNumber
    }
    private fun changeLastNumberSign() {
        if (currentNumber.isNotEmpty()) {
            val regex = Regex("([-+]?\\d*\\.?\\d+)(?:[\\+\\-x/]|$)")
            val matchResult = regex.findAll(currentNumber).lastOrNull()
            matchResult?.let {
                val lastNumber = it.value
                val signChangedNumber = if (lastNumber.startsWith("-")) {
                    lastNumber.substring(1)
                } else {
                    "-$lastNumber"
                }
                currentNumber = currentNumber.removeRange(it.range).plus(signChangedNumber)
                resultTextView.text = currentNumber
            }
        }
    }
    private fun performOperation() {
        if (currentNumber.isNotEmpty()) {
            if (currentNumber.isNotEmpty() && validOperators.contains(currentNumber.last().toString())) {
                showError("Syntax error")
                return
            }
            val modifiedExpression = currentNumber.replace("x", "*")
            val expression = Expression(modifiedExpression)

            if (!expression.checkSyntax()) {
                showError("Syntax error")
                return
            }

            try {
                val result = expression.calculate()
                if (result.isNaN()) {
                    showError("Result is undefined")
                    return
                }
                val resultAsString = result.toString()
                val isScientificNotation = resultAsString.matches(Regex("[\\d.]+E[+-]?\\d+"))
                val formatter = when {
                    isScientificNotation && resultAsString.length <= maxLength -> DecimalFormat("#.######E0")
                    resultAsString.length > maxLength -> DecimalFormat("0.####E0")
                    else -> DecimalFormat("#.##########")
                }
                var formattedResult = formatter.format(result)
                formattedResult = formattedResult.replace(',', '.')
                resultTextView.text = formattedResult
                currentNumber = formattedResult
            } catch (e: Exception) {
                showError("Calculation error")
            }
        }
    }
    private fun clearCalculator() {
        currentNumber = ""
        resultTextView.text = ""
    }

    private fun backspaceOperation() {
        if (currentNumber.isNotEmpty()) {
            currentNumber = currentNumber.dropLast(1)
            resultTextView.text = currentNumber
        }
    }
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

