package com.codekatana.passwordgen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import java.util.Random
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity() {
    private var sb: SeekBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sb = findViewById(R.id.seekNumWords)
        val numWords = findViewById<TextView>(R.id.lblTxtNumWords)
        sb!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                numWords.text = "$i"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }


    fun onClickGenerate(view: View) {
        val rand = Random()
        val task = WikiDownloader().execute(sb!!.progress)

        val textBox = findViewById<EditText>(R.id.txtGenerated)
        val chkUppercase = findViewById<CheckBox>(R.id.chkUpperCase)
        val chkNumbers = findViewById<CheckBox>(R.id.chkNumbers)
        val chkSymbols = findViewById<CheckBox>(R.id.chkSymbols)

        val hasNumber = chkNumbers.isChecked
        val hasSymbols = chkSymbols.isChecked
        val hasUppercase = chkUppercase.isChecked

        var generated = ""
        try {
            val result = task.get()
            for (word in result) {
                generated = if (hasUppercase) {
                    generated + word.substring(0, 1).toUpperCase() + word.substring(1)
                } else {
                    generated + word
                }
            }

            if (hasNumber) {
                generated = generated.replaceFirst("[oO]".toRegex(), "0")
                generated = generated.replaceFirst("[eE]".toRegex(), "3")
            }

            if (hasSymbols) {
                val powerBallNumber = rand.nextInt(MAX_RAND)
                generated = if (powerBallNumber % 2 == 0)
                    generated.replaceFirst("[sS]".toRegex(), "\\$")
                else
                    generated.replaceFirst("[iI]".toRegex(), "!")
                if (powerBallNumber % 3 == 0)
                    generated = generated.replaceFirst("[aA]".toRegex(), "@")
            }

            textBox.setText(generated)
            val numChars = findViewById<TextView>(R.id.lblNumChars)
            numChars.text = getString(R.string.txt_NumChars, generated.length)

        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: ExecutionException) {
            e.printStackTrace()
        }

    }

    fun onClickCopy(view: View) {
        val textBox = findViewById<EditText>(R.id.txtGenerated)
        val generated = textBox.text.toString()
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Generated Password", generated)
        clipboard.primaryClip = clip

        val context = applicationContext
        val text = resources.getText(R.string.txt_copied)
        val duration = Toast.LENGTH_LONG

        Toast.makeText(context, text, duration).show()
    }

    companion object {
        const val MAX_RAND = 15
    }

}
