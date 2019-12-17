package com.codekatana.passwordgen

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.coroutines.CoroutineContext

class MainActivity(override val coroutineContext: CoroutineContext) : AppCompatActivity(), DownloadListener, CoroutineScope {

    override fun updateUI(result: List<String>) {
        val textBox = findViewById<EditText>(R.id.txtGenerated)

        val rand = Random()
        val hasNumber = findViewById<CheckBox>(R.id.chkNumbers).isChecked
        val hasSymbols = findViewById<CheckBox>(R.id.chkSymbols).isChecked
        val hasUppercase = findViewById<CheckBox>(R.id.chkUpperCase).isChecked

        val words = mutableListOf<String>()
        words.addAll(result)

        var generated = ""
        val nextRand = rand.nextInt(words.size)
        if (hasUppercase) {
            var word = result[nextRand]
            word = word.substring(0, 1).toUpperCase() + word.substring(1)
            words[nextRand] = word
        }
        words.forEach { generated += it }

        if (hasNumber) {
            when {
                generated.contains("o") -> generated = generated.replaceFirst("o", "0")
                generated.contains("e") -> generated = generated.replaceFirst("e", "3")
                generated.contains("l") -> generated = generated.replaceFirst("l", "1")
            }
        }

        if (hasSymbols) {
            val powerBallNumber = rand.nextInt(MAX_RAND)
            for (i in 0..(powerBallNumber % 2)) {
                when {
                    generated.contains("s") -> generated = generated.replaceFirst("s", "$")
                    generated.contains("i") -> generated = generated.replaceFirst("i", "!")
                    generated.contains("a") -> generated = generated.replaceFirst("a", "@")
                }
            }
        }

        textBox.setText(generated)
        val numChars = findViewById<TextView>(R.id.lblNumChars)
        numChars.text = getString(R.string.txt_NumChars, generated.length)
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
            WordProcessor(EzPassApplication.instance).apply { getWords() }
        }

    }

    fun onClickGenerate(view: View) {

    }

    fun onClickCopy(view: View) {
        val textBox = findViewById<EditText>(R.id.txtGenerated)
        val generated = textBox.text.toString()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Generated Password", generated)
        clipboard.primaryClip = clip

        val context = applicationContext
        val text = getString(R.string.txt_copied)
        val duration = Toast.LENGTH_LONG

        Toast.makeText(context, text, duration).show()
    }

    companion object {
        const val MAX_RAND = 15
    }

}


interface DownloadListener {
    fun updateUI(result: List<String>)
}