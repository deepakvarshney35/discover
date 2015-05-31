package com.enormous.discover.consumer.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.enormous.discover.consumer.DiscoverApp;
import com.enormous.discover.consumer.R;
import com.enormous.discover.consumer.common.Chat;
import com.enormous.discover.consumer.common.FirebaseListAdapter;
import com.enormous.discover.consumer.common.ImageLoader;
import com.enormous.discover.consumer.common.Utils;
import com.enormous.discover.consumer.services.ParsePushNotificationReceiver;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Chat using Firebase Backend.
 */
public class ChatMessageActivity extends Activity {

    private static final String FIREBASE_URL = "https://discover-merchant.firebaseio.com";

    private boolean orderFlag = false;
    private boolean orderShare = false;
    private boolean orderComplete = false;
    private Firebase ref, pRef;
    private ValueEventListener connectedListener;
    private ChatListAdapter chatListAdapter;
    private EditText chatComment;
    private String customerObjectId;
    private String merchantUserId;
    private ImageView sendComment;
    private ImageView sendPhoto;
    private LinearLayout layoutAboveAll;
    boolean opened;
    ListView listView;

    private String merchantName, merchantPhone, merchantPic = "", merchantEmail;

    ProgressDialog imageUploadProgress;
    protected static final int BROWSE_GALLERY_REQUEST_PROFILE = 0;
    protected static final int CHOOSE_GALLERY = 1;
    private static final int CHOOSE_CAMERA = 2;

    ParseFile attachmentPicture;

    Date dateCamera;

    String profilePictureUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_message_view);

        getActionBar().setDisplayShowHomeEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        findViews();

        Intent intent = getIntent();

        getIntentData(intent);

        //Get Consumer Object ID
        customerObjectId = ParseUser.getCurrentUser().getObjectId();
        if (ParseUser.getCurrentUser().getString("profilePictureUrl") != null)
            profilePictureUrl = ParseUser.getCurrentUser().getString("profilePictureUrl");

        if (merchantPhone != null) {
            //Save Check in upon clicking the Brand
            ParseQuery<ParseObject> query = ParseQuery.getQuery("ChatBrandList");
            query.whereEqualTo("userObjectId", ParseUser.getCurrentUser().getObjectId());
            query.whereEqualTo("brandObjectId", merchantUserId);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> chatslist, ParseException e) {
                    // TODO Auto-generated method stub
                    if (e == null) {
                        if (chatslist.size() == 0) {
                            ParseObject checkin = new ParseObject("ChatBrandList");
                            checkin.put("userObjectId", ParseUser.getCurrentUser().getObjectId());
                            checkin.put("brandObjectId", merchantUserId);
                            checkin.put("brandName", merchantName);
                            checkin.put("brandPhone", merchantPhone);
                            checkin.put("brandPic", merchantPic);
                            checkin.put("brandEmail", merchantEmail);
                            checkin.saveInBackground();
                        }
                    }

                }
            });
        }
        SharedPreferences sharedpreferences = getSharedPreferences("MyPrefs", Service.MODE_PRIVATE);
        Editor editor = sharedpreferences.edit();
        editor.putInt("key" + merchantUserId, 0);
        editor.commit();

        //Set up action Bar
        getActionBar().setTitle(merchantName);
        getActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.indigo500)));

        //Unique ID to identify chat will be consumer/customer Object ID + merchant/Publisher Object ID
        // Setup our Firebase ref
        ref = new Firebase(FIREBASE_URL).child(customerObjectId + merchantUserId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_chat, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_call:
                Intent callCustomer = new Intent(Intent.ACTION_CALL);
                callCustomer.setData(Uri.parse("tel:" + merchantPhone));
                if (merchantPhone != null) startActivity(callCustomer);
                else Toast.makeText(this, "Phone no. not found", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_mail:
                Intent mailCustomer = new Intent(Intent.ACTION_SEND);
                mailCustomer.setType("text/html");
                mailCustomer.putExtra(Intent.EXTRA_EMAIL, merchantEmail);
                //mailCustomer.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                //mailCustomer.putExtra(Intent.EXTRA_TEXT, "I'm email body.");
                if (merchantEmail != null)
                    startActivity(Intent.createChooser(mailCustomer, "Send Email"));
                else Toast.makeText(this, "Email not found", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    private void findViews() {
        layoutAboveAll = (LinearLayout) findViewById(R.id.layoutAboveAll);
        sendComment = (ImageView) findViewById(R.id.sendComment);
        chatComment = (EditText) findViewById(R.id.chatComment);
        sendPhoto = (ImageView) findViewById(R.id.sendPhoto);
    }

    private void getIntentData(Intent intent) {
        merchantUserId = (String) intent.getExtras().get("merchantUserId");
        merchantName = (String) intent.getExtras().get("merchantName");
        merchantPhone = (String) intent.getExtras().get("merchantPhone");
        if ((String) intent.getExtras().get("merchantPic") != null)
            merchantPic = (String) intent.getExtras().get("merchantPic");
        merchantEmail = (String) intent.getExtras().get("merchantEmail");
    }

    @Override
    protected void onResume() {
        super.onResume();

        sendComment.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String input = chatComment.getText().toString();
                if (!input.equals("")) {

/*					if(merchantPhone!=null){
                    if(chatListAdapter.getCount()==0){
						//Save Chat info upon clicking send for the first time
						ParseObject checkin = new ParseObject("ChatBrandList");
						checkin.put("userObjectId", ParseUser.getCurrentUser().getObjectId());
						checkin.put("brandObjectId", merchantUserId);
						checkin.put("brandName", merchantName);
						checkin.put("brandPhone", merchantPhone);
						checkin.put("brandPic", merchantPic);
						checkin.put("brandEmail", merchantEmail);
						checkin.saveInBackground();
					}
					}*/
                    // Create our 'model', a Chat object
                    Date date = new Date();
                    pRef = ref.push();
                    Chat chat = new Chat(pRef.getName(), input, ParseUser.getCurrentUser().getObjectId(), date.getTime());
                    // Create a new, auto-generated child of that chat location, and save our chat data there
                    pRef.setValue(chat);

                    chatComment.setText("");
                    //Sends Parse Notification to channel named after Unique ID. (customer + merchant ID)

                    // Find users near a given location
                    ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                    userQuery.whereEqualTo("objectId", merchantUserId);

                    // Find devices associated with these users
                    ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
                    //pushQuery.whereMatchesQuery("user", userQuery);
                    pushQuery.whereEqualTo("channels", "Publisher");
                    pushQuery.whereEqualTo("deviceId", merchantUserId);
                    try {
                        JSONObject data = new JSONObject("{\"header\": \"" + input + "\","
                                + "\"action\": \"com.enormous.discover.publisher.UPDATE_STATUS\","
                                + "\"myObjectId\": \"" + customerObjectId + "\","
                                + "\"merchantUserId\": \"" + merchantUserId + "\","
                                + "\"customerObjectId\": \"" + customerObjectId + "\","
                                + "\"profilePictureUrl\": \"" + profilePictureUrl + "\","
                                + "\"customerName\": \"" + ParseUser.getCurrentUser().getString("firstName") + " " + ParseUser.getCurrentUser().getString("lastName") + "\"}");
                        //Send Push notification
                        ParsePush push = new ParsePush();
                        push.setQuery(pushQuery);
                        //push.setChannel(customerObjectId+merchantUserId);
                        push.setData(data);
                        push.sendInBackground(new SendCallback() {

                            @Override
                            public void done(ParseException e) {
                                // TODO Auto-generated method stub
                                if (e == null) {
                                    //Toast.makeText(ChatMessageActivity.this, "DONE", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ChatMessageActivity.this, "" + e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        sendPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                showPictureChooser();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
        listView = (ListView) findViewById(R.id.listComments);
        // Tell our list adapter that we only want 50 messages at a time
        chatListAdapter = new ChatListAdapter(ref.limit(50), this, R.layout.chat_layout, customerObjectId);
        listView.setAdapter(chatListAdapter);
        chatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatListAdapter.getCount() - 1);
            }
        });

        // Finally, a little indication of connection status
        connectedListener = ref.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    layoutAboveAll.setVisibility(View.GONE);
                    //Toast.makeText(MessageViewActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                    //swipeRefreshLayout.setRefreshing(false);
                } else {
                    layoutAboveAll.setVisibility(View.VISIBLE);
                    //Toast.makeText(MessageViewActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                    //swipeRefreshLayout.setRefreshing(true);
                }
            }

            @Override
            public void onCancelled() {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        ref.getRoot().child(".info/connected").removeEventListener(connectedListener);
        chatListAdapter.cleanup();
    }
    /**
     * User: greg
     * Date: 6/21/13
     * Time: 2:39 PM
     */

    /**
     * This class is an example of how to use FirebaseListAdapter. It uses the <code>Chat</code> class to encapsulate the
     * data for each individual chat message
     */
    public class ChatListAdapter extends FirebaseListAdapter<Chat> {

        // The username for this client. We use this to indicate which messages originated from this user
        private static final String FIREBASE_URL = "https://confess.firebaseio.com";
        private Firebase ref;
        int voteCountNum;
        ArrayList<String> upVotedConfession, downVotedConfession;

        public ChatListAdapter(Query ref, Activity activity, int layout, String ObjectId) {
            super(ref, Chat.class, layout, activity);
            this.ref = new Firebase(FIREBASE_URL).child(ObjectId);
        }

        /**
         * Bind an instance of the <code>Chat</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
         * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
         * to the constructor, as well as a single <code>Chat</code> instance that represents the current data to bind.
         *
         * @param view A view instance corresponding to the layout we passed to the constructor.
         * @param chat An instance representing the current state of a chat message
         */
        @Override
        protected void populateView(final View view, final Chat chat) {
            voteCountNum = 0;
            orderFlag = false;
            orderComplete = false;
            orderShare = false;
            TextView timeText = (TextView) view.findViewById(R.id.chatTime);
            TextView chatMessage = (TextView) view.findViewById(R.id.chatMessage);
            final LinearLayout messageBackground = (LinearLayout) view.findViewById(R.id.messageBackground);
            ImageView attachedPhoto = (ImageView) view.findViewById(R.id.attachedPhoto);
            String chatText = chat.getMessage();

            orderShare = checkShare(chatText);
            orderComplete = checkOrderComplete(chatText);
            if (chatText.equals("::New Order::")) orderFlag = true;

            if (chatText.contains("attachmentPicture.jpg")) {
                attachedPhoto.setVisibility(View.VISIBLE);
                messageBackground.setTag("Normal");
                chatMessage.setVisibility(View.GONE);
                ImageLoader.getInstance().displayImage(getApplicationContext(), chatText, attachedPhoto, false, 200, 200, R.drawable.placeholder_image);
            } else {
                if (orderFlag) {
                    attachedPhoto.setVisibility(View.VISIBLE);
                    chatMessage.setVisibility(View.VISIBLE);
                    attachedPhoto.setImageResource(R.drawable.ic_shopping_cart_white_48dp);
                    messageBackground.setTag("NewOrder");
                    attachedPhoto.setBackgroundColor(Color.TRANSPARENT);
                } else if (orderComplete) {
                    attachedPhoto.setVisibility(View.VISIBLE);
                    chatMessage.setVisibility(View.VISIBLE);
                    attachedPhoto.setImageResource(R.drawable.ic_shopping_cart_white_48dp);
                    messageBackground.setTag("OrderComplete");
                    attachedPhoto.setBackgroundColor(Color.TRANSPARENT);
                } else if (orderShare) {
                    messageBackground.setTag("Share");
                    attachedPhoto.setVisibility(View.GONE);
                    chatMessage.setVisibility(View.VISIBLE);
                } else {
                    attachedPhoto.setVisibility(View.GONE);
                    messageBackground.setTag("Normal");
                    chatMessage.setVisibility(View.VISIBLE);
                    chatText = chatText.replaceAll("\\r|\\n", " ");
                    chatMessage.setText(chatText);
                }
            }
            String author = chat.getAuthor();
            //Change layout according to "in message" or "out message"
            if (!author.equals(ParseUser.getCurrentUser().getObjectId())) {
                if (orderComplete){
                    messageBackground.setBackgroundResource(R.drawable.msg_in_order);
                    chatMessage.setText(merchantName+"has completed your order"+"\r\n"+"Order ID: "+getOrderId(chatText));
                }
                else messageBackground.setBackgroundResource(R.drawable.msg_in);
                chatMessage.setPadding(30, 0, 10, 0);
                /*if(!orderFlag) {
                    LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(500, 500);
                    paramss.setMargins(30, 0, 5, 0);
                    attachedPhoto.setLayoutParams(paramss);
                }*/
                LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.LEFT;
                params.setMargins(10, 0, 0, 0);
                messageBackground.setLayoutParams(params);
            } else {
                if (orderFlag) messageBackground.setBackgroundResource(R.drawable.msg_out_com);
                //else if (orderShare) messageBackground.setBackgroundResource(R.drawable.msg_out_fb);
                else messageBackground.setBackgroundResource(R.drawable.msg_out);
                chatMessage.setPadding(10, 0, 30, 0);
                /*if(!orderFlag) {
                    LinearLayout.LayoutParams paramss = new LinearLayout.LayoutParams(500, 500);
                    paramss.setMargins(5, 0, 30, 0);
                    attachedPhoto.setLayoutParams(paramss);
                }*/
                LayoutParams paramsd = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                paramsd.gravity = Gravity.RIGHT;
                paramsd.setMargins(0, 0, 10, 0);
                messageBackground.setLayoutParams(paramsd);
                if (orderFlag) chatMessage.setText("New Order Placed");
                else if(orderShare) chatMessage.setText("You shared the offer "+getOfferName(chatText));
            }
            //Opens up image viewer Activity
            attachedPhoto.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (chat.getMessage().contains("attachmentPicture.jpg")) {
                        ArrayList<String> urls = new ArrayList<String>();
                        urls.add(chat.getMessage());
                        Intent goToViewPhotos = new Intent(view.getContext(), ProductPhotoViewerActivity.class);
                        goToViewPhotos.putStringArrayListExtra("imageUrls", urls);
                        goToViewPhotos.putExtra("imagePos", 1);
                        startActivity(goToViewPhotos);
                    }
                }
            });
            messageBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (messageBackground.getTag().equals("OrderComplete")) {
                        Intent goToOrders = new Intent(ChatMessageActivity.this, OrdersActivity.class);
                        goToOrders.putExtra("orderId",getOrderId(chat.getMessage()));
                        startActivity(goToOrders);
                        ChatMessageActivity.this.overridePendingTransition(R.anim.slide_in_left, R.anim.scale_out);
                    }
                }
            });

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(chat.getTime());

            SimpleDateFormat df1 = new SimpleDateFormat("hh:mm a");
            SimpleDateFormat day = new SimpleDateFormat("dd");
            SimpleDateFormat month = new SimpleDateFormat("MM");
            SimpleDateFormat df2 = new SimpleDateFormat("dd-MM");
            Calendar cal = Calendar.getInstance();
            int dayCal = cal.get(Calendar.DAY_OF_MONTH);
            int monthCal = cal.get(Calendar.MONTH) + 1; //java month starts with 0
            if (calendar.get(Calendar.DAY_OF_MONTH) == dayCal && (calendar.get(Calendar.MONTH) + 1) == monthCal) {
                timeText.setText("Today " + df1.format(calendar.getTime()));
            } else {
                timeText.setText(df2.format(calendar.getTime()));
            }

        }
    }

    private boolean checkOrderComplete(String chatText) {
        if (chatText.contains(" ")) {
            if (chatText.substring(0, chatText.indexOf(' ')).equals("::OrderComplete::"))
                return true;
            else return false;
        } else return false;
    }

    private String getOrderId(String chatText) {
        return chatText.substring(chatText.indexOf(' ')).trim();
    }

    private String getOfferName(String chatText) {
        String tempText = chatText.substring(chatText.indexOf(' ')).trim();
        return tempText.substring(tempText.indexOf(' ')).trim();
    }

    private boolean checkShare(String chatText) {
        if (chatText.contains(" ")) {
            if (chatText.substring(0, chatText.indexOf(' ')).equals("::NewShare::")) return true;
            else return false;
        }
        else return false;
    }

    //Dialog box for choosing camera/gallery
    public void showPictureChooser() {
        Builder builder = new Builder(ChatMessageActivity.this);
        View customView = getLayoutInflater().inflate(R.layout.dialog_picture_chooser, null);
        ListView optionsListView = (ListView) customView.findViewById(R.id.dialogListView);
        DialogListViewAdapter adapter = new DialogListViewAdapter(ChatMessageActivity.this);
        optionsListView.setAdapter(adapter);
        builder.setView(customView);

        //create and show dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        //set onItemClick listener on options listview
        optionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adv, View v,
                                    int pos, long arg3) {
                if (pos == 0) {
                    captureImageUsingCamera();
                }
                if (pos == 1) {
                    //fire image picker intent to choose existing picture
                    Intent pictureIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    pictureIntent.setType("image/*");
                    startActivityForResult(pictureIntent, CHOOSE_GALLERY);
                }
                dialog.dismiss();
            }
        });
    }

    public void captureImageUsingCamera() {
        Intent pictureIntent = null;
        Uri profilePicUri = null;
        try {
            //fire camera intent to take picture
            pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //save image to external cache dir
            File imagesFolder = new File(DiscoverApp.EXTERNAL_CACHE_DIR.getAbsolutePath() + "/images");
            if (!imagesFolder.exists()) {
                imagesFolder.mkdirs();
            }
            dateCamera = new Date();
            File tempFile = new File(imagesFolder.getAbsolutePath() + "/" + dateCamera.getTime() + "attachmentPicture.jpg");
            if (tempFile.exists()) {
                tempFile.delete();
            }
            profilePicUri = Uri.fromFile(tempFile);
            //tell the intent to save the image in order to get full resoulution photo
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, profilePicUri);
            startActivityForResult(pictureIntent, CHOOSE_CAMERA);
        } catch (Exception e) {
            Log.d("TEST", "taking picture using camera failed: " + e.getMessage());
        }
    }

    class DialogListViewAdapter extends BaseAdapter {
        String[] options;
        Bitmap[] bitmaps;

        public DialogListViewAdapter(Context c) {
            options = new String[]{"Take a new picture", "Choose an existing picture"};
            bitmaps = new Bitmap[]{BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_camera), BitmapFactory.decodeResource(getResources(), R.drawable.ic_action_gallery)};
        }


        @Override
        public int getCount() {
            return options.length;
        }

        @Override
        public Object getItem(int pos) {
            return options[pos];
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }


        @SuppressLint("ViewHolder")
        @Override
        public View getView(int pos, View convertView, ViewGroup container) {
            View row = getLayoutInflater().inflate(R.layout.listview_dialog_item, container, false);
            TextView itemName = (TextView) row.findViewById(R.id.itemNameTextView);
            ImageView itemIcon = (ImageView) row.findViewById(R.id.itemIconImageView);
            itemName.setText(options[pos]);
            itemIcon.setImageBitmap(bitmaps[pos]);

            return row;
        }


    }

    //on clicking photo or selecting photo this method is called.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //if its gallery then we save and upload photo to parse.
        if (requestCode == CHOOSE_GALLERY) {
            if (data != null) {
                Bitmap bitmap = null;
                int reqWidth = 500;
                int reqHeight = 500;

                File imagesFolder = new File(DiscoverApp.EXTERNAL_CACHE_DIR.getAbsolutePath() + "/images");
                if (!imagesFolder.exists()) {
                    imagesFolder.mkdirs();
                }
                Date date = new Date();
                File tempFile = new File(imagesFolder.getAbsolutePath() + "/" + date.getTime() + "attachmentPicture.jpg");
                Uri imageUri = data.getData();
                InputStream is = null;
                OutputStream os = null;
                try {
                    is = getContentResolver().openInputStream(imageUri);
                    os = new FileOutputStream(tempFile);
                    Utils.copyStream(is, os);
                    bitmap = Utils.decodeImageFile(tempFile, reqWidth, reqHeight);

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (os != null) {
                        try {
                            os.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                //compress image
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.JPEG, 70, baos);

                imageUploadProgress = new ProgressDialog(this);
                imageUploadProgress.setMessage("Sending");
                imageUploadProgress.show();
                //upload image to parse
                attachmentPicture = new ParseFile("attachmentPicture.jpg", baos.toByteArray());
                ParseObject attachedPhoto = new ParseObject("AttachedPhotos");
                attachedPhoto.put("attachedPhoto", attachmentPicture);
                attachedPhoto.saveInBackground(new SaveCallback() {

                    @Override
                    public void done(ParseException e) {
                        // TODO Auto-generated method stub
                        Log.d("url", attachmentPicture.getUrl() + "");
                        Date date = new Date();
                        pRef = ref.push();
                        Chat chat = new Chat(pRef.getName(), attachmentPicture.getUrl(), ParseUser.getCurrentUser().getObjectId(), date.getTime());
                        // Create a new, auto-generated child of that chat location, and save our chat data there
                        pRef.setValue(chat);
                        imageUploadProgress.dismiss();


                        // Find users near a given location
                        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                        userQuery.whereEqualTo("objectId", merchantUserId);

                        // Find devices associated with these users
                        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
                        //pushQuery.whereMatchesQuery("user", userQuery);
                        pushQuery.whereEqualTo("channels", "Publisher");
                        pushQuery.whereEqualTo("deviceId", merchantUserId);
                        try {
                            JSONObject data = new JSONObject("{\"header\": \"" + attachmentPicture.getUrl() + "\","
                                    + "\"action\": \"com.enormous.discover.publisher.UPDATE_STATUS\","
                                    + "\"myObjectId\": \"" + customerObjectId + "\","
                                    + "\"merchantUserId\": \"" + merchantUserId + "\","
                                    + "\"customerObjectId\": \"" + customerObjectId + "\","
                                    + "\"profilePictureUrl\": \"" + profilePictureUrl + "\","
                                    + "\"customerName\": \"" + ParseUser.getCurrentUser().getString("firstName") + " " + ParseUser.getCurrentUser().getString("lastName") + "\"}");
                            //Send Push notification
                            ParsePush push = new ParsePush();
                            push.setQuery(pushQuery); // Set our Installation query
                            //push.setChannel(customerObjectId+merchantUserId);
                            push.setData(data);
                            push.sendInBackground(new SendCallback() {

                                @Override
                                public void done(ParseException e) {
                                    // TODO Auto-generated method stub
                                    if (e == null) {
                                        //Toast.makeText(ChatMessageActivity.this, "DONE", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(ChatMessageActivity.this, "" + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
            //check if photo is clicked by camera
        } else if (requestCode == CHOOSE_CAMERA) {
            //set up progress dialog
            if (resultCode != 0) {
                Bitmap bitmap = null;
                int reqWidth = 500;
                int reqHeight = 500;
                File imagesFolder = new File(DiscoverApp.EXTERNAL_CACHE_DIR.getAbsolutePath() + "/images");
                if (!imagesFolder.exists()) {
                    imagesFolder.mkdirs();
                }
                File tempFile = new File(imagesFolder.getAbsolutePath() + "/" + dateCamera.getTime() + "attachmentPicture.jpg");
                //BITMAP = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
                bitmap = Utils.decodeImageFile(tempFile, reqWidth, reqHeight);
                //compress image
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(CompressFormat.JPEG, 70, baos);

                imageUploadProgress = new ProgressDialog(this);
                imageUploadProgress.setMessage("Sending");
                imageUploadProgress.show();
                //upload image to parse
                attachmentPicture = new ParseFile("attachmentPicture.jpg", baos.toByteArray());
                ParseObject attachedPhoto = new ParseObject("AttachedPhotos");
                attachedPhoto.put("attachedPhoto", attachmentPicture);
                attachedPhoto.saveInBackground(new SaveCallback() {

                    @Override
                    public void done(ParseException e) {
                        // TODO Auto-generated method stub
                        Log.d("url", attachmentPicture.getUrl() + "");
                        Date date = new Date();
                        pRef = ref.push();
                        Chat chat = new Chat(pRef.getName(), attachmentPicture.getUrl(), ParseUser.getCurrentUser().getObjectId(), date.getTime());
                        // Create a new, auto-generated child of that chat location, and save our chat data there
                        pRef.setValue(chat);
                        imageUploadProgress.dismiss();

                        // Find users near a given location
                        ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                        userQuery.whereEqualTo("objectId", merchantUserId);

                        // Find devices associated with these users
                        ParseQuery<ParseInstallation> pushQuery = ParseInstallation.getQuery();
                        //pushQuery.whereMatchesQuery("user", userQuery);
                        pushQuery.whereEqualTo("channels", "Publisher");
                        pushQuery.whereEqualTo("deviceId", merchantUserId);

                        try {
                            JSONObject data = new JSONObject("{\"header\": \"" + attachmentPicture.getUrl() + "\","
                                    + "\"action\": \"com.enormous.discover.publisher.UPDATE_STATUS\","
                                    + "\"myObjectId\": \"" + customerObjectId + "\","
                                    + "\"merchantUserId\": \"" + merchantUserId + "\","
                                    + "\"customerObjectId\": \"" + customerObjectId + "\","
                                    + "\"profilePictureUrl\": \"" + profilePictureUrl + "\","
                                    + "\"customerName\": \"" + ParseUser.getCurrentUser().getString("firstName") + " " + ParseUser.getCurrentUser().getString("lastName") + "\"}");
                            //Send Push notification
                            ParsePush push = new ParsePush();
                            //push.setChannel(customerObjectId+merchantUserId);
                            push.setQuery(pushQuery);
                            push.setData(data);
                            push.sendInBackground(new SendCallback() {

                                @Override
                                public void done(ParseException e) {
                                    // TODO Auto-generated method stub
                                    if (e == null) {
                                        //Toast.makeText(ChatMessageActivity.this, "DONE", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(ChatMessageActivity.this, "" + e.toString(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    protected void onNewIntent(Intent intent) {

        getIntentData(intent);
        customerObjectId = ParseUser.getCurrentUser().getObjectId();
        // Setup our Firebase ref
        ref = new Firebase(FIREBASE_URL).child(customerObjectId + merchantUserId);

        getActionBar().setTitle(merchantName);

        ParsePushNotificationReceiver.openedConfession = customerObjectId + merchantUserId;

        onStart();

        super.onNewIntent(intent);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub

        ParsePushNotificationReceiver.openedConfession = "";

        super.onPause();
    }
}
