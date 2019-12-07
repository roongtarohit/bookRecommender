package com.example.android.books;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class KitaabRecyclerView extends RecyclerView{

	private View view;

	private AdapterDataObserver mDataObserver = new AdapterDataObserver() {
		@Override
		public void onChanged() {
			super.onChanged();
			updateEmptyStatus();
		}
	};

	public KitaabRecyclerView(Context context) {
		super(context);
	}

	public KitaabRecyclerView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public KitaabRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	public void setEmptyView(View emptyView) {
		this.view = emptyView;
	}

	@Override
	public void setAdapter(RecyclerView.Adapter adapter) {

		if (getAdapter() != null) {
			getAdapter().unregisterAdapterDataObserver(mDataObserver);
		}
		if (adapter != null) {
			adapter.registerAdapterDataObserver(mDataObserver);
		}
		super.setAdapter(adapter);

		updateEmptyStatus();
	}

	private void updateEmptyStatus() {

		if (view != null && getAdapter() != null) {

			final boolean showEmptyView = getAdapter().getItemCount() == 0;
			view.setVisibility(showEmptyView ? VISIBLE : GONE);

			setVisibility(showEmptyView ? GONE : VISIBLE);
		}
	}
}
