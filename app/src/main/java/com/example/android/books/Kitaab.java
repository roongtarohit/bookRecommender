package com.example.android.books;

class Kitaab {

	private String bookTitle;
	private String bookAuthor;
	private float bookRating;
	private float bookPrice;
	Kitaab(String title, String authors, float rating, float price) {
		this.bookTitle = title;
		this.bookAuthor = authors;
		this.bookRating = rating;
		this.bookPrice = price;
	}
	String getTitle() {
		return bookTitle;
	}
	String getAuthor() {
		return bookAuthor;
	}
	float getRating() {
		return bookRating;
	}
	float getPrice() {
		return bookPrice;
	}
}
