package com.example.photoviewer;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS = "photoviewer_prefs";
    private static final String KEY_URL = "site_url";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_AUTHOR = "author_name";

    private static final String LOCAL_URL = "http://10.0.2.2:8000";
    private static final String CLOUD_URL = "https://elponchis.pythonanywhere.com";

    private TextView textView;
    private TextView textServer;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;

    private final List<PostItem> postList = new ArrayList<>();
    private ImageAdapter adapter;

    private String siteUrl;
    private String token;
    private String authorName;

    private Uri pickedImageUri;
    private ImageView dialogPreview;
    private TextView dialogFileName;
    private ActivityResultLauncher<String> imagePicker;

    private CLoadImage taskDownload;
    private UploadPost taskUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        textServer = findViewById(R.id.textServer);
        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);

        adapter = new ImageAdapter(postList, this::showFullScreen);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(() -> onClickDownload(null));

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    pickedImageUri = uri;
                    if (dialogPreview != null) {
                        dialogPreview.setImageURI(uri);
                    }
                    if (dialogFileName != null) {
                        dialogFileName.setText(uri.getLastPathSegment());
                    }
                });

        loadSettings();
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        siteUrl = prefs.getString(KEY_URL, LOCAL_URL);
        token = prefs.getString(KEY_TOKEN, "");
        authorName = prefs.getString(KEY_AUTHOR, "");
        textServer.setText(siteUrl);
    }

    private void saveSettings(String url, String newToken) {
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_URL, url)
                .putString(KEY_TOKEN, newToken)
                .apply();
        loadSettings();
    }

    private void saveAuthor(String author) {
        authorName = author;
        getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_AUTHOR, author)
                .apply();
    }

    // 동기화 버튼
    public void onClickDownload(View v) {
        if (taskDownload != null && taskDownload.getStatus() == AsyncTask.Status.RUNNING) {
            taskDownload.cancel(true);
        }

        taskDownload = new CLoadImage();
        taskDownload.execute(siteUrl + "/api_root/Post/");
    }

    // 새로운 이미지 게시 버튼
    public void onClickUpload(View v) {
        pickedImageUri = null;

        View form = getLayoutInflater().inflate(R.layout.dialog_upload, null);
        EditText editAuthor = form.findViewById(R.id.editAuthor);
        EditText editTitle = form.findViewById(R.id.editTitle);
        EditText editText = form.findViewById(R.id.editText);
        Button btnPick = form.findViewById(R.id.btnPickImage);
        dialogPreview = form.findViewById(R.id.imagePreview);
        dialogFileName = form.findViewById(R.id.textFileName);

        editAuthor.setText(authorName);
        btnPick.setOnClickListener(b -> imagePicker.launch("image/*"));

        new AlertDialog.Builder(this)
                .setTitle("새로운 이미지 게시")
                .setView(form)
                .setPositiveButton("게시", (d, w) -> {
                    String author = editAuthor.getText().toString().trim();
                    String title = editTitle.getText().toString().trim();
                    String body = editText.getText().toString().trim();

                    if (author.isEmpty()) {
                        toast("작성자를 입력하세요");
                        return;
                    }
                    if (title.isEmpty()) {
                        toast("제목을 입력하세요");
                        return;
                    }
                    if (pickedImageUri == null) {
                        toast("이미지를 선택하세요");
                        return;
                    }
                    if (token.isEmpty()) {
                        toast("설정에서 API 토큰을 입력하세요");
                        return;
                    }

                    saveAuthor(author);
                    taskUpload = new UploadPost(pickedImageUri);
                    taskUpload.execute(title, body, author);
                })
                .setNegativeButton("취소", null)
                .setOnDismissListener(d -> {
                    dialogPreview = null;
                    dialogFileName = null;
                })
                .show();
    }

    // 설정 버튼
    public void onClickSettings(View v) {
        View form = getLayoutInflater().inflate(R.layout.dialog_settings, null);
        EditText editUrl = form.findViewById(R.id.editUrl);
        EditText editToken = form.findViewById(R.id.editToken);

        editUrl.setText(siteUrl);
        editToken.setText(token);

        form.findViewById(R.id.btnPresetLocal)
                .setOnClickListener(b -> editUrl.setText(LOCAL_URL));
        form.findViewById(R.id.btnPresetCloud)
                .setOnClickListener(b -> editUrl.setText(CLOUD_URL));

        new AlertDialog.Builder(this)
                .setTitle("서버 설정")
                .setView(form)
                .setPositiveButton("저장", (d, w) -> {
                    String url = editUrl.getText().toString().trim();
                    if (url.endsWith("/")) {
                        url = url.substring(0, url.length() - 1);
                    }
                    saveSettings(url, editToken.getText().toString().trim());
                    toast("저장되었습니다");
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showFullScreen(PostItem item) {
        ImageView view = new ImageView(this);
        view.setImageBitmap(item.getImage());
        view.setAdjustViewBounds(true);

        new AlertDialog.Builder(this)
                .setTitle(item.getTitle())
                .setView(view)
                .setPositiveButton("닫기", null)
                .show();
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    private class CLoadImage extends AsyncTask<String, Integer, List<PostItem>> {

        private String error = null;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(0);
            textView.setText("동기화 중...");
        }

        @Override
        protected List<PostItem> doInBackground(String... urls) {
            List<PostItem> result = new ArrayList<>();

            try {
                String apiUrl = urls[0];
                URL urlAPI = new URL(apiUrl);
                HttpURLConnection conn = (HttpURLConnection) urlAPI.openConnection();
                if (!token.isEmpty()) {
                    conn.setRequestProperty("Authorization", "Token " + token);
                }
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);

                int responseCode = conn.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    error = "서버 응답 " + responseCode;
                    return result;
                }

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                is.close();

                JSONArray aryJson = new JSONArray(sb.toString());

                for (int i = 0; i < aryJson.length(); i++) {
                    if (isCancelled()) {
                        break;
                    }

                    JSONObject postJson = aryJson.getJSONObject(i);
                    String imageUrl = postJson.optString("image", "");

                    if (imageUrl.isEmpty() || "null".equals(imageUrl)) {
                        continue;
                    }

                    HttpURLConnection imgConn =
                            (HttpURLConnection) new URL(imageUrl).openConnection();
                    imgConn.setConnectTimeout(5000);
                    imgConn.setReadTimeout(5000);
                    InputStream imgStream = imgConn.getInputStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(imgStream);
                    imgStream.close();

                    if (bitmap != null) {
                        result.add(new PostItem(
                                postJson.optString("author_name", ""),
                                postJson.optString("title", ""),
                                postJson.optString("text", ""),
                                postJson.optString("published_date", ""),
                                bitmap));
                    }

                    publishProgress((i + 1) * 100 / aryJson.length());
                }

            } catch (IOException | JSONException e) {
                error = e.getMessage();
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(List<PostItem> images) {
            progressBar.setVisibility(View.GONE);
            swipeRefresh.setRefreshing(false);

            if (error != null) {
                textView.setText("동기화 실패: " + error);
                return;
            }

            if (images.isEmpty()) {
                textView.setText("불러올 이미지가 없습니다.");
            } else {
                textView.setText("이미지 로드 성공! (" + images.size() + "개)");
            }

            postList.clear();
            postList.addAll(images);
            adapter.notifyDataSetChanged();
        }
    }

    private class UploadPost extends AsyncTask<String, Void, Boolean> {

        private final Uri imageUri;
        private String message = "";

        UploadPost(Uri imageUri) {
            this.imageUri = imageUri;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setIndeterminate(true);
            textView.setText("업로드 중...");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String title = params[0];
            String text = params[1];
            String author = params[2];
            String boundary = "----PhotoViewer" + System.currentTimeMillis();
            String lineEnd = "\r\n";

            HttpURLConnection conn = null;

            try {
                URL url = new URL(siteUrl + "/api_root/Post/");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);
                conn.setRequestProperty("Authorization", "Token " + token);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + boundary);

                String now = new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ssZ", Locale.KOREA).format(new Date());

                DataOutputStream out = new DataOutputStream(conn.getOutputStream());

                writeField(out, boundary, "author_name", author);
                writeField(out, boundary, "title", title);
                writeField(out, boundary, "text", text);
                writeField(out, boundary, "created_date", now);
                writeField(out, boundary, "published_date", now);

                out.writeBytes("--" + boundary + lineEnd);
                out.writeBytes("Content-Disposition: form-data; name=\"image\"; "
                        + "filename=\"upload.jpg\"" + lineEnd);
                out.writeBytes("Content-Type: image/jpeg" + lineEnd);
                out.writeBytes(lineEnd);

                InputStream in = getContentResolver().openInputStream(imageUri);
                byte[] buffer = new byte[8192];
                int read;
                while (in != null && (read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                if (in != null) {
                    in.close();
                }

                out.writeBytes(lineEnd);
                out.writeBytes("--" + boundary + "--" + lineEnd);
                out.flush();
                out.close();

                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_CREATED || code == HttpURLConnection.HTTP_OK) {
                    message = "게시 완료";
                    return true;
                }

                message = "게시 실패 (" + code + ") " + readError(conn);
                return false;

            } catch (IOException e) {
                message = "게시 실패: " + e.getMessage();
                e.printStackTrace();
                return false;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }

        private void writeField(DataOutputStream out, String boundary, String name, String value)
                throws IOException {
            out.writeBytes("--" + boundary + "\r\n");
            out.writeBytes("Content-Disposition: form-data; name=\"" + name + "\"\r\n");
            out.writeBytes("Content-Type: text/plain; charset=UTF-8\r\n");
            out.writeBytes("\r\n");
            out.write(value.getBytes("UTF-8"));
            out.writeBytes("\r\n");
        }

        private String readError(HttpURLConnection conn) {
            try (InputStream es = conn.getErrorStream()) {
                if (es == null) {
                    return "";
                }
                BufferedReader reader = new BufferedReader(new InputStreamReader(es, "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                return sb.toString();
            } catch (IOException e) {
                return "";
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            progressBar.setIndeterminate(false);
            progressBar.setVisibility(View.GONE);
            textView.setText(message);
            toast(message);

            if (success) {
                onClickDownload(null);
            }
        }
    }
}
