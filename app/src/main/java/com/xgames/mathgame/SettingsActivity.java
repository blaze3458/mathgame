package com.xgames.mathgame;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{

    Settings settings;
    Switch sAddSwitch,sSubSwitch,sMulSwitch,sDivSwitch;
    SeekBar timeSeekBar;
    EditText eMinEditText,eMaxEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settings = new Settings(this);

        initializeElements();
    }

    private void initializeElements(){
        sAddSwitch = findViewById(R.id.addSwitch);
        sSubSwitch = findViewById(R.id.subSwitch);
        sMulSwitch = findViewById(R.id.mulSwitch);
        sDivSwitch = findViewById(R.id.divSwitch);

        eMinEditText = findViewById(R.id.minValEditText);
        eMaxEditText = findViewById(R.id.maxValEditText);

        timeSeekBar = findViewById(R.id.timeSeekBar);
        timeSeekBar.setMax(120);
        setSeekbarListener();

        findViewById(R.id.applyChangesButton).setOnClickListener(this);
        findViewById(R.id.restoreButton).setOnClickListener(this);

        setDefaultSettings();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();

        if(i == R.id.applyChangesButton){
            applyChanges();
        }else if(i == R.id.restoreButton){
            restoreSettings();
        }
    }

    private void setDefaultSettings(){
        sAddSwitch.setChecked(settings.getAddition());
        sSubSwitch.setChecked(settings.getSubtraction());
        sMulSwitch.setChecked(settings.getMultiplication());
        sDivSwitch.setChecked(settings.getDivision());

        eMinEditText.setText(String.valueOf(settings.getMinValue()));
        eMaxEditText.setText(String.valueOf(settings.getMaxValue()));

        timeSeekBar.setProgress(settings.getTime());
    }

    private void applyChanges(){
        String min = eMinEditText.getText().toString();
        String max = eMaxEditText.getText().toString();

        if(!validMaxMinValue(min,max))
            return;

        settings.setAddition(sAddSwitch.isChecked());
        settings.setSubtraction(sSubSwitch.isChecked());
        settings.setMultiplication(sMulSwitch.isChecked());
        settings.setDivision(sDivSwitch.isChecked());
        settings.setTime(timeSeekBar.getProgress());
        settings.setMinValue(Integer.parseInt(min));
        settings.setMaxValue(Integer.parseInt(max));
        settings.setApplyChanges(true);
        settings.updateData();
        Toast.makeText(this,"The changes were applied.",Toast.LENGTH_SHORT).show();
    }

    private void restoreSettings(){
        settings.setDefaultData();
        setDefaultSettings();
    }

    private boolean validMaxMinValue(String min,String max){
        if(min.isEmpty() || max.isEmpty()){
            Toast.makeText(this,"You cannot leave it blank the Max or Min values...",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(Integer.parseInt(min) >= Integer.parseInt(max)){
            Toast.makeText(this,"You cannot write the minimum value bigger than maximum value...",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(Integer.parseInt(min) > 9999 || Integer.parseInt(max) > 9999){
            Toast.makeText(this,"You cannot write the minimum or maximum value bigger than 9999.",Toast.LENGTH_SHORT).show();
            return false;
        }

        if(Integer.parseInt(min) <= 0 || Integer.parseInt(max) <= 0){
            Toast.makeText(this,"You cannot write the minimum or minimum value equal or smaller than 0.",Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void setSeekbarListener() {
        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int min = 30;
                int max = 120;
                if (progress < min) {
                    seekBar.setProgress(min);
                }
                if(progress > max)
                    seekBar.setProgress(max);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
