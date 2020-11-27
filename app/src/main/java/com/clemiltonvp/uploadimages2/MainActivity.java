package com.clemiltonvp.uploadimages2;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.clemiltonvp.uploadimages2.model.Upload;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class MainActivity extends AppCompatActivity {

    private Button btnAbrir, btnUpload;
    private EditText editNome;
    private TextView txtMostrar;
    private ImageView imageView;
    private ProgressBar progressBar;
    private Uri imageUri;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;
    //Singleton
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    public static final int PICK_IMAGE_GALLERY=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAbrir = findViewById(R.id.btnAbrir);
        btnUpload = findViewById(R.id.btnUpload);
        editNome = findViewById(R.id.editNome);
        txtMostrar = findViewById(R.id.txtMostrar);
        imageView = findViewById(R.id.imageView);
        progressBar = findViewById(R.id.progressBar);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Verificar se o usuario nãa esta logado!
        if(auth.getCurrentUser()==null){
            //Fazer o login com usuario admin
            auth.signInWithEmailAndPassword("admin@gmail.com","admin123")
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(getApplicationContext(),
                                        "Logado!",
                                        Toast.LENGTH_LONG
                                ).show();
                            }else{
                                Toast.makeText(getApplicationContext(),
                                        "Não consegui fazer o login!",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                    });
        }



        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        databaseReference = FirebaseDatabase.getInstance().getReference("uploads");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        btnAbrir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                abrirImagem();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnUpload.setClickable(false);
                uploadImagem();
            }
        });
    }
    public void uploadImagem(){
        if(imageUri==null){
            Toast.makeText(this,"Selecione uma imagem!",Toast.LENGTH_LONG).show();
            btnUpload.setClickable(true);
            return;
        }
        // Criando o nome da imagem p/ upload
        StorageReference imagemRef = storageReference
                .child(System.currentTimeMillis()+"."+getFileExtension(imageUri));
        //Fazendo o upload da Imagem
        //Upload da imagem no STORAGE
        UploadTask uploadTask = imagemRef.putFile(imageUri);

        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                btnUpload.setClickable(true);

                taskSnapshot.getStorage().getDownloadUrl()
                        .addOnSuccessListener(MainActivity.this, new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                //Log.i("URI",uri.toString());
                                adicionaNoUriDatabase(uri);
                            }

                        });
            }
        });
        uploadTask.addOnFailureListener(MainActivity.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });

        //  uploadTask.addOnFailureListener(MainActivity.this, new )
    }

    public void adicionaNoUriDatabase(Uri uri){
        //databaseReference.push().child("uri").setValue(uri.toString());
        String nome = editNome.getText().toString();
        Upload upload = new Upload(nome,uri.toString());


        databaseReference.push().setValue(upload);
    }


    private String getFileExtension(Uri uri){
        ContentResolver cr = getContentResolver();
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(cr.getType(uri));
    }




    public void abrirImagem(){
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");

        startActivityForResult(intent,PICK_IMAGE_GALLERY);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*Toast.makeText(this,
                "requestCode: "+requestCode+ "\n"+"resultCode: "+
                        resultCode,Toast.LENGTH_LONG).show();*/
        if(requestCode==PICK_IMAGE_GALLERY && resultCode==RESULT_OK
                && data!=null && data.getData()!=null
        ){

            imageUri = data.getData();
            Toast.makeText(this,imageUri.toString(),Toast.LENGTH_LONG).show();
            Glide.with(MainActivity.this).load(imageUri).into(imageView);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Toast.makeText(this,"Settings",Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}