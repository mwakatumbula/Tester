package com.hover.tester.services;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.hover.sdk.onboarding.HoverIntegrationActivity;
import com.hover.tester.actions.OperatorAction;
import com.hover.tester.database.Contract;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class OperatorService {
	public static final String TAG = "OperatorService", ID = "service_id", SLUG = "service_slug",
			NAME = "service_name", COUNTRY = "country", CURRENCY = "currency", ACTION_LIST = "service_actions";

	public int mId;
	public String mName, mOpSlug, mCountryIso, mCurrencyIso;
	public List<OperatorAction> mActions;

	public OperatorService(Intent data, Context c) {
		mId = data.getIntExtra("serviceId", -1);
		mName = data.getStringExtra("serviceName");
		mOpSlug = data.getStringExtra("opSlug");
		mCountryIso = data.getStringExtra("countryName");
		mCurrencyIso = data.getStringExtra("currency");
		mActions = getActionsFromSdk(c);
		save(c);
	}

	public OperatorService(Cursor cursor, Context c) {
		mId = cursor.getInt(cursor.getColumnIndex(Contract.OperatorServiceEntry.COLUMN_SERVICE_ID));
		mName = cursor.getString(cursor.getColumnIndex(Contract.OperatorServiceEntry.COLUMN_NAME));
		mOpSlug = cursor.getString(cursor.getColumnIndex(Contract.OperatorServiceEntry.COLUMN_OP_SLUG));
		mCountryIso = cursor.getString(cursor.getColumnIndex(Contract.OperatorServiceEntry.COLUMN_COUNTRY));
		mCurrencyIso = cursor.getString(cursor.getColumnIndex(Contract.OperatorServiceEntry.COLUMN_CURRENCY));
		mActions = getActionsFromSdk(c);
	}

	private List<OperatorAction> getActionsFromSdk(Context c) {
		JSONArray jsonActions = HoverIntegrationActivity.getActionsList(mId, c);
		List<OperatorAction> actions = new ArrayList<>(jsonActions.length());
		try {
			for (int a = 0; a < jsonActions.length(); a++)
				actions.add(new OperatorAction(jsonActions.getJSONObject(a), mId));
		} catch (JSONException e) { Log.e(TAG, "Exception processing actions from json", e); }
		return actions;
	}

	public OperatorService save(Context c) {
		mId = (int) ContentUris.parseId(c.getContentResolver().insert(Contract.OperatorServiceEntry.CONTENT_URI, getBasicContentValues()));
		saveActions(c);
		return this;
	}

	public static int count(Context c) {
		Cursor countCursor = c.getContentResolver().query(Contract.OperatorServiceEntry.CONTENT_URI, new String[] {"count(*) AS count"}, null, null, null);
		if (countCursor != null) {
			countCursor.moveToFirst();
			int count = countCursor.getInt(0);
			countCursor.close();
			return count;
		}
		return 0;
	}

	private ContentValues getBasicContentValues() {
		ContentValues cv = new ContentValues();
		cv.put(Contract.OperatorServiceEntry.COLUMN_SERVICE_ID, mId);
		cv.put(Contract.OperatorServiceEntry.COLUMN_NAME, mName);
		cv.put(Contract.OperatorServiceEntry.COLUMN_OP_SLUG, mOpSlug);
		cv.put(Contract.OperatorServiceEntry.COLUMN_COUNTRY, mCountryIso);
		cv.put(Contract.OperatorServiceEntry.COLUMN_CURRENCY, mCurrencyIso);
		return cv;
	}

	public void saveActions(Context c) {
		for (OperatorAction opAction: mActions)
			opAction.save(c);
	}

	public static int getId(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndex(Contract.OperatorServiceEntry.COLUMN_SERVICE_ID));
	}
}
