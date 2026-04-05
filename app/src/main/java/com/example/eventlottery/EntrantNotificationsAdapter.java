package com.example.eventlottery;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * List rows for EntrantNotificationsActivity.
 */
public class EntrantNotificationsAdapter extends ArrayAdapter<InAppNotification> {

    /**
     * Creates a new EntrantNotificationsAdapter.
     *
     * @param context the hosting context
     * @param items   list of in-app notifications to display
     */
    public EntrantNotificationsAdapter(Context context, ArrayList<InAppNotification> items) {
        super(context, 0, items);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.item_notification, parent, false);
        }

        InAppNotification n = getItem(position);
        if (n == null) {
            return view;
        }

        TextView title = view.findViewById(R.id.text_notification_title);
        TextView message = view.findViewById(R.id.text_notification_message);
        TextView time = view.findViewById(R.id.text_notification_time);

        title.setText(n.getTitle());
        message.setText(n.getMessage());
        DateFormat fmt = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        time.setText(fmt.format(new Date(n.getTimestampMillis())));

        title.setTypeface(null, n.isRead() ? Typeface.NORMAL : Typeface.BOLD);

        return view;
    }
}
