package com.codekatana.passwordgen

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity(), CoroutineScope by CoroutineScope(Dispatchers.Default) {

    private fun updateUI(wc: Int): String {
        val rand = Random()
        val hasNumber = findViewById<CheckBox>(R.id.chkNumbers).isChecked
        val hasSymbols = findViewById<CheckBox>(R.id.chkSymbols).isChecked
        val hasUppercase = findViewById<CheckBox>(R.id.chkUpperCase).isChecked
        val words = EzPassApplication.wordBank.getWords(wc).toMutableList()
        launch { EzPassApplication.wordProcessor.getWords() }
        var generated = ""
        val nextRand = rand.nextInt(words.size)
        if (hasUppercase) {
            var word = words[nextRand]
            word = word.substring(0, 1).toUpperCase(Locale.ENGLISH) + word.substring(1)
            words[nextRand] = word
        }
        generated = words.joinToString(" ")

        if (hasNumber) {
            generated = WordProcessor.sprinkleNumbers(generated)
        }

        if (hasSymbols) {
            generated = WordProcessor.specialize(generated)
        }
        return generated
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val sb = findViewById<SeekBar>(R.id.seekNumWords)

        sb.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                findViewById<TextView>(R.id.lblTxtNumWords).text = "${i + 2}"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        launch {
            EzPassApplication.wordProcessor.getWords()
        }

    }

    fun onClickGenerate(view: View) {
        val numWordsNeeded = findViewById<SeekBar>(R.id.seekNumWords).progress + 2
        launch {
            val generated = async { updateUI(numWordsNeeded) }
            val textBox = findViewById<EditText>(R.id.txtGenerated)
            val numChars = findViewById<TextView>(R.id.lblNumChars)
            val generatedVal = generated.await()
            val length = generatedVal.filterNot { ' ' == it }.length
            runOnUiThread {
                textBox.setText(generatedVal)
                numChars.text = getString(R.string.txt_NumChars, length)
            }
        }
    }

    fun onClickCopy(view: View) {
        val textBox = findViewById<EditText>(R.id.txtGenerated)
        val generated = textBox.text.toString().filterNot { it == ' ' }
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Generated Password", generated)
        clipboard.primaryClip = clip

        val context = applicationContext
        val text = getString(R.string.txt_copied)
        val duration = Toast.LENGTH_LONG

        Toast.makeText(context, text, duration).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    companion object {
        const val MAX_RAND = 15
    }

}
