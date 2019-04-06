package byuics246.budgeting;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

class ThreeColumnsAdapter extends ArrayAdapter<Transaction> {
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    int mResourse;
    ArrayList <Transaction> mObjects;

    int second=0;
    int first=0;

    public ThreeColumnsAdapter(Context context, int resource, ArrayList<Transaction> objects) {
        super(context, resource, objects);
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mResourse = resource;
        mObjects = objects;
    }

    @Override
    public int getCount() {
        int count=mObjects.size(); //counts the total number of elements from the arrayList.
        return count;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView =  mLayoutInflater.inflate( mResourse, null );

        String date = getItem(position).getDate();
        if (date.equals("04/05/2019"))
        {
            second +=1;
        }
        if (date.equals("03/05/2019"))
        {
            first +=1;
        }
        String category = getItem(position).getCategory();
        String amount = String.valueOf(getItem(position). getAmount());
        String user = getItem(position). getUser();
        String description = getItem(position). getDescription();
        String id = getItem(position). getId();

        Transaction expense1 = new Transaction(date, user, category, amount, description, id);

        LayoutInflater inflater = LayoutInflater.from(mContext);
//        convertView = inflater.inflate(mResourse, parent, false);

        TextView tvDate= (TextView) convertView.findViewById(R.id.textViewExpensesHistoryCell11);
        TextView tvCategory= (TextView) convertView.findViewById(R.id.textViewExpensesHistoryCell2);
        TextView tvAmount= (TextView) convertView.findViewById(R.id.textViewExpensesHistoryCell3);

        tvDate.setText(date);
        tvAmount.setText(toString().valueOf(amount));
        if (parent.getContext().getClass().getSimpleName().equals("ExpensesActivity")) {
            tvCategory.setText(new Conversion().convertExpenseCategoryIDToString(Integer.valueOf(category), parent));
        }
        else if (parent.getContext().getClass().getSimpleName().equals("IncomeActivity")) {
            tvCategory.setText(new Conversion().convertIncomeCategoryIDToString(Integer.valueOf(category), parent));
        }

        return convertView;
    }
}
