package com.ltl.mpmplab2_2;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String RECORD_FILE_NAME = "record.txt";
    private final static Long ANIMATION_DURATION_MILLIS = 1000L;
    private final static Long GAME_DURATION_MILLIS = 60000L;

    private final FileHandler fileHandler = new FileHandler(RECORD_FILE_NAME, this);

    private Button yesButton, noButton, startButton;
    private TextView leftText, rightText, pointsText, rulesText, timerText, recordText;
    private String[] colorNames;
    private int[] colors;
    private final HashMap<String, Integer> colorsMap = new HashMap<>();
    private Integer points = 0;

    private CountDownTimer timer;
    boolean isStared = false;

    // TODO: generator may be part of different class?
    Random generator = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        yesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClick(AnswerOptions.YES);
            }
        });

        noButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleClick(AnswerOptions.NO);
            }
        });

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    startGame(GAME_DURATION_MILLIS);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void init(){
        yesButton = findViewById(R.id.yesButton);
        noButton = findViewById(R.id.noButton);
        startButton =findViewById(R.id.startButton);

        leftText = findViewById(R.id.leftTextView);
        rightText = findViewById(R.id.rightTextView);
        pointsText = findViewById(R.id.pointsTextView);
        rulesText = findViewById(R.id.rulesTextView);
        timerText = findViewById(R.id.timerTextView);
        recordText = findViewById(R.id.recordTextView);

        Integer previousRecord = fileHandler.loadRecord();
        String text = getText(R.string.record_text) + " " + previousRecord;

        recordText.setText(text);

        colorNames = getResources().getStringArray(R.array.color_names_array);
        colors = getResources().getIntArray(R.array.game_colors_array);
        if (colorNames.length != colors.length) {
            throw new IllegalArgumentException(
                    "The number of keys doesn't match the number of values.");
        }
        for (int i = 0; i < colorNames.length; i++){
            colorsMap.put(colorNames[i], colors[i]);
        }

        shuffle();
    }

    private void startGame(Long timeMillis) throws FileNotFoundException {
        if (isStared){
            timer.cancel();
            timer.onFinish();
            isStared = false;
            return;
        }
        isStared = true;
        points = 0;
        pointsText.setText(String.format(getString(R.string.current_points_text), points));
        startOpeningAnimations();

        timer = new CountDownTimer(timeMillis + ANIMATION_DURATION_MILLIS, 1) {
            @Override
            public void onTick(long l) {
                timerText.setText(String.valueOf(l / 1000));
            }
            @Override
            public void onFinish() {
                isStared = false;
                timerText.setText(R.string.finished_text);
                AlertDialog alertDialog = createAlertDialog();
                alertDialog.show();
                startEndingAnimations();
            }
        }.start();
    }

    private void handleClick(AnswerOptions answerOptions) {
        checkAnswer(answerOptions);
        shuffle();
    }

    private void checkAnswer(AnswerOptions answer){
        int expectedColor = colorsMap.get(leftText.getText());

        if (expectedColor == rightText.getCurrentTextColor() && answer == AnswerOptions.YES){
            points++;
            pointsText.setText(String.format(getString(R.string.current_points_text), points));
        } else if (expectedColor != rightText.getCurrentTextColor() && answer == AnswerOptions.NO){
            points++;
            pointsText.setText(String.format(getString(R.string.current_points_text), points));
        } else {
            if (points > 0) points--;
            pointsText.setText(String.format(getString(R.string.current_points_text), points));
        }
    }

    private void shuffle(){
        int randomTextIndex = generator.nextInt(colors.length);
        int randomColorIndex = generator.nextInt(colorNames.length);
        leftText.setText(colorNames[randomTextIndex]);
        leftText.setTextColor(colors[randomColorIndex]);

        randomTextIndex = generator.nextInt(colors.length);
        randomColorIndex = generator.nextInt(colorNames.length);
        rightText.setText(colorNames[randomTextIndex]);
        rightText.setTextColor(colors[randomColorIndex]);
    }

    private void startOpeningAnimations(){
        Animation moveUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.move_upwards_disappear);
        Animation moveDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.move_downwards_disappear);
        rulesText.startAnimation(moveUp);
//        startButton.startAnimation(moveDown);
        startButton.setText(getString(R.string.stop_button_text));

        Animation reverseMoveDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.reverse_move_downwards_disappear);
        yesButton.startAnimation(reverseMoveDown);
        noButton.startAnimation(reverseMoveDown);
    }

    private void startEndingAnimations(){
        Animation reverseMoveUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.reverse_move_upwards_disappear);
        Animation reverseMoveDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.reverse_move_downwards_disappear);
        rulesText.startAnimation(reverseMoveUp);
//        startButton.startAnimation(reverseMoveDown);
        startButton.setText(getString(R.string.start_button_text));


        Animation moveDown = AnimationUtils.loadAnimation(MainActivity.this, R.anim.move_downwards_disappear);
        yesButton.startAnimation(moveDown);
        noButton.startAnimation(moveDown);
    }

    private AlertDialog createAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getText(R.string.finished_text))
                .setCancelable(true)
                .setMessage(getResources().getQuantityString(R.plurals.point_plurals, points, points))
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        Integer previousRecord = fileHandler.loadRecord();
        if (previousRecord < points){
            fileHandler.saveRecord(points);
            builder.setMessage(getText(R.string.new_record_text));
            String text = getText(R.string.record_text) + " " +  points;
            recordText.setText(text);
        }
        return builder.create();
    }

//    private void saveRecord(){
//        FileOutputStream fileOutputStream = null;
//        try {
//            fileOutputStream = openFileOutput(RECORD_FILE_NAME, MODE_PRIVATE);
//            fileOutputStream.write(points.toString().getBytes());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (fileOutputStream != null) {
//                try {
//                    fileOutputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }
//
//    private Integer loadRecord(){
//        FileInputStream fileInputStream = null;
//        String record = "";
//        try {
//            fileInputStream = openFileInput(RECORD_FILE_NAME);
//            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//            record = bufferedReader.readLine();
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (fileInputStream != null){
//                try {
//                    fileInputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//        if (record.equals("")){
//            return 0;
//        }
//        return Integer.parseInt(record);
//    }
}