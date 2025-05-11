package com.example.calculator

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvResult: TextView
    private var currentInput: String = ""
    private var lastNumeric: Boolean = false
    private var lastDot: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvResult = findViewById(R.id.tvResult)

        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        for (id in buttons) {
            findViewById<Button>(id).setOnClickListener { appendDigit((it as Button).text.toString()) }
        }

        findViewById<Button>(R.id.btnDot).setOnClickListener { appendDot() }
        findViewById<Button>(R.id.btnAdd).setOnClickListener { appendOperator("+") }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { appendOperator("-") }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { appendOperator("*") }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { appendOperator("/") }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clearInput() }
        findViewById<Button>(R.id.btnDelete).setOnClickListener { deleteLast() }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { calculateResult() }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { calculatePercentage() } // Added Percentage Button Listener
    }

    private fun appendDigit(digit: String) {
        currentInput += digit
        lastNumeric = true
        tvResult.text = currentInput
    }

    private fun appendDot() {
        if (lastNumeric && !lastDot) {
            currentInput += "."
            lastNumeric = false
            lastDot = true
            tvResult.text = currentInput
        }
    }

    private fun appendOperator(op: String) {
        if (lastNumeric && !endsWithOperator()) {
            currentInput += op
            lastNumeric = false
            lastDot = false
            tvResult.text = currentInput
        }
    }

    private fun clearInput() {
        currentInput = ""
        tvResult.text = "0"
        lastNumeric = false
        lastDot = false
    }

    private fun deleteLast() {
        if (currentInput.isNotEmpty()) {
            currentInput = currentInput.dropLast(1)
            tvResult.text = if (currentInput.isEmpty()) "0" else currentInput
        }
    }

    private fun endsWithOperator(): Boolean {
        return currentInput.endsWith("+") || currentInput.endsWith("-") ||
                currentInput.endsWith("*") || currentInput.endsWith("/")
    }

    private fun calculateResult() {
        try {
            val result = eval(currentInput)
            tvResult.text = result.toString()
            currentInput = result.toString()
            lastDot = currentInput.contains(".")
        } catch (e: Exception) {
            tvResult.text = "Error"
            currentInput = ""
        }
    }

    // New Percentage Calculation Function
    private fun calculatePercentage() {
        if (lastNumeric) {
            try {
                val value = currentInput.toDouble()
                val result = value / 100
                tvResult.text = result.toString()
                currentInput = result.toString()
                lastDot = currentInput.contains(".")
            } catch (e: Exception) {
                tvResult.text = "Error"
                currentInput = ""
            }
        }
    }

    private fun eval(expr: String): Double {
        return object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < expr.length) expr[pos].code else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.code) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < expr.length) throw RuntimeException("Unexpected: " + expr[pos])
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    x = when {
                        eat('+'.code) -> x + parseTerm()
                        eat('-'.code) -> x - parseTerm()
                        else -> return x
                    }
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    x = when {
                        eat('*'.code) -> x * parseFactor()
                        eat('/'.code) -> x / parseFactor()
                        else -> return x
                    }
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.code)) return parseFactor()
                if (eat('-'.code)) return -parseFactor()

                var x: Double
                val startPos = pos
                if (eat('('.code)) {
                    x = parseExpression()
                    eat(')'.code)
                } else if (ch in '0'.code..'9'.code || ch == '.'.code) {
                    while (ch in '0'.code..'9'.code || ch == '.'.code) nextChar()
                    x = expr.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected character: ${ch.toChar()}")
                }

                return x
            }
        }.parse()
    }
}
