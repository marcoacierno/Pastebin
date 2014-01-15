package com.revonline.pastebin;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import com.revonline.pastebin.trending_pastes.PopPastes;
import com.revonline.pastebin.user.User;
import com.revonline.pastebin.user.UserActivity;

public class MyActivity extends Activity {
    public static final String DEBUG_TAG = "Debug Tag";
    private Pastebin pastebin;
    private EditText viewPasteTitle;
    private String language;
    private String time;
    private int visiblity;
    private EditText pasteText;
    private CodeShareReceiver codeshareResponse;
    private static final String[] fixedLanguages = new String[]{"4cs", "6502acme", "6502kickass", "6502tasm", "abap", "actionscript",
            "actionscript3", "ada", "algol68", "apache", "applescript", "apt_sources", "arm", "asm", "asp", "asymptote", "autoconf", "autohotkey", "autoit", "avisynth", "awk",
            "bascomavr", "bash", "basic4gl", "bibtex", "blitzbasic", "bnf", "boo", "bf", "c", "c_mac", "cil", "csharp", "cpp",
            "cpp-qt", "c_loadrunner", "caddcl", "cadlisp", "cfdg", "chaiscript", "clojure", "klonec", "klonecpp", "cmake", "cobol",
            "coffeescript", "cfm", "css", "cuesheet", "d", "dcl", "dcpu16", "dcs", "delphi", "oxygene", "diff", "div", "dos", "dot", "e", "ecmascript", "eiffel", "email", "epc",
            "erlang", "fsharp", "falcon", "fo", "f1", "fortran", "freebasic", "freeswitch", "gambas", "gml", "gdb", "genero", "genie", "gettext", "go", "groovy",
            "gwbasic", "haskell", "haxe", "hicest", "hq9plus", "html4strict", "html5", "icon", "idl",
            "ini", "inno", "intercal", "io", "j", "java", "java5", "javascript", "jquery", "kixtart", "latex", "ldif", "lb", "lsl2",
            "lisp", "llvm", "locobasic", "logtalk", "lolcode", "lotusformulas",
            "lotusscript", "lscript", "lua", "m68k", "magiksf", "make", "mapbasic", "matlab", "mirc", "mmix", "modula2", "modula3", "68000devpac", "mpasm", "mxml", "mysql",
            "nagios", "newlisp", "text", "nsis", "oberon2", "objeck", "objc", "ocaml-brief", "ocaml", "octave", "pf", "glsl", "oobas", "oracle11", "oracle8", "oz", "parasail", "parigp",
            "pascal", "pawn", "pcre", "per", "perl", "perl6", "php", "php-brief", "pic16", "pike", "pixelbender",
            "plsql", "postgresql", "povray", "powershell", "powerbuilder", "proftpd", "progress", "prolog", "properties", "providex",
            "purebasic", "pycon", "python", "pys60", "q", "qbasic", "rsplus", "rails", "rebol", "reg", "rexx", "robots", "rpmspec", "ruby", "gnuplot", "sas", "scala", "scheme",
            "scilab", "sdlbasic", "smalltalk", "smarty", "spark", "sparql", "sql", "stonescript", "systemverilog", "tsql", "tcl", "teraterm", "thinbasic", "typoscript", "unicon", "uscript", "ups",
            "urbi", "vala", "vbnet", "vedit", "verilog", "vhdl", "vim", "visualprolog", "vb", "visualfoxpro", "whitespace", "whois", "winbatch", "xbasic", "xml",
            "xorg_conf", "xpp", "yaml", "z80", "zxbasic"};
    public static boolean apiLower11;
    public static final String EXTRA_FLAG_FORK = "EXTRA.FLAG_FORK";
    public User user;
    private RadioButton privateButton;
    private MenuItem IOmenuitem;
    private CheckBox anonimo;//true => posta come anonimo
                             //false => posta come un utente, se loggato.
    //    private int portait = 1;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(DEBUG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // load user
        user = new User(this);

//        Log.d(DEBUG_TAG, "portait = " + portait);

        pastebin = new Pastebin(this);
        viewPasteTitle = (EditText) findViewById(R.id.pastetitle);
        Spinner viewLanguage = (Spinner) findViewById(R.id.spinnerlinguaggio);
        Spinner viewTime = (Spinner) findViewById(R.id.spinnerscadenza);
        RadioGroup viewVisiblity = (RadioGroup) findViewById(R.id.accessibilita);
        pasteText = (EditText) findViewById(R.id.codearea);

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

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this, R.array.expiration, android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewTime.setAdapter(adapter2);
        viewTime.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
                time = "N";
            }
        });

        codeshareResponse = new CodeShareReceiver();
        IntentFilter intentFilter = new IntentFilter(CodeShareReceiver.SHARE_SUCCESS);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        registerReceiver(codeshareResponse, intentFilter);

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
        if (apiLower11)
        {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.menu, menu);

            IOmenuitem = menu.findItem(R.id.loginmenu);
            IOmenuitem.setTitle(getString(R.string.IO, (user.isLogged() ? user.getUserName() : "Login")));
        }
        return super.onCreateOptionsMenu(menu);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (apiLower11)
        {
            Intent intent;
            switch (item.getItemId())
            {
                case R.id.popularpaste:
                    intent = new Intent(this, PopPastes.class);
                    startActivity(intent);
                    break;
                case R.id.loginmenu:
                    intent = new Intent(this, UserActivity.class);
                    startActivity(intent);
                    break;
            }
        }
        return super.onOptionsItemSelected(item);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void sharePaste(View view)
    {
        Log.d(MyActivity.DEBUG_TAG, "sharePaste - language => " + language + ", time = " + time);
        //String title, String code, String language, String scadenza, int visibility
        String codice = pasteText.getText().toString();

        if (codice.length() < 1)
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

        pastebin.postPaste(viewPasteTitle.getText().toString(), codice, language, time, visiblity, anonimo.isChecked(), user.getUserKey());
    }

    @Override
    protected void onPause() {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
//        unregisterReceiver(codeshareResponse);
    }
}
