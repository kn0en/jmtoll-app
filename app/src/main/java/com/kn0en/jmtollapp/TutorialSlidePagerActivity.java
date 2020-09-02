package com.kn0en.jmtollapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class TutorialSlidePagerActivity extends AppCompatActivity {

    private TutorialSlidePageAdapter tutorialSlidePageAdapter;
    private LinearLayout sliderIndicator;
    private MaterialButton buttonTutorialAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial_slide);

        sliderIndicator = (LinearLayout) findViewById(R.id.slideIndicators);
        buttonTutorialAction = (MaterialButton) findViewById(R.id.buttonNext);

        setupTutorialItems();

        ViewPager2 tutorialViewPager2 = findViewById(R.id.pager);
        tutorialViewPager2.setAdapter(tutorialSlidePageAdapter);

        setupTutorialIndicators();
        setCurrentTutorialIndicator(0);

        tutorialViewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentTutorialIndicator(position);
            }
        });

        buttonTutorialAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tutorialViewPager2.getCurrentItem() + 1 < tutorialSlidePageAdapter.getItemCount()){
                    tutorialViewPager2.setCurrentItem(tutorialViewPager2.getCurrentItem() + 1);
                }else {
                    finish();
                }
            }
        });
    }

    private void setupTutorialItems(){
        List<TutorialSlidePageItem> tutorialSlidePageItems = new ArrayList<>();
        TutorialSlidePageItem item1 = new TutorialSlidePageItem();
        item1.setTextTitle("TUTORIAL PENGGUNAAN APLIKASI 'JMTOLL APP'");
        item1.setTextContent("1. Setelah pengguna masuk aplikasi maka akan langsung di arahkan ke menu utama. " +
                "Pada menu utama terdapat fitur utama yang akan digunakan pengguna yaitu Fitur 'MAPS'. " +
                "\n" +
                "2. Klik pada hamburger bar untuk melihat menu navigasi.");
        item1.setImage(R.drawable.slide1);

        TutorialSlidePageItem item2 = new TutorialSlidePageItem();
        item2.setTextContent("3. Sebelum menggunakan aplikasi ini, pengguna DIHARUSKAN mengisi data diri terlebih dahulu." +
                "\n" +
                "4. Pada menu navigasi pilih fitur 'MY PROFILE' untuk merubah informasi data diri.");
        item2.setImage(R.drawable.slide2);

        TutorialSlidePageItem item2_1 = new TutorialSlidePageItem();
        item2_1.setTextContent("5. Setelah masuk fitur 'MY PROFILE', lengkapi data diri yang sudah tersedia," +
                "\n" +
                "6. Isi data dengan benar dan tekan 'CONFIRM' untuk menyimpan data." +
                "\n\n" +
                "NB *Untuk mengisi 'CAR GROUP' sesuaikan dengan kendaraan pengguna menurut 'Kepmen PU No 370/KPTS/M/2007 tentang Golongan Jenis Kendaraan Bermotor pada Jalan Tol yang sudah beroperasi'. ");
        item2_1.setImage(R.drawable.slide2_1);

        TutorialSlidePageItem item3 = new TutorialSlidePageItem();
        item3.setTextContent("7. Setelah selesai 'CONFIRM' data diri, maka otomatis akan langsung dialhkan ke menu utama." +
                "\n" +
                "8. Untuk menggunakan fitur utama 'MAPS', tekan icon 'LOCATION' untuk mengetahui lokasi pengguna saat ini. Setelah itu tekan 'FIND OFFICER' untuk mengetahui petugas yang terdekat dengan pengguna saat ini.");
        item3.setImage(R.drawable.slide3);

        TutorialSlidePageItem item3_1 = new TutorialSlidePageItem();
        item3_1.setTextContent("9. Setelah menekan 'FIND OFFICER' maka akan muncul pop up yang artinya sistem sedang mencari petugas yang terdekat dengan pengguna saat ini.");
        item3_1.setImage(R.drawable.slide3_1);

        TutorialSlidePageItem item4 = new TutorialSlidePageItem();
        item4.setTextContent("10. Jika terdapat petugas terdekat yang tersedia dengan lokasi pengguna saat ini. Maka otomatis sistem akan membuat rute jalur penjemputan serta menampilkan informasi nama petugas, nomer telepon yang bisa dihubungi pengguna serta jarak antara petugas sampai ke lokasi pengguna.");
        item4.setImage(R.drawable.slide4);

        TutorialSlidePageItem item5 = new TutorialSlidePageItem();
        item5.setTextContent("10. Setelah selesai melakukan penjemputan oleh petugas, pengguna dapat memberi 'RATING' kualitas pelayanan dari petugas dengan masuk ke navigasi menu lalu pilih fitur 'HISTORY' yang tersedia pada menu navigasi.");
        item5.setImage(R.drawable.slide5);

        tutorialSlidePageItems.add(item1);
        tutorialSlidePageItems.add(item2);
        tutorialSlidePageItems.add(item2_1);
        tutorialSlidePageItems.add(item3);
        tutorialSlidePageItems.add(item3_1);
        tutorialSlidePageItems.add(item4);
        tutorialSlidePageItems.add(item5);

        tutorialSlidePageAdapter = new TutorialSlidePageAdapter(tutorialSlidePageItems);
    }

    private void setupTutorialIndicators(){
        ImageView[] indicators = new ImageView[tutorialSlidePageAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        layoutParams.setMargins(8,0,8,0);
        for (int i = 0; i < indicators.length; i++){
            indicators[i] = new ImageView(getApplicationContext());
            indicators[i].setImageDrawable(ContextCompat.getDrawable(
                    getApplicationContext(),
                    R.drawable.tutorial_indicator_inactive
            ));
            indicators[i].setLayoutParams(layoutParams);
            sliderIndicator.addView(indicators[i]);
        }
    }

    private void setCurrentTutorialIndicator(int index){
        int childCount = sliderIndicator.getChildCount();
        for (int i = 0;  i < childCount; i++){
            ImageView imageView = (ImageView) sliderIndicator.getChildAt(i);
            if (i == index){
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.tutorial_indicator_active)
                );
            }else {
                imageView.setImageDrawable(
                        ContextCompat.getDrawable(getApplicationContext(),R.drawable.tutorial_indicator_inactive)
                );
            }
        }
        if (index == tutorialSlidePageAdapter.getItemCount() - 1){
            buttonTutorialAction.setText("Finish");
        }else {
            buttonTutorialAction.setText("Next");
        }
    }
}
