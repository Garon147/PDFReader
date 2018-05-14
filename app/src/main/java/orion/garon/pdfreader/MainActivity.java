package orion.garon.pdfreader;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfRenderer;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.graphics.Bitmap;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    static final String FILE_PATH_KEY = "file_path";

    private DrawerLayout mDrawerLayout;
    private SlidePageFragment slidePageFragment;
    private SearchAlertDialog searchAlertDialog;
    private String filePath;

    private Button mButtonPageUp;
    private Button mButtonPageDown;
    private TextView mPageCounter;
    private ArrayList<Button> buttonArrayList = new ArrayList<>();

    public int mCurrentPageNumber;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mButtonPageUp = findViewById(R.id.page_up);
        mButtonPageDown = findViewById(R.id.page_down);
        buttonArrayList.add(mButtonPageUp);
        buttonArrayList.add(mButtonPageDown);
        mCurrentPageNumber = 0;

        mPageCounter = findViewById(R.id.pages_counter);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M &&
                        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1001);
        }

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                        selectDrawerItem(item);
                        return true;
                    }
                }
        );

        loadPreferences();

        setButtonListeners();
    }

    private void setButtonListeners () {

        mButtonPageUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int currentPage = slidePageFragment.pdfView.getCurrentPage();
                currentPage--;
                setPageNumber(currentPage, false);
            }
        });

        mButtonPageDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int currentPage = slidePageFragment.pdfView.getCurrentPage();
                currentPage++;
                setPageNumber(currentPage, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;

            case R.id.search:
                if (slidePageFragment != null) {

                    searchAlertDialog = new SearchAlertDialog();
                    searchAlertDialog.show(getFragmentManager(), "search_fragment");
                    return true;
                } else {
                    return false;
                }
            case R.id.all_mode:
                if (slidePageFragment != null) {

                    slidePageFragment.setCurrentFileState(PDFFileState.ALL_PAGES);
                    return true;
                } else {
                    return false;
                }
            case R.id.single_mode:
                if (slidePageFragment != null) {

                    slidePageFragment.setCurrentFileState(PDFFileState.SINGLE_PAGE);

                    return true;
                } else {
                    return false;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void selectDrawerItem(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.openFile:
                new MaterialFilePicker()
                        .withActivity(this)
                        .withRequestCode(1000)
                        .withFilter(Pattern.compile(".*\\.pdf"))
                        .start();
                break;
            case R.id.exit:
                System.exit(0);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            switch (requestCode) {

                case 1000:
                    filePath = data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH);
                    openPdfFragment(filePath);
                    break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        switch (requestCode) {

            case 1001:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission granted!",
                            Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(this, "Permission not granted!",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    public void setPageNumber(int pageNumber, boolean isFromSearch) {

        if (mCurrentPageNumber != pageNumber) {

            if (searchAlertDialog != null) {

                searchAlertDialog.dismiss();
            }

            mCurrentPageNumber = pageNumber;

            if (isFromSearch) {

                switch (pageNumber) {

                    case 0:
                        pageNumber = 0;
                        break;

                    default:
                        pageNumber--;
                        break;
                }

                mCurrentPageNumber = pageNumber;
            }


            slidePageFragment.pdfView.jumpTo(pageNumber, true);
            setTextCounter();
        }
    }

    public void changeButtonsVisbility (int visibility) {

        for (Button button : buttonArrayList) {

            button.setVisibility(visibility);
        }
    }

    private void loadPreferences() {

        SharedPreferences sharedPreferences = getSharedPreferences(FILE_PATH_KEY, MODE_PRIVATE);
        filePath = sharedPreferences.getString(FILE_PATH_KEY, "");

        openPdfFragment(filePath);
    }

    private void openPdfFragment(String filePath) {

        Bundle fragmentBundle = new Bundle();
        fragmentBundle.putString("File_path", filePath);

        slidePageFragment = new SlidePageFragment();
        slidePageFragment.setArguments(fragmentBundle);
        FragmentManager manager = getFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.add(R.id.contentLayout, slidePageFragment, "content_fragment");
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void setTextCounter() {

        if (mPageCounter != null) {

            String currentPageNumber = String.valueOf(slidePageFragment.pdfView.getCurrentPage() + 1);
            String allPagesNumber = String.valueOf(slidePageFragment.pdfView.getPageCount());
            mPageCounter.setText(currentPageNumber+"/"+allPagesNumber);
        }
    }
}
