package byuics246.budgeting;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

//import android.widget.TextView;

/**
 * Handles all activities on the Register page
 *
 * @author Cameron Johnson
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Main";

    private EditText mEmailField;
    private EditText mPasswordField;
    private EditText mNameField;
    private CheckBox saveLoginCheckBox;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;
    private Boolean saveLogin;
    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE;


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

        requestPermission();
        // Buttons
        findViewById(R.id.buttonSignInLogin).setOnClickListener(this);
        findViewById(R.id.buttonSignInRegister).setOnClickListener(this);

        //reset password
        findViewById(R.id.textViewSignInForgotPassword).setOnClickListener(this);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
    }

    /**
     * Logs a user into Firebase Authentication
     * <p>
     *     This function accepts the user credentials as parameters. The login form is validated,
     *     then the info is passed to the Firebase Authentication if the form is valid. If the user
     *     logged in successfully, they are redirected to the expense page. If the log in was not
     *     successful then a pop up is displayed, prompting the user to try again.
     * </p>
     * @param email String for the user email to be authenticated with
     * @param password String for the user password to be authenticated with
     * @param name String for the display name to be saved in the shared preferences
     */
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
                            new AlertDialog.Builder(MainActivity.this)
                                    .setTitle("Authentication Failed")
                                    .setMessage("Invalid credentials. Please try again.")
                                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).setNegativeButton("", null).show();
                        }
                    }
                });
    }

    /**
     * Saves a user's login info locally
     * <p>
     *     This function accepts the user credentials as parameters. If the checkbox
     *     to save user info is checked, it will store user credentials on the device
     *     so it will then be loaded next time the app is opened.
     * </p>
     * @param email String for the user email to be authenticated with
     * @param password String for the user password to be authenticated with
     * @param name String for the display name to be saved in the shared preferences
     */
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

    /**
     * Allows user to request password reset
     * <p>
     *     When a user chooses to reset password, an email will be sent to them to
     *     confirm request and to ask for a new password for their account.
     * </p>
     */
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

    /**
     * Required field for password reset
     * <p>
     *     Checks to see if user has input in their email. The user must do so
     *     to know where to send change password request.
     * </p>
     * @return
     */
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

    /**
     * Opens register page for new users
     * <p>
     *     New users select to register and then create their account
     * </p>
     */
    public void openRegisterPage() {
        Intent registerIntent = new Intent(this, RegisterActivity.class);
        startActivity(registerIntent);
    }

    /**
     * Validates that all necessary fields are filled out before submission
     * <p>
     *     Confirms that email, password, and name fields are completed before
     *     submission. If a field is missing, display error.
     * </p>
     * @return
     */
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

    /**
     * Actions for onclicklistener
     * <p>
     *     Directs the onclicklistener calls to their appropriate activities. Code is commented
     *     out for future development.
     * </p>
     * @param v view name associated with call
     */
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonSignInRegister) {
            openRegisterPage();
        } else if (i == R.id.buttonSignInLogin) {
            signIn(mEmailField.getText().toString(), mPasswordField.getText().toString(), mNameField.getText().toString());
        } else if (i == R.id.textViewSignInForgotPassword) {
            resetPassword();
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Password Reset")
                    .setMessage("Please check your email and follow the link to reset your password.")
                    .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).setNegativeButton("", null).show();
//        } else if (i == R.id.signOutButton) {
//            signOut();
          }
    }

    /**
     * Code for future development
     */
    public void requestPermission(){
        //request and check permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            }else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
//            }
        } else {
            // Permission has already been granted
        }
    }
}
