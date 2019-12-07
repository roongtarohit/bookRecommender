package com.example.android.books;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

	private EditText searchText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		setupUI(findViewById(R.id.main_parent));

		searchText = findViewById(R.id.user_input_edit_text_view);

		final ImageButton search = findViewById(R.id.search_button);

		searchText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					search.performClick();
					return true;
				}
				return false;
			}
		});
	}

	public void searchFor(View view) {
		EditText userInput = findViewById(R.id.user_input_edit_text_view);
		String input = userInput.getText().toString();

		RadioButton titleChecked = findViewById(R.id.title_radio);
		RadioButton authorChecked = findViewById(R.id.author_radio);
		RadioButton isbnChecked = findViewById(R.id.isbn_radio);

		if (!input.isEmpty()) {
			Intent results = new Intent(MainActivity.this, ResultsActivity.class);
			results.putExtra("topic", searchText.getText().toString().toLowerCase());

			if (titleChecked.isChecked()) {
				results.putExtra("title", "intitle=");
			} else if (authorChecked.isChecked()) {
				results.putExtra("author", "inauthor=");
			} else if (isbnChecked.isChecked()) {
				results.putExtra("isbn", "isbn=");
			}
			startActivity(results);

		} else {
			Toast.makeText(
					MainActivity.this,
					getString(R.string.enter_text),
					Toast.LENGTH_SHORT)
					.show();
		}
	}
	public void setupUI(View view) {
		if (!(view instanceof EditText)) {
			view.setOnTouchListener(new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.performClick();
					return false;
				}
			});
		}

		if (view instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
				View innerView = ((ViewGroup) view).getChildAt(i);
				setupUI(innerView);
			}
		}
	}


	public void scannerButton(View view){
		Intent intent = new Intent(MainActivity.this, BarcodeActivity.class);
		Toast.makeText(MainActivity.this,"Scan by barcode", Toast.LENGTH_LONG).show();
		startActivity(intent);
	}

	public void imageButton(View view){
		Intent intent = new Intent(MainActivity.this, UploadImageActivity.class);
		Toast.makeText(MainActivity.this,"Search by image", Toast.LENGTH_LONG).show();
		startActivity(intent);
	}
}

