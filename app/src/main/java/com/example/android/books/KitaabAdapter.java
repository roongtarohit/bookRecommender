package com.example.android.books;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

class KitaabAdapter extends RecyclerView.Adapter<KitaabAdapter.CardViewHolder> {

	private final String LOG_TAG = KitaabAdapter.class.getSimpleName();

	private List<Kitaab> kitaabList;

	//
	private OnKitaabListener monKitaabListener;

	//Added the parameter
	KitaabAdapter(List<Kitaab> listOfBooks, OnKitaabListener onKitaabListener) {
		this.kitaabList = listOfBooks;

		//
		this.monKitaabListener = onKitaabListener;
	}

	@Override
	public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.kitaab_card, parent, false);

		// added parameter
		return new CardViewHolder(view, monKitaabListener);
	}

	@Override
	public void onBindViewHolder(CardViewHolder holder, int position) {
		Kitaab currentBook = kitaabList.get(position);
		holder.bookTitle.setText(currentBook.getTitle());

		try {
			String authors = currentBook.getAuthor();
			if (!authors.isEmpty()) {
				holder.bookAuthor.setText(authors);
			}

		} catch (NullPointerException e) {
			Log.v(LOG_TAG, "No information on authors");
			holder.bookAuthor.setVisibility(View.INVISIBLE);
		}

		holder.bookRating.setRating(currentBook.getRating());
		String price = "";
		if (currentBook.getPrice() > 0) {
			price = "$" + currentBook.getPrice();
			holder.bookPrice.setText(price);
		}
	}

	@Override
	public int getItemCount() {
		return kitaabList.size();
	}

	void clear() {
		kitaabList = new ArrayList<>();
	}
	void addAll(List<Kitaab> data) {
		for (int i = 0; i < data.size(); i++) {
			Kitaab book = data.get(i);
			kitaabList.add(book);
			notifyDataSetChanged();
		}
	}

	static class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		TextView bookTitle;

		//
		OnKitaabListener onKitaabListener;
		TextView bookAuthor;
		RatingBar bookRating;
		TextView bookPrice;
		//Extra parameter
		CardViewHolder(View itemView, OnKitaabListener onKitaabListener) {
			super(itemView);

			//
			this.onKitaabListener = onKitaabListener;
			bookTitle = itemView.findViewById(R.id.book_title_text_view);
			bookAuthor = itemView.findViewById(R.id.author_text_view);
			bookRating = itemView.findViewById(R.id.rating_bar);
			bookRating.setMax(5);
			bookRating.setNumStars(5);
			Drawable progress = bookRating.getProgressDrawable();
			DrawableCompat.setTint(progress, Color.YELLOW);
			bookPrice = itemView.findViewById(R.id.retail_price_text_view);
			itemView.setOnClickListener(this);
		}

		@Override
		public void onClick(View view) {
			onKitaabListener.onKitaabClick(getAdapterPosition(), bookTitle.getText().toString());
		}
	}

	//
	public interface OnKitaabListener{
		void onKitaabClick(int position, String title);
	}
}
