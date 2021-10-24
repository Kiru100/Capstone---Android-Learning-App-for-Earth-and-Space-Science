package com.example.capstone;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class EditEmail extends Fragment {
    private String password;
    
   private EditText edtNewEmail,edtConfirmNewEmail;

    private FirebaseUser student;

    private FirebaseMethods mFirebaseMethods;

    public EditEmail() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView=inflater.inflate(R.layout.fragment_edit_email, container, false);

        //firebase method
        mFirebaseMethods = new FirebaseMethods(getActivity());
        //textview
        TextView tvCurrentEmail = rootView.findViewById(R.id.tvCurrentEmail);
        //edit text
        edtNewEmail=rootView.findViewById(R.id.edtNewEmail);
        edtConfirmNewEmail=rootView.findViewById(R.id.edtConfirmNewEmail);
        //button
        Button btnConfirmEditEmail = rootView.findViewById(R.id.btnConfirmEditEmail);
        getEmail(rootView);

        btnConfirmEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmEmail();
            }
        });

        return rootView;
    }

    private void changeEmail(){
        FirebaseAuth auth = FirebaseAuth.getInstance();

        //re-authenticate user email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        AuthCredential credential = EmailAuthProvider
                .getCredential(auth.getCurrentUser().getEmail(), password);
        user.reauthenticate(credential)
                .addOnCompleteListener(task -> {

                    //change email here
                    FirebaseUser user1 = FirebaseAuth.getInstance().getCurrentUser();
                    user1.updateEmail(edtNewEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        //TODO: Don't allow user from using existing email
                                        //TODO:prompt if fail or success as well as check if password is incorrect
                                        mFirebaseMethods.updateEmail(edtNewEmail.getText().toString());


                                        final NavController navController = Navigation.findNavController(getView());
                                        navController.navigate(R.id.action_editEmail_to_manageAccount);
                                        Toast.makeText(getActivity(), "You successfully changed your email.", Toast.LENGTH_SHORT).show();
                                    }
                                    else{
                                        Toast.makeText(getContext(), "Check your password.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                });

    }

    private void confirmDialog() {

            ConfirmPasswordDialog dialog=new ConfirmPasswordDialog();
            dialog.show(getParentFragmentManager(),"Confirm Dialog");
            dialog.getParentFragmentManager().setFragmentResultListener("1",EditEmail.this, (requestKey, result) -> {
              password = result.getString("1");
                changeEmail();
            });
    }

    public void confirmEmail(){
        String email =  edtNewEmail.getText().toString().trim();
        String confirmEmail=edtConfirmNewEmail.getText().toString().trim();

        if (email.isEmpty()) {
            edtNewEmail.setError("Email is required");
            edtNewEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtNewEmail.setError("Please provide valid email.");
            edtNewEmail.requestFocus();
            return;
        }
        if (confirmEmail.isEmpty()) {
            edtConfirmNewEmail.setError("Email is required");
            edtConfirmNewEmail.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(confirmEmail).matches()) {
            edtConfirmNewEmail.setError("Please provide valid email.");
            edtConfirmNewEmail.requestFocus();
            return;
        }
        if(!email.equals(confirmEmail)){
            edtConfirmNewEmail.setError("Email don't match.");
            edtConfirmNewEmail.requestFocus();
            return;
        }else {
            confirmDialog();
        }
    }

    public void getEmail(View view){
        student=FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance("https://capstoneproject-4b898-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("Students");
        String userID = student.getUid();

        final TextView CurrentEmail=view.findViewById(R.id.tvCurrentEmail);

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Student studentProfile= snapshot.getValue(Student.class);
                if(studentProfile!=null){
                    String semail=studentProfile.getSEmail();
                    CurrentEmail.setText(semail);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}
