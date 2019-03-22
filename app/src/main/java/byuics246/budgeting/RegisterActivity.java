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
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Register";

    private EditText mEmail;
    private EditText mPassword;
    private EditText mPassword2;

    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    // [Start Firebase declare]
    private FirebaseAuth mAuth;
    // [End Firebase declare]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mEmail       = findViewById(R.id.editTextRegisterEmail);
        mPassword    = findViewById(R.id.editTextRegisterPassword);
        mPassword2   = findViewById(R.id.editTextRegisterRepeatPassword);

        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        findViewById(R.id.buttonRegisterSubmit).setOnClickListener(this);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
    }

    private void createAccount(final String email, final String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            assert user != null;
                            user.sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Email verification sent.");
                                            }
                                        }
                                    });
                            saveLoginInfo(email, password);
                            new AlertDialog.Builder(RegisterActivity.this)
                                    .setTitle("Account Created")
                                    .setMessage("You will be directed to the sign in page.")
                                    .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent openSignInActivity = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(openSignInActivity);
                                        }
                                    }).setNegativeButton("", null).show();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            new AlertDialog.Builder(RegisterActivity.this)
                                    .setTitle("Email in Use")
                                    .setMessage("This email is already registered. Sign in instead?")
                                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            saveLoginInfo(email, password);
                                            Intent openSignInActivity = new Intent(getApplicationContext(), MainActivity.class);
                                            startActivity(openSignInActivity);
                                        }
                                    }).setNegativeButton("No", null).show();
                        }

                        // ...
                    }
                });
        // [END create_user_with_email]
    }

    private void saveLoginInfo(String email, String password) {
            loginPrefsEditor.clear();
            loginPrefsEditor.putBoolean("saveLogin", true);
            loginPrefsEditor.putString("email", email);
            loginPrefsEditor.putString("password", password);
            loginPrefsEditor.commit();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.buttonRegisterSubmit) {
            createAccount(mEmail.getText().toString(), mPassword.getText().toString());
        }
    }

    // function from https://stackoverflow.com/questions/6119722/how-to-check-edittexts-text-is-email-address-or-not
    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email)
                .matches();
    }

//    private Map<String, Object> buildUser() {
//        Map<String, Object> user = new HashMap<>();
//        user.put("FirstName", mFirstName.getText().toString());
//        user.put("LastName", mLastName.getText().toString());
//        user.put("FamilyID", mFamilyID.getText().toString());
//
//        return user;
//    }

    private boolean validateForm() {
        boolean valid = true;

        //Check if Email is empty
        String email = mEmail.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmail.setError("Required.");
            valid = false;
        } else {
            mEmail.setError(null);
        }

        //Check if Password is empty
        String password = mPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPassword.setError("Required.");
            valid = false;
        } else {
            mPassword.setError(null);
        }

        //Check if password2 is empty
        String password2 = mPassword2.getText().toString();
        if (TextUtils.isEmpty(password2)) {
            mPassword2.setError("Required.");
            valid = false;
        } else {
            mPassword2.setError(null);
        }

        //Check if passwords don't match
        if (!password.equals(password2)) {
            mPassword.setError("Password Mismatch.");
            mPassword2.setError("Password Mismatch.");
            valid = false;
        }

        //Check if passwords are too short
        if (password.length() < 6) {
            mPassword.setError("Password To Short.");
            mPassword2.setError("Password To Short.");
            Toast.makeText(RegisterActivity.this, "Password needs to be 6 characters.",
                    Toast.LENGTH_SHORT).show();
            valid = false;
        }

        //Check if email is well formatted
        if (!isEmailValid(email)) {
            mEmail.setError("Email not Valid");
            valid = false;
        }

        // Check for email in use is handled as default case be authentication failing in
        // onComplete function in user creation function.

        return valid;
    }
}
