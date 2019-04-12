package byuics246.budgeting;




import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class SwipeRecyclerViewAdapter extends RecyclerSwipeAdapter<SwipeRecyclerViewAdapter.SimpleViewHolder> {
    private static final String TAG = " SwipeRecyclerView";
    private Context mContext;
    String folderName = "";
    private ArrayList<Transaction> transactionsList;
    Transaction toDelete;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    //Login preferences
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    public SwipeRecyclerViewAdapter(Context context, ArrayList<Transaction> objects) {
        this.mContext = context;
        this.transactionsList = objects;
        /**activates LoginPrefs*/
        loginPreferences = context.getSharedPreferences("loginPrefs", context.MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();
    }


    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swipe_layout, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {
        final Transaction item = transactionsList.get(position);

        viewHolder.date.setText(item.getDate());
        viewHolder.amount.setText("$"+ item.getAmount());
        if (mContext.getClass().getSimpleName().equals("ExpensesActivity")) {
            viewHolder.category.setText(new Conversion().convertExpenseCategoryIDToString(Integer.valueOf(item.getCategory()), mContext));
        }
        else if (mContext.getClass().getSimpleName().equals("IncomeActivity")) {
            viewHolder.category.setText(new Conversion().convertIncomeCategoryIDToString(Integer.valueOf(item.getCategory()), mContext));
        }

        viewHolder.description.setText("Created by " + item.getUser() + ". " +item.getDescription());

        viewHolder.swipeLayout.setShowMode(SwipeLayout.ShowMode.PullOut);

        //swipe left
        viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Left, viewHolder.swipeLayout.findViewById(R.id.bottom_wrapper1));

        //swipe right
        viewHolder.swipeLayout.addDrag(SwipeLayout.DragEdge.Right, viewHolder.swipeLayout.findViewById(R.id.bottom_wraper));



        viewHolder.swipeLayout.addSwipeListener(new SwipeLayout.SwipeListener() {
            @Override
            public void onStartOpen(SwipeLayout layout) {

            }

            @Override
            public void onOpen(SwipeLayout layout) {

            }

            @Override
            public void onStartClose(SwipeLayout layout) {

            }

            @Override
            public void onClose(SwipeLayout layout) {

            }

            @Override
            public void onUpdate(SwipeLayout layout, int leftOffset, int topOffset) {

            }

            @Override
            public void onHandRelease(SwipeLayout layout, float xvel, float yvel) {

            }
        });

//        viewHolder.swipeLayout.getSurfaceView().setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(mContext, " Click : " + item.getAmount() + " \n" + item.getAmount(), Toast.LENGTH_SHORT).show();
//            }
//        });

        viewHolder.btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), "Clicked on Information " + viewHolder.date.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });


//        viewHolder.edit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Toast.makeText(view.getContext(), "Clicked on Edit  " + viewHolder.date.getText().toString(), Toast.LENGTH_SHORT).show();
//            }
//        });

        viewHolder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mContext.getClass().getSimpleName().equals("ExpensesActivity")) {
                    folderName = "Expenses";
                }
                else if (mContext.getClass().getSimpleName().equals("IncomeActivity")) {
                    folderName = "Income";
                }

                toDelete = transactionsList.get(position);
                        db.collection(loginPreferences.getString("email", "")
                                + "/Budget/" + folderName).document(toDelete.getId())
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                                        mItemManger.removeShownLayouts(viewHolder.swipeLayout);
                                        transactionsList.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, transactionsList.size());
                                        mItemManger.closeAllItems();
                                        Toast.makeText(mContext, "Deleted " + viewHolder.date.getText().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error deleting document", e);
                                    }
                                });
            }
        });

        mItemManger.bindView(viewHolder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return transactionsList.size();
    }

    @Override
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public static class SimpleViewHolder extends RecyclerView.ViewHolder{
        public SwipeLayout swipeLayout;
        public TextView date;
        public TextView category;
        public TextView amount;
        public TextView description;
        public TextView delete;
//        public TextView edit;
        public ImageButton btnLocation;
        public SimpleViewHolder(View itemView) {
            super(itemView);

            swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
            date = (TextView) itemView.findViewById(R.id.dateTextViewExpensesHistory);
            category = (TextView) itemView.findViewById(R.id.categoryTextViewExpensesHistory);
            description = (TextView) itemView.findViewById(R.id.descriptionTextViewExpensesHistory);
            amount = (TextView) itemView.findViewById(R.id.amountTextViewExpensesHistory);
            delete = (TextView) itemView.findViewById(R.id.Delete);
//            edit = (TextView) itemView.findViewById(R.id.Edit);
            btnLocation = (ImageButton) itemView.findViewById(R.id.btnLocation);
        }
    }
}