package com.minimize.android.rxrecyclerexample;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import com.jakewharton.rxbinding.widget.RxTextView;
import com.jakewharton.rxbinding.widget.TextViewAfterTextChangeEvent;
import com.minimize.android.rxrecycleradapter.OnGetItemViewType;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.minimize.android.rxrecycleradapter.TypesViewHolder;
import com.minimize.android.rxrecycleradapter.ViewHolderInfo;
import com.minimize.android.rxrecyclerexample.databinding.ActivityMainBinding;
import com.minimize.android.rxrecyclerexample.databinding.ItemHeaderLayoutBinding;
import com.minimize.android.rxrecyclerexample.databinding.ItemLayoutBinding;
import java.util.ArrayList;
import java.util.List;
import rx.functions.Action1;
import rx.functions.Func1;

public class MainActivity extends AppCompatActivity {
  final int TYPE_HEADER = 0;
  final int TYPE_ITEM = 1;
  List<String> dataSet;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ActivityMainBinding mActivityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

    //ViewHolderInfo List
    List<ViewHolderInfo> viewHolderInfoList = new ArrayList<>();
    viewHolderInfoList.add(new ViewHolderInfo(R.layout.item_layout, TYPE_ITEM));
    viewHolderInfoList.add(new ViewHolderInfo(R.layout.item_header_layout, TYPE_HEADER));

    //Dummy DataSet
    dataSet = new ArrayList<>();
    dataSet.add("This");
    dataSet.add("is");
    dataSet.add("an");
    dataSet.add("example");
    dataSet.add("of RX!");

    //Set layout manager because... Exception... That's why!
    mActivityMainBinding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

    RxDataSource<String> rxDataSource = new RxDataSource<>(dataSet);
    rxDataSource.map(String::toLowerCase)
        .repeat(10)
        .<ItemLayoutBinding>bindRecyclerView(mActivityMainBinding.recyclerView,
        R.layout.item_layout).subscribe(viewHolder -> {
      ItemLayoutBinding b = viewHolder.getViewDataBinding();
      String item = viewHolder.getItem();
      b.textViewItem.setText(String.valueOf(item));
    });
    dataSet = rxDataSource.getRxAdapter().getDataSet();

    RxTextView.afterTextChangeEvents(mActivityMainBinding.searchEditText).subscribe(new Action1<TextViewAfterTextChangeEvent>() {
      @Override public void call(final TextViewAfterTextChangeEvent event) {
        rxDataSource.updateDataSet(dataSet) //base items should remain the same
            .filter(new Func1<String, Boolean>() {
              @Override public Boolean call(String s) {
                return s.toLowerCase().contains(event.view().getText());
              }
            }).updateAdapter();
      }
    });

    rxDataSource.bindRecyclerView(mActivityMainBinding.recyclerView, viewHolderInfoList, new OnGetItemViewType() {
      @Override public int getItemViewType(int position) {
        if (position % 2 == 0) //headers are at even pos
        {
          return TYPE_HEADER;
        }
        return TYPE_ITEM;
      }
    }).subscribe(new Action1<TypesViewHolder<String>>() {
      @Override public void call(TypesViewHolder<String> vH) {
        final ViewDataBinding b = vH.getViewDataBinding();
        if (b instanceof ItemLayoutBinding) {
          final ItemLayoutBinding iB = (ItemLayoutBinding) b;
          iB.textViewItem.setText("ITEM: " + vH.getItem());
        } else if (b instanceof ItemHeaderLayoutBinding) {
          ItemHeaderLayoutBinding hB = (ItemHeaderLayoutBinding) b;
          hB.textViewHeader.setText("HEADER: " + vH.getItem());
        }
      }
    });

    rxDataSource.filter(new Func1<String, Boolean>() {
      @Override public Boolean call(String s) {
        return s.length() > 0;
      }
    }).map(new Func1<String, String>() {
      @Override public String call(String s) {
        return s.toLowerCase();
      }
    }).updateAdapter();
  }
}
