package com.example.blockchain;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.state.ToggleableState;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import com.example.blockchain.utils.KeyUtils;

public class LoginActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> fileChooserLauncherPublic;
    private ActivityResultLauncher<Intent> fileChooserLauncherPrivate;
    private boolean[] files;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.files = new boolean[2];

        Button btnSelectPublicKey = findViewById(R.id.btnSelectPublicKey);
        Button btnSelectPrivateKey = findViewById(R.id.btnSelectPrivateKey);
        Button btnCheck = findViewById(R.id.btnCheck);


        this.initializerPublic();
        this.initializerPrivate();

        btnSelectPublicKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooserPublic();
            }
        });
        btnSelectPrivateKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFileChooserPrivate();
            }
        });

        btnCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                manager();
            }
        });
    }

    private void showFileChooserPublic() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/vnd.exstream-package");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fileChooserLauncherPublic.launch(Intent.createChooser(intent, "Select a file to upload"));
    }

    private void showFileChooserPrivate() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pgp-keys");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fileChooserLauncherPrivate.launch(Intent.createChooser(intent, "Select a file to upload"));

    }

    private void initializerPublic() {
        fileChooserLauncherPublic = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ImageView ivTickPublic = findViewById(R.id.ivTickPublicKey);
                            ivTickPublic.setVisibility(View.VISIBLE);
                            files[0] = true;
                        }
                    }
                }
        );
    }

    private void initializerPrivate() {
        fileChooserLauncherPrivate = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            ImageView ivTickPrivateKey = findViewById(R.id.ivTickPrivateKey);
                            ivTickPrivateKey.setVisibility(View.VISIBLE);
                            files[1] = true;
                        }
                    }
                }
        );
    }

    private void manager() {
        if(this.files[0] && this.files[1]){
            //enviar los files al nodo;
            Toast.makeText(this, "Safety Checks Underway",
                    Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "Please, Insert all the files",
                    Toast.LENGTH_SHORT).show();
    }

    public static byte[] readBytesFromUri(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    private String getPathFromUri(Uri uri) {
        String path = uri.getPath();
        // Implementación para obtener la ruta del archivo en el sistema de archivos del dispositivo.
        // Nota: La implementación específica puede variar según el proveedor de contenido.
        return path;
    }
}

