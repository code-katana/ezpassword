package com.codekatana.passwordgen;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {


    private SeekBar sb = null;
    OkHttpClient client = new OkHttpClient();

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


    public void onGenerateClick(View v) {
        EditText textBox = findViewById(R.id.txtPassword);
        String password = getRandomWord(sb.getProgress());
        textBox.setText(password);
    }

    private String getRandomWord(int count) {
        StringBuilder sb = new StringBuilder();
        Random rand = new Random();
        List<String> words = getRandomParagraph();
        int numWords = words.size();
        for (int i = 0; i < count; i++) {
            String n = words.get(rand.nextInt(numWords));
            sb.append(n.trim());
        }
        return sb.toString();
    }

    private List<String> getRandomParagraph() {
        List<String> out = new ArrayList<>();
        Request getReq = new Request.Builder()
                .url("https://en.wikipedia.org/w/api.php")
                .addHeader("User-Agent", "Password Generator 1.0")
                .post(RequestBody.create(MediaType.parse("application/json"), "{\n" +
                        "\t\"action\": \"query\",\n" +
                        "\t\"format\": \"json\",\n" +
                        "\t\"list\": \"random\",\n" +
                        "\t\"rnlimit\": \"5\"\n" +
                        "}"))
                .build();
        try (Response response = client.newCall(getReq).execute()) {
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            out.addAll(Arrays.asList(response.body().string().split(" ")));
        } catch (IOException e) {

            e.printStackTrace();
        }
        return out;
    }

}
