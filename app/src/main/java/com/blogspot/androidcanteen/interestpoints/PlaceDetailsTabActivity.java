package com.blogspot.androidcanteen.interestpoints;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.location.places.Place;

import java.util.Random;

public class PlaceDetailsTabActivity extends AppCompatActivity implements IRequestListener {


    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;



   Toolbar toolbar;
    TabLayout tabLayout;

    public ImageView backImage;
    ProgressBar imageProgressBar;

    public PlaceDetails_InfoFragment infoFragment;
    public PlaceDetails_PhotosFragment photosFragment;
    public PlaceDetails_ReviewsFragment reviewsFragment;

    boolean detailsObtained = false;
    String[] allImagesReferences;


    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_details_tab);

        toolbar = (Toolbar) findViewById(R.id.detailsToolbar);
        setSupportActionBar(toolbar);



        String placeTitle = getIntent().getStringExtra("Title");
        final InterestPoint point = IPDatabase.getInstance().getPointByTitle(placeTitle);

       getSupportActionBar().setTitle("");

        fab = (FloatingActionButton) findViewById(R.id.directionButton);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?&daddr="+point.lat+","+point.lng));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addCategory(Intent.CATEGORY_LAUNCHER );
                intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                startActivity(intent);
            }
        });

        //Show title only when the layout is collapsed
        final CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {



                if(verticalOffset==0)
                    fab.setVisibility(View.VISIBLE);
                else
                    fab.setVisibility(View.INVISIBLE);


                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();

                }

                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitleEnabled(true);
                    collapsingToolbarLayout.setTitle(point.title);


                    isShow = true;
                } else if(isShow) {


                    collapsingToolbarLayout.setTitleEnabled(false);
                     collapsingToolbarLayout.setTitle(" ");//carefull there should a space between double quote otherwise it wont work
                    isShow = false;
                }
            }
        });


        //FRAGMENTS

        infoFragment = new PlaceDetails_InfoFragment(this);
        photosFragment = new PlaceDetails_PhotosFragment(this);
        reviewsFragment = new PlaceDetails_ReviewsFragment(this);


        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        backImage = (ImageView)findViewById(R.id.back_image);
        imageProgressBar = (ProgressBar)findViewById(R.id.imagePrograssBar);
      //  imageProgressBar.getIndeterminateDrawable().setColorFilter(0xFFFF0000, android.graphics.PorterDuff.Mode.MULTIPLY);

        backImage.setVisibility(View.INVISIBLE);

        //TAB LAYOUT

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);


        for(int i=0; i<mSectionsPagerAdapter.getCount();i++)
        {
            tabLayout.addTab(tabLayout.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)));
        }


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        /***************************************************************************************/



        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

       mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
           @Override
           public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

           }

           @Override
           public void onPageSelected(int position) {


               tabLayout.getTabAt(position).select();

               if(detailsObtained) {
                   if(position==2)
                       if(allImagesReferences.length!=0)
                      photosFragment.DownloadAllPhotos(allImagesReferences);
                   else
                           photosFragment.WarnOfNoPhotos();
               }
               else
                   mViewPager.setCurrentItem(0);
           }

           @Override
           public void onPageScrollStateChanged(int state) {

           }
       });




        //Data


        String detailsLink = RequestUtils.getLinkForPlaceDetails(point.id);

        if(GlobalVariables.isNetworkAvailable())
        {
            RequestPlaceDetailsAsyncTask task = new RequestPlaceDetailsAsyncTask(this);
            task.execute(detailsLink);}
        else {
            GlobalVariables.ToastShort("Internet connection required");

        }





    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_place_details_tab, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void OnRequestCompleted(RequestTypes type, Object result) {

        switch (type)
        {
            case DETAILS:

                PlaceDetailsJsonObject jsonResult = new PlaceDetailsJsonObject();

                if(!jsonResult.CreateJsonObject((String)result))
                {
                    GlobalVariables.ToastShort("Could not get details. Try gain later.");
                    finish();
                }
                else {


                    infoFragment.UpdateData(jsonResult);
                   reviewsFragment.UpdateData(jsonResult);


                    //Download a single image to display in the banner. All the other images will be downloaded if the user
                    // moves to the Photo fragment. They will not all be downloaded here in order to save net data

                    if(jsonResult.photosRefs.length!=0) {
                        RequestPlacePhotosAsyncTask downloadSingleImage = new RequestPlacePhotosAsyncTask(this);
                        downloadSingleImage.execute(RequestUtils.getLinkForPhoto(jsonResult.photosRefs[0], 1600));
                    }
                    else
                    {
                        backImage.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.bg));
                        backImage.setVisibility(View.VISIBLE);
                        imageProgressBar.setVisibility(View.INVISIBLE);
                    }

                    detailsObtained = true;
                    allImagesReferences = jsonResult.photosRefs;
                }


                break;

            case PHOTOS:

                //Will be called when the single image is downloaded
                    PhotoResult result1 = (PhotoResult)result;

                backImage.setImageBitmap(result1.imageBitmap);
                backImage.setVisibility(View.VISIBLE);
                imageProgressBar.setVisibility(View.INVISIBLE);
                break;

        }
    }



    public class SectionsPagerAdapter extends FragmentPagerAdapter {



        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position)
            {
                case 0:
                    return infoFragment;
                case 1:
                    return reviewsFragment;

                case 2:
                    return photosFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Info";
                case 1:
                    return "Reviews";
                case 2:
                    return "Photos";
            }
            return null;
        }
    }
}
