package com.example.vlad.financemanager;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle toggleActionBar;
    private final CharSequence Titles[] = {"Outcome", "Income"};

    private ArrayList<SpinnerItem> spinnerItems;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /**Sliding tabs**/
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), Titles, 2));

        // Give the PagerSlidingTabStrip the ViewPager
        PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        // Attach the view pager to the tab strip
        tabsStrip.setViewPager(viewPager);

        /**Toggle action bar**/
        DrawerLayout drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        toggleActionBar = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);

        drawerLayout.addDrawerListener(toggleActionBar);
        toggleActionBar.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /**Spinner setting**/
        SpinnerInit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(toggleActionBar.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public void SpinnerInit(){

       spinnerItems = new ArrayList<>();
       spinnerItems.add(new SpinnerItem("Cash", R.drawable.cash));
       spinnerItems.add(new SpinnerItem("Credit", R.drawable.credit_card));
       spinnerItems.add(new SpinnerItem("Salary", R.drawable.dollar));

        Spinner spinner = (Spinner)findViewById(R.id.spinnerAccounts);
        spinner.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        CustomSpinnerAdapter adapter = new CustomSpinnerAdapter(this,R.layout.spinner_item, spinnerItems);
        spinner.setAdapter(adapter);

        //OnItemSelected
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent,
                                       View itemSelected, int selectedItemPosition, long selectedId) {

                Toast toast = Toast.makeText(getApplicationContext(),
                        "Ваш выбор: " + ((SpinnerItem)parent.getItemAtPosition(selectedItemPosition)).getAccountName(),
                        Toast.LENGTH_SHORT);
                toast.show();
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
