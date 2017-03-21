package com.pediapp.android;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;

public class pediApp extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    private SignInButton btnSignIn;
    private Button btnSignOut;
    private Button btnRevoke;
    private Button btnSiguiente;
    private TextView txtNombre;
    private TextView txtEmail;
    private EditText et1,et2,et3,et4;
    private GoogleApiClient apiClient;
    private static final int RC_SIGN_IN = 1001;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedi_app);

        btnSignIn = (SignInButton)findViewById(R.id.sign_in_button);
        btnSignOut = (Button)findViewById(R.id.sign_out_button);
        btnRevoke = (Button)findViewById(R.id.revoke_button);
        txtNombre = (TextView)findViewById(R.id.txtNombre);
        txtEmail = (TextView)findViewById(R.id.txtEmail);
        btnSiguiente = (Button)findViewById(R.id.siguiente_button);

        //Google API Client

        GoogleSignInOptions gso =
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build();

        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //Personalización del botón de login

        btnSignIn.setSize(SignInButton.SIZE_STANDARD);
        btnSignIn.setColorScheme(SignInButton.COLOR_LIGHT);
        btnSignIn.setScopes(gso.getScopeArray());

        //Eventos de los botones

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.GoogleSignInApi.signOut(apiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                updateUI(false);
                            }
                        });
            }
        });

        btnRevoke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Auth.GoogleSignInApi.revokeAccess(apiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(Status status) {
                                updateUI(false);
                            }
                        });
            }
        });

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // txtNombre.setText("Se Ira a Siguiente");
                setContentView(R.layout.hijos);
                et1=(EditText)findViewById(R.id.et1);
                et2=(EditText)findViewById(R.id.et2);
                et3=(EditText)findViewById(R.id.et3);
                et4=(EditText)findViewById(R.id.et4);




            }
        });

        updateUI(false);
    }
    public void alta (View v) {
        AdminSqLiteOpenHelper admin = new AdminSqLiteOpenHelper(this,"adm", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        String cod = et1.getText().toString();
        String nn = et2.getText().toString();
        String va = et3.getText().toString();
        String vf= et4.getText().toString();
        ContentValues reg = new ContentValues();
        reg.put("codigo", cod);
        reg.put("nombrehijo", nn);
        reg.put("vacunasapli",va);
        reg.put("vacunasfal", vf);
        bd.insert("padreshijos", null, reg);
        bd.close();
        et1.setText("");
        et2.setText("");
        et3.setText("");
        et4.setText("");
        Toast.makeText(this, "Se cargaron los datos del artículo",
                Toast.LENGTH_SHORT).show();
    }

    public void consultaporcodigo(View v) {
        AdminSqLiteOpenHelper admin = new AdminSqLiteOpenHelper(this,
                "adm", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        String cod = et1.getText().toString();
        Cursor fil = bd.rawQuery(
                "select nombrehijo, vacunasapli, vacunasfal from padreshijos where codigo=" + cod, null);
        if (fil.moveToFirst()) {
            et2.setText(fil.getString(0));
            et3.setText(fil.getString(1));
            et4.setText(fil.getString(2));

        } else
            Toast.makeText(this, "No existe un artículo con dicho código",
                    Toast.LENGTH_SHORT).show();
        bd.close();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Error de conexion!", Toast.LENGTH_SHORT).show();
        Log.e("GoogleSignIn", "OnConnectionFailed: " + connectionResult);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result =
                    Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            //Usuario logueado --> Mostramos sus datos
            GoogleSignInAccount acct = result.getSignInAccount();
            txtNombre.setText(acct.getDisplayName());
            txtEmail.setText(acct.getEmail());
            updateUI(true);
        } else {
            //Usuario no logueado --> Lo mostramos como "Desconectado"
            updateUI(false);
            btnSiguiente.setVisibility (View.GONE);
        }
    }

    private void updateUI(boolean signedIn) {
        if (signedIn) {
            btnSignIn.setVisibility(View.GONE);
            btnSignOut.setVisibility(View.VISIBLE);
            btnRevoke.setVisibility(View.VISIBLE);
            txtEmail.setText("Conectado a pediApp");
            btnSiguiente.setVisibility (View.VISIBLE);
        } else {
            txtNombre.setText("Desconectado");
            txtEmail.setText("Desconectado");

            btnSignIn.setVisibility(View.VISIBLE);
            btnSignOut.setVisibility(View.GONE);
            btnRevoke.setVisibility(View.GONE);

        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(apiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            showProgressDialog();
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    hideProgressDialog();
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Silent SignI-In");
            progressDialog.setIndeterminate(true);
        }

        progressDialog.show();
    }

    private void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.hide();
        }
    }
}