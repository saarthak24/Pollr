package com.akotnana.pollr.activities;

/**
 * Created by anees on 11/12/2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akotnana.pollr.R;
import com.akotnana.pollr.fragments.DashboardFragment;
import com.akotnana.pollr.fragments.ProfileFragment;
import com.akotnana.pollr.fragments.ResponseFragment;
import com.akotnana.pollr.utils.DataStorage;
import com.akotnana.pollr.utils.RoundRectCornerImageView;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.akotnana.pollr.utils.DataStorage.decodeSampledBitmapFromFile;

public class NavigationActivity extends AppCompatActivity implements ResponseFragment.OnFragmentInteractionListener, DashboardFragment.OnFragmentInteractionListener, ProfileFragment.OnFragmentInteractionListener {

    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    private NavigationView nvDrawer;
    private ActionBarDrawerToggle drawerToggle;
    private TextView toolbarTitle;
    private int prevIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title1);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Find our drawer view
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        nvDrawer = (NavigationView) findViewById(R.id.nvView);
        nvDrawer.setItemIconTintList(null);
        // Setup drawer view
        setupDrawerContent(nvDrawer);

        drawerToggle = setupDrawerToggle();

        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);

        Fragment fragment = null;
        try {
            fragment = (Fragment) DashboardFragment.class.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

        toolbarTitle.setText("Polls");
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.drawer_open,  R.string.drawer_close);
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });

        View hView = navigationView.getHeaderView(0);
        final CircleImageView imageView = (CircleImageView) hView.findViewById(R.id.materialup_profile_image);

        File path = new File(getApplicationContext().getFilesDir(), "pollr_images/");
        if (!path.exists()) path.mkdirs();
        final File imageFile = new File(path, "image.jpg");
        // use imageFile to open your image
        Log.d("ProfileEditActivity", "" + imageFile.exists());
        if (imageFile.exists()) {
            Bitmap bitmap = decodeSampledBitmapFromFile(imageFile.getAbsolutePath(), 1000, 700);
            Drawable d = new BitmapDrawable(getResources(), bitmap);
            imageView.setImageDrawable(d);
        } else if (!new DataStorage(getApplicationContext()).getData("imageURI").equals("")) {
            Uri imageURI1 = Uri.parse(new DataStorage(getApplicationContext()).getData("imageURI"));
            //imageView.setImageURI(null);
            imageView.setImageURI(imageURI1);
        } else {
            //imageView.setImageURI(null);
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageReference = storage.getReferenceFromUrl("gs://pollr-89a97.appspot.com").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
            final long ONE_MEGABYTE = 1024 * 1024;
            storageReference.getBytes(3*ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    File path = new File(getApplicationContext().getFilesDir(), "pollr_images/");
                    if (!path.exists()) path.mkdirs();
                    File image = new File(path, "image.jpg");
                    OutputStream fOut = null;
                    try {
                        fOut = new FileOutputStream(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                    try {
                        fOut.flush(); // Not really required
                        fOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    // do not forget to close the stream

                    try {
                        MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),image.getAbsolutePath(),image.getName(),image.getName());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    imageView.setImageBitmap(bitmap);
                }
            });
        }

    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked

        Fragment fragment = null;
        Class fragmentClass = null;
        switch(menuItem.getItemId()) {
            case R.id.dashboard_fragment:
                mDrawer.closeDrawers();
                fragmentClass = DashboardFragment.class;
                Log.d("Navigation", "regular");
                break;
            case R.id.responses_fragment:
                mDrawer.closeDrawers();
                fragmentClass = ResponseFragment.class;
                Log.d("Navigation", "clear");
                break;
            case R.id.profile_fragment:
                mDrawer.closeDrawers();
                fragmentClass = ProfileFragment.class;
                Log.d("Navigation", "clear");
                break;
            case R.id.sign_out:
                //TODO: Log off firebase
                mDrawer.closeDrawers();
                Toast.makeText(this, "Logging off...",
                        Toast.LENGTH_LONG).show();
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return;
        }

        Log.d("MainActivity", "" + (fragmentClass == null));

        if(fragmentClass == null) {
            //profile
            try {
                fragment = (Fragment) DashboardFragment.class.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

            // Highlight the selected item has been done by NavigationView
            menuItem.setChecked(true);
            // Set action bar title
            toolbarTitle.setText(menuItem.getTitle());

        } else {

            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Insert the fragment by replacing any existing fragment
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit();

            // Highlight the selected item has been done by NavigationView
            menuItem.setChecked(true);
            // Set action bar title
            toolbarTitle.setText(menuItem.getTitle());
            // Close the navigation drawer
            mDrawer.closeDrawers();


        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawer.openDrawer(GravityCompat.START);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // `onPostCreate` called when activity start-up is complete after `onStart()`
    // NOTE! Make sure to override the method with only a single `Bundle` argument
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to

        for (Fragment fragment : getSupportFragmentManager().getFragments()) {
            Log.d("requestCode", String.valueOf(requestCode));
            fragment.onActivityResult(requestCode, resultCode, data);
        }

        if(requestCode == ProfileFragment.CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {


        /*View headerView = navigationView.inflateHeaderView(R.layout.nav_header);
        CircleImageView imageView = headerView.findViewById(R.id.materialup_profile_image);*/

            View hView = nvDrawer.getHeaderView(0);
            final CircleImageView imageView = (CircleImageView) hView.findViewById(R.id.materialup_profile_image);

            File path = new File(getApplicationContext().getFilesDir(), "pollr_images/");
            if (!path.exists()) path.mkdirs();
            final File imageFile = new File(path, "image.jpg");
            // use imageFile to open your image
            Log.d("ProfileEditActivity", "" + imageFile.exists());
            if (imageFile.exists()) {
                Bitmap bitmap = decodeSampledBitmapFromFile(imageFile.getAbsolutePath(), 1000, 700);
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                imageView.setImageDrawable(d);
            } else if (!new DataStorage(getApplicationContext()).getData("imageURI").equals("")) {
                Uri imageURI1 = Uri.parse(new DataStorage(getApplicationContext()).getData("imageURI"));
                //imageView.setImageURI(null);
                imageView.setImageURI(imageURI1);
            } else {
                //imageView.setImageURI(null);
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageReference = storage.getReferenceFromUrl("gs://pollr-89a97.appspot.com").child(FirebaseAuth.getInstance().getCurrentUser().getUid() + ".jpg");
                final long ONE_MEGABYTE = 1024 * 1024;
                storageReference.getBytes(3*ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        File path = new File(getApplicationContext().getFilesDir(), "pollr_images/");
                        if (!path.exists()) path.mkdirs();
                        File image = new File(path, "image.jpg");
                        OutputStream fOut = null;
                        try {
                            fOut = new FileOutputStream(image);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
                        try {
                            fOut.flush(); // Not really required
                            fOut.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        // do not forget to close the stream

                        try {
                            MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(),image.getAbsolutePath(),image.getName(),image.getName());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        }

        if (requestCode == 0) {
            int toCheck = 0;
            CharSequence i = toolbarTitle.getText();
            if (i.equals("Polls")) {
                toCheck = 0;
                Log.d("Navigation", "regular");


            } else if (i.equals("Profile")) {
                Log.d("Navigation", "clear");


                toCheck = 1;

            }
            nvDrawer.getMenu().getItem(toCheck).setChecked(true);
        }
    }
}
