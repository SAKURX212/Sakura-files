package com.sakura.filemanager;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.documentfile.provider.DocumentFile;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private LinearLayout root, content, grid;
    private EditText search;
    private TextView locationTitle, emptyText;
    private DocumentFile currentFolder;
    private DocumentFile rootFolder;
    private final List<DocumentFile> files = new ArrayList<>();
    private static final int PICK_FOLDER = 100;

    private int blue = Color.rgb(55, 130, 255);
    private int dark = Color.rgb(35, 43, 58);
    private int muted = Color.rgb(130, 140, 155);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(Color.rgb(242, 246, 253));
        getWindow().setNavigationBarColor(Color.rgb(242, 246, 253));
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR |
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );

        buildInterface();
    }

    private GradientDrawable shape(int color, int radius) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(radius);
        return d;
    }

    private GradientDrawable glassBackground() {
        GradientDrawable d = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.WHITE, Color.rgb(235, 243, 255)}
        );
        d.setCornerRadius(32);
        d.setStroke(1, Color.rgb(225, 233, 246));
        return d;
    }

    private TextView text(String value, int size, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return t;
    }

    private void buildInterface() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(20, 18, 20, 12);
        root.setBackgroundColor(Color.rgb(242, 246, 253));
        setContentView(root);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setOrientation(LinearLayout.HORIZONTAL);

        TextView title = text("Sakura Files", 29, dark, true);
        header.addView(title, new LinearLayout.LayoutParams(0, 55, 1));

        TextView profile = text("✿", 25, blue, true);
        profile.setGravity(Gravity.CENTER);
        profile.setBackground(glassBackground());
        header.addView(profile, new LinearLayout.LayoutParams(48, 48));

        root.addView(header);

        TextView subtitle = text("Your files, beautifully organised.", 13, muted, false);
        root.addView(subtitle);

        search = new EditText(this);
        search.setSingleLine(true);
        search.setTextSize(15);
        search.setHint("⌕   Search files and folders");
        search.setHintTextColor(muted);
        search.setPadding(18, 0, 18, 0);
        search.setBackground(glassBackground());

        LinearLayout.LayoutParams searchParams =
                new LinearLayout.LayoutParams(-1, 54);
        searchParams.setMargins(0, 22, 0, 20);
        root.addView(search, searchParams);

        locationTitle = text("Browse", 21, dark, true);
        root.addView(locationTitle);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, 14, 0, 0);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        LinearLayout bottom = new LinearLayout(this);
        bottom.setGravity(Gravity.CENTER);
        bottom.setPadding(8, 10, 8, 10);
        bottom.setBackground(glassBackground());

        TextView browse = navItem("▦\nBrowse", true);
        TextView recents = navItem("◷\nRecents", false);
        TextView shared = navItem("♧\nShared", false);

        bottom.addView(browse, new LinearLayout.LayoutParams(0, 55, 1));
        bottom.addView(recents, new LinearLayout.LayoutParams(0, 55, 1));
        bottom.addView(shared, new LinearLayout.LayoutParams(0, 55, 1));

        LinearLayout.LayoutParams bottomParams =
                new LinearLayout.LayoutParams(-1, 72);
        bottomParams.setMargins(0, 12, 0, 0);
        root.addView(bottom, bottomParams);

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) {
                showFiles();
            }
            @Override public void afterTextChanged(Editable e) {}
        });

        showHome();
    }

    private TextView navItem(String value, boolean selected) {
        TextView t = text(value, 13, selected ? blue : muted, true);
        t.setGravity(Gravity.CENTER);
        return t;
    }

    private void showHome() {
        content.removeAllViews();

        TextView heading = text("Locations", 16, dark, true);
        content.addView(heading);

        addLocationCard("Phone Storage", "Browse folders and files", "📱", true);
        addLocationCard("Choose a Folder", "Select a folder from your device", "📂", false);

        if (rootFolder != null) {
            addLocationCard("Continue Browsing", "Open your selected storage", "✨", true);
        }

        TextView note = text(
                "Your files stay on your device. Choose a folder to get started.",
                13, muted, false
        );
        note.setPadding(4, 22, 4, 0);
        content.addView(note);
    }

    private void addLocationCard(String name, String description,
                                 String emoji, boolean openSaved) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(16, 12, 16, 12);
        card.setBackground(glassBackground());

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, 82);
        params.setMargins(0, 12, 0, 0);
        content.addView(card, params);

        TextView icon = text(emoji, 27, blue, true);
        icon.setGravity(Gravity.CENTER);
        card.addView(icon, new LinearLayout.LayoutParams(48, 48));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(12, 0, 0, 0);

        labels.addView(text(name, 15, dark, true));
        labels.addView(text(description, 12, muted, false));

        card.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(text("›", 28, muted, false));

        card.setOnClickListener(v -> {
            if (openSaved && rootFolder != null) {
                currentFolder = rootFolder;
                showFiles();
            } else {
                chooseFolder();
            }
        });
    }

    private void chooseFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
        );
        startActivityForResult(intent, PICK_FOLDER);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FOLDER && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                int flags = data.getFlags() &
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION |
                         Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                getContentResolver().takePersistableUriPermission(uri, flags);

                rootFolder = DocumentFile.fromTreeUri(this, uri);
                currentFolder = rootFolder;
                showFiles();
            }
        }
    }

    private void showFiles() {
        if (currentFolder == null) {
            showHome();
            return;
        }

        content.removeAllViews();

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);

        TextView back = text("‹  Back", 14, blue, true);
        back.setPadding(0, 0, 14, 0);
        back.setOnClickListener(v -> {
            if (currentFolder != null && currentFolder.getParentFile() != null) {
                currentFolder = currentFolder.getParentFile();
                showFiles();
            } else {
                currentFolder = null;
                showHome();
            }
        });
        top.addView(back);

        TextView folderName = text(
                currentFolder.getName() == null ? "Files" : currentFolder.getName(),
                19, dark, true
        );
        top.addView(folderName);

        content.addView(top);

        TextView storage = text("Folders and files", 13, muted, false);
        storage.setPadding(0, 8, 0, 14);
        content.addView(storage);

        grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        content.addView(grid);

        emptyText = text("This folder is empty.", 14, muted, false);
        emptyText.setGravity(Gravity.CENTER);
        emptyText.setPadding(0, 40, 0, 0);

        showGridItems();
    }

    private void showGridItems() {
        grid.removeAllViews();
        files.clear();

        DocumentFile[] items = currentFolder.listFiles();
        String query = search.getText().toString().toLowerCase().trim();

        for (DocumentFile file : items) {
            String name = file.getName() == null ? "Unnamed" : file.getName();

            if (!name.toLowerCase().contains(query)) continue;
            files.add(file);
        }

        if (files.isEmpty()) {
            grid.addView(emptyText);
            return;
        }

        for (DocumentFile file : files) {
            addFileRow(file);
        }
    }

    private void addFileRow(DocumentFile file) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(14, 10, 14, 10);
        row.setBackground(glassBackground());

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(-1, 68);
        params.setMargins(0, 0, 0, 10);
        grid.addView(row, params);

        TextView icon = text(file.isDirectory() ? "📁" : "📄", 25, blue, false);
        row.addView(icon, new LinearLayout.LayoutParams(42, 48));

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setPadding(10, 0, 0, 0);

        labels.addView(text(
                file.getName() == null ? "Unnamed" : file.getName(),
                14, dark, true
        ));

        labels.addView(text(
                file.isDirectory() ? "Folder" : "File",
                11, muted, false
        ));

        row.addView(labels, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(text("›", 24, muted, false));

        row.setOnClickListener(v -> {
            if (file.isDirectory()) {
                currentFolder = file;
                showFiles();
            } else {
                openFile(file);
            }
        });
    }

    private void openFile(DocumentFile file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(file.getUri(), getContentResolver().getType(file.getUri()));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "No app found to open this file.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFolder != null && currentFolder.getParentFile() != null) {
            currentFolder = currentFolder.getParentFile();
            showFiles();
        } else {
            currentFolder = null;
            showHome();
        }
    }
}
