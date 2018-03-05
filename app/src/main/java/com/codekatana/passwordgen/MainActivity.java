package com.codekatana.passwordgen;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {


    public static final int MAX_RAND = 15;
    private SeekBar sb = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sb = findViewById(R.id.seekNumWords);
        final TextView numWords = findViewById(R.id.lblTxtNumWords);
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                numWords.setText(Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }


    public void onClickGenerate(View v) {
        Random rand = new Random();
        AsyncTask<Integer, Integer, List<String>> task = new WikiDownloader().execute(sb.getProgress());

        EditText textBox = findViewById(R.id.txtGenerated);
        CheckBox chkUppercase = findViewById(R.id.chkUpperCase);
        CheckBox chkNumbers = findViewById(R.id.chkNumbers);
        CheckBox chkSymbols = findViewById(R.id.chkSymbols);

        boolean hasNumber = chkNumbers.isChecked();
        boolean hasSymbols = chkSymbols.isChecked();
        boolean hasUppercase = chkUppercase.isChecked();

        String generated = "";
        try {
            List<String> result = task.get();
            for (String word : result) {
                if (hasUppercase) {
                    generated = generated + word.substring(0, 1).toUpperCase() + word.substring(1);
                } else {
                    generated = generated + word;
                }
            }

            if (hasNumber) {
                generated = generated.replaceFirst("[oO]", "0");
                generated = generated.replaceFirst("[eE]", "3");
            }

            if (hasSymbols) {
                int powerBallNumber = rand.nextInt(MAX_RAND);
                if (powerBallNumber % 2 == 0)
                    generated = generated.replaceFirst("[sS]", "\\$");
                if (powerBallNumber % 3 == 0)
                    generated = generated.replaceFirst("[iI]", "!");
                if (powerBallNumber % 4 == 0)
                    generated = generated.replaceFirst("[aA]", "@");
            }

            textBox.setText(generated);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}
