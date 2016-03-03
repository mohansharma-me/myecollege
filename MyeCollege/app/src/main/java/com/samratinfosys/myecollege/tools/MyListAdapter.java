package com.samratinfosys.myecollege.tools;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.samratinfosys.myecollege.R;
import com.samratinfosys.myecollege.json_classes.Chat;
import com.samratinfosys.myecollege.json_classes.ChatHead;
import com.samratinfosys.myecollege.json_classes.College;
import com.samratinfosys.myecollege.json_classes.MyJSON;
import com.samratinfosys.myecollege.json_classes.Timeline;
import com.samratinfosys.myecollege.tools.loader.ImageLoader;
import com.samratinfosys.myecollege.utils.Helper;

import java.util.List;

/**
 * Created by iAmMegamohan on 12-04-2015.
 */

public class MyListAdapter implements ListAdapter, ImageLoader.IImageLoader {

    public final static int IMAGE_COLLEGES=0;
    public final static int IMAGE_TIMELINE=1;

    private Context context;
    private AdapterFor adapterFor=AdapterFor.NONE;
    private List<?> dataObjects=null;
    private int itemLayout;

    public enum AdapterFor {
        NONE,
        COLLEGES,
        TIMELINES,
        MESSAGES,
        MYJSON_USERS,
        CHAT
    }

    public MyListAdapter(Context context, int itemLayout) {
        this.context=context;
        this.itemLayout=itemLayout;
    }

    public void setAdapterFor(AdapterFor adapterFor, List<?> dataObjects) {
        this.adapterFor = adapterFor;
        this.dataObjects=dataObjects;
    }

    public List<?> getDataObjects() {
        return dataObjects;
    }

    public AdapterFor getAdapterFor() {
        return adapterFor;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
            
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        if(dataObjects!=null)
            return dataObjects.size();
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(dataObjects!=null)
            return dataObjects.get(position);
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(itemLayout, parent, false);

        if(adapterFor==AdapterFor.COLLEGES)
            return initCollegeListItem(row,position);

        if(adapterFor==AdapterFor.TIMELINES)
            return initTimelineListItem(row,position);

        if(adapterFor==AdapterFor.MESSAGES)
            return initMessageListItem(row, position);

        if(adapterFor==AdapterFor.MYJSON_USERS)
            return initMyJsonUsers(row,position);

        if(adapterFor==AdapterFor.CHAT)
            return initChat(row,position);


        return row;
    }

    private View initChat(final View row, int position) {
        Chat chat = (Chat) getItem(position);
        if(chat==null) return null;

        TextView tvFrom=(TextView)row.findViewById(R.id.lblFrom);
        TextView tvTo=(TextView)row.findViewById(R.id.lblTo);

        if(chat.isReceived) {
            tvFrom.setText(chat.message.text);
            tvFrom.setVisibility(View.VISIBLE);
        } else {
            tvTo.setText(chat.message.text);
            tvTo.setVisibility(View.VISIBLE);
        }

        return row;
    }

    private View initMyJsonUsers(final View row, int position) {
        MyJSON myJSON = (MyJSON) getItem(position);
        if(myJSON==null || !myJSON.isReady()) return null;

        TextView tvTitle=(TextView)row.findViewById(R.id.listview_message_title);
        TextView tvHeading=(TextView)row.findViewById(R.id.listview_message_heading);
        ImageView imgImage=(ImageView)row.findViewById(R.id.listview_message_image);

        tvTitle.setText(myJSON.getString("name"));
        tvHeading.setText(myJSON.getString("account_user_id"));
        imgImage.setImageDrawable(context.getResources().getDrawable(R.drawable.hourglass));

        ImageLoader.downloadProfilePicture(imgImage, myJSON.getInt("profile_id",0), ImageLoader.ImageType.Icon,9,this).startLoader();

        return row;
    }

    private View initMessageListItem(final View row, int position) {
        ChatHead chatHead = (ChatHead) getItem(position);
        if(chatHead==null) return null;

        TextView tvTitle=(TextView)row.findViewById(R.id.listview_message_title);
        TextView tvHeading=(TextView)row.findViewById(R.id.listview_message_heading);
        ImageView imgImage=(ImageView)row.findViewById(R.id.listview_message_image);

        tvTitle.setText(chatHead.fromName);
        String msgLine=chatHead.messageLine!=null?chatHead.messageLine:"";
        tvHeading.setText(msgLine+" ("+chatHead.newCount+")");
        imgImage.setImageDrawable(context.getResources().getDrawable(R.drawable.hourglass));

        ImageLoader.downloadProfilePicture(imgImage, chatHead.fromId, ImageLoader.ImageType.Icon,9,this).startLoader();

        return row;
    }

    private View initTimelineListItem(final View row, int position) {
        final Timeline timeline=(Timeline)getItem(position);
        if(timeline==null) return null;

        TextView lblTimelineID=(TextView)row.findViewById(R.id.lblTimelineID);
        TextView lblHeading=(TextView)row.findViewById(R.id.lblHeading);
        final ImageView imgTimelineImage=(ImageView)row.findViewById(R.id.imgTimelineImage);
        final TextView lblTimelineText=(TextView)row.findViewById(R.id.lblTimelineText);
        TextView lblTimelineTime=(TextView)row.findViewById(R.id.lblTimelineTime);

        lblTimelineID.setText(timeline.id+"");
        if(timeline.timestamp!=null) {
            lblTimelineTime.setText(timeline.timestamp);
        } else {
            lblTimelineTime.setText("-");
        }
        if(timeline.data!=null) {
            if(timeline.data.data_heading!=null) {
                lblHeading.setText(timeline.data.data_heading);
            } else {
                lblHeading.setVisibility(View.GONE);
            }

            if(timeline.data.rawObject!=null) {

                String timelineText=timeline.data.rawObject.getString("timelineText");
                if(timelineText!=null) {
                    lblTimelineText.setText(timelineText);
                } else {
                    lblTimelineText.setVisibility(View.GONE);
                }


                Bitmap timelineImage=null;
                try {
                    String bitmapData=timeline.data.rawObject.getString("timelineImage");
                    if(bitmapData!=null) {
                        timelineImage=Helper.Images.ReadBitmap(row.getContext(),Base64.decode(bitmapData.getBytes(),Base64.DEFAULT),128,128,true);
                    }
                    bitmapData=null;
                } catch(Exception ex) {
                    Helper.Log(ex,"timelineImage: setting it");
                }

                if(timelineImage!=null) {
                    imgTimelineImage.setImageBitmap(timelineImage);
                    imgTimelineImage.setVisibility(View.VISIBLE);
                } else {
                    imgTimelineImage.setVisibility(View.GONE);
                }


            }

        }
        return row;
    }

    private View initCollegeListItem(View row, int position) {
        College college= (College) getItem(position);
        if(college==null) return null;

        TextView tvTitle=(TextView)row.findViewById(R.id.listview_item1_title);
        TextView tvHeading=(TextView)row.findViewById(R.id.listview_item1_heading);
        ImageView imgImage=(ImageView)row.findViewById(R.id.listview_item1_image);

        tvTitle.setText(college.full_name);
        tvHeading.setText("("+college.code+") - "+college.city);
        imgImage.setImageDrawable(context.getResources().getDrawable(R.drawable.hourglass));

        ImageLoader.downloadCollegeLogo(imgImage,college.id, ImageLoader.ImageType.Icon,9,this).startLoader();

        return row;
    }


    @Override
    public void imageStatus(int imageId, int percentage) {

    }

    @Override
    public void imageUpdated(int imageId, ImageLoader imageLoader) {
        if(imageLoader.success && imageLoader.imageView!=null && imageLoader.bitmap!=null) {
            if(adapterFor==AdapterFor.MESSAGES)
                imageLoader.imageView.setBackgroundColor(imageLoader.imageView.getContext().getResources().getColor(R.color.MyTheme_colors_transparent));
            imageLoader.imageView.setImageBitmap(imageLoader.bitmap);
        } else {
            if(adapterFor==AdapterFor.MESSAGES) {
                imageLoader.imageView.setBackgroundColor(imageLoader.imageView.getContext().getResources().getColor(R.color.MyTheme_colors_black33));
                imageLoader.imageView.setImageDrawable(imageLoader.imageView.getContext().getResources().getDrawable(R.drawable.ic_action_person));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return ListAdapter.IGNORE_ITEM_VIEW_TYPE;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        if(dataObjects!=null)
            return dataObjects.size()==0;
        return true;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}
