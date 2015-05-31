package com.enormous.pkpizzas.consumer.activities;

import android.app.ActionBar;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.enormous.pkpizzas.consumer.R;
import com.enormous.pkpizzas.consumer.common.SlidingTabStrip;
import com.enormous.pkpizzas.consumer.common.Utils;
import com.enormous.pkpizzas.consumer.fragments.InviteFriendsFragmentByEmail;
import com.enormous.pkpizzas.consumer.fragments.InviteFriendsFragmentByText;
import com.enormous.pkpizzas.consumer.models.Contact;

import java.util.ArrayList;

public class InviteFriendsActivity extends FragmentActivity {

    private ActionBar actionBar;
    private EditText searchEditText;
    private ImageButton searchImageButton;
    private ViewPager pager;
    private SlidingTabStrip slidingTabStrip;
    private ContactsPagerAdapter adapter;
    private TextFriendsListViewAdapter textFriendsAdapter;
    private EmailFriendsListViewAdapter emailFriendsAdapter;
	public ArrayList<Contact> contacts = null;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_invite_friends);
		findViews();

		//set actionBar properties
		actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowHomeEnabled(false);

		//set up pager
		adapter = new ContactsPagerAdapter(getSupportFragmentManager());
		pager.setAdapter(adapter);
        slidingTabStrip.showDividers(true);
        slidingTabStrip.shouldExpand(true);
        slidingTabStrip.setViewPager(pager);

		//fetch all user contacts
		new AsyncTask<Void, Void, ArrayList<Contact>>() {

			@Override
			protected ArrayList<Contact> doInBackground(Void... params) {
				return getContacts();
			}

			@Override
			protected void onPostExecute(ArrayList<Contact> result) {
				super.onPostExecute(result);
				contacts = result;
				emailFriendsAdapter = new EmailFriendsListViewAdapter();
				adapter.byEmailFragment.setAdapter(emailFriendsAdapter);
				textFriendsAdapter = new TextFriendsListViewAdapter();
				adapter.byTextFragment.setAdapter(textFriendsAdapter);
			}
		}.execute();

		//set onEditorActionListener on searchEditText to detect when user presses ENTER/SEARCH
		searchEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEARCH) {
					Utils.hideKeyboard(InviteFriendsActivity.this, searchEditText.getWindowToken());
				}
				return true;
			}
		});

		//set onClick listener onSearchImageButton to clear editText when pressed
		searchImageButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				searchEditText.setText("");
			}
		});

		//set up searchEditText listener
		searchEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence constraint, int arg1, int arg2, int arg3) {
				if(emailFriendsAdapter!=null){
				emailFriendsAdapter.getFilter().filter(constraint);
				textFriendsAdapter.getFilter().filter(constraint);
				
				if (constraint.toString().trim().length() == 0) {
					searchImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_search_edittext));
				}
				else { 
					searchImageButton.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_cancel_edittext));
				}
				}
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
			}
		});
	}

    private void findViews() {
		actionBar = getActionBar();
        slidingTabStrip = (SlidingTabStrip) findViewById(R.id.slidingTabStrip);
		searchEditText = (EditText) findViewById(R.id.searchEditText);
		searchImageButton = (ImageButton) findViewById(R.id.searchImageButton);
		pager = (ViewPager) findViewById(R.id.pager);
	}

    private class ContactsPagerAdapter extends FragmentPagerAdapter {

		ArrayList<Fragment> fragments;
		public InviteFriendsFragmentByEmail byEmailFragment;
		public InviteFriendsFragmentByText byTextFragment;

		public ContactsPagerAdapter(FragmentManager fm) {
			super(fm);
			fragments = new ArrayList<Fragment>();
			byEmailFragment = new InviteFriendsFragmentByEmail();
			fragments.add(byEmailFragment);
			byTextFragment = new InviteFriendsFragmentByText();
			fragments.add(byTextFragment);
		}

		@Override
		public Fragment getItem(int pos) {
			return fragments.get(pos);
		}

		@Override
		public int getCount() {
			return fragments.size();
		}

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "By Email";
                case 1:
                    return "By Text";
            }
            return null;
        }
    }


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();
		overridePendingTransition(R.anim.scale_in, R.anim.slide_out_right);
	}
	
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		getMenuInflater().inflate(R.menu.menu_invite_friends, menu);
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			onBackPressed();
		}
//		if (item.getItemId() == R.id.action_share) {
//			Intent shareIntent = new Intent(Intent.ACTION_SEND)	;
//			shareIntent.setType("text/plain");
//			shareIntent.setPackage("com.facebook.katana");
//			shareIntent.putExtra(Intent.EXTRA_TITLE, "Hello");
//			shareIntent.putExtra(Intent.EXTRA_TEXT, "Hey! Have you tried Discover yet? I'm sure you'd like it!");
//			startActivity(shareIntent);
//		}
		return true;
	}

    private ArrayList<Contact> getContacts() {
		ArrayList<Contact> contacts = new ArrayList<Contact>();
		ContentResolver cResolver = getContentResolver();
		Cursor mainCursor = cResolver.query(ContactsContract.Contacts.CONTENT_URI, new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, ContactsContract.Contacts.HAS_PHONE_NUMBER}, null, null, null);
		if (mainCursor.getCount() > 0) {
			while (mainCursor.moveToNext()) {
				String id = mainCursor.getString(mainCursor.getColumnIndex(ContactsContract.Contacts._ID));
				String name = mainCursor.getString(mainCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				Uri thumbnail = null;
				if (mainCursor.getString(mainCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)) != null) {					
					thumbnail = Uri.parse(mainCursor.getString(mainCursor.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI)));
				}
				String phone = null;
				if (mainCursor.getInt(mainCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
					Cursor phoneCursor = cResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER}, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "= ?", new String[]{id}, null);
					phoneCursor.moveToFirst();
					phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					phoneCursor.close();
				}
				String email = null;
				Cursor emailCursor = cResolver.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, new String[]{ContactsContract.CommonDataKinds.Email.DATA}, ContactsContract.CommonDataKinds.Email.CONTACT_ID + "= ?", new String[]{id}, null);
				if (emailCursor.moveToFirst()) {
					email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				}
				emailCursor.close();

				//finally, add a new contact 
				contacts.add(new Contact(id, name, email, phone, thumbnail));
			}
		}
		mainCursor.close();
		return contacts;
	}

    private class EmailFriendsListViewAdapter extends BaseAdapter implements Filterable {

		ArrayList<Contact> contactsWithEmail;
		ArrayList<Contact> contactsWithEmailCopy;

		public EmailFriendsListViewAdapter() {
			contactsWithEmail = new ArrayList<Contact>();
			contactsWithEmailCopy = new ArrayList<Contact>();
			if (contacts != null) {
				for (Contact contact : contacts) {
					if (contact.email != null) {
						if (!contact.name.contains("@")) {							
							contactsWithEmail.add(contact);
							contactsWithEmailCopy.add(contact);
						}
					}
				}
			}
		}

		@Override
		public int getCount() {
			return contactsWithEmail.size();
		}

		@Override
		public Object getItem(int pos) {
			return contactsWithEmail.get(pos);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		class ViewHolder {
			ImageView thumbnail;
			TextView name;
			TextView info;
			Button invite;

			public ViewHolder(View row) {
				thumbnail = (ImageView) row.findViewById(R.id.thumbnailImageView);
				name = (TextView) row.findViewById(R.id.nameTextView);
				info = (TextView) row.findViewById(R.id.infoTextView);
				invite = (Button) row.findViewById(R.id.inviteButton);
			}
		}

		@Override
		public View getView(final int pos, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewHolder holder = null;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.listview_invite_friends_item, parent, false);
				holder = new ViewHolder(row);
				row.setTag(holder);
			}
			else {
				holder = (ViewHolder) row.getTag();
			}
			holder.name.setText(contactsWithEmail.get(pos).name);
			holder.info.setText(contactsWithEmail.get(pos).email);
			if (contactsWithEmail.get(pos).thumbnail != null) {
				holder.thumbnail.setImageURI(contactsWithEmail.get(pos).thumbnail);
			}
			else {
				holder.thumbnail.setImageDrawable(getResources().getDrawable(R.drawable.default_profilepic));
			}
			holder.invite.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent sendEmail = new Intent();
					sendEmail.setAction(Intent.ACTION_SENDTO);
					sendEmail.setType("text/plain");
					String uriText = "mailto:" + Uri.encode(contactsWithEmail.get(pos).email) + "?subject=" + Uri.encode("Magic Invitation") + "&body=" + Uri.encode("Hey " + contactsWithEmail.get(pos).name + ", have you tried Magic yet? I think you'd like it...");
					sendEmail.setData(Uri.parse(uriText));
					startActivity(Intent.createChooser(sendEmail, "Send invitation via: "));
				}
			});

			return row;
		}

		@Override
		public Filter getFilter() {
			return new Filter() {

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					constraint = constraint.toString().trim().toLowerCase();
					ArrayList<Contact> filteredContacts = new ArrayList<Contact>(); 
					FilterResults results = new FilterResults();
					for (Contact contact : contactsWithEmailCopy) {
						if (contact.name.toLowerCase().contains(constraint)) {
							filteredContacts.add(contact);
						}
					}
					results.count = filteredContacts.size();
					results.values = filteredContacts;

					return results;
				}

				@Override
				protected void publishResults(CharSequence constraint ,
						FilterResults results) {
					contactsWithEmail.clear();
					contactsWithEmail.addAll((ArrayList<Contact>) results.values);
					notifyDataSetChanged();
				}
			};
		} 

	}

    private class TextFriendsListViewAdapter extends BaseAdapter implements Filterable {

		ArrayList<Contact> contactsWithPhone;
		ArrayList<Contact> contactsWithPhoneCopy;

		public TextFriendsListViewAdapter() {
			contactsWithPhone = new ArrayList<Contact>();
			contactsWithPhoneCopy = new ArrayList<Contact>();
			if (contacts != null) {
				for (Contact contact : contacts) {
					if (contact.phone != null) {
						if (!contact.name.contains("@")) {							
							contactsWithPhone.add(contact);
							contactsWithPhoneCopy.add(contact);
						}
					}
				}
			}
		}

		@Override
		public int getCount() {
			return contactsWithPhone.size();
		}

		@Override
		public Object getItem(int pos) {
			return contactsWithPhone.get(pos);
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		class ViewHolder {
			ImageView thumbnail;
			TextView name;
			TextView info;
			Button invite;

			public ViewHolder(View row) {
				thumbnail = (ImageView) row.findViewById(R.id.thumbnailImageView);
				name = (TextView) row.findViewById(R.id.nameTextView);
				info = (TextView) row.findViewById(R.id.infoTextView);
				invite = (Button) row.findViewById(R.id.inviteButton);
			}
		}

		@Override
		public View getView(final int pos, View convertView, ViewGroup parent) {
			View row = convertView;
			ViewHolder holder = null;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.listview_invite_friends_item, parent, false);
				holder = new ViewHolder(row);
				row.setTag(holder);
			}
			else {
				holder = (ViewHolder) row.getTag();
			}
			holder.name.setText(contactsWithPhone.get(pos).name);
			holder.info.setText(contactsWithPhone.get(pos).phone);
			if (contactsWithPhone.get(pos).thumbnail != null) {
				holder.thumbnail.setImageURI(contactsWithPhone.get(pos).thumbnail);
			}
			else {
				holder.thumbnail.setImageDrawable(getResources().getDrawable(R.drawable.default_profilepic));
			}
			holder.invite.setTag(pos);
			holder.invite.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent sendSMS = new Intent();
					sendSMS.setAction(Intent.ACTION_SENDTO);
					sendSMS.setType("vnd.android-dir/mms-sms");
					sendSMS.putExtra("sms_body", "Hey " + contactsWithPhone.get(pos).name + ", have you tried Magic yet? I think you'd like it...");
					sendSMS.setData(Uri.parse("sms:" + contactsWithPhone.get(pos).phone));
					startActivity(sendSMS);
				}
			});

			return row;
		}

		@Override
		public Filter getFilter() {
			return new Filter() {

				@Override
				protected FilterResults performFiltering(CharSequence constraint) {
					constraint = constraint.toString().trim().toLowerCase();
					ArrayList<Contact> filteredContacts = new ArrayList<Contact>(); 
					FilterResults results = new FilterResults();
					for (Contact contact : contactsWithPhoneCopy) {
						if (contact.name.toLowerCase().contains(constraint)) {
							filteredContacts.add(contact);
						}
					}
					results.count = filteredContacts.size();
					results.values = filteredContacts;

					return results;
				}

				@Override
				protected void publishResults(CharSequence constraint ,
						FilterResults results) {
					contactsWithPhone.clear();
					contactsWithPhone.addAll((ArrayList<Contact>) results.values);
					notifyDataSetChanged();
				}
			};
		} 

	}
}
