package com.revonline.pastebin;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.revonline.pastebin.codeshare.CodeShareReceiver;
import com.revonline.pastebin.trending_pastes.PopPastes;
import com.revonline.pastebin.user.User;
import com.revonline.pastebin.user.UserActivity;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ShareCodeActivity extends Activity {

  public static final String DEBUG_TAG = "DebugTag";
  public static final String EXTRA_FLAG_FORK = "EXTRA.FLAG_FORK";
  public static boolean apiLower11;
  static {
    apiLower11 = !(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);
  }
  private Pastebin pastebin;
  private String pasteTitle;
  private String language;
  private String time;
  private int visiblity;
  private String pasteCode = "";
  private CodeShareReceiver codeshareResponse;
  private User user;
  private RadioButton privateButton;
  private MenuItem IOmenuitem;
  private CheckBox
    anonimo;
//true => posta come anonimo -- false => posta come un utente, se loggato.
  //    private String[] expirationValues;
  private EditText pasteText;
  private String[] fixedLanguages;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    Log.d(DEBUG_TAG, "onCreate");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.codeshare);

    PreferenceManager.setDefaultValues(this, R.xml.settings, false);

    fixedLanguages = getResources().getStringArray(R.array.fixedlanguages);
//        expirationValues = getResources().getStringArray(R.array.expirationvalues);

    // load user
    user = new User(this);
    pastebin = new Pastebin(this);
    codeshareResponse = new CodeShareReceiver();

    initUI(); // prepare UI elements
    initNavigation();

    // if it's a fork
    String fork = getIntent().getStringExtra(EXTRA_FLAG_FORK);
    if (fork != null) {
      pasteText.setText(fork);
    }
  }

  // sad, the design is very bad now
  private void initUI() {
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
    pasteText = (EditText) findViewById(R.id.codearea);
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
        switch (checkedId) {
          case R.id.access_pubblico:
            visiblity = 0;
            break;
          case R.id.access_private:
            visiblity = 1;
            break;
          case R.id.access_nolista:
            visiblity = 2;
            break;
        }
      }
    });

    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                                                                         R.array.languages,
                                                                         android.R.layout.simple_spinner_item);
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
    viewLanguage.setSelection(
      searchPosition(fixedLanguages, sharedPreferences.getString("pref_defaultlanguage", null)));

    adapter = ArrayAdapter.createFromResource(this, R.array.expiration,
                                              android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    viewTime.setAdapter(adapter);
    viewTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // this code is very efficient, why change?
        switch (position) {
          case 0:
            time = "N";
            break;
          case 1:
            time = "10M";
            break;
          case 2:
            time = "1H";
            break;
          case 3:
            time = "1D";
            break;
          case 4:
            time = "1W";
            break;
          case 5:
            time = "2W";
            break;
          case 6:
            time = "1M";
            break;
        }

//                time = expirationValues[position - 1];
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

    viewTime.setSelection(
      searchExpirationTimeFromString(sharedPreferences.getString("pref_defaultexpiration", null)));

    Log.d(DEBUG_TAG, "user.isLogged() => " + user.isLogged());
    privateButton = (RadioButton) findViewById(R.id.access_private);
    privateButton.setEnabled(user.isLogged());

    anonimo = (CheckBox) findViewById(R.id.postacomeanonimo);
    anonimo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        privateButton.setEnabled(!b);

        if (b) {
          if (privateButton.isChecked()) {
            privateButton.setChecked(false);
          }
        }
      }
    });
  }

  @SuppressLint("NewApi")
  private void initNavigation() {
    // >= 11
    if (!apiLower11) {
      ActionBar actionBar = getActionBar();

      String[] items = {getString(R.string.createpaste), getString(R.string.pastepopolari),
                        getString(R.string.IO, (user.isLogged() ? user.getUserName() : "Login"))
      };

      //actionBar.setDisplayHomeAsUpEnabled(false);
      actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
      actionBar.setDisplayOptions(actionBar.getDisplayOptions() ^ ActionBar.DISPLAY_SHOW_TITLE);
      ArrayAdapter<String> adapter = new ArrayAdapter<>(
        this,
        android.R.layout.simple_spinner_dropdown_item, items);

      actionBar.setListNavigationCallbacks(adapter, new ActionBar.OnNavigationListener() {
        @Override
        public boolean onNavigationItemSelected(int itemPosition, long itemId) {
          Intent intent;
          switch (itemPosition) {
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

  // nel file dovrebbe essere salvato direttamente l'indice e non la stringa!
  private static int searchPosition(String[] arr, String text) {
    // non cerco per text a null, o se l'array è null
      if (text == null || arr == null) {
          return -1;
      }

      for (int i = 0; i < arr.length; ++i) {
          if (arr[i].equals(text)) {
              return i;
          }
      }

    return -1;
  }

  private static int searchExpirationTimeFromString(String text) {
    switch (text) {
      case "N":
        return 0;
      case "10M":
        return 1;
      case "1H":
        return 2;
      case "1D":
        return 3;
      case "1W":
        return 4;
      case "2W":
        return 5;
      case "1M":
        return 6;
    }

    return 0;
  }

  @Override
  public void onResume() {
    Log.d(DEBUG_TAG, "onResume ShareCodeActivity");
    super.onResume();
    user.update();

    //
    //controllo se effettivamente IOmenuitem è stato creato..
      if (apiLower11 && IOmenuitem != null) {
          IOmenuitem
            .setTitle(getString(R.string.IO, (user.isLogged() ? user.getUserName() : "Login")));
      }

      if (!apiLower11) {
          setSelectedNavigationItem(getActionBar(), 0);
      }

    anonimo.setEnabled(user.isLogged());
    privateButton.setEnabled(user.isLogged());

    IntentFilter intentFilter = new IntentFilter(CodeShareReceiver.SHARE_SUCCESS);
    intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
    registerReceiver(codeshareResponse, intentFilter);
  }

  /**
   * fixbug -- SE CHIAMATO SU API < 11 CRASH *
   */
  private void setSelectedNavigationItem(ActionBar b, int pos) {
    try {
      //do the normal tab selection in case all tabs are visible
      b.setSelectedNavigationItem(pos);

      //now use reflection to select the correct Spinner if
      // the bar's tabs have been reduced to a Spinner

      View
        action_bar_view =
        findViewById(getResources().getIdentifier("action_bar", "id", "android"));
      Class<?> action_bar_class = action_bar_view.getClass();
      Field tab_scroll_view_prop = action_bar_class.getDeclaredField("mTabScrollView");
      tab_scroll_view_prop.setAccessible(true);
      //get the value of mTabScrollView in our action bar
      Object tab_scroll_view = tab_scroll_view_prop.get(action_bar_view);
        if (tab_scroll_view == null) {
            return;
        }
      Field spinner_prop = tab_scroll_view.getClass().getDeclaredField("mTabSpinner");
      spinner_prop.setAccessible(true);
      //get the value of mTabSpinner in our scroll view
      Object tab_spinner = spinner_prop.get(tab_scroll_view);
        if (tab_spinner == null) {
            return;
        }
      Method
        set_selection_method =
        tab_spinner.getClass().getSuperclass()
          .getDeclaredMethod("setSelection", Integer.TYPE, Boolean.TYPE);
      set_selection_method.invoke(tab_spinner, pos, true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    unregisterReceiver(codeshareResponse);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    if (apiLower11) {
      inflater.inflate(R.menu.menu, menu);

      IOmenuitem = menu.findItem(R.id.loginmenu);
      IOmenuitem.setTitle(getString(R.string.IO, (user.isLogged() ? user.getUserName() : "Login")));
    }
    inflater.inflate(R.menu.menuforall, menu);

    return super.onCreateOptionsMenu(
      menu);    //To change body of overridden methods use File | Settings | File Templates.
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent;

    switch (item.getItemId()) {
      case R.id.popularpaste:
          if (!apiLower11) {
              return false;
          }

        intent = new Intent(this, PopPastes.class);
        break;
      case R.id.loginmenu:
          if (!apiLower11) {
              return false;
          }

        intent = new Intent(this, UserActivity.class);
        break;
      case R.id.opensettings:
        intent = new Intent(this, Settings.class);
        break;
      case R.id.savedraft:
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(ShareCodeActivity.this);

        final String draft = preferences.getString("draft", null);

        if (draft == null && pasteCode.length() == 0) {
          Toast.makeText(this, getString(R.string.no_draft_to_restore), Toast.LENGTH_SHORT).show();
          return true;
        }

        if (pasteCode.length() == 0) {
          new AlertDialog.Builder(this)
              .setTitle(R.string.draft)
              .setMessage(R.string.restore_delete_draft)
              .setPositiveButton(R.string.restore_draft, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                  pasteText.setText(draft);
                  Toast.makeText(ShareCodeActivity.this, R.string.draft_restored, Toast.LENGTH_SHORT).show();
                }
              })
              .setNegativeButton(R.string.delete_draft, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int which) {
                  final SharedPreferences.Editor editor = preferences.edit();
                  editor.putString("draft", null);
                  editor.commit();

                  Toast.makeText(ShareCodeActivity.this, R.string.draft_deleted, Toast.LENGTH_SHORT).show();
                }
              })
              .setNeutralButton(R.string.close, null)
              .show();

          return true;
        }

        Toast.makeText(this, R.string.draft_saved, Toast.LENGTH_SHORT).show();
        final SharedPreferences.Editor preferencesEditor = preferences.edit();
        preferencesEditor.putString("draft", pasteCode);
        preferencesEditor.commit();
        return true;
      default:
        return false;
    }

    startActivity(intent);
    return super.onOptionsItemSelected(
      item);    //To change body of overridden methods use File | Settings | File Templates.
  }

  public void sharePaste(View view) {
    Log.d(ShareCodeActivity.DEBUG_TAG, "sharePaste - language => " + language + ", time = " + time);
    //String title, String code, String language, String scadenza, int visibility

    if (pasteCode == null || pasteCode.length() < 1) {
      Toast.makeText(this, R.string.cannotbeblank, Toast.LENGTH_SHORT).show();
      return;
    }

    pastebin.postPaste(pasteTitle, pasteCode, language, time, visiblity, anonimo.isChecked(),
                       user.getUserKey());
  }
}
