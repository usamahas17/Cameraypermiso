package com.example.cameraypermiso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.CAMERA;

public class MainActivity extends AppCompatActivity {

    ImageView fotito;
    Button boton;
    private String carpetareaiz = "fotoscapturadas";
    private String rutaimagen = carpetareaiz+ "mis fotos";
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fotito =  findViewById(R.id.fotito);
        boton = findViewById(R.id.boton);
        if (validarpermisos()){
            boton.setEnabled(true);
        }else{
            boton.setEnabled(false);
        }
    }
    public boolean validarpermisos(){
        if (Build.VERSION.SDK_INT <Build.VERSION_CODES.M)
            return  true;
        if ((checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED) &&(checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
            return  true;
        if((shouldShowRequestPermissionRationale(CAMERA)) || (shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)))
            cargardialogo();
        else
            requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE,CAMERA}, 100);
        return false;
    }
    private void cargardialogo(){
        AlertDialog.Builder dialogo =  new AlertDialog.Builder(MainActivity.this);
        dialogo.setTitle("permisos desactivados");
        dialogo.setMessage("debes aceptar los permisos para que la apliacion funciones correctamente");
       dialogo.setPositiveButton("aceptar", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialogInterface, int i) {
               requestPermissions(new  String[]{WRITE_EXTERNAL_STORAGE,CAMERA},100);

           }
       });
       dialogo.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
              boton.setEnabled(true);
            }else{
                solicitarpermisos();
            }

        }
    }
    private void solicitarpermisos(){
        final CharSequence [] opciones = {"si","no"};
        final AlertDialog.Builder alerta = new AlertDialog.Builder(MainActivity.this);
        alerta.setTitle("deseas aceptar los permisos");
        alerta.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0 :
                        Intent pedirpermiso = new Intent();
                        pedirpermiso.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",getPackageName(),null);
                        pedirpermiso.setData(uri);
                        startActivity(pedirpermiso);
                        break;
                    default:
                        Toast.makeText(getApplication(),"los permisos fuero rechazados",Toast.LENGTH_LONG).show();
                        dialogInterface.dismiss();
                        break;
                }

            }
        });
        alerta.show();
    }

    public void cargarfoto(View m){
        cargarimagen();

    }
    public void cargarimagen(){
        final CharSequence[] opciones = {"tomar foto","ir a galeria","cancelar"};
        final AlertDialog.Builder alertaopciones = new AlertDialog.Builder(MainActivity.this);
        alertaopciones.setTitle("seleciona una opcion");
        alertaopciones.setItems(opciones, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch(i){
                    case 0 ://tomar foto
                        tomarfoto();
                        //Toast.makeText(getApplication(),"tomando foto",Toast.LENGTH_LONG).show();
                        break;
                    case 1: //ir a galeria
                        Intent galeria = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galeria.setType("image/");
                        startActivityForResult(galeria.createChooser(galeria,"selecione alguna apliacacio"),10);
                        break;
                    default:
                        dialogInterface.dismiss();
                }
            }
        });
        alertaopciones.show();

    }

    private void tomarfoto() {
        String nombre_foto = "";
        File archivoImagen = new File(Environment.getExternalStorageDirectory(), rutaimagen);
        boolean estaCreado = archivoImagen.exists();

        if(estaCreado){
            nombre_foto = (System.currentTimeMillis())/1000 + ".jpg";
        } else {
            estaCreado = archivoImagen.mkdirs();
        }

        this.path = Environment.getExternalStorageDirectory()+File.separator+rutaimagen+File.separator+nombre_foto;

        File nuevaFoto = new File(this.path);

        Intent intentoCamara = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intentoCamara.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(nuevaFoto));

        startActivityForResult(intentoCamara, 20);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case 10:
                    final Uri path = data.getData();
                    fotito.setImageURI(path);
                    break;
                case 20:
                    MediaScannerConnection.scanFile(this, new String[]{this.path}, null, new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String s, Uri uri) {
                            Log.i("ruta","path "+ s);

                        }
                    });


                    //Toast.makeText(getApplication(), "recuperando foto de la cámara", Toast.LENGTH_SHORT).show();

                    Bitmap bitmap = BitmapFactory.decodeFile(this.path);
                    fotito.setImageBitmap(bitmap);
                    break;

            }

        }
    }
}