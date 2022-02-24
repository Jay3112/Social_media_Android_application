package com.example.jaypatel.homoheart;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Main2Activity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    Menu menu;
    BottomNavigationView navigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        HomeIntent();
    }

    public boolean loadFragment (Fragment fragment){
        if(fragment != null){
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,fragment).commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        Fragment fragment = null;
        menu = navigation.getMenu();
        menu.findItem(R.id.navigation_home).setIcon(R.drawable.ic_home_border_white);
        menu.findItem(R.id.navigation_notifications).setIcon(R.drawable.ic_favorite_border_white);
        menu.findItem(R.id.navigation_profile).setIcon(R.drawable.ic_person_border_white);
        menu.findItem(R.id.navigation_chat).setIcon(R.drawable.ic_chat_bubble_border_white);

        switch (menuItem.getItemId()){
            case R.id.navigation_home:
                fragment = new HomeTab();
                menuItem.setIcon(R.drawable.ic_home_white);
                menuItem.setChecked(true);
                getSupportActionBar().setTitle("Homoheart");
                break;

            case R.id.navigation_notifications:
                fragment = new NotificationTab();
                menuItem.setChecked(false);
                menuItem.setIcon(R.drawable.ic_favorite_white);
                getSupportActionBar().setTitle("Notifications");
                break;

            case R.id.navigation_chat:
                fragment = new Chattab();
                menuItem.setChecked(false);
                menuItem.setIcon(R.drawable.ic_chat_bubble_white);
                getSupportActionBar().setTitle("Chat");
                break;

            case R.id.navigation_profile:
                fragment = new ProfileTab1();
                menuItem.setChecked(false);
                menuItem.setIcon(R.drawable.ic_person_white_24dp);
                getSupportActionBar().setTitle("Profile");
                break;
        }
        return loadFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        menu = navigation.getMenu();
        MenuItem menuItem = menu.getItem(0);
        if(menuItem.isChecked()){
            super.finish();
        }else{
            //intent to home page
            HomeIntent();
        }
    }

    public void ProfIntent(){
        menu = navigation.getMenu();
        onNavigationItemSelected(menu.getItem(3));
    }

    public void HomeIntent(){
        menu = navigation.getMenu();
        onNavigationItemSelected(menu.getItem(0));
    }
}
