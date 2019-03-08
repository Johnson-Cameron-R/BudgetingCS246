package byuics246.budgeting;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "Register";

    private EditText mFirstName;
    private EditText mLastName;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mPassword2;
    private CheckBox mFamilyCheck;
    private EditText mFamilyID;

    // [Start Firebase declare]
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    // [End Firebase declare]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirstName   = findViewById(R.id.editTextRegisterFirstName);
        mLastName    = findViewById(R.id.editTextRegisterLastName);
        mEmail       = findViewById(R.id.editTextRegisterEmail);
        mPassword    = findViewById(R.id.editTextRegisterPassword);
        mPassword2   = findViewById(R.id.editTextRegisterRepeatPassword);
        mFamilyCheck = findViewById(R.id.checkBoxRegisterExistingFamily);
        mFamilyID    = findViewById(R.id.editTextRegisterFamilyID);

        findViewById(R.id.buttonRegisterSubmit).setOnClickListener(this);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
    }

    private void createAccount(final String email, String password) {
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
                            db.collection("UserInfo").document(email).set(buildUser());
                            FirebaseUser user = mAuth.getCurrentUser();
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            mEmail.setError("Email In Use");
                            Toast.makeText(RegisterActivity.this, "Email is already associated with an account.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // ...
                    }
                });
        // [END create_user_with_email]
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

    private Map<String, Object> buildUser() {
        Map<String, Object> user = new HashMap<>();
        user.put("FirstName", mFirstName.getText().toString());
        user.put("LastName", mLastName.getText().toString());
        user.put("FamilyID", mFamilyID.getText().toString());

        return user;
    }

    private boolean validateForm() {
        boolean valid = true;

        //Check if firstname is empty
        String FirstName = mFirstName.getText().toString();
        if (TextUtils.isEmpty(FirstName)) {
            mFirstName.setError("Required.");
            valid = false;
        } else {
            mFirstName.setError(null);
        }

        //Check if Lastname is empty
        String LastName = mLastName.getText().toString();
        if (TextUtils.isEmpty(LastName)) {
            mLastName.setError("Required.");
            valid = false;
        } else {
            mLastName.setError(null);
        }

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

        //Check for email in use is handled as default case be authentication failing in
        // onComplete function in user creation function.

        return valid;
    }
}
