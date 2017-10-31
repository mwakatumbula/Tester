package com.hover.tester.actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hover.sdk.main.HoverParameters;
import com.hover.tester.MainActivity;
import com.hover.tester.WakeUpHelper;
import com.hover.tester.services.OperatorService;
import com.hover.tester.R;
import com.hover.tester.utils.Utils;
import com.hover.tester.database.Contract;
import com.hover.tester.database.DbHelper;

public class ActionDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
	public static final String TAG = "ActionDetailFragment";
	private static final int VARIABLE_LOADER = 0, RESULT_LOADER = 1;
	OperatorService mService;
	OperatorAction mAction;
	RecyclerView variableRecycler;
	private VariableAdapter mVariableAdapter;
	private ResultAdapter mResultAdapter;

	public ActionDetailFragment() { }

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);

		if (getArguments().containsKey(OperatorAction.ID)) {
			mAction = OperatorAction.load(getArguments().getInt(OperatorAction.ID), getContext());
			if (mAction == null) {
				((ActionDetailActivity) getActivity()).shutDown();
				return;
			}
			mService = OperatorService.load(mAction.mOpId, getContext());
			fillToolbar();
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.frag_action_detail, container, false);
		addVariableView(root);
		addResultView(root);
		return root;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getArguments().containsKey(WakeUpHelper.SOURCE) && getActivity() != null)
			((ActionDetailActivity) getActivity()).makeRequest(getArguments());
	}

	private void fillToolbar() {
		((ActionDetailActivity) getActivity()).setTitle(mAction.mName, mService.mOpSlug + " " + mService.mName);
	}

	private void addVariableView(View root) {
		variableRecycler = (RecyclerView) root.findViewById(R.id.variable_list);
		variableRecycler.setLayoutManager(new LinearLayoutManager(root.getContext()));
		mVariableAdapter = new VariableAdapter(getActivity(), null, mAction);
		variableRecycler.setAdapter(mVariableAdapter);
		getLoaderManager().initLoader(VARIABLE_LOADER, null, this);
	}

	private void addResultView(View root) {
		RecyclerView resultRecycler = (RecyclerView) root.findViewById(R.id.result_list);
		resultRecycler.setLayoutManager(new LinearLayoutManager(root.getContext()));
		mResultAdapter = new ResultAdapter(getActivity(), null, mAction);
		resultRecycler.setAdapter(mResultAdapter);
		getLoaderManager().initLoader(RESULT_LOADER, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Log.d(TAG, "creating loader");
		if (id == VARIABLE_LOADER)
			return new CursorLoader(getActivity(), Contract.ActionVariableEntry.CONTENT_URI, Contract.VARIABLE_PROJECTION,
				Contract.ActionVariableEntry.COLUMN_ACTION_ID + " = " + mAction.mId, null, null);
		else
			return new CursorLoader(getActivity(), Contract.ActionResultEntry.CONTENT_URI, Contract.RESULT_PROJECTION,
					Contract.ActionResultEntry.COLUMN_ACTION_ID + " = " + mAction.mId, null, ActionResult.SORT_ORDER);
	}
	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (loader.getId() == VARIABLE_LOADER) {
			mVariableAdapter.swapCursor(cursor);
			Log.d(TAG, "variable cursor count: " + mVariableAdapter.getItemCount());
		} else {
			mResultAdapter.swapCursor(cursor);
			Log.d(TAG, "result cursor count: " + mResultAdapter.getItemCount());
		}
	}
	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == VARIABLE_LOADER)
			mVariableAdapter.swapCursor(null);
		else
			mResultAdapter.swapCursor(null);
	}

	@Override
	public void onPause() {
		super.onPause();
		saveExtras();
	}

	void addAndSaveExtras(HoverParameters.Builder hpb) throws NullPointerException {
		for (int i = 0; i < mVariableAdapter.getItemCount(); i++) {
			ActionVariable va = ((VariableAdapter.ViewHolder) variableRecycler.findViewHolderForAdapterPosition(i)).mVariable;
			Log.e(TAG, "Adding Extra: " + va.mName + ": " + va.mValue);
			va.save(getActivity());
			if (va.mValue == null || va.mValue.isEmpty())
				throw new NullPointerException("You must provide a value for " + va.mName);
			hpb.extra(va.mName, va.mValue);
			if (va.mName.equals("amount"))
				hpb.extra("currency", mService.mCurrencyIso);
		}
	}

	void saveExtras() {
		for (int i = 0; i < mVariableAdapter.getItemCount(); i++)
			((VariableAdapter.ViewHolder) variableRecycler.findViewHolderForAdapterPosition(i)).mVariable.save(getActivity());
	}

	void showResult(int resultCode, Intent data) {
		Log.e(TAG, "showing snack. trans id: " + data.getLongExtra("transaction_id", -1));
		if (resultCode == Activity.RESULT_OK) {
			Log.e(TAG, data.getStringExtra("response_message"));
			Snackbar.make(getView(), "Request Sent. " + data.getStringExtra("response_message"), Snackbar.LENGTH_LONG).show();
		} else if (data != null && data.hasExtra("result")) {
			Log.e(TAG, data.getStringExtra("result"));
			Snackbar.make(getView(), "Failure. " + data.getStringExtra("result"), Snackbar.LENGTH_LONG).show();
		}
		getLoaderManager().initLoader(RESULT_LOADER, null, this);
	}
}