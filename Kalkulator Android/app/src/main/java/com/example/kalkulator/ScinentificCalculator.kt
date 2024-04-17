package com.example.kalkulator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat

class ScientificCalculator : AppCompatActivity() {

    private lateinit var resultTextView: TextView
    private var expression: String = ""
    private val validOperators = setOf("+", "-", "x", "/")
    private val maxLength = 20

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.scientific_calculator)

        resultTextView = findViewById(R.id.tvmain)

        savedInstanceState?.let {
            resultTextView.text = it.getString("RESULT_TEXT", expression)
            expression = savedInstanceState.getString("EXPRESSION", "")
        }
        setButtonListeners()
    }

    private fun setButtonListeners() {
        val buttonIDs = listOf(
            R.id.btnZero, R.id.btnOne, R.id.btnTwo, R.id.btnThree, R.id.btnFour, R.id.btnFive, R.id.btnSix, R.id.btnSeven,
            R.id.btnEight, R.id.btnNine, R.id.btnDot, R.id.btnAdd, R.id.btnSub, R.id.btnMul, R.id.btnDiv, R.id.btnSin, R.id.btnCos,
            R.id.btnTan, R.id.btnLog, R.id.btnLn, R.id.btnPower, R.id.btnSqrt, R.id.btnInv, R.id.btnSign, R.id.btnProc, R.id.btnB1, R.id.btnB2
        )

        buttonIDs.forEach { id ->
            findViewById<Button>(id).setOnClickListener { button ->
                onButtonClicked(button)
            }
        }

        findViewById<Button>(R.id.btnAC).setOnClickListener { clearAll() }
        findViewById<Button>(R.id.btnC).setOnClickListener { clearLast() }
        findViewById<Button>(R.id.btnEqual).setOnClickListener { calculateResult() }
        findViewById<Button>(R.id.btnSign).setOnClickListener {
            changeLastNumberSign()
        }

    }
    private fun changeLastNumberSign() {
        if (expression.isNotEmpty()) {
            val regex = Regex("([-+]?\\d*\\.?\\d+)(?:[\\+\\-x*/]|$)")
            val matchResult = regex.findAll(expression).lastOrNull()
            matchResult?.let {
                val lastNumber = it.value
                val signChangedNumber = if (lastNumber.startsWith("-")) {
                    lastNumber.substring(1)
                } else {
                    "-$lastNumber"
                }
                expression = expression.removeRange(it.range).plus(signChangedNumber)
                resultTextView.text = expression
            }
        }
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("RESULT_TEXT", resultTextView.text.toString())
        outState.putString("EXPRESSION", expression)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        resultTextView.text = savedInstanceState.getString("RESULT_TEXT", expression)
        expression = savedInstanceState.getString("EXPRESSION", "")
    }

    private fun onButtonClicked(button: View) {
        if (button is Button) {
            if (expression.length >= maxLength) {
                return
            }
            val buttonText = button.text.toString()

            if (buttonText == "." && expression.isNotEmpty()) {
                val lastNumberSegment = expression.split(Regex("[^0-9.]")).lastOrNull()
                if (lastNumberSegment?.contains(".") == true) return
            }
            val numericButtonPressed = buttonText.toIntOrNull()
            if (numericButtonPressed != null) {
                val segments = expression.split(Regex("(?<=[-+x/()^%])|(?=[-+x/()^%])"))
                if (segments.isNotEmpty()) {
                    val lastSegment = segments.last()
                    if (lastSegment.matches(Regex("0+")) && numericButtonPressed in 1..9) {
                        expression = segments.dropLast(1).joinToString("") + buttonText
                        resultTextView.text = expression
                        return
                    } else if (lastSegment == "0" && numericButtonPressed == 0) {
                        return
                    }
                }
            }
            val operators = listOf("+", "-", "x", "/", "^","%")
            val lastChar = expression.lastOrNull()?.toString() ?: ""
            val secondLastChar = if (expression.length >= 2) expression[expression.length - 2] else null
            if (operators.contains(buttonText)) {
                if (expression.isEmpty() && buttonText != "-") {
                    return
                }
                if ((expression.length == 1 && expression == "-"&& operators.contains(buttonText)) || (secondLastChar == '(' && lastChar == "-" && operators.contains(buttonText))) {
                    return
                }
                else if (expression.length > 1 && operators.contains(lastChar) && operators.contains(buttonText)) {
                    expression = expression.dropLast(1)
                }
                if (lastChar == "(" && buttonText != "-") {
                    return
                }
            }
            expression += if (buttonText in listOf("sin", "cos", "tan", "log", "ln","√")) {
                "$buttonText("
            }else {
                buttonText
            }
            resultTextView.text = expression
        }
    }

    private fun clearAll() {
        expression = ""
        resultTextView.text = ""
    }

    private fun clearLast() {
        if (expression.isNotEmpty()) {
            expression = expression.dropLast(1)
            resultTextView.text = expression
        }
    }

    private fun calculateResult() {
        if (expression.isNotEmpty()) {
            if (expression.isNotEmpty() && validOperators.contains(expression.last().toString())) {
                showError("Syntax Error")
                return
            }
            try {
                var modifiedExpression = expression

                val openParenthesesCount = modifiedExpression.count { it == '(' }
                val closedParenthesesCount = modifiedExpression.count { it == ')' }
                val unclosedParenthesesCount = openParenthesesCount - closedParenthesesCount
                if (unclosedParenthesesCount > 0) {
                    modifiedExpression += ")".repeat(unclosedParenthesesCount)
                }

                modifiedExpression = modifiedExpression.replace('x', '*').replace("log", "log10")

                val syntaxCheckResult = Expression(modifiedExpression).checkSyntax()

                if (syntaxCheckResult) {
                    val result = Expression(modifiedExpression).calculate()
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
                    expression = formattedResult
                } else {
                    showError("Syntax Error")
                }
            } catch (e: Exception) {
                showError("Error")
                expression = ""
            }
        }
    }
    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}
