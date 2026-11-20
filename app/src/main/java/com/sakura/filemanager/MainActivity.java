
package com.sakura.filemanager;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import androidx.documentfile.provider.DocumentFile;
import java.util.*;

public class MainActivity extends Activity {

    private LinearLayout main, toolbar, grid;
    private EditText search;
    private TextView path;
    private DocumentFile root, current;
    private final int BLUE = Color.rgb(35, 165, 235);
    private final int PICK_FOLDER = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showHome();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private TextView text(String value, int size) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(Color.rgb(25, 25, 30));
        return t;
    }

    private void showHome() {
        main = new LinearLayout(this);
        main.setOrientation(1);
        main.setBackgroundColor(Color.rgb(246, 245, 250));
        main.setPadding(16, 12, 16, 8);

        TextView title = text("Sakura Files", 24);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        main.addView(title, new LinearLayout.LayoutParams(-1, 55));

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(16);
        search.setHint("⌕  Search files and folders");
        search.setPadding(16, 4, 16, 4);
        search.setBackground(shape(Color.WHITE, 35));
        main.addView(search, new LinearLayout.LayoutParams(-1, 48));

        path = text("Browse", 14);
        path.setPadding(4, 16, 4, 12);
        main.addView(path);

        ScrollView scroll = new ScrollView(this);
        grid = new LinearLayout(this);
        grid.setOrientation(1);
        scroll.addView(grid);

        main.addView(scroll, new LinearLayout.LayoutParams(
                -1, 0, 1));

        Button browse = new Button(this);
        browse.setText("📁  Browse phone storage");
        browse.setAllCaps(false);
        main.addView(browse);

        TextView bottom = text(
                "◷  Recents          ♟  Shared          📁  Browse", 14);
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(0, 16, 0, 12);
        main.addView(bottom);

        setContentView(main);

        browse.setOnClickListener(v -> pickFolder());

        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st,
                    int count, int after) {}
            public void onTextChanged(CharSequence s, int st,
                    int before, int count) {
                renderFiles();
            }
            public void afterTextChanged(android.text.Editable e) {}
        });

        renderFiles();
    }

    private void pickFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, PICK_FOLDER);
    }

    @Override
    protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);

        if (request == PICK_FOLDER && result == RESULT_OK
                && data != null) {
            Uri uri = data.getData();

            try {
                getContentResolver().takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION |
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                );
            } catch (Exception ignored) {}

            root = DocumentFile.fromTreeUri(this, uri);
            current = root;
            renderFiles();
        }
    }

    private void renderFiles() {
        if (grid == null) return;

        grid.removeAllViews();

        if (current == null) {
            path.setText("Browse");
            TextView empty = text(
                    "Choose a folder to start browsing your files.",
                    16);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(10, 50, 10, 50);
            grid.addView(empty);
            return;
        }

        path.setText(current.getName() == null
                ? "Phone Storage"
                : current.getName());

        DocumentFile[] files = current.listFiles();
        Arrays.sort(files, (a, b) -> {
            if (a.isDirectory() && !b.isDirectory()) return -1;
            if (!a.isDirectory() && b.isDirectory()) return 1;
            return String.valueOf(a.getName()).compareToIgnoreCase(
                    String.valueOf(b.getName()));
        });

        String query = search.getText().toString().toLowerCase();
        int columns = 3;

        LinearLayout row = null;
        int count = 0;

        for (DocumentFile file : files) {
            String name = file.getName() == null
                    ? "Unknown"
                    : file.getName();

            if (!name.toLowerCase().contains(query)) continue;

            if (count % columns == 0) {
                row = new LinearLayout(this);
                row.setGravity(Gravity.TOP);
                grid.addView(row, new LinearLayout.LayoutParams(
                        -1, -2));
            }

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(1);
            card.setGravity(Gravity.CENTER);
            card.setPadding(3, 10, 3, 10);

            TextView icon = text(
                    file.isDirectory() ? "📁" : "📄", 48);
            icon.setGravity(Gravity.CENTER);

            TextView label = text(name, 13);
            label.setGravity(Gravity.CENTER);
            label.setMaxLines(2);
            label.setEllipsize(android.text.TextUtils.TruncateAt.END);

            TextView info = text(
                    file.isDirectory()
                            ? file.listFiles().length + " items"
                            : android.text.format.Formatter
                                    .formatShortFileSize(this, file.length()),
                    11);
            info.setTextColor(Color.GRAY);
            info.setGravity(Gravity.CENTER);

            card.addView(icon);
            card.addView(label);
            card.addView(info);

            row.addView(card, new LinearLayout.LayoutParams(
                    0, 145, 1));

            card.setOnClickListener(v -> {
                if (file.isDirectory()) {
                    current = file;
                    renderFiles();
                } else {
                    openFile(file);
                }
            });

            count++;
        }

        if (count == 0) {
            TextView empty = text("No files found", 16);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 50, 0, 50);
            grid.addView(empty);
        }
    }

    private void openFile(DocumentFile file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mime = getContentResolver()
                    .getType(file.getUri());

            intent.setDataAndType(file.getUri(),
                    mime == null ? "*/*" : mime);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open this file",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (current != null && root != null
                && !current.getUri().equals(root.getUri())) {
            DocumentFile parent = current.getParentFile();
            if (parent != null) {
                current = parent;
                renderFiles();
                return;
            }
        }
        super.onBackPressed();
    }
}

