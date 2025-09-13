package com.example.itemcrudns;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private final String mJSONURLString = "http://172.34.98.64:8000/api/items";
    private final String imgUrl = "http://172.34.98.64:8000/storage/";
    private Bitmap bitmap;

    public ImageView imageView;

    private String imagePath;
    private String fileName;
    private ActivityResultLauncher<Intent> startActivityIntent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mContext = getApplicationContext();
        Button btnSearch = findViewById(R.id.search);
        EditText desc = findViewById(R.id.description);
        EditText cost = findViewById(R.id.cost);
        EditText sell = findViewById(R.id.sell);
        EditText itemId = findViewById(R.id.item_no);
        EditText imgName =  findViewById(R.id.imageName);
        ImageView imageView =  findViewById(R.id.imageView);
        Button delete =  findViewById(R.id.btnDelete);
        Button save = findViewById(R.id.save);
        Button btnUpdate =  findViewById(R.id.update);
        Button buttonChoose = findViewById(R.id.buttonChoose);
        Button btnView =  findViewById(R.id.view_button);

        startActivityIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Log.i("result", result.toString());
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Intent data = result.getData();
                            if (data != null) {

                                Uri filePath = data.getData();
                                String path = filePath.getPath();

                                fileName = path.substring(path.lastIndexOf("/")+1);
//                                fileName = filePath.getLastPathSegment();
                                Log.i("file", "file://" + filePath.toString());
                                Log.i("content", filePath.getPath().toString());
                                Log.i("pic", new File(filePath.getPath()).toString());
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                Picasso.get().setLoggingEnabled(true);
                                Picasso.get().load(filePath).fit().centerCrop().into(imageView);
                            }
                        }
                    }
                });

        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
//                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                startActivityIntent.launch(Intent.createChooser(intent, "Select Picture"));
//                Intent galleryIntent= new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//                startActivityIntent.launch(galleryIntent);
            }

        });

        btnSearch.setOnClickListener(view -> {
            String urlString = mJSONURLString+"/"+itemId.getText();
            Log.i("url","url"+ urlString);
            // Initialize a new RequestQueue instance
            RequestQueue requestQueue = Volley.newRequestQueue(mContext);
            // Initialize a new JsonObjectRequest instance
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    urlString,
                    null,
                    response -> {
                        Log.i("json",String.valueOf(response));
                        try {
                            String description = response.getString("description");
                            String item_cost = response.getString("cost_price");
                            String item_sell = response.getString("sell_price");
                            String image_url = imgUrl + response.getString("img_path");

                            // Display the formatted json data in text view
                            desc.setText(description);
                            cost.setText(item_cost);
                            sell.setText(item_sell);
                            Picasso.get()
                                    .load(image_url)
                                    .into(imageView);
                            //  }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        // Do something when error occurred
                        Log.e("error :",error.getMessage());
                    });

            // Add JsonObjectRequest to the RequestQueue
            requestQueue.add(jsonObjectRequest);
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("url","url"+ mJSONURLString);
                JSONObject jsonItem = new JSONObject();
                try {
                    jsonItem.put("description", desc.getText());
                    jsonItem.put("sell_price", sell.getText());
                    jsonItem.put("cost_price", cost.getText());
//                    jsonItem.put("img_path",imgName.getText().toString().trim());
//                    jsonItem.put("img_path",fileName);
                    jsonItem.put("uploads", getStringImage(bitmap));
                    //Log.i("url","url"+ jsonItem.toString());
                    Log.d("tag", jsonItem.toString(4));
                }catch (JSONException e) {
                    e.printStackTrace();
                }
                // Initialize a new RequestQueue instance
                RequestQueue requestQueue = Volley.newRequestQueue(mContext);

                // Initialize a new JsonObjectRequest instance
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                        Request.Method.POST,
                        mJSONURLString,
                        jsonItem,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try{
                                    String status = response.getString("status");
                                    String message = response.getString("message");
                                    Toast.makeText(getApplicationContext(),message, Toast.LENGTH_LONG).show();

                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener(){
                            @Override
                            public void onErrorResponse(VolleyError error){
                                // Do something when error occurred
                                Toast.makeText(getApplicationContext(),error.toString(), Toast.LENGTH_LONG).show();
                                Log.i("error", error.toString());
                            }
                        });
                jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                        0,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                // Add JsonObjectRequest to the RequestQueue
                requestQueue.add(jsonObjectRequest);
            }
        });

    }

    private String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }


}