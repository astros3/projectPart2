package com.example.eventlottery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

/**
 * Comment list adapter. Shows a delete (dustbin) button on each row only in organizer mode.
 */
public class CommentAdapter extends ArrayAdapter<String> {

    /** Callback fired when the delete button on a comment row is clicked. */
    public interface OnDeleteClickListener {
        /**
         * Called when the user requests to delete the comment at the given position.
         *
         * @param position adapter position of the comment to delete
         */
        void onDelete(int position);
    }

    private final boolean organizerMode;
    private final OnDeleteClickListener deleteListener;

    /**
     * Creates a new CommentAdapter.
     *
     * @param context        the hosting context
     * @param texts          list of comment strings to display
     * @param organizerMode  when true a delete button is shown on each row
     * @param deleteListener callback invoked when the delete button is tapped
     */
    public CommentAdapter(@NonNull Context context,
                          @NonNull List<String> texts,
                          boolean organizerMode,
                          @NonNull OnDeleteClickListener deleteListener) {
        super(context, 0, texts);
        this.organizerMode = organizerMode;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_comment, parent, false);
        }

        TextView textComment = convertView.findViewById(R.id.textComment);
        ImageButton btnDelete = convertView.findViewById(R.id.btnDeleteComment);

        String text = getItem(position);
        textComment.setText(text != null ? text : "");

        if (organizerMode) {
            btnDelete.setVisibility(View.VISIBLE);
            int pos = position;
            btnDelete.setOnClickListener(v -> deleteListener.onDelete(pos));
        } else {
            btnDelete.setVisibility(View.GONE);
            btnDelete.setOnClickListener(null);
        }

        return convertView;
    }
}
