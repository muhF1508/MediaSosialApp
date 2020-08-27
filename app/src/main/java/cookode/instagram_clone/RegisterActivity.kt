package cookode.instagram_clone

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        btn_register.setOnClickListener {
            createAccount()
        }

        btn_signin_link.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun createAccount() {
        val fullname: String = fullname_register.text.toString()
        val username: String = username_register.text.toString()
        val email: String = email_register.text.toString()
        val password: String = password_register.text.toString()

        if (TextUtils.isEmpty(fullname) && TextUtils.isEmpty(username) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password)) {
            Toast.makeText(this,"Tidak boleh ada yang kosong",Toast.LENGTH_SHORT).show()
        } else {
            val progressDialog = ProgressDialog(this@RegisterActivity)
            progressDialog.setTitle("Register")
            progressDialog.setMessage("Please Wait")
            progressDialog.setCanceledOnTouchOutside(false)
            progressDialog.show()
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    saveUserInfo(fullname, username, email, progressDialog)
                    finish()
                } else {
                    progressDialog.dismiss()
                    mAuth.signOut()
                    Toast.makeText(this, "Email/Password salah", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveUserInfo(email: String, fullName: String, userName: String, progressDialog: ProgressDialog) { // constractor fungsinya untuk bisa menyuplai pembuatan value
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid // UID itu parameter sebagai pengambilan value
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")

        val userMap = HashMap<String, Any>() // HasMap adalah sebuah variable data banyak/bertumpuk/Array/List & String ini adalah KEY kalau Any adalah Value-nya.
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName.toLowerCase()
        userMap["username"] = userName.toLowerCase()
        userMap["email"] = email
        userMap["bio"] = "Hey Iam student at IDN Boarding School"
        //create default image profile
        userMap["image"] = "https://firebasestorage.googleapis.com/v0/b/instagram-app-256b6.appspot.com/o/Default%20Images%2Fprofile.png?alt=media&token=ecebab92-ce4f-463c-a16a-a81fc34b0772"

        usersRef.child(currentUserID).setValue(userMap) // untuk menerapkan apakah User Akunnya udh ada atau belum.
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    progressDialog.dismiss()
                    Toast.makeText(this,"Account sudah dibuat", Toast.LENGTH_LONG).show()

                    //step 16 get post
                    FirebaseDatabase.getInstance().reference
                        .child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID)
                        .setValue(true)

                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                } else{
                    val message = task.exception!!.toString()
                    Toast.makeText(this,"Error: $message", Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}