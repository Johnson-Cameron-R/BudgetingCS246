package byuics246.budgeting;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
//import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Main";

//    Planning to use with a modal pop up for notifying the user if they logged in
//    private TextView mStatusTextView;
//    private TextView mDetailTextView;

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mNameField;
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;


    // [Start declare_auth]
    private FirebaseAuth mAuth;
    // [End declare_auth]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPasswordField = findViewById(R.id.editTextSignInPassword);
        mEmailField = findViewById(R.id.editTextSignInUsername);
        mNameField = findViewById(R.id.editTextSignInDisplayName);
        saveLoginCheckBox = findViewById(R.id.checkBoxSignInSaveUser);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin == true) {
            mEmailField.setText(loginPreferences.getString("email", ""));
            mPasswordField.setText(loginPreferences.getString("password", ""));
            mNameField.setText(loginPreferences.getString("name", ""));
            saveLoginCheckBox.setChecked(true);
        }
        // Buttons
        findViewById(R.id.buttonSignInLogin).setOnClickListener(this);
        findViewById(R.id.buttonSignInRegister).setOnClickListener(this);

        //reset password
        findViewById(R.id.textViewSignInForgotPassword).setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    //     OnStart event
//    @Override
//    public void onStart() {
//        super.onStart();
//        // Check if user is signed in (non-null) and update UI accordingly.
////        FirebaseUser currentUser = mAuth.getCurrentUser();
//        //updateUI(currentUser);
//    }
    //     [END on_start_check_user]

    private void signIn(final String email, final String password, final String name) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            Log.d(TAG, "FormNotValid");
            return;
        }
        Log.d(TAG, "FormValid");

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            if (user.isEmailVerified()) {
                                Log.d(TAG, "Email is verified.");
                                saveLoginInfo(email, password, name);
                                // send user to expense page after login
                                Log.d(TAG, "saved credentials, sending to expense page");
                                Intent openExpensesActivity = new Intent(getApplicationContext(), ExpensesActivity.class);
                                startActivity(openExpensesActivity);
                            } else {
                                new AlertDialog.Builder(MainActivity.this)
                                        .setTitle("Verify Account")
                                        .setMessage("Account is not verified. Please check your email to verify your account first.")
                                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).setNegativeButton("", null).show();
                                Log.d(TAG, "Email is not verified !.");
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void saveLoginInfo(String email, String password, String name) {
        if (saveLoginCheckBox.isChecked()) {
            loginPrefsEditor.putBoolean("saveLogin", true);
            loginPrefsEditor.putString("email", email);
            loginPrefsEditor.putString("password", password);
            loginPrefsEditor.putString("name", name);
            loginPrefsEditor.commit();
        } else {
            loginPrefsEditor.clear();
            loginPrefsEditor.commit();
        }

    }

//    private void signOut() {
//        mAuth.signOut();
//    }

    public void resetPassword() {
        if (!validatePasswordReset()) {
            Log.d(TAG, "FormNotValid");
            return;
        }

        mAuth.sendPasswordResetEmail(mEmailField.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                        }
                    }
                });
    }

    private boolean validatePasswordReset() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        return valid;
    }

    public void openRegisterPage() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        String name = mNameField.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mNameField.setError("Required.");
            valid = false;
        } else {
            mNameField.setError(null);
        }

        return valid;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonSignInRegister) {
            openRegisterPage();
        } else if (i == R.id.buttonSignInLogin) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString(), mNameField.getText().toString());
        } else if (i == R.id.textViewSignInForgotPassword) {
            resetPassword();
//        } else if (i == R.id.signOutButton) {
//            signOut();
          }
    }
}
