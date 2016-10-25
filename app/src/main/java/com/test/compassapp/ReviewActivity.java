package com.test.compassapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import static android.os.Environment.DIRECTORY_PICTURES;

public class ReviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        recyclerView.setAdapter(new CaptureAdapter());
    }

    class CaptureAdapter extends RecyclerView.Adapter<CaptureAdapter.ViewHolder> {

        private File[] files;

        public class ViewHolder extends RecyclerView.ViewHolder {
            protected TextView textView;
            protected ImageView imageView;

            public ViewHolder(View view) {
                super(view);
                this.textView = (TextView) view.findViewById(R.id.label);
                this.imageView = (ImageView) view.findViewById(R.id.image);
            }
        }

        public CaptureAdapter() {

            File dir = getApplicationContext().getExternalFilesDir(DIRECTORY_PICTURES);
            files = dir.listFiles();
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File lhs, File rhs) {
                    return (int) (rhs.lastModified() - lhs.lastModified());
                }
            });
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_capture, parent, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ArrayList<File> uris = new ArrayList<>();
                    uris.add((File) v.getTag());
                    String title = DeviceUtil.getDeviceName();
                    Util.emailLogs(ReviewActivity.this, title, "", uris);
                }
            });
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            File file = files[position];
            holder.textView.setText(DateUtils.getRelativeTimeSpanString(file.lastModified()));
            Picasso.with(getApplicationContext())
                    .load(file)
                    .into(holder.imageView);
            holder.itemView.setTag(file);
        }

        @Override
        public int getItemCount() {
            return files.length;
        }
    }


}
