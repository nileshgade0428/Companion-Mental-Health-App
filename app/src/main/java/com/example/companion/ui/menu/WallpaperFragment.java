package com.example.companion.ui.menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.companion.R;
import com.example.companion.ui.home.HomeActivity;
import com.example.companion.ui.menu.image;

import java.util.ArrayList;

public class WallpaperFragment extends Fragment {

    private ArrayList<com.example.companion.ui.menu.image> image = new ArrayList<>();
    private static final int NUM_COLUMNS = 2;
    public static int theme = -1;
    Button d;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.activity_wallpaper, container, false);
        d = root.findViewById(R.id.def);
        d.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                theme = -1;
                Toast.makeText(getContext(), "Default Wallpaper Set", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getContext(), HomeActivity.class);
                startActivity(i);
            }
        });
        return root;
    }
}



