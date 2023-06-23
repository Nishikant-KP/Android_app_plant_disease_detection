package com.example.plant_disease_detection;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.plant_disease_detection.ml.ConvertedModel;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {


    File imageFile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button b = findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker.Companion.with(MainActivity.this)
                        .galleryOnly()
                        .start();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == Activity.RESULT_OK){
            imageFile = ImagePicker.Companion.getFile(data);
            LoadImage();
        }

    }

    private ByteBuffer convert(Bitmap bm){
        ByteBuffer byteBuffer = ByteBuffer.allocate(4 * 224 * 224 * 3);
        byteBuffer.order(ByteOrder.nativeOrder());
        int[] integerArrayList = new int[224*224];
        bm.getPixels(integerArrayList,0,bm.getWidth(),0,0,bm.getWidth(),bm.getHeight());
        int pixel = 0;
        for(int i =0;i <224;i++){
            for(int j =0;j<224;j++){
                int input =integerArrayList[pixel++];

                byteBuffer.putFloat((((input.shar(16)  && 0xFF) - IMAGE_MEAN) / IMAGE_STD))
                byteBuffer.putFloat((((input.shr(8) and 0xFF) - IMAGE_MEAN) / IMAGE_STD))
                byteBuffer.putFloat((((input and 0xFF) - IMAGE_MEAN) / IMAGE_STD))



            }
        }






    }

    private void LoadImage() {
        try {
            ConvertedModel model = ConvertedModel.newInstance(this);

            // Creates inputs for reference.
            Bitmap bitmap1 = BitmapFactory.decodeResource(getResources(),R.drawable.bacterial);
            Bitmap map = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
            Bitmap bitmap = Bitmap.createScaledBitmap(bitmap1,224,224,false);
            ByteBuffer byteBuffer = ByteBuffer.allocate(bitmap.getByteCount());
            bitmap.copyPixelsToBuffer(byteBuffer);
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ConvertedModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            Log.i("TAG", "LoadImage: "+outputFeature0.getFloatArray());

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }
}