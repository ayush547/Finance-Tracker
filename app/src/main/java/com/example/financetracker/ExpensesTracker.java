package com.example.financetracker;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.shawnlin.numberpicker.NumberPicker;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class ExpensesTracker extends AppCompatActivity {
    private static final String TAG = "ExpensesTracker";
    private static AppCompatActivity context;
    BottomSheetBehavior bottomSheetBehavior;
    RelativeLayout RLSummary;
    Button addCategory;
    RecyclerView categoriesRV;
    RecyclerView.Adapter categoriesAdapter;
    RecyclerView.LayoutManager categoriesLayoutManager;
    TextView balanceTV, expensesTV, incomeTV;
    static ArrayList<CategoryItem> categoryItems = new ArrayList<>();
    ArrayList<LogItem> logItems = new ArrayList<>();

    ItemTouchHelper itemTouchHelper;

    @Override
    protected void onPause() {
        super.onPause();
        saveCategoriesList();
        saveBalance();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expenses_tracker);
        context = this;

        RLSummary = findViewById(R.id.rl_summary);
        balanceTV = findViewById(R.id.tv_balance);
        balanceTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNumberPopup(-1,balanceTV);
            }
        });
        incomeTV = findViewById(R.id.tv_income);
        incomeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNumberPopup(-1, incomeTV);
            }
        });
        expensesTV = findViewById(R.id.tv_expense);
        expensesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editNumberPopup(-1, expensesTV);
            }
        });
        addCategory = findViewById(R.id.btn_add_category);
        addCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNewCategoryDialog();
            }
        });
        View bottomSheet = findViewById(R.id.bs_categories);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        categoriesRV = findViewById(R.id.rv_categories);
        loadBalance();
        loadCategoriesList();

        if(categoryItems.isEmpty()){
            setUpCategoryItemArray();
        }
        setupCategoryRecyclerView();
    }

    private void deleteCategoryItem(int pos) {
        categoryItems.remove(pos);
        categoriesAdapter.notifyItemRemoved(pos);
    }

    private void addCategoryItem(int pos, CategoryItem item) {
        categoryItems.add(pos, item);
        categoriesAdapter.notifyItemInserted(0);
        categoriesLayoutManager.scrollToPosition(0);
    }

    private void saveBalance() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("balance", balanceTV.getText().toString());
        editor.putString("expense", expensesTV.getText().toString());
        editor.putString("income", incomeTV.getText().toString());
        editor.apply();
    }

    private void loadBalance() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        String balance = sharedPreferences.getString("balance", "Rs. 0");
        String expense = sharedPreferences.getString("expense", "Rs. 0");
        String income = sharedPreferences.getString("income", "Rs. 0");
        balanceTV.setText(balance);
        expensesTV.setText(expense);
        incomeTV.setText(income);
    }

    private void saveCategoriesList() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(categoryItems);
        editor.putString("categories", json);
        editor.apply();
    }

    private void loadCategoriesList() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("categories", null);
        Type type = new TypeToken<ArrayList<CategoryItem>>() {
        }.getType();
        categoryItems = gson.fromJson(json, type);
        if (categoryItems == null)
            categoryItems = new ArrayList<>();
    }


    private void setupCategoryRecyclerView() {
        categoriesLayoutManager = new LinearLayoutManager(this);
        categoriesAdapter = new CategoriesAdapter(categoryItems);
        categoriesRV.setLayoutManager(categoriesLayoutManager);
        categoriesRV.setAdapter(categoriesAdapter);

        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getBindingAdapterPosition();

                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        //del
                        deleteCategoryItem(pos);
                        break;
                    case ItemTouchHelper.RIGHT:
                        //add
                        showAddEntryPopup(pos, 0);
                        categoriesAdapter.notifyItemChanged(pos);
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(context, R.color.brand_red))
                        .addSwipeLeftActionIcon(R.drawable.delete_img)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(context, R.color.brand_green))
                        .addSwipeRightActionIcon(R.drawable.add_entry_img)
                        .create()
                        .decorate();
            }
        });
        itemTouchHelper.attachToRecyclerView(categoriesRV);
    }

    public static void editNumberPopup(int pos, TextView tv) {
        int currentNum = Integer.parseInt(tv.getText().toString().substring(4));

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_number_editor_popup, null);
        LinearLayout parent = view.findViewById(R.id.parent_editor);
        parent.setGravity(Gravity.CENTER);
        NumberPicker n1, n2, n3, n4, n5;
        n1 = view.findViewById(R.id.number_picker_1);
        n2 = view.findViewById(R.id.number_picker_2);
        n3 = view.findViewById(R.id.number_picker_3);
        n4 = view.findViewById(R.id.number_picker_4);
        n5 = view.findViewById(R.id.number_picker_5);

        n1.setValue(currentNum / 10000);
        n2.setValue(currentNum % 10000 / 1000);
        n3.setValue(currentNum % 1000 / 100);
        n4.setValue(currentNum % 100 / 10);
        n5.setValue(currentNum % 10);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "Cancelled!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        tv.setText("Rs. " + (n1.getValue() * 10000 + n2.getValue() * 1000 + n3.getValue() * 100 + n4.getValue() * 10 + n5.getValue()));
                        Toast.makeText(context, "Value updated.", Toast.LENGTH_SHORT).show();
                        if(pos!=-1)
                        {
                            categoryItems.get(pos).setAmount((n1.getValue() * 10000 + n2.getValue() * 1000 + n3.getValue() * 100 + n4.getValue() * 10 + n5.getValue()));
                        }
                    }
                }).show();
    }

    private void showAddEntryPopup(int initPos, int amt) {
        final int[] pos = {initPos};
        final CategoryItem[] currentCategory = {categoryItems.get(pos[0])};

        Calendar calendar = Calendar.getInstance();
        final int[] YEAR = {calendar.get(Calendar.YEAR)};
        final int[] MONTH = {calendar.get(Calendar.MONTH)};
        final int[] DATE = {calendar.get(Calendar.DATE)};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_new_data_entry_popup, null);

        CalendarView calendarView = view.findViewById(R.id.calendar_view);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int date) {
                MONTH[0] = month + 1;
                YEAR[0] = year;
                DATE[0] = date;
            }
        });

        Spinner categorySpinner = view.findViewById(R.id.category_spinner);
        ArrayAdapter<CategoryItem> categoryAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryItems);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        categorySpinner.setSelection(pos[0]);

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                pos[0] = adapterView.getSelectedItemPosition();
                currentCategory[0] = categoryItems.get(pos[0]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        EditText titleET, amountET;
        titleET = view.findViewById(R.id.title_et);
        amountET = view.findViewById(R.id.amount_et);
        if (amt > 0) amountET.setText(amt);

        builder.setTitle("Add Expense Item")
                .setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "Cancelled!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String amount, title;
                        title = titleET.getText().toString();
                        amount = amountET.getText().toString();
                        if (TextUtils.isEmpty(amount)) {
                            Toast.makeText(context, "Invalid Amount!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        logItems.add(new LogItem(YEAR[0], MONTH[0], DATE[0], title, Integer.parseInt(amount)));
                        currentCategory[0].setAmount(currentCategory[0].getAmount() + Integer.parseInt(amount));
                        categoriesAdapter.notifyItemChanged(pos[0]);
                        Toast.makeText(context, "Added Rs. " + Integer.parseInt(amount) + " to " + currentCategory[0].getTitle(), Toast.LENGTH_SHORT).show();
                    }
                }).show();
    }

    private void setUpCategoryItemArray() {
        categoryItems.add(new CategoryItem("Transport", 0));
        categoryItems.add(new CategoryItem("Outings", 0));
        categoryItems.add(new CategoryItem("Food", 0));
        categoryItems.add(new CategoryItem("House", 0));
        categoryItems.add(new CategoryItem("Office", 0));
        categoryItems.add(new CategoryItem("Apparels", 0));
        categoryItems.add(new CategoryItem("Misc", 0));
    }

    private void showNewCategoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_new_category_popup, null);

        EditText titleET, amountET;
        titleET = view.findViewById(R.id.title_et);
        amountET = view.findViewById(R.id.amount_et);

        builder.setView(view).setTitle("New Category")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(context, "Cancelled!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String amount, title;
                        title = titleET.getText().toString();
                        amount = amountET.getText().toString();
                        if (TextUtils.isEmpty(title) || TextUtils.isEmpty(amount)) {
                            Toast.makeText(context, "Invalid Fields!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        addCategoryItem(0, new CategoryItem(title, Integer.parseInt(amount)));
                    }
                }).show();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setUpHeight(bottomSheetBehavior);
    }

    private void setUpHeight(BottomSheetBehavior bottomSheetBehavior) {
        TypedValue tv = new TypedValue();
        int actionBarHeight = 0;
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels - RLSummary.getHeight() - actionBarHeight;
        Log.d(TAG, "setUpHeight: " + displayMetrics.heightPixels + " " + RLSummary.getHeight());
        bottomSheetBehavior.setPeekHeight(height, true);
    }
}
