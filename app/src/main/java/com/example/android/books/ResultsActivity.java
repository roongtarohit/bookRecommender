package com.example.android.books;

import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Intent;
import android.content.Loader;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ResultsActivity
		extends AppCompatActivity
		implements LoaderCallbacks<List<Kitaab>>, KitaabAdapter.OnKitaabListener {

	private String GOOGLE_URL = "https://www.googleapis.com/books/v1/volumes?q=";

	private static final String API_KEY = "  ";  //USE YOUR  KEY

	private static final String details = "KITAAB_TITLE";

	private TextView stateView;
	private KitaabAdapter kitaabAdapter;
	private static final int EARTHQUAKE_LOADER_ID = 1;
	private ProgressBar spinner;


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.list_of_kitaab);
		KitaabRecyclerView recyclerView = findViewById(R.id.recycler_view);
		recyclerView.setHasFixedSize(true);
		int orientation = this.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
		} else {
			recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
		}

		kitaabAdapter = new KitaabAdapter(new ArrayList<Kitaab>(), this);
		recyclerView.setAdapter(kitaabAdapter);
		stateView = findViewById(R.id.empty_text_view);
		recyclerView.setEmptyView(stateView);

		spinner = findViewById(R.id.progress_spinner);
		spinner.setIndeterminate(true);

		Intent queryIntent = getIntent();
		String searchText = getIntent().getStringExtra("topic");
		String processedQuery = "";
		String title = queryIntent.getStringExtra("title");
		String author = queryIntent.getStringExtra("author");
		String isbn = queryIntent.getStringExtra("isbn");

		if (title != null) {
			processedQuery = searchText + "&" + title + searchText;
		} else if (author != null) {
			processedQuery = searchText + "&" + author + searchText;
		} else if (isbn != null) {
			processedQuery = searchText + "&" + isbn + searchText;
		} else {
			processedQuery = searchText;
		}

		GOOGLE_URL += processedQuery + "&maxResults=40" + "&key=" + API_KEY;

		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

		if (networkInfo != null && networkInfo.isConnected()) {
			LoaderManager loaderManager = getLoaderManager();
			loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, ResultsActivity.this);
		} else {
			spinner.setVisibility(View.GONE);
			stateView.setText(R.string.no_internet_connection);
		}
	}

	@Override
	public Loader<List<Kitaab>> onCreateLoader(int id, Bundle args) {
		return new KitaabLoader(ResultsActivity.this, GOOGLE_URL);
	}

	@Override
	public void onLoadFinished(Loader<List<Kitaab>> loader, List<Kitaab> books) {
		spinner.setVisibility(View.GONE);
		stateView.setText(R.string.no_books);
		kitaabAdapter.clear();
		if (books != null && !books.isEmpty()) {
			kitaabAdapter.addAll(books);
		}
	}

	@Override
	public void onLoaderReset(Loader<List<Kitaab>> loader) {
		kitaabAdapter.clear();
	}

	// TO remove
	@Override
	public void onKitaabClick(int position, String kitaabTitle) {
		Log.d("CLICKED RECYCLER", position+" "+kitaabTitle);
		Intent intent = new Intent(this,SynopsisActivity.class);
		intent.putExtra(details,kitaabTitle);
		startActivity(intent);
	}
}

