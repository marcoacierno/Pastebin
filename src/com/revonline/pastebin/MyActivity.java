package com.revonline.pastebin;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.revonline.pastebin.trending_pastes.PopPastes;
import com.revonline.pastebin.user.User;
import com.revonline.pastebin.user.UserActivity;

public class MyActivity extends Activity
{
    public static final String DEBUG_TAG = "Debug Tag";
    private Pastebin pastebin;
    private String pasteTitle;
    private String language;
    private String time;
    private int visiblity;
    private String pasteCode;
    private CodeShareReceiver codeshareResponse;
    private static String[] fixedLanguages;
    public static boolean apiLower11;
    public static final String EXTRA_FLAG_FORK = "EXTRA.FLAG_FORK";
    private User user;
    private RadioButton privateButton;
    private MenuItem IOmenuitem;
    private CheckBox anonimo;//true => posta come anonimo
                             //false => posta come un utente, se loggato.
    private String[] expirationValues;
    //    private int portait = 1;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        fixedLanguages = getResources().getStringArray(R.array.fixedlanguages);
        expirationValues = getResources().getStringArray(R.array.expirationvalues);

        // load user
        user = new User(this);

//        Log.d(DEBUG_TAG, "portait = " + portait);

        pastebin = new Pastebin(this);
        EditText viewPasteTitle = (EditText) findViewById(R.id.pastetitle);
        viewPasteTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                pasteTitle = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Spinner viewLanguage = (Spinner) findViewById(R.id.spinnerlinguaggio);
        Spinner viewTime = (Spinner) findViewById(R.id.spinnerscadenza);
        RadioGroup viewVisiblity = (RadioGroup) findViewById(R.id.accessibilita);
        EditText pasteText = (EditText) findViewById(R.id.codearea);
        pasteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                pasteCode = charSequence.toString();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        viewVisiblity.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                //To change body of implemented methods use File | Settings | File Templates.
                switch (checkedId)
                {
                    case R.id.access_pubblico:
                        visiblity = 0;
                        break;
                    case R.id.access_nolista:
                        visiblity = 2;
                        break;
                    case R.id.access_private:
                        visiblity = 1;
                        break;
                }
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewLanguage.setAdapter(adapter);
        viewLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //To change body of implemented methods use File | Settings | File Templates.
                language = fixedLanguages[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //To change body of implemented methods use File | Settings | File Templates.
                language = "text";
            }
        });
        viewLanguage.setSelection(searchPosition(fixedLanguages, sharedPreferences.getString("pref_defaultlanguage", null)));

        adapter = ArrayAdapter.createFromResource(this, R.array.expiration, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewTime.setAdapter(adapter);
        viewTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // this code is very efficient, why change?
//                switch (position)
//                {
//                    case 0:
//                        time = "N";
//                        break;
//                    case 1:
//                        time = "10M";
//                        break;
//                    case 2:
//                        time = "1H";
//                        break;
//                    case 3:
//                        time = "1D";
//                        break;
//                    case 4:
//                        time = "1W";
//                        break;
//                    case 5:
//                        time = "2W";
//                        break;
//                    case 6:
//                        time = "1M";
//                        break;
//                }

                time = expirationValues[position];
                Log.d(DEBUG_TAG, "new time =>" + time);

                /***
                 * N = Never
                 10M = 10 Minutes
                 1H = 1 Hour
                 1D = 1 Day
                 1W = 1 Week
                 2W = 2 Weeks
                 1M = 1 Month

                 <item>Mai</item>
                 <item>10 Minuti</item>
                 <item>1 Ora</item>
                 <item>1 Giorno</item>
                 <item>1 Settimana</item>
                 <item>2 Settimane</item>
                 <item>1 Mese</item>
                 */
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //To change body of implemented methods use File | Settings | File Templates.
                time = "N";//i should change here too, but
            }
        });
        viewTime.setSelection(searchPosition(expirationValues, sharedPreferences.getString("pref_defaultexpiration", null)));

        codeshareResponse = new CodeShareReceiver();

        apiLower11 = !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);

        initNavigation();

        String fork = getIntent().getStringExtra(EXTRA_FLAG_FORK);

        if (fork != null)
        {
            pasteText.setText(fork);
        }

        Log.d(DEBUG_TAG, "user.isLogged() => " + user.isLogged());
        privateButton = (RadioButton)findViewById(R.id.access_private);
        privateButton.setEnabled(user.isLogged());

        anonimo = (CheckBox) findViewById(R.id.postacomeanonimo);
        anonimo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                privateButton.setEnabled(!b);

                if (b)
                {
                    if (privateButton.isChecked())
                    {
                        privateButton.setChecked(false);
                    }
                }
            }
        });
    }

    // nel file dovrebbe essere salvato direttamente l'indice e non la stringa!
    private int searchPosition(String[] arr, String text)
    {
        // non cerco per text a null, o se l'array è null
        if (text == null || arr == null) return -1;

        for (int i = 0; i < arr.length; ++i)
            if (arr[i].equals(text))
                return i;

        return -1;
    }

    @Override
    public void onResume()
    {
        Log.d(DEBUG_TAG, "onResume MyActivity");
        super.onResume();
        user.update();

        //
        //controllo se effettivamente IOmenuitem è stato creato..
        if (apiLower11 && IOmenuitem != null)
            IOmenuitem.setTitle(getString(R.string.IO, (user.isLogged() ? user.getUserName() : "Login")));

        anonimo.setEnabled(user.isLogged());
        privateButton.setEnabled(user.isLogged());

        IntentFilter intentFilter = new IntentFilter(CodeShareReceiver.SHARE_SUCCESS);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(codeshareResponse, intentFilter);
    }

    @SuppressLint("NewApi")
    private void initNavigation()
    {
        // >= 11
        if (!apiLower11)
        {
            ActionBar actionBar = getActionBar();

            String[] items = {getString(R.string.createpaste), getString(R.string.pastepopolari),
                    getString(R.string.IO, (user.isLogged() ? user.getUserName() : "Login"))
            };

            //actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
            actionBar.setDisplayOptions(actionBar.getDisplayOptions() ^ ActionBar.DISPLAY_SHOW_TITLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,
                    android.R.layout.simple_spinner_dropdown_item, items);

            actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
                @Override
                public boolean onNavigationItemSelected(int itemPosition, long itemId) {
                    Intent intent;
                    switch (itemPosition)
                    {
                        // Crea paste
                        case 0:
                            // nothing
                            break;
                        case 1:
                            intent = new Intent(getApplicationContext(), PopPastes.class);
                            startActivity(intent);
                            break;
                        case 2:
                            intent = new Intent(getApplicationContext(), UserActivity.class);
                            startActivity(intent);
                            break;
                    }

                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (apiLower11)
        {
            inflater.inflate(R.menu.menu, menu);

            IOmenuitem = menu.findItem(R.id.loginmenu);
            IOmenuitem.setTitle(getString(R.string.IO, (user.isLogged() ? user.getUserName() : "Login")));
        }
        inflater.inflate(R.menu.menuforall, menu);

        return super.onCreateOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;

        switch (item.getItemId())
        {
            case R.id.popularpaste:
                if (!apiLower11) return false;

                intent = new Intent(this, PopPastes.class);
                break;
            case R.id.loginmenu:
                if (!apiLower11) return false;

                intent = new Intent(this, UserActivity.class);
                break;
            case R.id.opensettings:
                intent = new Intent(this, Settings.class);
                break;
            default:
                return false;
        }

        startActivity(intent);
        return super.onOptionsItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void sharePaste(View view)
    {
        Log.d(MyActivity.DEBUG_TAG, "sharePaste - language => " + language + ", time = " + time);
        //String title, String code, String language, String scadenza, int visibility

        if (pasteCode == null || pasteCode.length() < 1)
        {
//            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
//            alertDialog.setTitle(R.string.errore);
//            alertDialog.setMessage(R.string.cannotbeblank);
//            alertDialog.setPositiveButton(R.string.OK, null);
//            alertDialog.show();
//
            Toast.makeText(this, R.string.cannotbeblank, Toast.LENGTH_SHORT).show();
            return;
        }

        pastebin.postPaste(pasteTitle, pasteCode, language, time, visiblity, anonimo.isChecked(), user.getUserKey());
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
        unregisterReceiver(codeshareResponse);
    }
}
