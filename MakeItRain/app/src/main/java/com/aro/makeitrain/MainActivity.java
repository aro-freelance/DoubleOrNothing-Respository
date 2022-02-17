package com.aro.makeitrain;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;

import java.text.NumberFormat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private TextView moneyAmountTextView;
    private ImageView diceImage;

    private TextView textViewDialog;
    private Button closeDialogButton;
    private Button doubleOrNothingButton;
    private TextView getMoneyAmountTextViewDialog;
    private ImageView dialogDiceImage;
    private TextView dnExplanation;

    private TextView endScreenText;
    private TextView gameOverHeader;
    private TextView newHighScoreHeader;
    private EditText highScoreEditText;
    private Button enterHighScoreButton;

    private int whichButton = 999;
    private int rollResult = 0;
    private boolean isWin = false;
    private int entryCost = 0;
    private int payout = 0;

    private int numberOfBets = 0;
    private int highestMoney = 0;


    private int highScore = 0;
    private String highScoreName = "";


    private int moneyCounter = 1000;
    private int doubleNothing = 0;

    private final int waitTimeBetweenDiceRollAndDialog = 600; //in milliseconds. note that the animations are 300ms

    NumberFormat numberFormat = NumberFormat.getCurrencyInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        moneyAmountTextView = findViewById(R.id.moneyamount_textview);
        diceImage = findViewById(R.id.dice_image);
        Button button1 = findViewById(R.id.button_1);
        Button button2 = findViewById(R.id.button_2);
        Button button3 = findViewById(R.id.button_3);
        Button button4 = findViewById(R.id.button_4);
        Button button5 = findViewById(R.id.button_5);
        Button button6 = findViewById(R.id.button_6);
        AdView adView = findViewById(R.id.ad_view);


        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        showMoney();


        button1.setOnClickListener(this::button1Method);
        button2.setOnClickListener(this::button2Method);
        button3.setOnClickListener(this::button3Method);
        button4.setOnClickListener(this::button4Method);
        button5.setOnClickListener(this::button5Method);
        button6.setOnClickListener(this::button6Method);



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){

            case R.id.new_game_menu_item:
                gameOver();
                return true;
            case R.id.rule_menu_item:
                showRulesDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void checkIfHighestMoney(){

        SharedPreferences getData = getSharedPreferences("aro_double_or_nothing", MODE_PRIVATE);
        SharedPreferences sharedPreferences = getSharedPreferences("aro_double_or_nothing", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        //get the highest money on current playthrough
        highestMoney = getData.getInt("highest_money_this_game", 1000);

        //get the high score, all time
        highScore = getData.getInt("high_score", 1000);

        //compare current money to highest. save new high if higher.
        if(moneyCounter > highestMoney){
            editor.putInt("highest_money_this_game", moneyCounter);
            editor.apply();
            highestMoney = moneyCounter;
            if(highestMoney > highScore){
                editor.putInt("high_score", highestMoney);
                editor.apply();
            }
        }


    }


    private void showRulesDialog(){
        BottomSheetDialog rulesDialog = new BottomSheetDialog(this);
        rulesDialog.setContentView(R.layout.rules_dialog);
        rulesDialog.show();
    }


    @Override
    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getSharedPreferences("aro_double_or_nothing", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("money_counter", moneyCounter);
        editor.putInt("number_of_bets", numberOfBets );
        editor.apply();

        checkIfHighestMoney();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences getData = getSharedPreferences("aro_double_or_nothing", MODE_PRIVATE);

        //load the money
        moneyCounter = getData.getInt("money_counter", 1000);
        if(moneyCounter == 0){
            moneyCounter = 1000;
        }
        showMoney();

        //load the turns
        numberOfBets = getData.getInt("number_of_bets", numberOfBets);

        //load the highest money in this playthrough
        checkIfHighestMoney();

    }

    //this handles the logic of winning
    private void checkWin(){

        //even
        if(whichButton == 1){
            //todo fix payouts to be percent of current money?
            payout = 200;
            if(rollResult == 2 || rollResult == 4 || rollResult == 6){
                isWin = true;
            }
        }
        //odd
        else if (whichButton == 2){
            payout = 200;
            if(rollResult == 1 || rollResult == 3 || rollResult == 5){
                isWin = true;
            }
        }
        //low
        else if (whichButton == 3){
            payout = 1200;
            if(rollResult == 1 || rollResult == 2 || rollResult == 3){
                isWin = true;
            }
        }
        //high
        else if (whichButton == 4){
            payout = 1200;
            if(rollResult == 4 || rollResult == 5 || rollResult == 6){
                isWin = true;
            }

        }
        //one
        else if (whichButton == 5){
            //payout set with entry in the button method
            if(rollResult == 1){
                isWin = true;
            }

        }
        //six
        else if (whichButton == 6){
            //payout set with entry in the button method
            if(rollResult == 6){
                isWin = true;
            }

        }
        //error
        else{
            Toast.makeText(this, "You pushed a button that shouldn't exist! This is unexpected!", Toast.LENGTH_SHORT).show();

        }

    }

    //this opens the bottom sheet and handles what happens on it. The bottom sheet is for handling the results after the roll.
    //which will include the double or nothing game
    private void showBottomSheet(){
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        bottomSheetDialog.setContentView(R.layout.fragment_results);
        //get any views from the bottom sheet that we need
        textViewDialog = bottomSheetDialog.findViewById(R.id.textView3);
        closeDialogButton = bottomSheetDialog.findViewById(R.id.close_dialog_button);
        getMoneyAmountTextViewDialog = bottomSheetDialog.findViewById(R.id.moneyamount_textview_dialog);
        doubleOrNothingButton = bottomSheetDialog.findViewById(R.id.double_or_nothing_button);
        dialogDiceImage = bottomSheetDialog.findViewById(R.id.dialog_dice_image);
        dnExplanation = bottomSheetDialog.findViewById(R.id.double_or_nothing_explanation_text);

        //hide the buttons
        doubleOrNothingButton.setVisibility(View.INVISIBLE);
        closeDialogButton.setVisibility(View.INVISIBLE);
        dnExplanation.setVisibility(View.GONE);
        dialogDiceImage.setVisibility(View.GONE);
        closeDialogButton.setText(R.string.stay_button_string);


        //if the roll is a loss
        if(!isWin){
            textViewDialog.setText(String.format("The roll was %s. You lose...", rollResult));
            closeDialogButton.setVisibility(View.VISIBLE);
            closeDialogButton.setText(R.string.you_lose_button_text_string);

            if(moneyCounter <= 0) {
                Handler mHandler = new Handler();
                mHandler.postDelayed(() -> {

                    bottomSheetDialog.hide();

                    gameOver();

                }, 200);

            }

        }
        //if the roll is a win
        else {
            textViewDialog.setText(String.format("The roll was %s. You win!", rollResult));
            moneyCounter = moneyCounter + payout;
            checkIfHighestMoney();
            closeDialogButton.setVisibility(View.VISIBLE);
            doubleOrNothingButton.setVisibility(View.VISIBLE);
            dnExplanation.setVisibility(View.VISIBLE);

        }

        showMoney();
        String moneyCounterString = numberFormat.format(moneyCounter);
        getMoneyAmountTextViewDialog.setText(moneyCounterString);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            bottomSheetDialog.getBehavior().setPeekHeight(500);
        }


        bottomSheetDialog.show();

        //play double or nothing
        doubleOrNothingButton.setOnClickListener(view ->{
            //hide the buttons
            doubleOrNothingButton.setVisibility(View.INVISIBLE);
            closeDialogButton.setVisibility(View.INVISIBLE);
            dnExplanation.setVisibility(View.INVISIBLE);

            //show the dice and roll it
            dialogDiceImage.setVisibility(View.VISIBLE);

            Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);
            shake.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //dice sound
                    Random d6 = new Random();
                    doubleNothing = d6.nextInt(6) + 1;

                    if(doubleNothing == 1){
                        dialogDiceImage.setBackgroundResource(R.drawable.dice_one);
                    }
                    else if (doubleNothing == 2){
                        dialogDiceImage.setBackgroundResource(R.drawable.dice_two);
                    }
                    else if (doubleNothing == 3){
                        dialogDiceImage.setBackgroundResource(R.drawable.dice_three);
                    }
                    else if (doubleNothing == 4){
                        dialogDiceImage.setBackgroundResource(R.drawable.dice_four);
                    }
                    else if (doubleNothing == 5){
                        dialogDiceImage.setBackgroundResource(R.drawable.dice_five);
                    }
                    else{
                        dialogDiceImage.setBackgroundResource(R.drawable.dice_six);
                    }


                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //wait x time and then show the result of the roll
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(() -> {

                        //win the double or nothing
                        if(doubleNothing >= 4){
                            int totalPayout = payout + payout;
                            textViewDialog.setText("It is a "+ doubleNothing+ ". " +
                                    "You doubled down and you won! Congratulations! Your total winnings are "
                                    + totalPayout);
                            //the first payout is resolved when the dialog sheet opens
                            moneyCounter = moneyCounter + payout;
                            checkIfHighestMoney();
                            showMoney();
                            String moneyCounterString1 = numberFormat.format(moneyCounter);
                            getMoneyAmountTextViewDialog.setText(moneyCounterString1);

                            //show button to leave
                            closeDialogButton.setText(R.string.you_win_button_string);
                            closeDialogButton.setVisibility(View.VISIBLE);

                        }
                        //lose the double or nothing
                        else{
                            textViewDialog.setText("It is a "+ doubleNothing+ ". Bad Luck! You lost your winnings...");
                            moneyCounter = moneyCounter - payout;
                            showMoney();
                            String moneyCounterString1 = numberFormat.format(moneyCounter);
                            getMoneyAmountTextViewDialog.setText(moneyCounterString1);

                            if(moneyCounter <= 0){
                                Handler mHandler1 = new Handler();
                                mHandler1.postDelayed(() -> {

                                    bottomSheetDialog.hide();

                                    gameOver();

                                }, 200);
                            }
                            else{
                                //show button to leave
                                closeDialogButton.setText(R.string.you_lose_button_text_string);
                                closeDialogButton.setVisibility(View.VISIBLE);
                            }



                        }

                    }, 400);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //don't repeat
                }
            });

            dialogDiceImage.startAnimation(shake);

        });


        //if the dialog is closed
        bottomSheetDialog.setOnCancelListener(dialogInterface -> {
            endRoll();
            bottomSheetDialog.dismiss();
        });

        //if the dialog is closed
        bottomSheetDialog.setOnDismissListener(dialogInterface -> endRoll());


        //if the button is clicked to close the dialog
        closeDialogButton.setOnClickListener(view -> {
          endRoll();
          bottomSheetDialog.dismiss();
        });



    }

    //this ends a roll so a new one can be started with a clean slate
    private void endRoll(){
        //reset all game variables
        doubleNothing = 0;
        rollResult = 0;
        whichButton = 999;
        isWin = false;
        entryCost = 0;
        payout = 0;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

    }

    //this handles the animation of the diceroll and then opens the bottomsheetdialog afterwards
    private void shakeAnimation(){
        Random random = new Random();
        int randomAnimation = random.nextInt(4) + 1;
        if(randomAnimation == 1){
            Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);
            shake.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //set initial image for suspense
                    rollDice();
                    rollDice();
                    rollDice();
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //get actual roll result
                    rollDice();

                    //wait x time and then show the bottom sheet
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(() -> {
                        checkWin();
                        showBottomSheet();
                    }, waitTimeBetweenDiceRollAndDialog);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //don't repeat
                }
            });
            diceImage.startAnimation(shake);
        }
        else if (randomAnimation == 2){
            Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation_2);
            shake.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //set initial image for suspense
                    rollDice();
                    rollDice();
                    rollDice();
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //get actual roll result
                    rollDice();

                    //wait x time and then show the bottom sheet
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(() -> {
                        checkWin();
                        showBottomSheet();
                    }, waitTimeBetweenDiceRollAndDialog);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //don't repeat
                }
            });
            diceImage.startAnimation(shake);
        }
        else if (randomAnimation == 3){
            Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation_3);
            shake.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //set initial image for suspense
                    rollDice();
                    rollDice();
                    rollDice();
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //get actual roll result
                    rollDice();

                    //wait x time and then show the bottom sheet
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(() -> {
                        checkWin();
                        showBottomSheet();
                    }, waitTimeBetweenDiceRollAndDialog);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //don't repeat
                }
            });
            diceImage.startAnimation(shake);
        }
        else{
            Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation_4);
            shake.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    //set initial image for suspense
                    rollDice();
                    rollDice();
                    rollDice();
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    //get actual roll result
                    rollDice();

                    //wait x time and then show the bottom sheet
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(() -> {
                        checkWin();
                        showBottomSheet();
                    }, waitTimeBetweenDiceRollAndDialog);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    //don't repeat
                }
            });
            diceImage.startAnimation(shake);
        }
    }

    //this sets a random number and sets the image
    private void rollDice(){
        //this is to make the roll more interesting
        Random d6 = new Random();
       rollResult = d6.nextInt(6) + 1;

        if(rollResult == 1){
            diceImage.setBackgroundResource(R.drawable.dice_one);
        }
        else if (rollResult == 2){
            diceImage.setBackgroundResource(R.drawable.dice_two);
        }
        else if (rollResult == 3){
            diceImage.setBackgroundResource(R.drawable.dice_three);
        }
        else if (rollResult == 4){
            diceImage.setBackgroundResource(R.drawable.dice_four);
        }
        else if (rollResult == 5){
            diceImage.setBackgroundResource(R.drawable.dice_five);
        }
        else{
            diceImage.setBackgroundResource(R.drawable.dice_six);
        }

    }


    //this starts the dice roll and handles anything else we need to do when the button is pressed
    private void getResult(){

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        numberOfBets = numberOfBets +1;

        //this handles both the dice animation and the roll result number by calling rollDice
        shakeAnimation();



    }

    //six
    private void button6Method(View view) {
        //todo six


        //todo set entry costs to be percent of money or something?
        entryCost = (int) (moneyCounter * .20);
        if(moneyCounter < 100){ entryCost = moneyCounter;}
        payout = entryCost * 3;
        if(moneyCounter >= entryCost){
            moneyCounter = moneyCounter - entryCost;
            showMoney();
            whichButton = 6;
            getResult();

        }
        else{
            Toast.makeText(this, "Not enough money to enter", Toast.LENGTH_SHORT)
                    .show();
        }


        //todo compare result to win con and set variables for win/lose and payout
        //todo then open the fragment to show the result and play double or nothing
    }
    //one
    private void button5Method(View view) {
        //todo one


        //todo set entry costs to be percent of money or something?
        entryCost = (int) (moneyCounter * .20);
        if(moneyCounter < 100){ entryCost = moneyCounter;}
        payout = entryCost * 3;
        if(moneyCounter >= entryCost){
            moneyCounter = moneyCounter - entryCost;
            showMoney();
            whichButton = 5;
            getResult();

        }
        else{
            Toast.makeText(this, "Not enough money to enter", Toast.LENGTH_SHORT)
                    .show();
        }

        //todo compare result to win con and set variables for win/lose and payout
        //todo then open the fragment to show the result and play double or nothing
    }
    //high
    private void button4Method(View view) {
        //todo high

        //todo set entry costs to be percent of money or something?
        entryCost = 500;
        if(moneyCounter >= entryCost){
            moneyCounter = moneyCounter - entryCost;
            showMoney();
            whichButton = 4;
            getResult();

        }
        else{
            Toast.makeText(this, "Not enough money to enter", Toast.LENGTH_SHORT)
                    .show();
        }

        //todo compare result to win con and set variables for win/lose and payout
        //todo then open the fragment to show the result and play double or nothing
    }
    //low
    private void button3Method(View view) {
        //todo low

        //todo set entry costs to be percent of money or something?
        entryCost = 500;
        if(moneyCounter >= entryCost){
            moneyCounter = moneyCounter - entryCost;
            showMoney();
            whichButton = 3;
            getResult();

        }
        else{
            Toast.makeText(this, "Not enough money to enter", Toast.LENGTH_SHORT)
                    .show();
        }

        //todo compare result to win con and set variables for win/lose and payout
        //todo then open the fragment to show the result and play double or nothing
    }
    //odd
    private void button2Method(View view) {
        //todo odd

        //todo set entry costs to be percent of money or something?
        entryCost = 100;
        if(moneyCounter >= entryCost){
            moneyCounter = moneyCounter - entryCost;
            showMoney();
            whichButton = 2;
            getResult();

        }
        else{
            Toast.makeText(this, "Not enough money to enter", Toast.LENGTH_SHORT)
                    .show();
        }

        //todo compare result to win con and set variables for win/lose and payout
        //todo then open the fragment to show the result and play double or nothing

    }
    //even
    private void button1Method(View view) {
        //todo even

        //todo set entry costs to be percent of money or something?
        entryCost = 100;
        if(moneyCounter >= entryCost){
            moneyCounter = moneyCounter - entryCost;
            showMoney();
            whichButton = 1;
            getResult();

        }
        else{
            Toast.makeText(this, "Not enough money to enter", Toast.LENGTH_SHORT)
                    .show();
        }



        //todo compare result to win con and set variables for win/lose and payout
        //todo then open the fragment to show the result and play double or nothing

    }

    public void gameOver(){


        BottomSheetDialog gameOverDialog = new BottomSheetDialog(this);
        gameOverDialog.setContentView(R.layout.game_over_dialog);

        SharedPreferences sharedPreferences = getSharedPreferences("aro_double_or_nothing", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        SharedPreferences getData = getSharedPreferences("aro_double_or_nothing", MODE_PRIVATE);

        highScoreEditText = gameOverDialog.findViewById(R.id.high_score_name_edittext);
        enterHighScoreButton = gameOverDialog.findViewById(R.id.enter_highscore_button);
        gameOverHeader = gameOverDialog.findViewById(R.id.game_over_header);
        endScreenText = gameOverDialog.findViewById(R.id.game_over_text);
        newHighScoreHeader = gameOverDialog.findViewById(R.id.new_high_score_header_text);


        assert newHighScoreHeader != null;
        newHighScoreHeader.setVisibility(View.GONE);
        highScoreEditText.setVisibility(View.GONE);
        enterHighScoreButton.setVisibility(View.GONE);


        checkIfHighestMoney();

        if(highestMoney >= highScore){
            //hide other views
            gameOverHeader.setVisibility(View.GONE);
            endScreenText.setVisibility(View.GONE);
            // show views for new high score
            newHighScoreHeader.setVisibility(View.VISIBLE);
            highScoreEditText.setVisibility(View.VISIBLE);
            enterHighScoreButton.setVisibility(View.VISIBLE);

            gameOverDialog.show();

            enterHighScoreButton.setOnClickListener(view -> {

                if(!TextUtils.isEmpty( highScoreEditText.getText().toString().trim())){

                    //get user input for high score name
                    highScoreName = highScoreEditText.getText().toString().trim();

                    //save high score name and high score number of bets. NOTE: High score is already being saved throughout the game so we do not save it again here.
                    editor.putString("high_score_name", highScoreName);
                    editor.putInt("high_score_bets", numberOfBets);

                    editor.apply();

                    //show other views
                    gameOverHeader.setVisibility(View.VISIBLE);
                    endScreenText.setVisibility(View.VISIBLE);
                    // hide views for new high score
                    newHighScoreHeader.setVisibility(View.GONE);
                    highScoreEditText.setVisibility(View.GONE);
                    enterHighScoreButton.setVisibility(View.GONE);

                    String gameOverString = "Highest Money - " + highestMoney + " \n" +
                            "Number of Bets - " + numberOfBets + " \n" +
                            "All Time High Score -  " + highScore + " belongs to " + highScoreName + " with " + numberOfBets + " bets.";


                    //set string to textview

                    endScreenText.setText(gameOverString);

                    gameOverDialog.show();



                }

            });

        }
        else{
            //show other views
            gameOverHeader.setVisibility(View.VISIBLE);
            endScreenText.setVisibility(View.VISIBLE);
            // hide views for new high score
            newHighScoreHeader.setVisibility(View.GONE);
            highScoreEditText.setVisibility(View.GONE);
            enterHighScoreButton.setVisibility(View.GONE);


            highScore = getData.getInt("high_score", 1000);
            highScoreName = getData.getString("high_score_name", "");
            int highScoreBets = getData.getInt("high_score_bets", 0);


            String gameOverString = "Highest Money - " + highestMoney + " \n" +
                    "Number of Bets - " + numberOfBets + " \n" +
                    "All Time High Score -  " + highScore + " belongs to " + highScoreName + " with " + highScoreBets + " bets.";


            //set string to textview

            endScreenText.setText(gameOverString);

            gameOverDialog.show();


        }

        gameOverDialog.setOnDismissListener(dialogInterface -> reset());
        gameOverDialog.setOnCancelListener(dialogInterface -> reset());







    }

    public void reset(){
        SharedPreferences sharedPreferences = getSharedPreferences("aro_double_or_nothing", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("money_counter", 0);
        editor.putInt("highest_money_this_game", 1000);
        editor.putInt("number_of_bets", 0);
        editor.apply();
        moneyCounter = 1000;
        showMoney();

        doubleNothing = 0;
        rollResult = 0;
        whichButton = 999;
        isWin = false;
        entryCost = 0;
        payout = 0;
        numberOfBets = 0;
        highestMoney = 0;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    public void showMoney (){
        String moneyCounterString = numberFormat.format(moneyCounter);
        moneyAmountTextView.setText(moneyCounterString);
        setMoneyTextColor();
        if(moneyCounter >= 20000){
            Snackbar.make(moneyAmountTextView, R.string.congrats, Snackbar.LENGTH_SHORT)
                    .show();
        }


    }

    public void setMoneyTextColor(){
        if(moneyCounter >= 20000){
            moneyAmountTextView.setTextColor(getResources().getColor(R.color.money_green)); //green
        }
        else if(moneyCounter < 0){
            moneyAmountTextView.setTextColor(Color.RED);
        }
        else{
            moneyAmountTextView.setTextColor(getResources().getColor(R.color.player_area_bg));

        }
    }

}
