package com.example.flagquiz;

import static android.view.View.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
public class MainActivity extends AppCompatActivity {
	private final int maxButtons = 15;
	private final int numberOfRegions = 6;
	private int maxQuestionNumber = 10;
	private int questionNumber =1;
	private int wrongAns =0;
	private int correctAns =0;
	private int numberOfButtons =3;
	private int currentNumberOfButtons =3;
	boolean chooseAnswer = true;
	boolean changeNumberOfButtons = false;
	boolean ClickedButtons = false;
	String realImageName;

	int [] buttonsIds= new int[maxButtons];
	boolean [] checkedAccessedRegions = new boolean[numberOfRegions]; //6
	String[] accessedRegions = new String[numberOfRegions];
	String [] images;


	ArrayList<Button> guesses = new ArrayList<>(maxButtons);


	TextView title,countryGuessed;
	ImageView flagImage;
	ImageButton nextImage;
	AssetManager assets;
	LinearLayout buttonsLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		title = findViewById(R.id.questionTitle);
		countryGuessed = findViewById(R.id.countryGuessed);
		flagImage = findViewById(R.id.flagImage);
		nextImage = findViewById(R.id.nextImage);
		buttonsLayout = findViewById(R.id.buttonsLayout);

		this.assets = this.getAssets(); //get reference on assets folder
		try{
			this.images = this.assets.list("png");
		}catch (IOException e){
			e.printStackTrace();
		}
		accessedRegions = new String[]{"Africa", "Asia", "Europe", "North_America", "South_America", "Oceania"};//6

		nextImage.setOnClickListener(view -> {
			if(chooseAnswer){
				if(changeNumberOfButtons){
					changeNumberOfButtons();
					changeNumberOfButtons = false;
				}
				ClickedButtons = false;
				resetButtonColors();
				func();
			}
		});

		//generate random ids
		for (int i=0; i<maxButtons; i++) {
			buttonsIds[i] = generateViewId();
		}
		resetCheckAccessedRegions();
		changeNumberOfButtons();
		func();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.my_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(@NonNull MenuItem item) {
		switch (item.getItemId()){
			case (R.id.menu_help):
				showHelp();
				return true;
			case (R.id.menu_new_game):
				newGame("Do You want to play new game ?", true);
				return true;
			case (R.id.submenu_button3):
				changeChoice(3);
				return true;
			case (R.id.submenu_button6):
				changeChoice(6);
				return true;
			case (R.id.submenu_button9):
				changeChoice(9);
				return true;
			case (R.id.submenu_button15):
				changeChoice(15);
				return true;
			case (R.id.menu_regions):
				changeAccessedRegions();
				return true;
			case (R.id.menu_question_number):
				changeQuestionNumber();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void resetCheckAccessedRegions() {
		for (int i =0; i<numberOfRegions; i++)
			checkedAccessedRegions[i] = true;
	}

	private void func(){
		try {
			//images contains all the names of the files in the assets/png directory
			int n = randImageInt();
			changeButtonText();
			Drawable image = Drawable.createFromStream(
					this.assets.open("png/" + this.images[n]),
					this.realImageName
			);

			flagImage.setImageDrawable(image);
		} catch (IOException e) {
			e.printStackTrace();
		}
		toggleButtons(true);
		changeTitle();
		countryGuessed.setText("");
		this.chooseAnswer =false;
	}

	private void changeTitle(){
		title.setText("Question "+ this.questionNumber + " out of "+ maxQuestionNumber);
	}

	private int randImageInt(){
		//this function make sure to take Name of flag in accessed regions
		//so that it will return the number of this image to accessed it in func() method
		String imageRegion;
		String [] img;
		int n;
		do{
			n = (int)(Math.random()*this.images.length);
			img= images[n].split("[-.]");
			imageRegion = img[0];
		}while(!belongToAccessedRegions(imageRegion));
		this.realImageName = img[1];
		return n;
	}
	private String randImageName(){
		// this function convert:
		//Africa-Burundi.png => Burundi
		String imageRegion, imageName;
		do{
			int n = (int)(Math.random()*this.images.length);
			String [] img = images[n].split("[-.]"); // Africa-Burundi.png => ["Africa", "Burundi", "png"]
			imageRegion = img[0];
			imageName = img[1];
		}while(!belongToAccessedRegions(imageRegion));
		return imageName;
	}
	private boolean belongToAccessedRegions(String imageRegion){
		for(int i=0; i< numberOfRegions; i++)
			if(checkedAccessedRegions[i] && imageRegion.equals(accessedRegions[i]))
				return true;
		return false;
	}

	private void changeChoice(int num){
		numberOfButtons =num;
		changeNumberOfButtons = true;
		Toast.makeText(
				this,
				"Your choice will be generated in the next question\n Your Choice is: "+num + " optional Buttons",
				Toast.LENGTH_LONG
		).show();
	}

	private void checkAnswer(View view){
		Button b = ((Button) view);
		if(questionNumber <=maxQuestionNumber){
			String name = b.getText().toString();
			toggleButtons(false);

			if(name.equals(realImageName)){
				countryGuessed.setText("You were correct!");
				changeButtonsColor(b,"green");
				correctAns++;
			}else{
				countryGuessed.setText("Wrong!, You choose the wrong answer :(");
				changeButtonsColor(b, "red");
				for (Button btn : guesses)
					if (btn.getText().toString().equals(realImageName))
						changeButtonsColor(btn,"green");

				wrongAns++;
			}
			ClickedButtons = true;
			questionNumber++;
			chooseAnswer =true;
		}
		if(questionNumber>maxQuestionNumber){
			newGame(wrongAns + " Wrong Answers and "+ correctAns+" correct Answers",false);
		}
	}

	private void changeButtonsColor(Button b, @NonNull String color){
		if(color.equals("red")){
			b.setBackgroundColor(Color.rgb(255,50,50));
			b.setTextColor(Color.rgb(0,0,0));
		}else if(color.equals("green")){
			b.setBackgroundColor(Color.rgb(50,255,50));
			b.setTextColor(Color.rgb(0,0,0));
		}
	}

	private void changeNumberOfButtons() {
		int guessesSize =guesses.size();
		if( guessesSize > numberOfButtons){ //only remove buttons
			for(int counter=guessesSize-1; counter>=numberOfButtons; counter--){
				buttonsLayout.removeViewAt(counter);
				guesses.remove(counter);
			}
		}
		else{//add new buttons where guesses.size() < numberOfButtons
			for (int ctr=guessesSize ;ctr< numberOfButtons; ctr++){
				Button b = new Button(this);
				b.setId(buttonsIds[ctr]);

				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT);
				param.setMargins(15,15,15,0);
				b.setLayoutParams(param);
				b.setBackgroundColor(Color.rgb(30,150,255));
				b.setTextColor(Color.rgb(255,255,255));
				b.setTextSize(18);
				b.setOnClickListener(this::checkAnswer); // this is the button ,it is a solution from the compiler to remove lambda
				guesses.add(b);
				buttonsLayout.addView(b);
			}
		}
		currentNumberOfButtons = numberOfButtons;

	}

	private void changeButtonText(){
		ArrayList<String> ImagesName= new ArrayList<>(currentNumberOfButtons);
		ImagesName.add(realImageName);
		while(ImagesName.size() <currentNumberOfButtons){
			String name = randImageName();
			if(!ImagesName.contains(name)){
				ImagesName.add(name);
			}
		}
		ImagesName.remove(0);
		int n = (int)(Math.random()*currentNumberOfButtons);
		ImagesName.add(n,realImageName);
		fillImagesName(ImagesName);
	}

	private void fillImagesName(@NonNull ArrayList<String> ImagesName){
		for (int i=0; i<ImagesName.size(); i++){
			guesses.get(i).setText(ImagesName.get(i));
		}
	}

	private void toggleButtons(boolean clickable) {
		if(clickable){
			for (int counter=0 ; counter <currentNumberOfButtons;  counter++) {
				guesses.get(counter).setClickable(true);
				guesses.get(counter).setTextColor(Color.rgb(255,255,255));//white
			}
		}else{
			for (int counter=0 ; counter <currentNumberOfButtons;  counter++) {
				guesses.get(counter).setClickable(false);
				guesses.get(counter).setTextColor(Color.rgb(200,150,200)); // gray
			}
		}
	}

	private void resetButtonColors(){
		for( Button btn: guesses)
			btn.setBackgroundColor(Color.rgb(30,150,255));
	}

	private void resetGameValues(){
		questionNumber=1;
		correctAns=0;
		wrongAns=0;
		realImageName="";
		resetButtonColors();
		func();
	}

	// Alert Dialogs

	private void changeQuestionNumber(){
		AlertDialog.Builder QNumDialog = new AlertDialog.Builder(this);
		QNumDialog.setTitle("Change Question Number");
		EditText questionNumberInput = new EditText(this);
		questionNumberInput.setInputType(InputType.TYPE_CLASS_NUMBER);
		QNumDialog.setView(questionNumberInput);
		QNumDialog.setPositiveButton("Change", (dialogInterface, i) -> {
			String input = questionNumberInput.getText().toString();
			if(input.equals("")){
				Toast.makeText(this, "Please enter a valid", Toast.LENGTH_SHORT).show();
			}else{

				int number = Integer.parseInt(input);
				if(number < questionNumber){
					Toast.makeText(this, "You passed this Question :(\nPlease Re-enter new value greater than "+questionNumber, Toast.LENGTH_LONG).show();
				}
				else {
					maxQuestionNumber = number;
					if(ClickedButtons){
						questionNumber--;
						changeTitle();
						questionNumber++;
					}else{
						changeTitle();
					}
					Toast.makeText(this, "As you wish :)", Toast.LENGTH_LONG).show();
				}
			}
		});
		QNumDialog.setNegativeButton("Cancel", (dialogInterface, i) -> {});

		QNumDialog.create().show();
	}

	private void changeAccessedRegions() {
		AlertDialog.Builder accReg = new AlertDialog.Builder(this);
		accReg.setTitle("Select Accessed Regions :");
		accReg.setNeutralButton("Change and reset game", (dialogInterface, i) -> resetGameValues());
		accReg.setPositiveButton("Change", (dialogInterface, i) -> Toast.makeText(
				this,
				"We Will update Accessed Regions the next question",
				Toast.LENGTH_LONG
		).show());
		accReg.setNegativeButton("Cancel", (dialogInterface, i) -> resetCheckAccessedRegions());
		accReg.setMultiChoiceItems(accessedRegions, checkedAccessedRegions, (dialogInterface, i, b) -> checkedAccessedRegions[i] = b);
		accReg.create().show();
	}

	private void showHelp() {
		AlertDialog.Builder help = new AlertDialog.Builder(this);
		help.setTitle("This game is About:");
		help.setMessage("lorem opossum lorem opossum lorem opossum lorem opossum lorem opossum lorem opossum lorem opossum lorem opossum lorem opossum lorem opossum lorem opossum lorem opossum lorem opossum ");
//		help.setCancelable(true);
		help.setPositiveButton("OK", (dialogInterface, i) -> {

		});
		help.create().show();
	}

	private void newGame(String title, boolean cancelable) {
		AlertDialog.Builder newGame = new AlertDialog.Builder(this);
		newGame.setTitle(title);
		if(!cancelable) {
			newGame.setCancelable(false);
		}
		newGame.setPositiveButton("Reset Game", (dialogInterface, i) -> resetGameValues());
		if (cancelable){
			newGame.setNegativeButton("Cancel", (dialogInterface, i) -> {});
		}
		newGame.create().show();
	}

}