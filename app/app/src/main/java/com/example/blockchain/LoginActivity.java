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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

import com.example.blockchain.users.Vote;
import com.example.blockchain.utils.KeyUtils;

public class LoginActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> fileChooserLauncherPublic;
    private ActivityResultLauncher<Intent> fileChooserLauncherPrivate;
    private boolean[] files;
    private Uri uriPublic;
    private Uri uriPrivate;


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
        //todo wtf android
        intent.setType("application/vnd.exstream-package");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fileChooserLauncherPublic.launch(Intent.createChooser(intent,
                "Select a file to upload"));
    }

    private void showFileChooserPrivate() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        //todo wtf android
        intent.setType("application/pgp-keys");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        fileChooserLauncherPrivate.launch(Intent.createChooser(intent,
                "Select a file to upload"));

    }

    private void initializerPublic() {
        fileChooserLauncherPublic = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            this.uriPublic = data.getData();
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
                            this.uriPrivate = data.getData();
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
            //send files to the node;
            Toast.makeText(this, "Safety Checks Underway",
                    Toast.LENGTH_SHORT).show();
            try {
                byte[] publicKey = this.readBytesFromUri(this, this.uriPublic);
                byte[] privateKey = this.readBytesFromUri(this, this.uriPrivate);

                PublicKey puk = KeyUtils.publicKeyReader(publicKey);
                PrivateKey prk = KeyUtils.privateKeyReader(privateKey);

                String num = String.valueOf((new Random().nextInt(34)));
                System.out.println(num);

                byte[] signature = sign(num, prk);
                System.out.println(verify(num, puk, signature));

            } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException |
                     SignatureException | InvalidKeyException  e) {
                System.out.println("Mal");
            }

        } else
            Toast.makeText(this, "Please, Insert all the files",
                    Toast.LENGTH_SHORT).show();
    }

    public byte[] readBytesFromUri(Context context, Uri uri) throws IOException {
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

    public static byte[] sign(String num, PrivateKey key) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(key);
        byte[] bytes = num.getBytes();
        signature.update(bytes);
        return signature.sign();
    }

    public static boolean verify(String num, PublicKey publicKey, byte[] givenSignature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);

        byte[] bytes = num.getBytes();
        signature.update(bytes);
        return signature.verify(givenSignature);
    }
}

