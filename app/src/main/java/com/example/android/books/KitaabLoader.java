package com.example.android.books;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

class KitaabLoader extends AsyncTaskLoader<List<Kitaab>> {

	private String url;
	private List<Kitaab> kitaabList;
	KitaabLoader(Context context, String url) {
		super(context);
		this.url = url;
	}

	@Override
	protected void onStartLoading() {
		if (kitaabList != null) {
			deliverResult(kitaabList);
		} else {
			forceLoad();
		}
	}
	@Override
	public List<Kitaab> loadInBackground() {
		if (url == null) {
			return null;
		}
		return QueryUtils.fetchBooks(url);
	}

	@Override
	public void deliverResult(List<Kitaab> data) {
		kitaabList = data;
		super.deliverResult(data);
	}
}
