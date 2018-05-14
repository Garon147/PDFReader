package orion.garon.pdfreader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.transition.Slide;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by VKI on 17.04.2018.
 */

public class SearchAlertDialog extends DialogFragment {

    private EditText searchText;

    public SearchAlertDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder searchPageAlertBuilder = new AlertDialog.Builder(getActivity());
        searchPageAlertBuilder.setTitle(R.string.search);
        searchPageAlertBuilder.setIcon(android.R.drawable.ic_search_category_default);

        LayoutInflater layoutInflater = getActivity().getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.fragment_search_dialog, null);
        searchPageAlertBuilder.setView(view);

        searchText = view.findViewById(R.id.search_page_edit_text);
        searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if ((keyEvent != null && (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        (i == EditorInfo.IME_ACTION_SEARCH)) {
                    int pageNumber = Integer.parseInt(searchText.getText().toString());
                    ((MainActivity) getActivity()).setPageNumber(pageNumber, true);
                }

                return false;
            }
        });
        return searchPageAlertBuilder.create();
    }
}
