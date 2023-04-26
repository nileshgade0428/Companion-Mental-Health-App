package com.example.companion.ui.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.example.companion.R;
//import com.example.companion.ui.adhd.adhdActivity;
import com.example.companion.ui.anxiety.anxietyActivity;
//import com.example.companion.ui.bipolar.bipolarActivity;
import com.example.companion.ui.depression.depressionActivity;
//import com.example.companion.ui.ocd.ocdActivity;
import com.example.companion.ui.ptsd.ptsdActivity;
import com.example.companion.ui.home.MainActivity;

public class QuizActivity extends MainActivity {

    Button anxiety;
    Button depression;
    Button ptsd;
    LinearLayout linearLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentFrameLayout = findViewById(R.id.frag_container);
        getLayoutInflater().inflate(R.layout.activity_quiz, contentFrameLayout);

        linearLayout = findViewById(R.id.quiz_layout);
        linearLayout.getBackground().setAlpha(80);

        anxiety = findViewById(R.id.button_anxiety);
        anxiety.getBackground().setAlpha(180);

        depression = findViewById(R.id.button_depression);
        depression.getBackground().setAlpha(180);

        ptsd = findViewById(R.id.button_ptsd);
        ptsd.getBackground().setAlpha(180);
    }
    public void anxiety_test(View view){
        Intent i_anxiety = new Intent(this, anxietyActivity.class);
        startActivity(i_anxiety);
    }

    public void depression_test(View view){
        Intent i_dep = new Intent(this, depressionActivity.class);
        startActivity(i_dep);
    }

    public void ptsd_test(View view){
        Intent i_ptsd = new Intent(this, ptsdActivity.class);
        startActivity(i_ptsd);
    }

}
